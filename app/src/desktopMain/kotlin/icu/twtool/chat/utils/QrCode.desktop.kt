package icu.twtool.chat.utils

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlin.math.ceil
import kotlin.math.floor

@Composable
actual fun QRCodeCanvas(
    content: String,
    logo: Painter,
    modifier: Modifier,
    color: Color,
    version: Int,
    logoDpSize: Dp,
    logoDpRadius: Dp,
) {
    val density = LocalDensity.current
    val logoSize = remember(logoDpSize) { with(density) { logoDpSize.toPx() }.let { Size(it, it) } }
    val logoRadius = remember(logoDpRadius) { with(density) { logoDpRadius.toPx() } }
    val matrixSize = remember(version) { 17 + 4 * version }

    Canvas(modifier) {
        val scale = size.width / matrixSize
        val pointSize = Size(1f * scale, 1f * scale)

        val matrix =
            QRCodeWriter().encode(
                content, BarcodeFormat.QR_CODE, matrixSize, matrixSize, mapOf(
                    EncodeHintType.QR_VERSION to version,
                    EncodeHintType.MARGIN to 0,
                )
            ).apply {
                val scaleLogo = logoSize / scale
                unsetLogo(
                    floor((width - scaleLogo.width) / 2).toInt(),
                    floor((height - scaleLogo.height) / 2).toInt(),
                    ceil((height + scaleLogo.width) / 2).toInt(),
                    ceil((height + scaleLogo.height) / 2).toInt(),
                )
                unsetPositionDetectionPatterns()
            }

        for (x in 0 until matrixSize) {
            for (y in 0 until matrixSize) {
                if (matrix[x, y]) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x.toFloat(), y.toFloat()) * scale,
                        size = pointSize,
                        cornerRadius = CornerRadius(pointSize.width / 2, pointSize.height / 2)
                    )
                }
            }
        }

        drawPositionDetectionPatterns(color, scale * 7 / 2, scale)

        translate((size.width - logoSize.width) / 2, (size.height - logoSize.height) / 2) {
            clipPath(Path().apply {
                addRoundRect(RoundRect(Rect(Offset.Zero, logoSize), CornerRadius(logoRadius)))
            }) {
                with(logo) {
                    draw(logoSize)
                }
            }
        }
    }
}

/**
 * 要显示 Logo 时，清除中间显示的点位
 */
fun BitMatrix.unsetLogo(left: Int, top: Int, right: Int, bottom: Int) {
    for (x in left until right) {
        for (y in top until bottom) {
            unset(x, y)
        }
    }
}

fun BitMatrix.unsetPositionDetectionPatterns() {
    unsetPositionDetectionPattern(IntOffset(0, 0))
    unsetPositionDetectionPattern(IntOffset(width - 7, 0))
    unsetPositionDetectionPattern(IntOffset(0, height - 7))
}

/**
 * 需要自定义位置限定图像时清除矩阵中的点位信息
 */
fun BitMatrix.unsetPositionDetectionPattern(offset: IntOffset) {
    for (x in 0 until 7) {
        for (y in 0 until 7) {
            unset(x + offset.x, y + offset.y)
        }
    }
}
