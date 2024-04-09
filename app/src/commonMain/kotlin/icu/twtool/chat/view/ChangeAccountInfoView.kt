package icu.twtool.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import icu.twtool.chat.cache.produceImageState
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.components.LoadingDialog
import icu.twtool.chat.components.LoadingDialogState
import icu.twtool.chat.components.rememberLoadingDialogState
import icu.twtool.chat.io.ICFile
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.param.UpdateInfoParam
import icu.twtool.chat.service.get
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.ElevationTokens
import icu.twtool.chat.utils.rememberFileChooser
import icu.twtool.cos.CommonObjectMetadata
import icu.twtool.cos.getCosClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
private fun InfoItem(label: String, onClick: () -> Unit = {}, content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        content()
    }
}

@Composable
private fun DividingLine(height: Dp = 8.dp) {
    Spacer(
        Modifier.fillMaxWidth().requiredHeight(height)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2))
    )
}

@Composable
fun ChangeAccountInfoView(paddingValues: PaddingValues, onBack: () -> Unit) {
    val info = LoggedInState.info ?: return Box {
        Text("empty")
    }
    var nickname by remember { mutableStateOf(info.nickname ?: "") }
    val nicknameFocusRequester = remember { FocusRequester() }

    val avatarUrl by remember { mutableStateOf(info.avatarUrl) }
    var chooserFile: ICFile? by remember(avatarUrl) { mutableStateOf(null) }

    val avatarPainter by produceImageState(avatarUrl, keys = arrayOf(avatarUrl))
    val chooserFilePainter by produceImageState(chooserFile, keys = arrayOf(chooserFile))

    val fileChooser = rememberFileChooser {
        chooserFile = it.firstOrNull()
    }

    var updateState by rememberLoadingDialogState()
    updateState?.let {
        LoadingDialog(
            it,
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }
    Column(
        Modifier.fillMaxWidth().fillMaxHeight().padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val scope = rememberCoroutineScope()
        Spacer(Modifier.requiredHeight(16.dp))
        Avatar(chooserFilePainter ?: avatarPainter, 56.dp, modifier = Modifier.clickable {
            scope.launch {
                fileChooser.launch()
            }
        })
        Spacer(Modifier.requiredHeight(16.dp))
        DividingLine()
        InfoItem("昵称", onClick = { nicknameFocusRequester.requestFocus() }) {
            Spacer(Modifier.requiredWidth(16.dp))
            BasicTextField(nickname, { nickname = it }, Modifier.focusRequester(nicknameFocusRequester))
        }
        DividingLine(1.dp)
        InfoItem("签名") {
            Spacer(Modifier.requiredWidth(16.dp))
            BasicTextField("", {}, enabled = false)
        }

        DividingLine()
        Spacer(Modifier.requiredHeight(32.dp))
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onBack, shape = MaterialTheme.shapes.small) {
                Text("取消")
            }
            Spacer(Modifier.requiredWidth(16.dp))
            Button({
                if (updateState != null) return@Button
                updateState = LoadingDialogState("请稍后...")
                scope.launch {
                    val param = chooserFile?.let {
                        updateState = LoadingDialogState("正在上传头像")
                        withContext(Dispatchers.IO) {
                            val key = "res/${LoggedInState.info?.uid}/${it.hashKey}"
                            getCosClient().putObject(key, it.inputStream(), CommonObjectMetadata(it.size))
                            updateState = LoadingDialogState("正在保存")

                            key
                        }
                    }.let {
                        UpdateInfoParam(nickname, it ?: avatarUrl)
                    }
                    val res = AccountService.get().updateInfo(param)
                    val job = launch {
                        delay(200)
                    }
                    job.join()
                    if (res.success) {
                        LoggedInState.info = res.data
                        updateState = LoadingDialogState("修改成功", true)
                    } else {
                        updateState = LoadingDialogState(res.msg, error = true)
                    }
                    delay(1000)
                    updateState = null
                }
            }, shape = MaterialTheme.shapes.small) {
                Text("保存")
            }
        }
    }
}