package icu.twtool.chat.io

import java.awt.Desktop
import java.io.File

class OpenableFileImpl(private val file: File) : OpenableFile {

    override fun open() {
        Desktop.getDesktop().open(file)
    }
}

actual fun getOpenableFile(path: String): OpenableFile {
    return OpenableFileImpl(File(path))
}