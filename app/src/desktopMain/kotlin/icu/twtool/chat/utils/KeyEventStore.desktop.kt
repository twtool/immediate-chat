package icu.twtool.chat.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.input.key.KeyEvent

class KeyEventStore {

    private val store = mutableListOf<(KeyEvent) -> Boolean>()

    fun add(handler: (KeyEvent) -> Boolean) {
        store.add(handler)
    }

    fun del(handler: (KeyEvent) -> Boolean) {
        store.remove(handler)
    }

    fun get(): List<(KeyEvent) -> Boolean> = store.toList().reversed()
}

val LocalKeyEventStore = compositionLocalOf<KeyEventStore> { error("not provided") }