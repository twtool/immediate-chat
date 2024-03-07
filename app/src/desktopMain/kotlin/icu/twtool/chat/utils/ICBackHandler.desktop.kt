package icu.twtool.chat.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type


@Suppress("ComposableNaming")
@Composable
actual fun _BackHandler(enabled: Boolean, onBack: () -> Unit) {
    val store = LocalKeyEventStore.current
    val handler = remember(onBack) {
        { event: KeyEvent ->
            if (event.key == Key.Escape && event.type == KeyEventType.KeyUp) {
                onBack()
                true
            } else false
        }
    }
    DisposableEffect(handler) {
        store.add(handler)
        onDispose {
            store.del(handler)
        }
    }
}