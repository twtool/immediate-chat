package icu.twtool.chat.view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import icu.twtool.chat.animate.toDpSize
import icu.twtool.chat.cache.produceAccountInfoState
import icu.twtool.chat.cache.produceImageState
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.components.file.FilePosition
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.components.file.URLImageRes
import icu.twtool.chat.constants.Platform
import icu.twtool.chat.constants.getPlatform
import icu.twtool.chat.server.dynamic.vo.DynamicDetailsVO
import icu.twtool.chat.state.DynamicViewState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.utils.formatLocal
import icu.twtool.image.compose.ICAsyncImage
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import kotlin.math.ceil

@Composable
private fun DynamicAttachment(
    attachment: String, modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onLook: (FileRes) -> Unit
) {
    val painter: Painter? by produceImageState(attachment, queryParameter = "imageView2/2/w/500/h/500/rq/50")
    var position = remember { FilePosition(null, DpSize.Zero, DpSize.Unspecified) }
    val density = LocalDensity.current
    ICAsyncImage(
        painter,
        contentDescription = "attachment",
        placeholder = null,
        modifier.onGloballyPositioned {
            val rect = it.boundsInRoot()
            position = with(density) {
                FilePosition(
                    rect.topLeft.round(),
                    it.size.toDpSize(),
                    targetSize = painter?.intrinsicSize?.toDpSize() ?: DpSize.Unspecified
                )
            }
        }.pointerInput(attachment) {
            detectTapGestures {
                onLook(URLImageRes(attachment, position))
            }
        },
        contentScale = contentScale
    )
}

@Composable
private fun DynamicAttachmentGrid(
    columns: Int, attachments: List<String>,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onLook: (FileRes) -> Unit
) {
    val size = attachments.size
    var num = 0
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(ceil(size.toFloat() / columns).toInt()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (column in 0 until columns) {
                    if (num >= size) return@Row
                    DynamicAttachment(
                        attachments[it * columns * column],
                        Modifier.weight(1f, fill = false).run { if (size > 1) aspectRatio(1f) else this },
                        contentScale = contentScale,
                        onLook = onLook
                    )
                    num++
                }
            }
        }
    }
}

@Composable
private fun DynamicAttachments(attachments: List<String>, onLook: (FileRes) -> Unit) {
    var columns = 1
    var modifier: Modifier = Modifier.heightIn(max = 600.dp, min = 0.dp)
    var contentScale: ContentScale = ContentScale.Crop
    when (attachments.size) {
        1 -> {
            modifier = modifier.fillMaxWidth(0.6f)
            contentScale = ContentScale.Fit
        }

        2, 4 -> {
            columns = 2
            modifier = modifier.fillMaxWidth(0.6f)
        }

        else -> {
            columns = 3
            modifier = modifier.fillMaxWidth()
        }
    }

    DynamicAttachmentGrid(
        columns,
        attachments,
        modifier,
        contentScale = contentScale,
        onLook = onLook
    )
}

@Composable
private fun DynamicItem(details: DynamicDetailsVO, onLook: (FileRes) -> Unit, modifier: Modifier = Modifier) {
    val info by produceAccountInfoState(details.uid, true)
    val avatar: Painter? by produceImageState(info?.avatarUrl, painterResource(Res.drawable.logo), true, keys = arrayOf(info))
    Row(modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Avatar(avatar, 42.dp)
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    info?.nickname ?: "未命名用户",
                    Modifier.weight(1f, true),
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                Text(
                    details.time.formatLocal(),
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalContentColor.current.copy(alpha = DisabledAlpha)
                )
            }
            Text(details.content)
            Spacer(Modifier.requiredHeight(8.dp))
            DynamicAttachments(details.attachments, onLook = onLook)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicView(paddingValues: PaddingValues, onLook: (FileRes) -> Unit) {
    val state = remember { DynamicViewState() }

    val pullToRefreshState = rememberPullToRefreshState { !state.loading }

    Box(Modifier.fillMaxWidth().padding(paddingValues)) {
        LazyColumn(
            Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection),
        ) {
            items(state.data, key = { it.id }) {
                DynamicItem(it, onLook = onLook)
            }
            item {
                LaunchedEffect(true) {
                    state.load()
                }
                Crossfade(state.loading) {
                    if (it) Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            Modifier.size(24.dp),
                            strokeCap = StrokeCap.Round,
                            strokeWidth = 4.dp
                        )
                    }
                }
            }
        }

        if (getPlatform() == Platform.Desktop) {
            FloatingActionButton({}, Modifier.align(Alignment.BottomEnd).offset((-16).dp, (-16).dp)) {
                Icon(Icons.Filled.Refresh, "refresh")
            }
        }
    }

}