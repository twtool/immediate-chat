package icu.twtool.chat.io

import icu.twtool.chat.constants.ApplicationDir
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ICFileImpl(private val file: File) : ICFile {

    override val key: String = file.absolutePath

    override val filename: String = file.nameWithoutExtension

    override val extension: String = file.extension
    override fun save(): String {
        val file: File = File(ApplicationDir).run { if (extension.isNotEmpty()) resolve(extension) else this }.let {
            var i = 0
            val suffix = if (extension.isNotEmpty()) ".$extension" else ""
            var file: File
            while (true) {
                val offset = if (i++ > 0) "($i)" else ""
                file = it.resolve("$filename$offset$suffix")
                if (file.exists()) continue
                break
            }
            file
        }
        Files.copy(this.file.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        return file.absolutePath
    }

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