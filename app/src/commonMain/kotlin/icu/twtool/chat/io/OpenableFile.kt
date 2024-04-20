package icu.twtool.chat.io

import androidx.compose.runtime.Composable

interface OpenableFile : ICFile {

    fun open(): Boolean

    fun exists(): Boolean
}

interface OpenableFileUtil {

    fun get(path: String): OpenableFile
}

@Composable
expect fun rememberOpenableFileUtil(): OpenableFileUtil