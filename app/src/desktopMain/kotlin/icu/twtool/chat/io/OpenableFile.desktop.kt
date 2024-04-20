package icu.twtool.chat.io

import androidx.compose.runtime.Composable
import java.io.File

class DesktopOpenableFileUtil : OpenableFileUtil {

    override fun get(path: String): OpenableFile {
        return ICFileImpl(File(path))
    }
}

@Composable
actual fun rememberOpenableFileUtil(): OpenableFileUtil {
    return DesktopOpenableFileUtil()
}