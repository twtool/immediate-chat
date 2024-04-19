package icu.twtool.chat.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.substring
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
import icu.twtool.chat.components.AccountInfoCard
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.components.BackTopAppBar
import icu.twtool.chat.components.LoadingDialog
import icu.twtool.chat.components.LoadingDialogState
import icu.twtool.chat.components.ThumbnailsImage
import icu.twtool.chat.components.WindowDialog
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.chat.model.FileMessageContent
import icu.twtool.chat.server.chat.model.ImageMessageContent
import icu.twtool.chat.server.chat.model.MessageContent
import icu.twtool.chat.server.chat.model.PlainMessageContent
import icu.twtool.chat.server.common.result
import icu.twtool.chat.state.ChatMessageItem
import icu.twtool.chat.state.ChatViewState
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens
import icu.twtool.chat.utils.FileType
import icu.twtool.chat.utils.onEnterKeyPressed
import icu.twtool.chat.utils.rememberFileChooser
import icu.twtool.cos.CommonObjectMetadata
import icu.twtool.cos.getCosClient
import icu.twtool.logger.getLogger
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_emoji
import immediatechat.app.generated.resources.ic_file
import immediatechat.app.generated.resources.ic_photo
import immediatechat.app.generated.resources.ic_unknown_file
import immediatechat.app.generated.resources.ic_word
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
                getCosClient().putObject(key, file.inputStream(), CommonObjectMetadata(file.size))
                state.send(ImageMessageContent(key), info.uid)
                delay.join()
            }
            sendingDialogState = null
        }
    }

    val sendFileChooser = rememberFileChooser {
        if (it.isEmpty() || sendingDialogState != null) return@rememberFileChooser
        val size = it.size
        scope.launch {
            it.forEachIndexed { index, file ->
                sendingDialogState = LoadingDialogState("发送中($index/$size)")
                val delay = launch { delay(200) }
                val key = "res/${LoggedInState.info?.uid}/${file.hashKey}"
                val success = withContext(Dispatchers.IO) {
                    val res =
                        getCosClient().putObject(key, file.inputStream(), CommonObjectMetadata(file.size))?.apply {
                            FileMapping[key] = file.save()
                            state.send(FileMessageContent(key, file.filename, file.extension), info.uid)
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
                        widthSizeClass,
                        state,
                        onLookFile = onLookFile,
                        navigateChatRoute,
                        navigateAccountInfoRoute,
                        Modifier.fillMaxWidth()
                    )
                }
                if (widthSizeClass < ICWindowWidthSizeClass.Expanded)
                    ChatViewInput(state.sending, paddingValues) {
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
                        scope.launch {
                            sendFileChooser.launch(FileType.FILE)
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
            RenderMessageContent(item.message.content, onLookFile = onLookFile)
        }
        if (item.me) ChatItemAvatar(item, widthSizeClass, onLookInfo, navigateChatRoute)
        else Box(Modifier.size(42.dp))
    }
}

@Composable
private fun RenderMessageContent(message: MessageContent, onLookFile: (FileRes) -> Unit) {
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
            Surface(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level1),
                shape = MaterialTheme.shapes.extraSmall,
                onClick = {

                }
            ) {
                Column(Modifier.width(250.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp, 8.dp).height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.fillMaxHeight()) {
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
                                else -> painterResource(Res.drawable.ic_unknown_file)
                            },
                            null,
                            Modifier.height(56.dp).padding(8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(
                        Modifier.height(1.dp).fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level4))
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)
                    ) {
                        val downloadState by produceState("未下载") {
                            if (FileMapping[message.url] != null) {
                                value = "已下载"
                            }
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
fun ChatViewExpandedInput(
    sending: Boolean, paddingValues: PaddingValues,
    sendImage: () -> Unit,
    sendFile: () -> Unit,
    onSend: (value: MessageContent) -> Unit,
) {
    var inputValue: String by remember { mutableStateOf("") }
    Surface(
        Modifier.fillMaxWidth()/*padding(bottom = paddingValues.calculateBottomPadding())*/,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level1)
    ) {
        Column(Modifier.navigationBarsPadding().imePadding()) {
            Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChatViewIcon("表情", painterResource(Res.drawable.ic_emoji)) {}
                ChatViewIcon("图片", painterResource(Res.drawable.ic_photo)) { sendImage() }
                ChatViewIcon("文件", painterResource(Res.drawable.ic_file)) { sendFile() }
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
fun ChatViewInput(sending: Boolean, paddingValues: PaddingValues, onSend: (value: MessageContent) -> Unit) {
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