package icu.twtool.chat.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import icu.twtool.chat.composition.LocalComposeWindow
import icu.twtool.chat.io.ICFile
import icu.twtool.chat.io.ICFileImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.awt.Component
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Stable
class DesktopFileChooser(
    private val parent: Component,
    private val onImageSelected: (List<ICFile>) -> Unit
) : FileChooser {

    var show by mutableStateOf(false)
        private set

    private val fileChooser by lazy { JFileChooser() }

    override suspend fun launch(input: FileType) {
        withContext(Dispatchers.IO) {
            show = true
            fileChooser.fileFilter = when (input) {
                FileType.IMAGE -> FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png")
                FileType.FILE -> null
            }
            fileChooser.isMultiSelectionEnabled = true
            if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                onImageSelected(fileChooser.selectedFiles.map { ICFileImpl(it) })
            }
            delay(200)
            show = false
        }
    }
}

@Composable
actual fun rememberFileChooser(onSelected: (List<ICFile>) -> Unit): FileChooser {
    val window = LocalComposeWindow.current
    val chooser = remember { DesktopFileChooser(window, onSelected) }
    ICBackHandler(chooser.show) {}
    return chooser
}