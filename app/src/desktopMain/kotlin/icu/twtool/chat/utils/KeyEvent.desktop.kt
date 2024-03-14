package icu.twtool.chat.utils

import androidx.compose.ui.input.key.KeyEvent

typealias AWTEvent = java.awt.event.KeyEvent

actual fun isEnterDown(
    event: KeyEvent,
    ctrlPressed: Boolean,
    altPressed: Boolean,
    shiftPressed: Boolean
): Boolean = (event.nativeKeyEvent as AWTEvent).let {
    it.id == AWTEvent.KEY_TYPED &&
            it.keyChar == '\n' &&
            it.isAltDown == altPressed &&
            it.isShiftDown == shiftPressed &&
            it.isControlDown == ctrlPressed
}