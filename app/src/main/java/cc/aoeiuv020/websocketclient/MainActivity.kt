package cc.aoeiuv020.websocketclient

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*

class MainActivity : Activity(), Logger {
    var webSocket: WebSocket? = null
    val wsUrl: String? get() = webSocket?.request()?.url()?.toString()
    val listener = Listener()
    val urlSet = mutableSetOf<String>()
    lateinit var urlAdapter: ArrayAdapter<String>
    val URL_KEY_NAME = "url_key_name"
    val messageListAdapter = MessageListAdapter(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPreferences(Context.MODE_PRIVATE).getStringSet(URL_KEY_NAME, setOf<String>()).let {
            urlSet.addAll(it)
        }
        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, urlSet.toList()).let {
            urlAdapter = it
            editTextUrl.setAdapter(it)
        }
        listView.adapter = messageListAdapter

        beforeConnect()
    }

    override fun onPause() {
        super.onPause()
        debug { "save url autocomplete list <${urlSet.size}>" }
        getPreferences(Context.MODE_PRIVATE).edit()
                .putStringSet(URL_KEY_NAME, urlSet)
                .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.close(1000, "destroy")
    }

    @Synchronized
    fun addMessage(message: Message) {
        debug { "add message <$message>" }
        messageListAdapter.add(message)
        listView.apply {
            post {
                setSelection(adapter.count - 1)
            }
        }
    }

    fun beforeConnect() {
        debug { "beforeConnect" }
        buttonConnect.apply {
            isClickable = true
            text = getString(R.string.connect)
            setOnClickListener {
                connecting(editTextUrl.text.toString().takeIf(String::isNotEmpty)
                        ?: getString(R.string.ws_url_example_hint)
                )
            }
        }
        buttonSend.apply {
            isClickable = false
            setColorFilter(color(R.color.sendDisable))
        }
    }

    fun connecting(str: String) {
        debug { "connecting $str" }
        urlAdapter.takeUnless { str in urlSet }?.apply {
            add(str)
            urlSet.add(str)
        }
        buttonConnect.apply {
            isClickable = false
            text = getString(R.string.connecting)
        }
        buttonSend.apply {
            isClickable = false
            setColorFilter(color(R.color.sendDisable))
        }
        val client = OkHttpClient()
        val request = try {
            Request.Builder().url(str).build()
        } catch (e: IllegalArgumentException) {
            error { "${e.message}" }
            beforeConnect()
            return
        }
        webSocket = client.newWebSocket(request, listener)
    }

    fun connected() {
        debug { "connected $wsUrl" }
        addMessage(Message("${wsUrl} connected", MessageType.LOG))
        buttonConnect.apply {
            isClickable = true
            text = getString(R.string.close)
            setOnClickListener {
                webSocket?.close(1000, "user close")
                        ?: closed()
            }
        }
        buttonSend.apply {
            isClickable = true
            setColorFilter(color(R.color.sendEnable))
            setOnClickListener {
                editTextMessage.text.toString().takeIf(String::isNotEmpty)?.let {
                    addMessage(Message(it.toString(), MessageType.ME))
                    webSocket?.send(it)
                            ?: closed()
                    editTextMessage.setText("")
                }
            }
        }
    }

    fun message(text: String) {
        addMessage(Message(text, MessageType.OTHER))
    }

    fun closed() {
        debug { "closed $wsUrl" }
        addMessage(Message("${wsUrl} closed", MessageType.LOG))
        webSocket = null
        beforeConnect()
    }

    fun failure() {
        debug { "failure" }
        closed()
    }

    inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket?, response: Response?) {
            this@MainActivity.webSocket = webSocket
            runOnUiThread {
                connected()
            }
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            runOnUiThread {
                text?.let { message(it) }
            }
        }

        override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
            webSocket?.close(1000, "closing")
        }

        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
            runOnUiThread {
                closed()
            }
        }

        override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
            runOnUiThread {
                failure()
            }
        }
    }
}
