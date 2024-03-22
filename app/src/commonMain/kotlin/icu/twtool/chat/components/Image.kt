package icu.twtool.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.round
import icu.twtool.chat.animate.toDpSize
import icu.twtool.chat.components.file.FilePosition
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.components.file.ImageRes

@Composable
fun ThumbnailsImage(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onLook: (FileRes) -> Unit = {}
) {
    var position = remember { FilePosition(null, DpSize.Zero, DpSize.Unspecified) }
    val density = LocalDensity.current

    Image(
        painter, contentDescription = contentDescription,
        modifier = modifier.onGloballyPositioned {
            val rect = it.boundsInRoot()
            position = with(density) {
                FilePosition(
                    rect.topLeft.round(),
                    it.size.toDpSize(),
                    targetSize = painter.intrinsicSize.toDpSize()
                )
            }
        }.pointerInput(painter) {
            detectTapGestures {
                onLook(ImageRes({ painter }, position, contentDescription))
            }
        }
    )
}