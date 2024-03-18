package icu.twtool.chat.animate

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize


context(Density)
fun IntOffset.toDpOffset(): DpOffset = DpOffset(x.toDp(), y.toDp())

context(Density)
fun DpOffset.toIntOffset(): IntOffset = IntOffset(x.roundToPx(), y.roundToPx())

context(Density)
fun IntSize.toDpSize(): DpSize = DpSize(width.toDp(), height.toDp())

context(Density)
fun DpSize.toIntSize(): IntSize = IntSize(width.roundToPx(), height.roundToPx())

context(Density)
fun Size.toDpSize(): DpSize = DpSize(width.toDp(), height.toDp())
