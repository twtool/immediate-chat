package icu.twtool.chat.view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.DialogProperties
import icu.twtool.chat.animate.toDpSize
import icu.twtool.chat.app.DynamicDetailsRoute
import icu.twtool.chat.cache.produceAccountInfoState
import icu.twtool.chat.cache.produceImageState
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.components.LoadingDialog
import icu.twtool.chat.components.LoadingDialogState
import icu.twtool.chat.components.file.FilePosition
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.components.file.URLImageRes
import icu.twtool.chat.constants.Platform
import icu.twtool.chat.constants.getPlatform
import icu.twtool.chat.database.database
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.dynamic.DynamicService
import icu.twtool.chat.server.dynamic.param.LikeDynamicParam
import icu.twtool.chat.server.dynamic.vo.DynamicDetailsVO
import icu.twtool.chat.service.get
import icu.twtool.chat.state.DynamicViewState
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens
import icu.twtool.chat.utils.formatLocal
import icu.twtool.image.compose.ICAsyncImage
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_like_fill
import immediatechat.app.generated.resources.ic_like_outline
import immediatechat.app.generated.resources.logo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import kotlin.math.ceil

@Composable
private fun DynamicAttachment(
    attachment: String, modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onLook: (FileRes) -> Unit
) {
    val painter: Painter? by produceImageState(
        attachment, queryParameter = "imageView2/2/w/500/h/500/rq/50",
        keys = arrayOf(attachment)
    )
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
fun DynamicAttachments(attachments: List<String>, onLook: (FileRes) -> Unit) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LikeNicknames(ids: List<Long>, friendsInfos: Map<Long, AccountInfo>) {
    val likeInfos = remember(ids, friendsInfos) {
        val res = mutableListOf<AccountInfo>()
        ids.forEach {
            friendsInfos[it]?.let(res::add)
        }
        res
    }
    if (likeInfos.isEmpty()) return
    FlowRow(
        Modifier.fillMaxWidth()
            .padding(8.dp, 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painterResource(Res.drawable.ic_like_outline),
            null,
            Modifier.size(12.dp).align(Alignment.CenterVertically),
            tint = MaterialTheme.colorScheme.primary
        )

        likeInfos.forEachIndexed { index, info ->
            Text(
                (info.nickname ?: "未命名用户").let { if (index == 0) it else "$it," },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun DynamicItem(
    details: DynamicDetailsVO, friendsInfos: Map<Long, AccountInfo>,
    onLook: (FileRes) -> Unit, modifier: Modifier = Modifier,
    onLike: suspend (cancel: Boolean) -> Boolean,
    onClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val info by produceAccountInfoState(details.uid, true)
    val avatar: Painter? by produceImageState(
        info?.avatarUrl,
        painterResource(Res.drawable.logo),
        true,
        keys = arrayOf(info)
    )
    var liked by remember { mutableStateOf(details.likeIds.contains(LoggedInState.info?.uid)) }
    var likeIds by remember { mutableStateOf(details.likeIds) }
    Row(
        modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Avatar(avatar, 42.dp)
        Column(Modifier.weight(1f)) {
            Text(
                info?.nickname ?: "未命名用户",
                Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Text(details.content)
            Spacer(Modifier.requiredHeight(4.dp))
            DynamicAttachments(details.attachments, onLook = onLook)
            Spacer(Modifier.requiredHeight(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    details.time.formatLocal(),
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalContentColor.current.copy(alpha = DisabledAlpha)
                )
                IconButton({
                    scope.launch {
                        if (onLike(liked)) {
                            liked = !liked
                            val loggedUID = LoggedInState.info?.uid ?: return@launch
                            if (liked && !likeIds.contains(loggedUID)) likeIds =
                                likeIds.toMutableList().apply { add(loggedUID) }
                            else if (!liked && likeIds.contains(loggedUID)) likeIds =
                                likeIds.toMutableList().apply { remove(loggedUID) }
                        }
                    }
                }, Modifier.size(24.dp)) {
                    Icon(
                        if (liked) painterResource(Res.drawable.ic_like_fill)
                        else painterResource(Res.drawable.ic_like_outline),
                        null,
                        Modifier.size(12.dp),
                        tint = if (liked) MaterialTheme.colorScheme.primary
                        else LocalContentColor.current
                    )
                }
            }

            Spacer(Modifier.requiredHeight(4.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                LikeNicknames(likeIds, friendsInfos)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicView(
    paddingValues: PaddingValues, onLook: (FileRes) -> Unit,
    navigateToDynamicDetailsRoute: () -> Unit,
) {
    val state = remember { DynamicViewState() }
    val friendsInfos by produceState(mapOf<Long, AccountInfo>()) {
        withContext(Dispatchers.IO) {
            val res = mutableMapOf<Long, AccountInfo>()
            LoggedInState.info?.let {
                res[it.uid] = it
                database.friendQueries.selectAllByLoggedUid(it.uid) { _, _, uid, nickname, avatarUrl, _, _ ->
                    AccountInfo(uid, nickname, avatarUrl)
                }.executeAsList().forEach { info ->
                    res[info.uid] = info
                }
            }
            value = res
        }
    }

    val pullToRefreshState = rememberPullToRefreshState { !state.loading }
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            state.load(true)
            delay(1000)
            pullToRefreshState.endRefresh()
        }
    }

    var likeLoadingState by remember { mutableStateOf<LoadingDialogState?>(null) }

    likeLoadingState?.let {
        LoadingDialog(it, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false))
    }

    Box(Modifier.fillMaxWidth().padding(paddingValues).nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        LazyColumn(
            Modifier.fillMaxSize(),
        ) {
            items(state.data, key = { it.id }) {
                DynamicItem(
                    it, friendsInfos, onLook = onLook,
                    onLike = { cancel ->
                        if (likeLoadingState != null) return@DynamicItem false
                        likeLoadingState = LoadingDialogState("请稍后...")
                        val res = DynamicService.get().like(LikeDynamicParam(it.id, cancel))
                        delay(200)
                        if (!res.success) {
                            likeLoadingState = LoadingDialogState(res.msg, error = true)
                            delay(500)
                        }
                        likeLoadingState = null
                        res.success
                    },
                    onClick = {
                        DynamicDetailsRoute.open(it) {
                            navigateToDynamicDetailsRoute()
                        }
                    }
                )
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

            if (getPlatform() == Platform.Desktop) {
                item {
                    Spacer(Modifier.requiredHeight(96.dp))
                }
            }
        }

        if (getPlatform() == Platform.Desktop) {
            FloatingActionButton(
                { pullToRefreshState.startRefresh() },
                Modifier.align(Alignment.BottomEnd).offset((-16).dp, (-16).dp)
            ) {
                Icon(Icons.Filled.Refresh, "refresh")
            }
        }
    }
}