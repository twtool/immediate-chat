package icu.twtool.chat.io

import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class ICFileImpl(private val file: File) : ICFile {

    override val key: String = file.absolutePath

    private val bytes: ByteArray by lazy { file.readBytes() }

    override fun readBytes(): ByteArray {
        return bytes
    }

    override val size: Long get() = bytes.size.toLong()

    override fun inputStream(): InputStream {
        return ByteArrayInputStream(bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ICFileImpl

        return key == other.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

}