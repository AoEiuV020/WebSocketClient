package cc.aoeiuv020.websocketclient

import android.content.Context
import android.os.Build

/**
 * Created by AoEiuV020 on 17-6-30.
 */
fun Context.color(res: Int) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    getColor(res)
} else {
    @Suppress("DEPRECATION")
    resources.getColor(res)
}

