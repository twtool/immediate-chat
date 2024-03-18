package icu.twtool.chat.io

import icu.twtool.chat.server.common.datetime.currentEpochSeconds
import io.ktor.util.sha1
import java.io.InputStream

interface ICFile {

    val key: String

    @OptIn(ExperimentalStdlibApi::class)
    val hashKey: String get() = sha1(key.toByteArray()).toHexString() + currentEpochSeconds()

    fun readBytes(): ByteArray

    fun inputStream(): InputStream

    val size: Long
}