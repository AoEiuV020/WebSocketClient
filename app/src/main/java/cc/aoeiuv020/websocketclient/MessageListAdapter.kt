package cc.aoeiuv020.websocketclient

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.message_item.view.*

/**
 * Created by AoEiuV020 on 17-6-30.
 */
class MessageListAdapter(val context: Context) : BaseAdapter() {
    val items = mutableListOf<Message>()

    @Synchronized
    fun add(message: Message) {
        items.add(message)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val root = convertView ?: View.inflate(context, R.layout.message_item, null)
        val item = getItem(position)
        root.textView.apply {
            text = item.text
            gravity = when (item.type) {
                MessageType.ME -> Gravity.END
                MessageType.OTHER -> Gravity.START
                MessageType.LOG -> Gravity.CENTER
            }
            when (item.type) {
                MessageType.ME -> context.color(R.color.messageFromMe)
                MessageType.OTHER -> context.color(R.color.messageFromOther)
                MessageType.LOG -> context.color(R.color.messageFromLog)
            }.let {
                setBackgroundColor(it)
            }
        }
        return root
    }

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getCount() = items.size
}