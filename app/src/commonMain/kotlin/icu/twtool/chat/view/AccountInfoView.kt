package icu.twtool.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.AccountInfoRoute
import icu.twtool.chat.app.ChatRoute
import icu.twtool.chat.cache.produceAccountInfoState
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.components.BackTopAppBar
import icu.twtool.chat.components.LoadingDialog
import icu.twtool.chat.components.LoadingDialogState
import icu.twtool.chat.navigation.window.systemBarWindowInsets
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.param.CheckFriendParam
import icu.twtool.chat.server.account.param.FriendRequestParam
import icu.twtool.chat.service.get
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_dynamic
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun AccountInfoTopAppBar(onBack: () -> Unit, title: String) {
    BackTopAppBar(onBack, title)
}

@Composable
fun AccessibleItem(icon: Painter, text: String, onClick: () -> Unit, modifier: Modifier = Modifier, space: Dp = 16.dp) {
    Row(
        Modifier.clickable(onClick = onClick).then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space)
    ) {
        Icon(icon, text)
        Text(text, Modifier.weight(1f, true))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, "right",
            tint = LocalContentColor.current.copy(DisabledAlpha)
        )
    }
}

@Composable
fun AccountInfoView(onBack: () -> Unit, navigateToChatRoute: () -> Unit, navigateToChangeAccountInfo: () -> Unit) {
    val info = AccountInfoRoute.info ?: return
    val newInfo by produceAccountInfoState(info)
    val me = info.uid == LoggedInState.info?.uid

    val friend = produceState(false) {
        value = AccountService.get().checkFriend(CheckFriendParam(info.uid)).data ?: false
    }

    val windowInsets = systemBarWindowInsets
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        AccountInfoTopAppBar(onBack, newInfo.nickname ?: "未命名用户") // TODO：显示备注

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(16.dp)) {
                Avatar(newInfo.avatarUrl, 42.dp)
                Spacer(Modifier.requiredWidth(16.dp))
                Column {
                    Text(newInfo.nickname ?: "未命名用户", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "UID ${newInfo.uid}",
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.copy(alpha = DisabledAlpha)
                    )
                }
            }
            Spacer(
                Modifier.fillMaxWidth().requiredHeight(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2))
            )
            AccessibleItem(
                painterResource(Res.drawable.ic_dynamic),
                "他的动态",
                {},
                Modifier.fillMaxWidth().padding(16.dp)
            )
        }

        var sendRequestState by remember { mutableStateOf<LoadingDialogState?>(null) }

        sendRequestState?.let {
            LoadingDialog(it, {})
        }

        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            Button({
                scope.launch {
                    if (me) {
                        navigateToChangeAccountInfo()
                    } else if (friend.value) ChatRoute.open(newInfo) {
                        navigateToChatRoute()
                    } else {
                        if (sendRequestState != null) return@launch
                        sendRequestState = LoadingDialogState("请求中...")
                        // TODO:允许编辑
                        val res = AccountService.get().sendFriendRequest(
                            FriendRequestParam(info.uid, "对方请求添加好友")
                        )

                        sendRequestState = if (res.success) LoadingDialogState("已发送", success = true)
                        else LoadingDialogState(res.msg, error = true)

                        delay(500)
                        sendRequestState = null
                    }
                }
            }, Modifier.weight(1f)) {
                Text(if (me) "编辑资料" else if (friend.value) "发起聊天" else "添加好友")
            }
        }
        Spacer(Modifier.requiredHeight(with(LocalDensity.current) { windowInsets.getBottom(this).toDp() }))
    }
}