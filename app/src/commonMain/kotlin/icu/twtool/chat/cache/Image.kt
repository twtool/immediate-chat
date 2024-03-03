package icu.twtool.chat.cache

import icu.twtool.cache.getCache
import icu.twtool.chat.service.creator
import icu.twtool.image.compose.urlByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
suspend fun loadImage(url: String?, cacheable: Boolean = true): ByteArray? {
    if (url.isNullOrBlank()) return null
    val key = "IMAGE:" + Base64.encode(url.toByteArray())
    val cache = getCache()
    return (if (cacheable) cache.getByteArray(key) else null) ?: (creator.client.urlByteArray(url).apply {
        if (cacheable) cache[key] = this
    })
}

