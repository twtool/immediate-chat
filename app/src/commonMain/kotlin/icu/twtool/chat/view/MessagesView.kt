package icu.twtool.chat.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.ChatRoute
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.state.MessageItem
import icu.twtool.chat.state.MessagesViewState
import icu.twtool.chat.state.WebSocketState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens
import kotlinx.coroutines.launch

@Composable
fun MessageViewItem(item: MessageItem, onClick: () -> Unit, selected: Boolean) {
    Surface(selected, onClick) {
        Row(Modifier.padding(16.dp).height(IntrinsicSize.Min)) {
            Avatar(item.avatarUrl, 42.dp)
            Spacer(Modifier.requiredWidth(8.dp))
            Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
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

    val topPadding = animateDpAsState(
        if (windowSize.widthSizeClass == ICWindowWidthSizeClass.Expanded && WebSocketState.error.value == null) 4.dp
        else 16.dp
    )

    var searchInput by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(paddingValues)) {
        AnimatedVisibility(WebSocketState.error.value != null) {
            Surface(
                Modifier.padding(top = if (windowSize.widthSizeClass == ICWindowWidthSizeClass.Expanded) 4.dp else 0.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp, 12.dp)) {
                    Text(WebSocketState.error.value ?: "")
                }
            }
        }
        Row(
            Modifier.padding(start = 16.dp, top = topPadding.value, bottom = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .height(32.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.requiredWidth(8.dp))
            Icon(
                Icons.Filled.Search, null,
                Modifier.size(20.dp),
                tint = LocalContentColor.current.copy(DisabledAlpha)
            )
            Spacer(Modifier.requiredWidth(8.dp))
            BasicTextField(searchInput, { searchInput = it }) {
                Box(contentAlignment = Alignment.CenterStart) {
                    if (searchInput.isEmpty())
                        Text("搜索", color = LocalContentColor.current.copy(DisabledAlpha))
                    it()
                }
            }
        }
        val filterItems = derivedStateOf {
            if (searchInput.isBlank()) return@derivedStateOf state.messageItemList
            state.messageItemList.filter {
                it.uid.toString().contains(searchInput) || (it.nickname?.contains(searchInput) ?: false)
            }
        }
        LazyColumn {
            items(filterItems.value, { it.uid }) {
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