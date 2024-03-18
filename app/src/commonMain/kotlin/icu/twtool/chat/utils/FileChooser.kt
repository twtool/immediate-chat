package icu.twtool.chat.utils

import androidx.compose.runtime.Composable
import icu.twtool.chat.io.ICFile

interface FileChooser {

    suspend fun launch()
}


@Composable
expect fun rememberFileChooser(onImageSelected: (List<ICFile>) -> Unit): FileChooser
