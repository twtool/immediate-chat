package icu.twtool.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import icu.twtool.chat.app.DynamicDetailsRoute
import icu.twtool.chat.cache.produceAccountInfoState
import icu.twtool.chat.cache.produceImageState
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.components.BackTopAppBar
import icu.twtool.chat.components.LoadingDialog
import icu.twtool.chat.components.LoadingDialogState
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.database.database
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.common.datetime.nowUTC
import icu.twtool.chat.server.dynamic.DynamicService
import icu.twtool.chat.server.dynamic.param.CommentDynamicParam
import icu.twtool.chat.server.dynamic.param.LikeDynamicParam
import icu.twtool.chat.server.dynamic.vo.DynamicCommentVO
import icu.twtool.chat.service.get
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens
import icu.twtool.chat.utils.formatLocal
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_like_fill
import immediatechat.app.generated.resources.ic_like_outline
import immediatechat.app.generated.resources.logo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource

@Composable
private fun DynamicDetailsTopAppBar(onBack: () -> Unit) {
    BackTopAppBar(onBack, "动态详情")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicDetailsView(onBack: () -> Unit, onLook: (FileRes) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        DynamicDetailsTopAppBar(onBack)

        val scope = rememberCoroutineScope()

        var loadingState by remember { mutableStateOf<LoadingDialogState?>(null) }

        loadingState?.let {
            LoadingDialog(it, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false))
        }

        val details by produceState(DynamicDetailsRoute.details ?: return@Column, DynamicDetailsRoute.details) {
            withContext(Dispatchers.IO) {
                val id = DynamicDetailsRoute.details?.id ?: return@withContext
                loadingState = LoadingDialogState("加载中...")
                val delay = launch {
                    delay(200)
                }
                val res = DynamicService.get().details(id.toString())
                delay.join()
                if (res.success) {
                    res.data?.let { value = it }
                } else {
                    loadingState = LoadingDialogState(res.msg, error = true)
                    delay(1000)
                }
                loadingState = null
            }
        }

        var showDeleteAlertDialog by remember { mutableStateOf(false) }
        if (showDeleteAlertDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAlertDialog = false },
                title = {
                    Text("删除该动态？")
                },
                confirmButton = {
                    TextButton({
                        if (loadingState != null) return@TextButton
                        loadingState = LoadingDialogState("删除中...")
                        scope.launch {
                            val res = DynamicService.get().delete(details.id.toString())
                            val delay = launch { delay(200) }
                            delay.join()
                            loadingState = if (res.success) LoadingDialogState("删除成功", success = true)
                            else LoadingDialogState(res.msg, error = true)
                            delay(500)
                            if (!res.success) loadingState = null
                            onBack()
                        }
                    }) {
                        Text("确定")
                    }
                }
            )
        }

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

        val info by produceAccountInfoState(details.uid, true)
        val avatar: Painter? by produceImageState(
            info?.avatarUrl,
            painterResource(Res.drawable.logo),
            true,
            keys = arrayOf(info)
        )

        var liked by remember(details.id) { mutableStateOf(details.likeIds.contains(LoggedInState.info?.uid)) }
        var likeIds by remember(details.id) { mutableStateOf(details.likeIds) }

        val comments = remember(details.comments) { mutableStateListOf(*details.comments.toTypedArray()) }
        var replyComment by remember(details.id) { mutableStateOf<DynamicCommentVO?>(null) }

        Row(
            Modifier.fillMaxWidth().weight(1f, false).padding(16.dp).verticalScroll(rememberScrollState()),
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
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        details.time.formatLocal(),
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.copy(alpha = DisabledAlpha)
                    )
                    Spacer(Modifier.requiredWidth(4.dp))
                    if (details.uid == LoggedInState.info?.uid) {
                        IconButton({
                            showDeleteAlertDialog = true
                        }, Modifier.size(24.dp)) {
                            Icon(
                                Icons.Filled.Delete, null, Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton({
                        if (loadingState != null) return@IconButton
                        loadingState = LoadingDialogState("请稍候...")
                        scope.launch {
                            val res = DynamicService.get().like(LikeDynamicParam(details.id, liked))
                            delay(200)
                            if (res.success) {
                                loadingState = null
                                liked = !liked
                                val loggedUID = LoggedInState.info?.uid ?: return@launch
                                if (liked && !likeIds.contains(loggedUID)) likeIds =
                                    likeIds.toMutableList().apply { add(loggedUID) }
                                else if (!liked && likeIds.contains(loggedUID)) likeIds =
                                    likeIds.toMutableList().apply { remove(loggedUID) }
                            } else {
                                loadingState = LoadingDialogState(res.msg, error = true)
                                delay(500)
                                loadingState = null
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
                    Column {
                        LikeNicknames(likeIds, friendsInfos)

                        if (comments.isNotEmpty()) {
                            Spacer(
                                Modifier.fillMaxWidth(0.75f)
                                    .requiredHeight(1.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level5))
                            )

                            DynamicComments(
                                comments,
                                friendsInfos,
                                onChangeReplyComment = { replyComment = it }
                            )

                        }
                    }
                }
            }
        }

        Spacer(Modifier.requiredHeight(4.dp))

        DynamicCommentInput(
            dynamicId = details.id,
            replyComment = replyComment,
            friendsInfos = friendsInfos,
            onChangeRelyComment = { replyComment = it },
            onComment = { content, replyId ->
                comments.add(
                    DynamicCommentVO(
                        (comments.lastOrNull()?.id ?: 0L) + 1L,
                        LoggedInState.info?.uid ?: 0L,
                        content = content,
                        replyId = replyId,
                        time = LocalDateTime.nowUTC()
                    )
                )
            }
        )
    }
}

@Composable
private fun DynamicCommentInput(
    dynamicId: Long,
    replyComment: DynamicCommentVO?,
    friendsInfos: Map<Long, AccountInfo>,
    onChangeRelyComment: (DynamicCommentVO?) -> Unit,
    onComment: (content: String, replyId: Long?) -> Unit
) {
    var content by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var commentLoadingState by remember { mutableStateOf<LoadingDialogState?>(null) }
    commentLoadingState?.let {
        LoadingDialog(it, properties = DialogProperties(false, dismissOnClickOutside = false))
    }

    Column(Modifier.padding(start = 66.dp, bottom = 16.dp, end = 16.dp)) {
        replyComment?.let {
            Text(
                "回复 ${friendsInfos[it.uid]?.nickname ?: "未命名用户"}:",
                style = MaterialTheme.typography.titleSmall,
                color = LocalContentColor.current.copy(DisabledAlpha),
                modifier = Modifier.clickable { onChangeRelyComment(null) }
            )
            Spacer(Modifier.requiredHeight(2.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicTextField(
                content, { content = it.substring(0, it.length.coerceAtMost(255)) },
                Modifier.clip(MaterialTheme.shapes.extraSmall)
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2))
                    .heightIn(min = 32.dp, max = 128.dp)
                    .padding(8.dp, 4.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(LocalContentColor.current),
                cursorBrush = SolidColor(LocalContentColor.current),
            ) {
                Box(contentAlignment = Alignment.CenterStart) {
                    if (content.isEmpty()) Text(
                        "发表评论",
                        color = LocalContentColor.current.copy(alpha = DisabledAlpha),
                        style = MaterialTheme.typography.bodySmall
                    )
                    it()
                    Text(
                        "${content.length}/255", style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current.copy(DisabledAlpha),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
            Button(
                {
                    val realContent = content.trim()
                    if (realContent.isEmpty() || realContent.length > 255) {
                        return@Button
                    }
                    if (commentLoadingState != null) return@Button
                    commentLoadingState = LoadingDialogState("请稍候...")
                    scope.launch {
                        val delay = launch { delay(200) }
                        val res = DynamicService.get().comment(
                            CommentDynamicParam(dynamicId, content.trim(), replyComment?.id)
                        )
                        delay.join()
                        if (!res.success) {
                            commentLoadingState = LoadingDialogState(res.msg, error = true)
                            delay(1000)
                        } else {
                            onComment(content, replyComment?.replyId)
                            content = ""
                        }
                        commentLoadingState = null
                    }
                },
                Modifier.height(32.dp).align(Alignment.Bottom), contentPadding = PaddingValues(8.dp, 4.dp),
                shape = MaterialTheme.shapes.extraSmall,
                enabled = content.trim().isNotEmpty()
            ) {
                Text("发表")
            }
        }
    }

}

private fun isPermission(
    comment: DynamicCommentVO,
    commentMap: Map<Long, DynamicCommentVO>,
    friendsInfos: Map<Long, AccountInfo>
): Boolean {
    if (!friendsInfos.containsKey(comment.uid)) return false
    if (comment.replyId == null) return true
    return commentMap[comment.replyId]?.let { isPermission(it, commentMap, friendsInfos) } ?: false
}

@Composable
fun DynamicComments(
    comments: List<DynamicCommentVO>, friendsInfos: Map<Long, AccountInfo>,
    onChangeReplyComment: (DynamicCommentVO?) -> Unit,
) {
    val style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
        MaterialTheme.colorScheme.secondary
    )
    val commentMap = comments.fold(mutableMapOf<Long, DynamicCommentVO>()) { acc, comment ->
        acc[comment.id] = comment
        acc
    }
    comments.forEach { comment ->
        if (!isPermission(comment, commentMap, friendsInfos)) return@forEach
        Text(
            buildAnnotatedString {
                withStyle(style) {
                    append(friendsInfos[comment.uid]?.nickname ?: "未命名用户")
                }
                comment.replyId?.let { replyId -> comments.find { it.id == replyId } }?.let {
                    append(" 回复 ")
                    withStyle(style) {
                        append(friendsInfos[it.uid]?.nickname ?: "未命名用户")
                    }
                }
                append(": ")
                append(comment.content)
            },
            Modifier.clickable { onChangeReplyComment(comment) }.padding(8.dp, 4.dp).fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}