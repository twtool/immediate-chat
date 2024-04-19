package icu.twtool.chat.utils

import androidx.compose.runtime.Composable
import icu.twtool.chat.io.ICFile

enum class FileType {
    IMAGE, FILE
}

interface FileChooser {

    suspend fun launch(input: FileType)
}


@Composable
expect fun rememberFileChooser(onSelected: (List<ICFile>) -> Unit): FileChooser
