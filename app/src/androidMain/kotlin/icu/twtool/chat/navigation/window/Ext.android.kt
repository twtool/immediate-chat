package icu.twtool.chat.navigation.window

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.window.layout.WindowMetricsCalculator

@Composable
actual fun _calculateWindowSize(): DpSize {
    LocalConfiguration.current
    val density = LocalDensity.current
    val activity = LocalContext.current as Activity
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
    return with(density) { metrics.bounds.toComposeRect().size.toDpSize() }
}