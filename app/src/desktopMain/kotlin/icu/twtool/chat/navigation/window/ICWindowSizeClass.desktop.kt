package icu.twtool.chat.navigation.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowState

@Composable
fun calculateWindowSizeClass(state: WindowState): ICWindowSizeClass {
    return ICWindowSizeClass.calculateFromSize(state.size)
}