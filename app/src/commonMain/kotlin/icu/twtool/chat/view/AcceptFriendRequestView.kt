package icu.twtool.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.AcceptFriendRequestRoute
import icu.twtool.chat.components.TextDialog
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.param.FriendAcceptParam
import icu.twtool.chat.service.get
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens
import kotlinx.coroutines.launch

@Composable
private fun TipText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
//        color = LocalContentColor.current.copy(alpha = DisabledAlpha)
    )
}

@Composable
private fun RequestMsg(msg: String) {
    Text(
        msg,
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2))
            .padding(8.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = LocalContentColor.current.copy(alpha = DisabledAlpha)
    )
}

@Composable
fun AcceptFriendRequestView(snackbarHostState: SnackbarHostState, paddingValues: PaddingValues, onBack: () -> Unit) {
    val info = AcceptFriendRequestRoute.request ?: return
    val scope = rememberCoroutineScope()
    var doRequest by remember { mutableStateOf(false) }
    if (doRequest) {
        TextDialog("请稍后...")
    }
    Column(
        Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.fillMaxWidth().weight(1f, fill = true).verticalScroll(rememberScrollState())) {
            TipText("验证信息")
            Spacer(Modifier.requiredHeight(8.dp))
            RequestMsg(info.msg)
        }

        Spacer(Modifier.requiredHeight(32.dp))

        Button(
            {
                if (doRequest) return@Button
                doRequest = true
                scope.launch {
                    val res =
                        AccountService.get().acceptFriendRequest(FriendAcceptParam(info.id))

                    if (res.success) onBack()
                    else snackbarHostState.showSnackbar(res.msg)
                }
            },
            enabled = !doRequest && snackbarHostState.currentSnackbarData == null,
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 56.dp)
        ) {
            Text("完成")
        }
        Spacer(Modifier.requiredHeight(32.dp))
    }
}