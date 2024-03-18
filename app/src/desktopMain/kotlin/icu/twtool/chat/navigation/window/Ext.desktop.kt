package icu.twtool.chat.navigation.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowState

val LocalWindowState = compositionLocalOf<WindowState> { error("No WindowSize Provided") }

@Composable
actual fun _calculateWindowSize(): DpSize {
    return LocalWindowState.current.size
}