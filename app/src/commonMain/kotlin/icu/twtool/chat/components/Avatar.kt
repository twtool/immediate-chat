package icu.twtool.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import icu.twtool.chat.cache.loadImage
import icu.twtool.image.compose.ICAsyncImage
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun Avatar(
    url: String? = null,
    size: Dp = 32.dp,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    onClick: (() -> Unit)? = null
) {
    ICAsyncImage(
        { loadImage(url) },
        painterResource(Res.drawable.logo),
        "Avatar",
        Modifier.size(size).clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .run { if (onClick != null) clickable(onClick = onClick) else this },
        contentScale = ContentScale.Crop
    )
}