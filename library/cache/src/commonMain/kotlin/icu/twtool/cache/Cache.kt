package icu.twtool.cache

import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface Cache {

    operator fun set(key: String, value: Int): Boolean
    operator fun set(key: String, value: Long): Boolean
    operator fun set(key: String, value: Float): Boolean
    operator fun set(key: String, value: Double): Boolean
    operator fun set(key: String, value: Boolean): Boolean
    operator fun set(key: String, value: ByteArray?): Boolean

    operator fun set(key: String, value: String?): Boolean
    operator fun set(key: String, value: Set<String>?): Boolean

    fun set(key: String, value: Any?, type: KType): Boolean

    fun getInt(key: String): Int
    fun getLong(key: String): Long
    fun getFloat(key: String): Float
    fun getDouble(key: String): Double
    fun getBoolean(key: String): Boolean
    fun getByteArray(key: String): ByteArray?

    fun getString(key: String): String?
    fun getStringSet(key: String): Set<String>?

    fun get(key: String, type: KType): Any?
}

inline operator fun <reified T : Any> Cache.set(key: String, value: T): Boolean = set(key, value, typeOf<T>())
inline operator fun <reified T : Any> Cache.get(key: String): T? = get(key, typeOf<T>()) as? T

expect fun getCache(): Cache
