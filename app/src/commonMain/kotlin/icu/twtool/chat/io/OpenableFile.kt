package icu.twtool.chat.io

interface OpenableFile {

    fun open()
}

expect fun getOpenableFile(path: String): OpenableFile