package icu.twtool.chat.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
expect fun QRCodeCanvas(
    content: String,
    logo: Painter,
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    version: Int = 3,
    logoDpSize: Dp = 32.dp,
    logoDpRadius: Dp = 8.dp,
)

fun DrawScope.drawPositionDetectionPatterns(color: Color, radius: Float, width: Float) {
    translate {
        drawPositionDetectionPattern(color, radius, width, Offset(radius, radius))
    }
    translate(left = this.size.width) {
        drawPositionDetectionPattern(color, radius, width, Offset(-radius, radius))
    }
    translate(top = this.size.height) {
        drawPositionDetectionPattern(color, radius, width, Offset(radius, -radius))
    }
}

fun DrawScope.drawPositionDetectionPattern(
    color: Color, radius: Float, width: Float, offset: Offset
) {
    drawCircle(
        color,
        style = Stroke(width),
        center = offset,
        radius = radius - width / 2
    )
    drawCircle(
        color,
        center = offset,
        radius = radius / 7 * 3
    )
}