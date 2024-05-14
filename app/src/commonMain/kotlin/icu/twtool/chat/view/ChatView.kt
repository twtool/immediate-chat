package icu.twtool.chat.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import icu.twtool.chat.app.AccountInfoRoute
import icu.twtool.chat.app.ChatRoute
import icu.twtool.chat.cache.FileMapping
import icu.twtool.chat.cache.loadAccountInfo
import icu.twtool.chat.cache.produceImageState
import icu.twtool.chat.components.*
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.constants.Platform
import icu.twtool.chat.constants.getPlatform
import icu.twtool.chat.io.ICFile
import icu.twtool.chat.io.OpenableFile
import icu.twtool.chat.io.rememberOpenableFileUtil
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.chat.model.FileMessageContent
import icu.twtool.chat.server.chat.model.ImageMessageContent
import icu.twtool.chat.server.chat.model.MessageContent
import icu.twtool.chat.server.chat.model.PlainMessageContent
import icu.twtool.chat.state.ChatMessageItem
import icu.twtool.chat.state.ChatViewState
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens
import icu.twtool.chat.utils.FileType
import icu.twtool.chat.utils.onEnterKeyPressed
import icu.twtool.chat.utils.rememberFileChooser
import icu.twtool.cos.CommonObjectMetadata
import icu.twtool.cos.TaskResult
import icu.twtool.cos.TaskState
import icu.twtool.cos.getCosClient
import icu.twtool.logger.getLogger
import immediatechat.app.generated.resources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource

private val log = getLogger("icu.twtool.chat.view.ChatView.kt")

@Composable
fun ChatInfoTopAppBar(onBack: () -> Unit, title: String, onClickMenu: () -> Unit) {
    BackTopAppBar(onBack, title) {
        IconButton(onClickMenu) {
            Icon(Icons.Filled.Menu, "Menu")
        }
    }
}

@Composable
fun ChatView(
    snackbarHostState: SnackbarHostState,
    widthSizeClass: ICWindowWidthSizeClass,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onLookFile: (FileRes) -> Unit,
    navigateToChatSettingsRoute: () -> Unit,
    navigateChatRoute: () -> Unit,
    navigateAccountInfoRoute: () -> Unit,
) {
    val info = ChatRoute.info ?: return
    val scope = rememberCoroutineScope()
    val state = remember(info.uid) { ChatViewState(scope, info.uid) }
    val openableFileUtil = rememberOpenableFileUtil()
    var showChatSettingPopup by remember { mutableStateOf(false) }

    var sendingDialogState by remember { mutableStateOf<LoadingDialogState?>(null) }

    val sendImageFileChooser = rememberFileChooser {
        if (it.isEmpty() || sendingDialogState != null) return@rememberFileChooser
        val size = it.size
        scope.launch {
            it.forEachIndexed { index, file ->
                sendingDialogState = LoadingDialogState("发送中($index/$size)")
                val delay = launch { delay(200) }
                val key = "res/${LoggedInState.info?.uid}/${file.hashKey}"
                withContext(Dispatchers.IO) {
                    getCosClient().putObject(key, file.inputStream(), CommonObjectMetadata(file.size))
                }
                state.send(ImageMessageContent(key), info.uid)
                delay.join()
            }
            sendingDialogState = null
        }
    }

    val sendFile = sendFile@{ files: List<ICFile> ->
        if (files.isEmpty() || sendingDialogState != null) return@sendFile
        val size = files.size
        scope.launch {
            files.forEachIndexed { index, file ->
                sendingDialogState = LoadingDialogState("发送中($index/$size)")
                val delay = launch { delay(200) }
                val key = "res/${LoggedInState.info?.uid}/${file.hashKey}"
                val success = withContext(Dispatchers.IO) {
                    val res =
                        getCosClient().putObject(key, file.inputStream(), CommonObjectMetadata(file.size))?.apply {
                            FileMapping[key] = file.save()
                            val fileSize =
                                if (file.size / 1024 > 1024) "${String.format("%.2f", file.size / 1024 / 1024.0)} MB"
                                else "${String.format("%.2f", file.size / 1024.0)} KB"
                            state.send(
                                FileMessageContent(
                                    key, file.filename, file.extension,
                                    fileSize,
                                    origin = "即时聊天 Linux 版"
                                ), info.uid
                            )
                        } != null
                    delay.join()
                    res
                }
                if (!success) {
                    sendingDialogState = LoadingDialogState("发送失败", error = true)
                    delay(2000)
                }
                sendingDialogState = null
            }
        }
    }

    val sendFileChooser = rememberFileChooser {
        sendFile(it)
    }

    sendingDialogState?.let {
        LoadingDialog(it, {}, DialogProperties(false, dismissOnClickOutside = false))
    }

    Column(Modifier.fillMaxSize()) {
        ChatInfoTopAppBar(
            onBack, info.nickname ?: "未命名用户",
            onClickMenu = {
                if (widthSizeClass < ICWindowWidthSizeClass.Expanded) navigateToChatSettingsRoute()
                else showChatSettingPopup = true
            }
        )
        Box(
            Modifier.fillMaxSize()
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    ChatViewMessages(
                        snackbarHostState,
                        widthSizeClass,
                        state,
                        onLookFile = onLookFile,
                        navigateChatRoute,
                        navigateAccountInfoRoute,
                        Modifier.fillMaxWidth()
                    )
                }
                if (widthSizeClass < ICWindowWidthSizeClass.Expanded)
                    ChatViewInput(
                        state.sending, paddingValues,
                        sendImage = {
                            scope.launch {
                                sendImageFileChooser.launch(FileType.IMAGE)
                            }
                        },
                        sendFile = {
                            scope.launch {
                                sendFileChooser.launch(FileType.FILE)
                            }
                        }
                    ) {
                        scope.launch {
                            state.send(it, info.uid)
                        }
                    }
                else ChatViewExpandedInput(
                    state.sending,
                    paddingValues,
                    sendImage = {
                        scope.launch {
                            sendImageFileChooser.launch(FileType.IMAGE)
                        }
                    },
                    sendFile = {
                        if (it == null) scope.launch {
                            sendFileChooser.launch(FileType.FILE)
                        } else {
                            val files = it.map { path ->
                                openableFileUtil.get(path.replaceFirst("file:", "").replace("%20", " "))
                            }.filter(OpenableFile::exists)
                            if (files.isNotEmpty()) {
                                sendFile(files)
                            }
                        }
                    }
                ) {
                    scope.launch {
                        state.send(it, info.uid)
                    }
                }
            }
            ChatSettingsPopup(showChatSettingPopup) {
                showChatSettingPopup = it
            }
        }
    }
}

@Composable
private fun ChatItemAvatar(
    item: ChatMessageItem, widthSizeClass: ICWindowWidthSizeClass,
    onLookInfo: (AccountInfo) -> Unit,
    navigateChatRoute: (AccountInfo) -> Unit
) {
    val info by produceState<AccountInfo?>(null, item) {
        value = if (item.me) LoggedInState.info else loadAccountInfo(item.message.sender)
    }

    val showPopup = remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Avatar(info?.avatarUrl, 42.dp) {
            if (widthSizeClass == ICWindowWidthSizeClass.Expanded) showPopup.value = true
            else info?.let(onLookInfo)
        }
        Spacer(Modifier.requiredHeight(4.dp))
        Text(
            item.createTimeFormat,
            style = MaterialTheme.typography.labelSmall
        )
        if (showPopup.value) info?.let {
            AccountInfoCardPopup(
                it,
                onDismissRequest = { showPopup.value = false },
                onLookInfoClick = {
                    showPopup.value = false
                    onLookInfo(it)
                },
                onSendClick = {
                    showPopup.value = false
                    navigateChatRoute(it)
                },
                DpOffset(if (item.me) (-52).dp else 52.dp, 0.dp),
                if (item.me) Alignment.TopEnd else Alignment.TopStart,
                showOpenChat = false
//                        showOpenChat = item.me
            )
        }
    }
}

@Composable
fun ChatViewMessageItem(
    widthSizeClass: ICWindowWidthSizeClass,
    item: ChatMessageItem,
    onLookInfo: (AccountInfo?) -> Unit,
    onLookFile: (FileRes) -> Unit,
    showMessage: (String) -> Unit,
    navigateChatRoute: (AccountInfo?) -> Unit
) {

    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (item.me) Box(Modifier.size(42.dp))
        else ChatItemAvatar(item, widthSizeClass, onLookInfo, navigateChatRoute)
        Row(
            Modifier.weight(1f),
            horizontalArrangement = if (item.me) Arrangement.End else Arrangement.Start
        ) {
            RenderMessageContent(item.id, item.message.content, onLookFile = onLookFile, showMessage = showMessage)
        }
        if (item.me) ChatItemAvatar(item, widthSizeClass, onLookInfo, navigateChatRoute)
        else Box(Modifier.size(42.dp))
    }
}

@Composable
private fun RenderMessageContent(
    id: Long, message: MessageContent,
    onLookFile: (FileRes) -> Unit,
    showMessage: (String) -> Unit
) {
    when (message) {
        is PlainMessageContent -> {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = MaterialTheme.shapes.small,
            ) {
                SelectionContainer(Modifier.padding(8.dp)) {
                    Text(message.value)
                }
            }
        }

        is ImageMessageContent -> {
            val painter by produceImageState(message.url, queryParameter = "imageView2/2/w/500/h/500/rq/50")
            if (painter != null) {
                painter?.let {
                    ThumbnailsImage(
                        it,
                        Modifier.clip(MaterialTheme.shapes.small).widthIn(max = 200.dp),
                        onLook = onLookFile
                    )
                }
            } else {
                Box(
                    Modifier.size(64.dp).background(MaterialTheme.colorScheme.onSecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painterResource(Res.drawable.ic_photo), null)
                }
            }
        }

        is FileMessageContent -> {
            val scope = rememberCoroutineScope()
            var taskResult by remember { mutableStateOf<TaskResult?>(null) }
            var taskState by remember { mutableStateOf<TaskState?>(null) }
            val taskPercentage = remember { Animatable(0f) }
            val openableFileUtil = rememberOpenableFileUtil()

            Surface(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level1),
                shape = MaterialTheme.shapes.extraSmall,
                onClick = {
                    val path = FileMapping[message.url]
                    if (path != null) {
                        if (!openableFileUtil.get(path).open()) {
                            showMessage("没有应用支持此文件")
                        }
                    } else {
                        if (taskResult == null) {
                            taskResult = getCosClient().getObject(
                                message.url, id,
                                message.filename, message.extension,
                                onChangePercentage = { scope.launch { taskPercentage.animateTo(it) } },
                                onChangeState = { state ->
                                    taskState = state
                                    taskResult?.let {
                                        if (state == TaskState.COMPLETED) {
                                            FileMapping[message.url] = it.path
                                        }
                                    }
                                }
                            )
                        }

                        taskState?.let {
                            when (it) {
                                TaskState.PAUSED -> taskResult?.parse()
                                TaskState.UNKNOWN -> {}
                                TaskState.FAILED -> {}
                                TaskState.COMPLETED -> {}
                                TaskState.IN_PROGRESS -> {}
                                TaskState.CANCELED -> {}
                            }
                        }
                    }
                }
            ) {
                Column(Modifier.width(250.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp, 8.dp).height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.fillMaxHeight().weight(1f, false)) {
                            Text(
                                message.filename + "." + message.extension,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.requiredHeight(8.dp))
                            Text(
                                message.size ?: "未知大小",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalContentColor.current.copy(DisabledAlpha)
                            )
                        }
                        Spacer(Modifier.requiredWidth(16.dp))
                        Image(
                            when (message.extension) {
                                "docx" -> painterResource(Res.drawable.ic_word)
                                "png" -> painterResource(Res.drawable.ic_png)
                                else -> painterResource(Res.drawable.ic_unknown_file)
                            },
                            null,
                            Modifier.height(56.dp).padding(8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp)) {
                        Spacer(
                            Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level4))
                        )
                        if (taskResult != null) {
                            if (taskState != TaskState.COMPLETED) {
                                if (taskState == TaskState.FAILED) {
                                    Box(
                                        Modifier.fillMaxHeight().fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                } else {
                                    Box(
                                        Modifier.fillMaxHeight().fillMaxWidth(taskPercentage.value)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)
                    ) {
                        val downloadState by produceState("未下载", taskState) {
                            val res = if (taskState == TaskState.FAILED) {
                                "下载失败"
                            } else if (FileMapping[message.url] != null) {
                                "已下载"
                            } else if (taskState != null) {
                                "下载中"
                            } else {
                                null
                            }
                            res?.let { value = it }
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp, 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                message.origin ?: "未知来源",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalContentColor.current.copy(DisabledAlpha),
                            )
                            Text(
                                downloadState,
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalContentColor.current.copy(DisabledAlpha),
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun ChatViewMessages(
    snackbarHostState: SnackbarHostState,
    widthSizeClass: ICWindowWidthSizeClass,
    state: ChatViewState,
    onLookFile: (FileRes) -> Unit,
    navigateChatRoute: () -> Unit,
    navigateAccountInfoRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    LaunchedEffect(state.messages.size) {
        lazyListState.animateScrollToItem(0)
    }

    var showChangeAccountInfoDialog by remember { mutableStateOf(false) }

    if (showChangeAccountInfoDialog) {
        WindowDialog(
            "编辑资料",
            { showChangeAccountInfoDialog = false }, Modifier.width(400.dp),
            properties = DialogProperties(dismissOnClickOutside = false)
        ) {
            Box(Modifier.fillMaxHeight(0.8f)) {
                ChangeAccountInfoView(
                    paddingValues = PaddingValues(),
                    onBack = { showChangeAccountInfoDialog = false }
                )
            }
        }
    }

    LazyColumn(
        modifier,
        state = lazyListState,
        reverseLayout = true,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(state.messages, key = { it.id }) { item ->
            ChatViewMessageItem(
                widthSizeClass,
                item,
                onLookInfo = {
                    if (widthSizeClass == ICWindowWidthSizeClass.Expanded && item.me) showChangeAccountInfoDialog = true
                    else {
                        it?.let { info ->
                            AccountInfoRoute.open(info) {
                                navigateAccountInfoRoute()
                            }
                        }
                    }
                },
                onLookFile = onLookFile,
                showMessage = { scope.launch { snackbarHostState.showSnackbar(it) } },
                navigateChatRoute = {
                    it?.let { info ->
                        scope.launch {
                            ChatRoute.open(info) {
                                navigateChatRoute()
                            }
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatViewIcon(tooltip: String, icon: Painter, onClick: () -> Unit) {
    TooltipBox(
        TooltipDefaults.rememberPlainTooltipPositionProvider(),
        {
            PlainTooltip {
                Text(tooltip)
            }
        },
        rememberTooltipState()
    ) {
        Icon(icon, tooltip, Modifier.size(20.dp).clickable(onClick = onClick))
    }
}

@Composable
expect fun Modifier.onIcExternalDrag(onDragFiles: (List<String>) -> Unit): Modifier

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatViewExpandedInput(
    sending: Boolean, paddingValues: PaddingValues,
    sendImage: () -> Unit,
    sendFile: (files: List<String>?) -> Unit,
    onSend: (value: MessageContent) -> Unit,
) {
    var inputValue: String by remember { mutableStateOf("") }
    Surface(
        Modifier.fillMaxWidth()
            .onIcExternalDrag {
                sendFile(it)
            }
        /*padding(bottom = paddingValues.calculateBottomPadding())*/,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level1)
    ) {
        Column(Modifier.navigationBarsPadding().imePadding()) {
            Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                ChatViewIcon("表情", painterResource(Res.drawable.ic_emoji)) {}
                ChatViewIcon("图片", painterResource(Res.drawable.ic_photo)) { sendImage() }
                ChatViewIcon("文件", painterResource(Res.drawable.ic_file)) { sendFile(null) }
            }
            BasicTextField(
                inputValue, { inputValue = it },
                Modifier.fillMaxWidth().height(128.dp).padding(8.dp, 0.dp).onEnterKeyPressed(ctrlPressed = true) {
                    onSend(PlainMessageContent(inputValue))
                    inputValue = ""
                    true
                },
                textStyle = LocalTextStyle.current.copy(LocalContentColor.current),
                cursorBrush = SolidColor(LocalContentColor.current),
            )
            Button(
                onClick = {
                    onSend(PlainMessageContent(inputValue))
                    inputValue = ""
                },
                Modifier.align(Alignment.End).padding(8.dp).height(28.dp),
                enabled = !sending,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(8.dp, 4.dp)
            ) {
                Text("发送")
            }
        }
    }
}

@Composable
fun ChatViewInput(
    sending: Boolean,
    paddingValues: PaddingValues,
    sendImage: () -> Unit,
    sendFile: () -> Unit,
    onSend: (value: MessageContent) -> Unit
) {
    var inputValue: String by remember { mutableStateOf("") }
    Surface(
        Modifier.fillMaxWidth()/*.padding(bottom = paddingValues.calculateBottomPadding())*/,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)
    ) {
        Column(Modifier.imePadding().navigationBarsPadding()) {
            Row(
                Modifier.height(IntrinsicSize.Min).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BasicTextField(
                    inputValue, { inputValue = it },
                    Modifier.weight(1f)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.background)
                        .heightIn(min = 32.dp)
                        .padding(8.dp)
                        .onEnterKeyPressed(ctrlPressed = true) {
                            onSend(PlainMessageContent(inputValue))
                            inputValue = ""
                            true
                        },
                )

                AnimatedVisibility(inputValue.isEmpty()) {
                    Row(
                        Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChatViewIcon("图片", painterResource(Res.drawable.ic_photo)) { sendImage() }
                        if (getPlatform() == Platform.Desktop) {
                            ChatViewIcon("文件", painterResource(Res.drawable.ic_file)) { sendFile() }
                        }
                    }
                }

                AnimatedVisibility(inputValue.isNotEmpty()) {
                    Button(
                        onClick = {
                            onSend(PlainMessageContent(inputValue))
                            inputValue = ""
                        },
                        Modifier.align(Alignment.Bottom).height(32.dp),
                        enabled = !sending,
                        shape = MaterialTheme.shapes.extraSmall,
                        contentPadding = PaddingValues(8.dp, 4.dp)
                    ) {
                        Text("发送")
                    }
                }

            }
        }
    }
}

@Composable
private fun AccountInfoCardPopup(
    info: AccountInfo,
    onDismissRequest: () -> Unit,
    onLookInfoClick: () -> Unit,
    onSendClick: () -> Unit,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    alignment: Alignment = Alignment.TopStart,
    showOpenChat: Boolean = true
) {
    Popup(
        offset = with(LocalDensity.current) { IntOffset(offset.x.roundToPx(), offset.y.roundToPx()) },
        onDismissRequest = onDismissRequest,
        alignment = alignment
    ) {
        AccountInfoCard(
            info,
            lookInfoText = if (info.uid == LoggedInState.info?.uid) "编辑资料" else "查看资料",
            onLookInfoClick = onLookInfoClick,
            onSendClick = onSendClick,
            showOpenChat = showOpenChat
        )
    }
}

@Composable
fun BoxScope.ChatSettingsPopup(show: Boolean, onChangeFocused: (Boolean) -> Unit) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(show) {
        if (show) focusRequester.requestFocus()
    }
    val width = animateDpAsState(targetValue = if (show) 300.dp else 0.dp)

    Surface(
        Modifier.align(Alignment.TopEnd)
            .width(width.value)
            .fillMaxHeight()
            .onFocusChanged {
                if (!show) return@onFocusChanged

                if (!it.hasFocus) onChangeFocused(false)
            }
            .focusRequester(focusRequester)
            .focusable(),
        shadowElevation = ElevationTokens.Level2
    ) {
        ChatSettingsView()
    }

}