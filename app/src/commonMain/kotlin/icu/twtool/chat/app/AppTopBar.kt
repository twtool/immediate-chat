package icu.twtool.chat.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.navigation.NavController
import icu.twtool.chat.navigation.NavRoute
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.ElevationTokens
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_back
import org.jetbrains.compose.resources.painterResource

enum class TopBarState {
    None,
    LoggedInAccount,
    NewFriend,
    AcceptFriendRequest,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(controller: NavController, windowSize: ICWindowSizeClass) {
    val currentRoute: NavRoute = controller.current
    val visible by derivedStateOf {
        windowSize.widthSizeClass < ICWindowWidthSizeClass.Expanded &&
                (currentRoute != LoginRoute)
    }

    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = containerColor,
        titleContentColor = MaterialTheme.colorScheme.onSurface
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val state by derivedStateOf {
            when (currentRoute) {
                MessagesRoute, FriendsRoute, DynamicRoute -> TopBarState.LoggedInAccount
                NewFriendRoute -> TopBarState.NewFriend
                AcceptFriendRequestRoute -> TopBarState.AcceptFriendRequest
                else -> TopBarState.None
            }
        }
        Crossfade(state) {
            when (it) {
                TopBarState.LoggedInAccount -> LoggedInAccountTopBar(colors)
                TopBarState.NewFriend -> NewFriendTopBar(colors, onBack = { controller.pop() })
                TopBarState.AcceptFriendRequest -> AcceptFriendRequestTopBar(colors) { controller.pop() }
                TopBarState.None -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFriendTopBar(colors: TopAppBarColors, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text("好友验证")
        },
        navigationIcon = {
            IconButton(onBack) {
                Icon(painterResource(Res.drawable.ic_back), "返回")
            }
        },
        actions = {
            TextButton({}) {
                Text("添加好友")
            }
        },
        colors = colors
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptFriendRequestTopBar(colors: TopAppBarColors, onBack: () -> Unit) {
    CenterTitleAndBackAppBar(colors, "同意好友申请", onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterTitleAndBackAppBar(colors: TopAppBarColors, title: String, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(title)
        },
        navigationIcon = {
            IconButton(onBack) {
                Icon(painterResource(Res.drawable.ic_back), "返回")
            }
        },
        colors = colors
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggedInAccountTopBar(colors: TopAppBarColors) {
    val info = LoggedInState.info

    TopAppBar(
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Avatar(info?.avatarUrl)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        (info?.nickname ?: "").ifBlank { "未命名用户" }, style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current
                    )
                    Text(
                        "\uD83D\uDFE2 在线",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        },
        colors = colors
    )
}