package icu.twtool.chat.navigation.window

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.window.layout.WindowMetricsCalculator

@Composable
fun calculateWindowSizeClass(activity: Activity): ICWindowSizeClass {
    LocalConfiguration.current
    val density = LocalDensity.current
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
    val size = with(density) { metrics.bounds.toComposeRect().size.toDpSize() }
    return ICWindowSizeClass.calculateFromSize(size)
}

fun currentWindowSizeClass(activity: Activity): ICWindowSizeClass {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
    val size = Density(activity.resources.displayMetrics.density).run { metrics.bounds.toComposeRect().size.toDpSize() }
    return ICWindowSizeClass.calculateFromSize(size)
}