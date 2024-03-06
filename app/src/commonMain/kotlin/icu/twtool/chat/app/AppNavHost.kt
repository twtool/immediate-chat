package icu.twtool.chat.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import icu.twtool.chat.navigation.NavController
import icu.twtool.chat.navigation.NavHost
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.view.AcceptFriendRequestView
import icu.twtool.chat.view.AccountInfoView
import icu.twtool.chat.view.ChatView
import icu.twtool.chat.view.DynamicView
import icu.twtool.chat.view.FriendsView
import icu.twtool.chat.view.LoginView
import icu.twtool.chat.view.MessagesView
import icu.twtool.chat.view.NewFriendView

@Composable
fun AppNavHost(
    controller: NavController,
    windowSize: ICWindowSizeClass,
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
) {
    NavHost(controller, windowSize, paddingValues) {
        composable(LoginRoute) { _, paddingValues ->
            LoginView(snackbarHostState, paddingValues) {
                controller.navigateTo(MessagesRoute)
            }
        }
        composable(MessagesRoute) { state, paddingValues ->
            MessagesView(paddingValues, state.windowSize, navigateToChatRoute = { controller.navigateTo(ChatRoute) })
        }
        composable(MessagesRoute, ICWindowWidthSizeClass.Expanded) { state, paddingValues ->
            TwoPanel(
                {
                    MessagesView(
                        paddingValues,
                        state.windowSize,
                        navigateToChatRoute = { controller.navigateTo(ChatRoute, listOf(MessagesRoute)) })
                },
                { ChatView(onBack = { controller.pop() }) }
            )
        }
        composable(ChatRoute) { _, _ ->
            ChatView(onBack = { controller.pop() })
        }
        composable(ChatRoute, ICWindowWidthSizeClass.Expanded) { state, paddingValues ->
            TwoPanel(
                {
                    MessagesView(
                        paddingValues,
                        state.windowSize,
                        navigateToChatRoute = { controller.navigateTo(ChatRoute, listOf(MessagesRoute)) })
                },
                { ChatView(onBack = { controller.pop() }) }
            )
        }
        composable(FriendsRoute) { _, paddingValues ->
            FriendsView(snackbarHostState, paddingValues, controller::navigateTo)
        }
        composable(FriendsRoute, ICWindowWidthSizeClass.Expanded) { _, paddingValues ->
            TwoPanel(
                {
                    FriendsView(snackbarHostState, paddingValues) {
                        controller.navigateTo(it, listOf(FriendsRoute))
                    }
                },
                {

                }
            )
        }
        composable(DynamicRoute) { _, _ ->
            DynamicView()
        }
        composable(NewFriendRoute) { _, paddingValues ->
            NewFriendView(snackbarHostState, paddingValues, controller::navigateTo)
        }
        composable(NewFriendRoute, ICWindowWidthSizeClass.Expanded) { _, paddingValues ->
            TwoPanel(
                {
                    FriendsView(snackbarHostState, paddingValues) {
                        controller.navigateTo(it, listOf(FriendsRoute))
                    }
                },
                {
                    NewFriendView(snackbarHostState, paddingValues) {
                        controller.navigateTo(it, listOf(FriendsRoute, NewFriendRoute))
                    }
                }
            )
        }
        composable(AcceptFriendRequestRoute) { _, paddingValues ->
            AcceptFriendRequestView(snackbarHostState, paddingValues, onBack = { controller.pop() })
        }
        composable(AcceptFriendRequestRoute, ICWindowWidthSizeClass.Expanded) { _, paddingValues ->
            TwoPanel(
                {
                    FriendsView(snackbarHostState, paddingValues) {
                        controller.navigateTo(it, listOf(FriendsRoute))
                    }
                },
                { AcceptFriendRequestView(snackbarHostState, paddingValues, onBack = { controller.pop() }) }
            )
        }
        composable(AccountInfoRoute) { _, _ ->
            AccountInfoView(
                onBack = { controller.pop() },
                navigateToChatRoute = { controller.navigateTo(ChatRoute) }
            )
        }
        composable(AccountInfoRoute, ICWindowWidthSizeClass.Expanded) { _, paddingValues ->
            TwoPanel(
                {
                    FriendsView(snackbarHostState, paddingValues) {
                        controller.navigateTo(it, listOf(FriendsRoute))
                    }
                },
                {
                    AccountInfoView(
                        onBack = { controller.pop() },
                        navigateToChatRoute = { controller.navigateTo(ChatRoute, listOf(MessagesRoute)) }
                    )
                }
            )
        }
    }
}

@Composable
fun TwoPanel(
    one: @Composable () -> Unit,
    two: @Composable () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Row(modifier) {
        Box(Modifier.weight(0.4f)) {
            one()
        }
        Spacer(
            Modifier.requiredWidth(1.dp).fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        Box(Modifier.weight(0.6f)) {
            two()
        }
    }
}