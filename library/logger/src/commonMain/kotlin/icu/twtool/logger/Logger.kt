package icu.twtool.logger

interface Logger {

    fun info(msg: String)

    fun error(msg: String, error: Throwable)
}

expect fun getLogger(tag: String): Logger