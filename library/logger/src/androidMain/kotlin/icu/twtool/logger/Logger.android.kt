package icu.twtool.logger

import android.util.Log

class AndroidLogger(private val tag: String) : Logger {

    override fun info(msg: String) {
        Log.i(tag, msg)
    }

    override fun error(msg: String, error: Throwable?) {
        Log.e(tag, msg, error)
    }
}

actual fun getLogger(tag: String): Logger = AndroidLogger(tag)