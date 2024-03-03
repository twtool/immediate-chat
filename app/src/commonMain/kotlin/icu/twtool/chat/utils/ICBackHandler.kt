package icu.twtool.chat.utils

import androidx.compose.runtime.Composable

@Composable
expect fun _BackHandler(enabled: Boolean, onBack: () -> Unit)

@Composable
fun ICBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    _BackHandler(enabled, onBack)
}