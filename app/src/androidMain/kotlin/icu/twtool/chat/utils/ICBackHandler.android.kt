package icu.twtool.chat.utils

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@SuppressLint("ComposableNaming")
@Composable
actual fun _BackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}