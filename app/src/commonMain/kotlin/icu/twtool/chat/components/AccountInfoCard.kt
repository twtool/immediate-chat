package icu.twtool.chat.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import icu.twtool.chat.animate.toDpSize
import icu.twtool.chat.cache.produceAccountInfoState
import icu.twtool.chat.cache.produceImageState
import icu.twtool.chat.components.file.FilePosition
import icu.twtool.chat.components.file.URLImageRes
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens

@Composable
fun AccountInfoCard(
    info: AccountInfo?,
    onLookInfoClick: () -> Unit,
    onSendClick: () -> Unit,
    lookInfoText: String = "查看资料",
    showOpenChat: Boolean = true,
    onLook: (URLImageRes) -> Unit = {},
) {
    val avatar by produceImageState(info?.avatarUrl, keys = arrayOf(info))
    var position = remember { FilePosition(null, DpSize.Zero, DpSize.Unspecified) }
    val density = LocalDensity.current
    Surface(
        shadowElevation = ElevationTokens.Level2,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            Modifier.width(300.dp).padding(16.dp),
        ) {
            if (info == null) {
                Text("用户信息错误")
                return@Surface
            }
            val newInfo by produceAccountInfoState(info)
            Row {
                Avatar(avatar, 48.dp, MaterialTheme.shapes.small, Modifier
                    .onGloballyPositioned {
                        val rect = it.boundsInRoot()
                        position = with(density) {
                            FilePosition(
                                rect.topLeft.round(),
                                it.size.toDpSize(),
                                targetSize = avatar?.intrinsicSize?.toDpSize() ?: DpSize.Unspecified
                            )
                        }
                    }
                    .pointerInput(info) {
                        detectTapGestures {
                            info.avatarUrl?.let { url ->
                                URLImageRes(url, position)
                            }?.let(onLook)
                        }
                    })
                Spacer(Modifier.requiredWidth(16.dp))
                Column {
                    Text(newInfo.nickname ?: "未命名用户", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "UID ${newInfo.uid}",
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.copy(alpha = DisabledAlpha)
                    )
                }
            }

            Spacer(Modifier.requiredHeight(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onLookInfoClick,
                    Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) {
                    Text(lookInfoText)
                }
                if (showOpenChat) {
                    Button(onSendClick, Modifier.weight(1f)) {
                        Text("发起聊天")
                    }
                }
            }
        }
    }
}