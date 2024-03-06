package icu.twtool.chat.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.ChatRoute
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.state.MessageItem
import icu.twtool.chat.state.MessagesViewState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens
import kotlinx.coroutines.launch

@Composable
fun MessageViewItem(item: MessageItem, onClick: () -> Unit, selected: Boolean) {
    val color by animateColorAsState(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
    Surface(onClick, color = color) {
        Row(Modifier.padding(16.dp)) {
            Avatar(item.avatarUrl, 42.dp)
            Spacer(Modifier.requiredWidth(8.dp))
            Column {
                Row {
                    val nickname = item.nickname ?: "未命名用户"
                    Text(
                        text = nickname,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, true),
                        lineHeight = LocalTextStyle.current.fontSize
                    )
                    Text(
                        text = item.updateAt,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    item.message ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = LocalContentColor.current.copy(DisabledAlpha)
                )
            }
        }
    }
}

@Composable
fun MessagesView(paddingValues: PaddingValues, windowSize: ICWindowSizeClass, navigateToChatRoute: () -> Unit) {
    val selectedUid = ChatRoute.info?.uid
    val state = remember { MessagesViewState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        state.loadList()
    }

    val topPadding = when (windowSize.widthSizeClass) {
        ICWindowWidthSizeClass.Expanded -> 4.dp
        else -> 16.dp
    }

    var searchInput by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(paddingValues)) {
        Box(
            Modifier.padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = 16.dp)
                .fillMaxWidth()
                .height(32.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2))
        )
        LazyColumn {
            items(state.messageItemList, { it.uid }) {
                MessageViewItem(it,
                    selected = selectedUid == it.uid,
                    onClick = {
                        scope.launch {
                            ChatRoute.open(
                                AccountInfo(
                                    uid = it.uid,
                                    nickname = it.nickname,
                                    avatarUrl = it.avatarUrl
                                )
                            ) {
                                navigateToChatRoute()
                            }
                        }
                    }
                )
            }
        }
    }
}