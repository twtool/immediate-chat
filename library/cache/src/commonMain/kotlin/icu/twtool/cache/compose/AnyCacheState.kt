package icu.twtool.cache.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Stable
class AnyCacheState<T : Any>(
    private val key: String,
    private val type: KType,
    value: T? = null
) : CacheState() {

    private var _value: T? by mutableStateOf(value)
    val value: T? = _value

    private var isInitialized = false

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        if (!isInitialized) {
            isInitialized = true
            _value = cache.get(key, type) as T?
        }
        return _value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        _value = value
        isInitialized = true
        cache.set(key, value, type)
    }
}

inline fun <reified T : Any> anyCacheState(key: String, value: T? = null): AnyCacheState<T> =
    AnyCacheState(key, typeOf<T>(), value)