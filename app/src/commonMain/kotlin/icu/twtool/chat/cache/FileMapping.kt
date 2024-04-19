package icu.twtool.chat.cache

import icu.twtool.cache.Cache
import icu.twtool.cache.get
import icu.twtool.cache.getCache

object FileMapping {

    private val cache: Cache by lazy { getCache() }

    operator fun get(key: String): String? {
        return cache[key]
    }

    operator fun set(key: String, value: String) {
        cache[key] = value
    }

}