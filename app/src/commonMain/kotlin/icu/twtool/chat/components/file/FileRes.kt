package icu.twtool.chat.components.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import icu.twtool.chat.cache.produceImageState

@Immutable
class FilePosition(val offset: IntOffset?, val size: DpSize, val targetSize: DpSize)

@Immutable
sealed class FileRes(
    val position: FilePosition?,
    val desc: String?
)

@Immutable
open class ImageRes(
    val painter: @Composable () -> Painter?,
    position: FilePosition?,
    desc: String?
) : FileRes(position, desc)

class URLImageRes(
    val url: String,
    position: FilePosition? = null,
    desc: String? = null
) : ImageRes({
    produceImageState(url, keys = arrayOf(url)).value
}, position, desc)