package icu.twtool.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.DialogProperties
import icu.twtool.chat.animate.toDpSize
import icu.twtool.chat.cache.produceImageState
import icu.twtool.chat.components.LoadingDialog
import icu.twtool.chat.components.LoadingDialogState
import icu.twtool.chat.components.file.FilePosition
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.components.file.ImageRes
import icu.twtool.chat.io.ICFile
import icu.twtool.chat.server.dynamic.DynamicService
import icu.twtool.chat.server.dynamic.param.PublishDynamicParam
import icu.twtool.chat.service.get
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens
import icu.twtool.chat.utils.rememberFileChooser
import icu.twtool.cos.CommonObjectMetadata
import icu.twtool.cos.getCosClient
import icu.twtool.image.compose.ICAsyncImage
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_back
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublishDynamicTopAppBar(onBack: () -> Unit, onPublish: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text("分享心情")
        },
        navigationIcon = {
            IconButton(onBack) {
                Icon(painterResource(Res.drawable.ic_back), "返回")
            }
        },
        actions = {
            TextButton(onPublish) {
                Text("发布")
            }
        }
    )
}

@Composable
private fun AttachmentPreview(
    file: ICFile,
    onLook: (FileRes) -> Unit,
    modifier: Modifier = Modifier
) {
    val painter: Painter? by produceImageState(file)
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
        }.pointerInput(file) {
            detectTapGestures {
                painter?.let { p -> ImageRes({ p }, position, null) }?.let(onLook)
            }
        },
        contentScale = ContentScale.Crop
    )
}

@Composable
fun AttachmentsChooser(
    value: List<ICFile>,
    onAdd: (List<ICFile>) -> Unit,
    onLook: (FileRes) -> Unit,
    modifier: Modifier = Modifier
) {
    val fileChooser = rememberFileChooser { files ->
        onAdd(files.filter { !value.contains(it) })
    }
    val scope = rememberCoroutineScope()
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(value, key = { it.key }) { file ->
            AttachmentPreview(file, onLook, Modifier.size(100.dp))
        }
        item {
            Box(
                Modifier.size(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2))
                    .clickable {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                fileChooser.launch()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, null)
            }
        }
    }
}

@Composable
fun PublishDynamicView(
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onLook: (FileRes) -> Unit,
    onPublishComplete: () -> Unit,
) {
    var content: String by remember { mutableStateOf("") }
    val attachments = remember { mutableStateListOf<ICFile>() }
    val inputInteractionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()

    var publishState by remember { mutableStateOf<LoadingDialogState?>(null) }

    publishState?.let {
        LoadingDialog(it, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false))
    }

    val focused by inputInteractionSource.collectIsFocusedAsState()
    Column(Modifier.padding(paddingValues).fillMaxWidth()) {
        PublishDynamicTopAppBar(onBack = onBack, onPublish = {
            val realContent = content.trim()
            if (realContent.length !in 0..1024) {
                scope.launch {
                    snackbarHostState.showSnackbar("内容最长为 1024 个字符")
                }
                return@PublishDynamicTopAppBar
            }
            if (publishState != null) return@PublishDynamicTopAppBar
            publishState = LoadingDialogState("准备中...")
            scope.launch {
                withContext(Dispatchers.IO) {
                    val client = getCosClient()
                    val attachmentUrls = attachments.mapIndexed { index, it ->
                        publishState = LoadingDialogState("上传图片（${index}/${attachments.size}）")
                        val key = "res/${LoggedInState.info?.uid}/${it.hashKey}"
                        client.putObject(key, it.inputStream(), CommonObjectMetadata(it.size))
                        key
                    }
                    publishState = LoadingDialogState("正在发布...")
                    val res = DynamicService.get().publish(
                        PublishDynamicParam(realContent, attachmentUrls)
                    )
                    publishState = if (res.success) {
                        onPublishComplete()
                        content = ""
                        attachments.clear()
                        LoadingDialogState("已发布", success = true)
                    } else LoadingDialogState(res.msg, error = true)
                    delay(500)
                    publishState = null
                }
            }
        })
        Column(
            Modifier.padding(horizontal = 16.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicTextField(
                content, { content = it },
                Modifier.heightIn(64.dp).fillMaxWidth().weight(1f, false),
                interactionSource = inputInteractionSource,
                textStyle = LocalTextStyle.current.copy(LocalContentColor.current),
                cursorBrush = SolidColor(LocalContentColor.current),
            ) {
                if (content.isEmpty() && !focused)
                    Text("分享心情", color = LocalContentColor.current.copy(DisabledAlpha))
                it()
            }
            AttachmentsChooser(
                attachments,
                onAdd = { attachments.addAll(it) },
                onLook
            )
            Spacer(Modifier.requiredHeight(16.dp))
        }
    }
}