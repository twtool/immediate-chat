package icu.twtool.chat.io

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import icu.twtool.chat.constants.CacheDirFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class AndroidOpenableFile(
    private val file: File,
    private val context: Context
) : OpenableFile {

    override val key: String = file.absolutePath

    override val filename: String = file.nameWithoutExtension
    override val extension: String = file.extension

    private val mimeType: String? = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

    override fun open(): Boolean {
        Log.i("AndroidOpenableFile", "open: $file")
        val uri = FileProvider.getUriForFile(context, "icu.twtool.chat.file.provider", file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, mimeType)
//            Intent.createChooser()
        }
        try {
            context.startActivity(intent)
            return true
        } catch (e: ActivityNotFoundException) {
            return false
        }
    }

    override fun exists(): Boolean = file.exists()

    private val bytes: ByteArray by lazy { file.readBytes() }

    override fun readBytes(): ByteArray = bytes

    override fun inputStream(): InputStream = ByteArrayInputStream(bytes)

    override val size: Long = bytes.size.toLong()

    override fun save(): String {
        val file: File = CacheDirFile.run { if (extension.isNotBlank()) resolve(extension) else this }.let {
            var i = 0
            val suffix = if (extension.isNotBlank()) ".$extension" else ""
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
}

class AndroidOpenableFileUtil(
    private val context: Context
) : OpenableFileUtil {

    override fun get(path: String): OpenableFile {
        return AndroidOpenableFile(File(path), context)
    }
}

@Composable
actual fun rememberOpenableFileUtil(): OpenableFileUtil {
    val context = LocalContext.current
    return remember {
        AndroidOpenableFileUtil(context)
    }
}