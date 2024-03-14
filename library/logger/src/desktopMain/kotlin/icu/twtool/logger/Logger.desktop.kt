package icu.twtool.logger

import org.slf4j.LoggerFactory

class DesktopLogger(tag: String) : Logger {

    private val log = LoggerFactory.getLogger(tag)

    override fun info(msg: String) {
        log.info(msg)
    }

    override fun error(msg: String, error: Throwable?) {
        log.error(msg, error)
    }
}

actual fun getLogger(tag: String): Logger = DesktopLogger(tag)