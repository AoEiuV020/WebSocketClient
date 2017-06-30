package cc.aoeiuv020.websocketclient

import android.util.Log

/**
 * Created by AoEiuV020 on 17-6-30.
 */
interface Logger

fun Logger.debug(block: () -> String) {
    Log.d(this.javaClass.simpleName, block())
}

fun Logger.info(block: () -> String) {
    Log.i(this.javaClass.simpleName, block())
}

fun Logger.error(block: () -> String) {
    Log.e(this.javaClass.simpleName, block())
}
