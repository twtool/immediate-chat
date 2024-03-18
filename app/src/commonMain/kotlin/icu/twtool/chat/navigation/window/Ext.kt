package icu.twtool.chat.navigation.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize

@Composable
expect fun _calculateWindowSize(): DpSize

@Composable
fun calculateWindowSize(): DpSize = _calculateWindowSize()