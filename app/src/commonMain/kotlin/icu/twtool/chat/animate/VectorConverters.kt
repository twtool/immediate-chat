package icu.twtool.chat.animate

import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

val DpSize.Companion.VectorConverter: TwoWayConverter<DpSize, AnimationVector2D>
    get() = DpSizeToVector


private val DpSizeToVector: TwoWayConverter<DpSize, AnimationVector2D> =
    TwoWayConverter(
        { AnimationVector2D(it.width.value, it.height.value) },
        { DpSize(it.v1.dp, it.v2.dp) }
    )