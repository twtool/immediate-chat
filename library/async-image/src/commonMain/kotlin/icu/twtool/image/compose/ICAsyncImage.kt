package icu.twtool.image.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ICAsyncImage(
    data: suspend () -> ByteArray?,
    placeholder: Painter? = null,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.None,
) {
    val image: Painter? by produceState<Painter?>(null) {
        withContext(Dispatchers.IO) {
            value = data()?.let {
                BitmapPainter(it.encodedToImageBitmap())
            } ?: placeholder
        }
    }

    Crossfade(image) {
        if (it == null) {
            Box(modifier)
        }
        it?.let { bitmap ->
            Image(bitmap, contentDescription, modifier, contentScale = contentScale)
        }
    }
}

@Composable
fun ICAsyncImage(
    data: Painter?,
    contentDescription: String?,
    placeholder: Painter? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.None,
) {
    val image: Painter? = data ?: placeholder

    if (image != null) Image(image, contentDescription, modifier, contentScale = contentScale)
    else Box(modifier)
}

suspend fun HttpClient.urlByteArray(url: String): ByteArray? {
    val response = get(url)
    if (response.status != HttpStatusCode.OK) return null
    return response.readBytes()
}

expect fun ByteArray.encodedToImageBitmap(): ImageBitmap