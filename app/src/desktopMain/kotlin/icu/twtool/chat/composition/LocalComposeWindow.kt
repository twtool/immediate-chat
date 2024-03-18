package icu.twtool.chat.composition

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.awt.ComposeWindow

val LocalComposeWindow = compositionLocalOf<ComposeWindow> {
    error("CompositionLocal LocalComposeWindow Not Provided")
}