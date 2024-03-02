package icu.twtool.chat.navigation.window

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

var systemBarHeight: Dp = 0.dp

@Composable
actual fun systemBarsForVisualComponents(): WindowInsets = WindowInsets(0.dp, systemBarHeight, 0.dp, 0.dp)