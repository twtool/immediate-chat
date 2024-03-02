package icu.twtool.chat.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import icu.twtool.chat.components.AccountInfoCard
import icu.twtool.chat.constants.IconDynamic
import icu.twtool.chat.constants.IconFriend
import icu.twtool.chat.constants.IconMessage
import icu.twtool.chat.navigation.NavRoute
import icu.twtool.chat.navigation.window.systemBarWindowInsets
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.ElevationTokens
import icu.twtool.chat.view.DynamicRoute
import icu.twtool.chat.view.FriendsRoute
import icu.twtool.chat.view.MessagesRoute
import icu.twtool.image.compose.ICAsyncImage
import icu.twtool.logger.getLogger
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.logo
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

private val log = getLogger("icu.twtool.chat.app.AppNavigations.kt")

@Composable
fun AppNavigationRail(currentRoute: NavRoute, navigateTo: (NavRoute) -> Unit) {
    NavigationRail(
        windowInsets = systemBarWindowInsets.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
    ) {
        var showInfo by remember { mutableStateOf(false) }
        ICAsyncImage(
            { null },
            painterResource(Res.drawable.logo),
            null,
            Modifier.size(38.dp).clip(MaterialTheme.shapes.small)
                .clickable {
                    showInfo = true
                }
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)),
            ContentScale.Crop
        )

        Spacer(Modifier.requiredHeight(16.dp))

        AppNavigationRailItem(currentRoute, MessagesRoute, navigateTo, IconMessage, "消息")
        AppNavigationRailItem(currentRoute, FriendsRoute, navigateTo, IconFriend, "好友")
        AppNavigationRailItem(currentRoute, DynamicRoute, navigateTo, IconDynamic, "动态")

        if (showInfo) {
            Popup(
                offset = with(LocalDensity.current) { IntOffset(76.dp.roundToPx(), 0) },
                onDismissRequest = {
                    showInfo = false
                },
            ) {
                AccountInfoCard(LoggedInState.info)
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
            currentRoute == route,
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
        AppBottomNavigationBarItem(currentRoute == MessagesRoute, { navigateTo(MessagesRoute) }, IconMessage, "消息")
        AppBottomNavigationBarItem(currentRoute == FriendsRoute, { navigateTo(FriendsRoute) }, IconFriend, "好友")
        AppBottomNavigationBarItem(currentRoute == DynamicRoute, { navigateTo(DynamicRoute) }, IconDynamic, "动态")
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