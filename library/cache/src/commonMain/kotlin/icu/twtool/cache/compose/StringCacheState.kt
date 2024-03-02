package icu.twtool.cache.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.reflect.KProperty

@Stable
class StringCacheState constructor(
    private val key: String,
    value: String? = null
) : CacheState() {

    private var _value: String? by mutableStateOf(value)
    val value: String? = _value

    private var isInitialized = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        if (!isInitialized) {
            isInitialized = true
            _value = cache.getString(key)
        }
        return _value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        _value = value
        isInitialized = true
        cache[key] = value
    }
}