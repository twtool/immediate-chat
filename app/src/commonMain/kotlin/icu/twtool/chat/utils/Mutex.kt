package icu.twtool.chat.utils

import kotlinx.coroutines.sync.Mutex

inline fun Mutex.tryLockRun(owner: Any? = null, action: () -> Unit) {
    if (!tryLock(owner)) return
    try {
        action()
    } finally {
        unlock()
    }
}