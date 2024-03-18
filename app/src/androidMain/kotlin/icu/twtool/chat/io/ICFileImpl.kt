package icu.twtool.chat.io

import android.content.Context
import android.net.Uri
import java.io.ByteArrayInputStream
import java.io.InputStream

class ICFileImpl(
    private val context: Context,
    private val uri: Uri
) : ICFile {

    override val key: String = uri.toString()

    private val bytes: ByteArray by lazy { context.contentResolver.openInputStream(uri)!!.readBytes() }

    override val size: Long get() = bytes.size.toLong()

    override fun readBytes(): ByteArray {
        return bytes
    }

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