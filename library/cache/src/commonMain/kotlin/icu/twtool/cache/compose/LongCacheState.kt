package icu.twtool.cache.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.reflect.KProperty

@Stable
class LongCacheState(
    private val key: String,
    private val defaultValue: () -> Long
) : CacheState() {

    private var _value: Long by mutableStateOf(defaultValue())
    val value: Long = _value

    private var isInitialized = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Long {
        if (!isInitialized) {
            isInitialized = true
            _value = cache.getLong(key, defaultValue())
        }
        return _value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
        _value = value
        isInitialized = true
        cache[key] = value
    }
}