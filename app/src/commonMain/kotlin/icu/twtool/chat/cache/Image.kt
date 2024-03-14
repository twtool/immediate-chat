package icu.twtool.chat.cache

import icu.twtool.cache.get
import icu.twtool.cache.getCache
import icu.twtool.cache.set
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.service.creator
import icu.twtool.chat.service.get
import icu.twtool.image.compose.urlByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
suspend fun loadImage(url: String?, cacheable: Boolean = true): ByteArray? {
    if (url.isNullOrBlank()) return null
    val key = "IMAGE:" + Base64.encode(url.toByteArray())
    val cache = getCache()
    return (if (cacheable) cache.getByteArray(key) else null)
        ?: (runCatching { creator.client.urlByteArray(url) }.getOrNull()?.apply {
            if (cacheable) cache[key] = this
        })
}

suspend fun loadAccountInfo(uid: Long, cacheable: Boolean = true): AccountInfo? {
    val key = "ACCOUNT:$uid"
    val cache = getCache()
    val cacheValue = if (cacheable) cache.get<AccountInfo>(key) else null
    val result = cacheValue ?: (AccountService.get().getInfoByUID(uid.toString()).data)
    if (cacheable) cache[key] = result
    return result
}