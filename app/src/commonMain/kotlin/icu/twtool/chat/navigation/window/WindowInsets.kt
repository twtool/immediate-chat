package icu.twtool.chat.navigation.window

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable

@Composable
expect fun systemBarsForVisualComponents(): WindowInsets

val systemBarWindowInsets: WindowInsets
    @Composable
    get() = systemBarsForVisualComponents()