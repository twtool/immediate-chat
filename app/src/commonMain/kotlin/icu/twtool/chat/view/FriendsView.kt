package icu.twtool.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.AccountInfoRoute
import icu.twtool.chat.app.NewFriendRoute
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.navigation.NavRoute
import icu.twtool.chat.state.FriendsViewState
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_channel
import immediatechat.app.generated.resources.ic_friend
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun FriendListItem(name: String, onClick: () -> Unit, avatar: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp, 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        avatar()
        Text(name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun InternalAvatar(
    resource: DrawableResource,
    descriptionContent: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = LocalContentColor.current
) {
    Box(
        modifier.then(Modifier.background(containerColor)),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(resource), descriptionContent, tint = contentColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsView(snackbarHostState: SnackbarHostState, paddingValues: PaddingValues, navigateTo: (NavRoute) -> Unit) {
    val state = remember { FriendsViewState() }
    val avatarModifier = Modifier.clip(MaterialTheme.shapes.extraSmall).size(42.dp)
    val refreshState = rememberPullToRefreshState()

    LaunchedEffect(state) {
        state.loadFriendList {
            snackbarHostState.showSnackbar(it)
        }
    }

    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            state.loadFriendList(true) {
                snackbarHostState.showSnackbar(it)
            }
            delay(1000)
            refreshState.endRefresh()
        }
    }
    Box(Modifier.padding(paddingValues).nestedScroll(refreshState.nestedScrollConnection)) {
        if (refreshState.isRefreshing) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator({ refreshState.progress })
            }
        }

        LazyColumn(
            Modifier.fillMaxSize()
                .offset(y = with(LocalDensity.current) { refreshState.verticalOffset.toDp() })
        ) {
            item {
                FriendListItem("好友验证", { navigateTo(NewFriendRoute) }) {
                    InternalAvatar(
                        Res.drawable.ic_friend, "好友验证", avatarModifier,
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            item {
                FriendListItem("频道", {}) {
                    InternalAvatar(
                        Res.drawable.ic_channel, "频道", avatarModifier,
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            items(state.friendList, { it.uid }) {
                FriendListItem(it.nickname ?: "未命名用户", {
                    AccountInfoRoute.info = it
                    navigateTo(AccountInfoRoute)
                }) {
                    Avatar(it.avatarUrl, 42.dp)
                }
            }
        }

        FloatingActionButton({
            refreshState.startRefresh()
        }, Modifier.align(Alignment.BottomEnd).offset((-16).dp, (-16).dp)) {
            Icon(Icons.Filled.Refresh, null)
        }

    }
}