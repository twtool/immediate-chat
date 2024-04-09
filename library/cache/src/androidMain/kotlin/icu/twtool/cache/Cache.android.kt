package icu.twtool.cache

import android.content.Context
import com.tencent.mmkv.MMKV
import icu.twtool.logger.getLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

private val log = getLogger("icu.twtool.cache.Cache.android.kt")

object AndroidCache : Cache {
    private lateinit var mmkv: MMKV

    fun initialize(context: Context) {
        MMKV.initialize(context)
        mmkv = MMKV.defaultMMKV()
        log.info("initialized cache.")
    }

    override fun set(key: String, value: Int): Boolean = mmkv.encode(key, value)

    override fun set(key: String, value: Long): Boolean = mmkv.encode(key, value)
    override fun set(key: String, value: Float): Boolean = mmkv.encode(key, value)

    override fun set(key: String, value: Double): Boolean = mmkv.encode(key, value)

    override fun set(key: String, value: Boolean): Boolean = mmkv.encode(key, value)

    override fun set(key: String, value: ByteArray?): Boolean = mmkv.encode(key, value)

    override fun set(key: String, value: String?): Boolean = mmkv.encode(key, value)

    override fun set(key: String, value: Set<String>?): Boolean = mmkv.encode(key, value)

    override fun set(key: String, value: Any?, type: KType): Boolean {
        val strValue = Json.encodeToString(serializer(type), value)
        return set(key, strValue)
    }

    override fun getInt(key: String): Int = mmkv.decodeInt(key)

    override fun getLong(key: String): Long = mmkv.decodeLong(key)
    override fun getLong(key: String, defaultValue: Long): Long = mmkv.decodeLong(key, defaultValue)

    override fun getFloat(key: String): Float = mmkv.decodeFloat(key)

    override fun getDouble(key: String): Double = mmkv.decodeDouble(key)

    override fun getBoolean(key: String): Boolean = mmkv.decodeBool(key)

    override fun getByteArray(key: String): ByteArray? = mmkv.decodeBytes(key)

    override fun getString(key: String): String? = mmkv.decodeString(key)

    override fun getStringSet(key: String): Set<String>? = mmkv.decodeStringSet(key)

    override fun get(key: String, type: KType): Any? {
        val strValue = getString(key) ?: return null
        return Json.decodeFromString(serializer(type), strValue)
    }
}

actual fun getCache(): Cache = AndroidCache