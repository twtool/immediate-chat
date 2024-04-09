package icu.twtool.chat.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import icu.twtool.chat.cache.produceImageState
import icu.twtool.chat.components.AccountInfoCard
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.components.WindowDialog
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.navigation.NavController
import icu.twtool.chat.navigation.NavRoute
import icu.twtool.chat.navigation.window.systemBarWindowInsets
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.ElevationTokens
import icu.twtool.chat.view.ChangeAccountInfoView
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_dynamic
import immediatechat.app.generated.resources.ic_friend
import immediatechat.app.generated.resources.ic_message
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Composable
private fun NavigationBarAvatar(onClick: () -> Unit) {
    val painter by produceImageState(LoggedInState.info?.avatarUrl, keys = arrayOf(LoggedInState.info))
    Avatar(
        painter, 38.dp,
        MaterialTheme.shapes.small,
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun AppNavigationRail(
    snackbarHostState: SnackbarHostState,
    currentRoute: NavRoute,
    controller: NavController,
    navigateTo: (NavRoute) -> Unit,
    onLook: (FileRes) -> Unit
) {
    NavigationRail(
        windowInsets = systemBarWindowInsets.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start),
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)
    ) {
        val scope = rememberCoroutineScope()
        var showInfo by remember { mutableStateOf(false) }
        var showChangeAccountInfoDialog by remember { mutableStateOf(false) }

        NavigationBarAvatar { showInfo = true }

        Spacer(Modifier.requiredHeight(16.dp))

        AppNavigationRailItem(currentRoute, MessagesRoute, navigateTo, Res.drawable.ic_message, "消息")
        AppNavigationRailItem(currentRoute, FriendsRoute, navigateTo, Res.drawable.ic_friend, "好友")
        AppNavigationRailItem(currentRoute, DynamicRoute, navigateTo, Res.drawable.ic_dynamic, "动态")

        Spacer(Modifier.weight(1f))

        var showExitLoginDialog by remember { mutableStateOf(false) }
        if (showExitLoginDialog) {
            AlertDialog(
                { showExitLoginDialog = false },
                title = { Text("确定退出登录？") },
                text = { Text("退出登录后将无法收到新消息") },
                confirmButton = {
                    TextButton({
                        LoggedInState.logout()
                        navigateTo(LoginRoute)
                    }) {
                        Text("确定")
                    }
                }
            )
        }

        var showDropdownMenu by remember { mutableStateOf(false) }
        DropdownMenu(showDropdownMenu, { showDropdownMenu = false }, offset = DpOffset(16.dp, (-16).dp)) {
            DropdownMenuItem(
                onClick = { showExitLoginDialog = true; showDropdownMenu = false },
                text = { Text("退出登录") },
                leadingIcon = { Icon(Icons.AutoMirrored.Default.ExitToApp, null) })
        }
        IconButton({
            showDropdownMenu = true
        }) {
            Icon(Icons.Filled.Menu, null)
        }


        if (showChangeAccountInfoDialog) {
            WindowDialog(
                "编辑资料",
                { showChangeAccountInfoDialog = false }, Modifier.width(400.dp),
                properties = DialogProperties(dismissOnClickOutside = false)
            ) {
                Box(Modifier.fillMaxHeight(0.8f)) {
                    ChangeAccountInfoView(
                        paddingValues = PaddingValues(),
                        onBack = { showChangeAccountInfoDialog = false }
                    )
                }
            }
        }

        if (showInfo) {
            Popup(
                offset = with(LocalDensity.current) { IntOffset(76.dp.roundToPx(), 0) },
                onDismissRequest = {
                    showInfo = false
                },
            ) {
                AccountInfoCard(
                    LoggedInState.info,
                    lookInfoText = "编辑资料",
                    onLookInfoClick = {
                        showInfo = false
                        showChangeAccountInfoDialog = true
                    },
                    onSendClick = {
                        showInfo = false
                        scope.launch {
                            ChatRoute.open(LoggedInState.info ?: return@launch) {
                                controller.navigateTo(ChatRoute, listOf(MessagesRoute))
                            }
                        }
                    },
                    onLook = {
                        showInfo = false
                        onLook(it)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppNavigationRailItem(
    currentRoute: NavRoute,
    route: NavRoute,
    navigateTo: (NavRoute) -> Unit,
    icon: DrawableResource,
    label: String
) {
    TooltipBox(
        TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(label)
            }
        },
        rememberTooltipState()
    ) {
        NavigationRailItem(
            currentRoute == route || currentRoute.parent == route,
            onClick = {
                navigateTo(route)
            },
            icon = {
                Icon(vectorResource(icon), contentDescription = label)
            },
        )
    }
}

@Composable
fun AppBottomNavigationBar(currentRoute: NavRoute, navigateTo: (NavRoute) -> Unit) {
    NavigationBar {
        AppBottomNavigationBarItem(
            currentRoute == MessagesRoute,
            { navigateTo(MessagesRoute) },
            Res.drawable.ic_message,
            "消息"
        )
        AppBottomNavigationBarItem(
            currentRoute == FriendsRoute,
            { navigateTo(FriendsRoute) },
            Res.drawable.ic_friend,
            "好友"
        )
        AppBottomNavigationBarItem(
            currentRoute == DynamicRoute,
            { navigateTo(DynamicRoute) },
            Res.drawable.ic_dynamic,
            "动态"
        )
    }
}

@Composable
private fun RowScope.AppBottomNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: DrawableResource,
    label: String
) {
    NavigationBarItem(selected,
        onClick = onClick,
        icon = {
            Icon(vectorResource(icon), contentDescription = label)
        },
        label = {
            Text(label)
        }
    )
}