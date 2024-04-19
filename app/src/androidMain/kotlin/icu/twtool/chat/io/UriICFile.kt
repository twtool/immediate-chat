package icu.twtool.chat.io

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.UUID

class UriICFile(
    private val context: Context,
    private val uri: Uri
) : ICFile {

    override val key: String = uri.toString()

    private val name: String by lazy {
        context.contentResolver.query(uri, null, null, null)?.let {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    it.getString(nameIndex)
                } else {
                    null
                }
            } else {
                uri.lastPathSegment
            }
        } ?: UUID.randomUUID().toString()
    }

    override val filename: String by lazy { name.substringBeforeLast('.') }
    override val extension: String by lazy { name.substringAfterLast('.', "") }

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

        other as UriICFile

        return key == other.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

}