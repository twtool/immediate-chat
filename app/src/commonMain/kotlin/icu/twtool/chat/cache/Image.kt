package icu.twtool.chat.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import icu.twtool.cache.getCache
import icu.twtool.chat.io.ICFile
import icu.twtool.chat.service.creator
import icu.twtool.cos.getCosClient
import icu.twtool.image.compose.encodedToImageBitmap
import icu.twtool.image.compose.urlByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private val imageMemoryCache = LruCache<String, Painter>(20)

private val HTTP_URL_REGEX = Regex("^https?://.*")

@OptIn(ExperimentalEncodingApi::class)
suspend fun loadImage(url: String?, cacheable: Boolean = true, queryParameter: String? = null): ByteArray? {
    if (url.isNullOrBlank()) return null
    val key = "IMAGE:" + Base64.encode(url.toByteArray()) + queryParameter
    val cache = getCache()
    return (if (cacheable) cache.getByteArray(key) else null) // TODO: 尝试本地获取
        ?: (runCatching {
            if (url.matches(HTTP_URL_REGEX)) creator.client.urlByteArray(url)
            else getCosClient().getObject(url, queryParameter)?.readBytes()
        }.getOrNull()?.apply {
            if (cacheable) cache[key] = this
        })
}

@Composable
fun produceImageState(
    url: String?,
    placeholder: Painter? = null,
    cacheable: Boolean = true,
    queryParameter: String? = null,
    vararg keys: Any?
): State<Painter?> =
    produceState(placeholder, *keys) {
        url?.let {
            val key = it + queryParameter
            withContext(Dispatchers.IO) {
                if (cacheable) imageMemoryCache.get(key)?.let { cache ->
                    value = cache
                    return@withContext
                }
                loadImage(it, queryParameter = queryParameter)?.let { bytes ->
                    val painter = BitmapPainter(bytes.encodedToImageBitmap())
                    if (cacheable) imageMemoryCache.put(key, painter)
                    value = painter
                }
            }
        }
    }


@Composable
fun produceImageState(
    file: ICFile?,
    placeholder: Painter? = null,
    cacheable: Boolean = true,
    vararg keys: Any?
): State<Painter?> =
    produceState(placeholder, *keys) {
        withContext(Dispatchers.IO) {
            file?.let {
                if (cacheable) imageMemoryCache.get(file.key)?.let { cache ->
                    value = cache
                    return@withContext
                }
                BitmapPainter(it.readBytes().encodedToImageBitmap()).let { painter ->
                    if (cacheable) imageMemoryCache.put(file.key, painter)
                    value = painter
                }
            }
        }
    }
