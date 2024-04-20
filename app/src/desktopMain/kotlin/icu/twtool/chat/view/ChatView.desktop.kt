package icu.twtool.chat.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.onExternalDrag

@ExperimentalComposeUiApi
@Composable
actual fun Modifier.onIcExternalDrag(onDragFiles: (List<String>) -> Unit) = onExternalDrag {
    when (val dragData = it.dragData) {
        is DragData.FilesList -> onDragFiles(dragData.readFiles())
        else -> {}
    }
}