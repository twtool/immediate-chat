package icu.twtool.chat.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent

expect fun isEnterDown(event: KeyEvent, ctrlPressed: Boolean, altPressed: Boolean, shiftPressed: Boolean): Boolean

fun Modifier.onEnterKeyPressed(
    ctrlPressed: Boolean = false,
    altPressed: Boolean = false,
    shiftPressed: Boolean = false,
    onEnter: () -> Boolean,
): Modifier = onKeyEvent {
    if (isEnterDown(it, ctrlPressed, altPressed, shiftPressed)) onEnter() else false
}