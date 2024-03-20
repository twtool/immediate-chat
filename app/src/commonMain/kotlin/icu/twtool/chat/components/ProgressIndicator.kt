package icu.twtool.chat.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ICCircularProgressIndicator(size: Dp = 24.dp) {
    CircularProgressIndicator(
        Modifier.size(size),
        strokeCap = StrokeCap.Round,
        strokeWidth = 4.dp
    )
}

@Composable
fun ICCircularProgressIndicator(progress: () -> Float, size: Dp = 24.dp) {
    CircularProgressIndicator(
        progress = progress,
        Modifier.size(size),
        strokeCap = StrokeCap.Round,
        strokeWidth = 4.dp
    )
}