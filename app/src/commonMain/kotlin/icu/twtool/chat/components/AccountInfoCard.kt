package icu.twtool.chat.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens

@Composable
fun AccountInfoCard(info: AccountInfo?) {
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
            Row {
                Avatar(info.avatarUrl, 48.dp, MaterialTheme.shapes.small)
                Spacer(Modifier.requiredWidth(16.dp))
                Column {
                    Text(info.nickname ?: "未命名用户", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "UID ${info.uid}",
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.copy(alpha = DisabledAlpha)
                    )
                }
            }

            Spacer(Modifier.requiredHeight(32.dp))

            Row {
                Button(
                    {},
                    Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) {
                    Text("查看资料")
                }
                Spacer(Modifier.requiredWidth(8.dp))
                Button({}, Modifier.weight(1f)) {
                    Text("发起聊天")
                }
            }
        }
    }
}