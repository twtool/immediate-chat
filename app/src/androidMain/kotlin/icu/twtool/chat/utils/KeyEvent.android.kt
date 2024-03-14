package icu.twtool.chat.utils

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

actual fun isEnterDown(
    event: KeyEvent,
    ctrlPressed: Boolean,
    altPressed: Boolean,
    shiftPressed: Boolean
): Boolean = event.type == KeyEventType.Unknown &&
        event.key == Key.Enter &&
        event.isAltPressed == altPressed &&
        event.isShiftPressed == shiftPressed &&
        event.isCtrlPressed == ctrlPressed