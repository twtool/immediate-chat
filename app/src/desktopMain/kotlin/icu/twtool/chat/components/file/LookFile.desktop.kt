package icu.twtool.chat.components.file

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.onScale(onChange: (Float) -> Unit): Modifier {
    return this.onPointerEvent(PointerEventType.Scroll) {
        onChange(it.changes.first().scrollDelta.y * -0.1f)
    }
}