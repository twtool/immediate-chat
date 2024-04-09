package icu.twtool.chat.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.nav.dynamicComposition
import icu.twtool.chat.app.nav.friendComposition
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.navigation.NavController
import icu.twtool.chat.navigation.NavHost
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.view.AcceptFriendRequestView
import icu.twtool.chat.view.AccountInfoView
import icu.twtool.chat.view.ChangeAccountInfoView
import icu.twtool.chat.view.ChatSettingsView
import icu.twtool.chat.view.ChatView
import icu.twtool.chat.view.FriendsView
import icu.twtool.chat.view.LoginView
import icu.twtool.chat.view.MessagesView
import icu.twtool.chat.view.RegisterView
import icu.twtool.chat.view.ScanCodeView

@Composable
fun AppNavHost(
    controller: NavController,
    windowSize: ICWindowSizeClass,
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
    onLook: (FileRes) -> Unit
) {
    NavHost(controller, windowSize, paddingValues) {
        composable(LoginRoute) { _, paddingValues ->
            LoginView(snackbarHostState, paddingValues,
                onSuccess = {
                    controller.navigateTo(MessagesRoute)
                },
                navigateToRegisterRoute = {
                    controller.navigateTo(RegisterRoute)
                }
            )
        }
        composable(RegisterRoute) { _, paddingValues ->
            RegisterView(
                snackbarHostState,
                paddingValues,
                navigateToLoginRoute = {
                    controller.navigateTo(LoginRoute)
                },
                navigateToHomeRoute = {
                    controller.navigateTo(MessagesRoute)
                }
            )
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
                {
                    ChatView(
                        state.windowSize.widthSizeClass,
                        paddingValues,
                        onBack = { controller.pop() },
                        onLookFile = onLook,
                        navigateToChatSettingsRoute = {},
                        navigateChatRoute = { controller.navigateTo(ChatRoute, listOf(MessagesRoute)) },
                        navigateAccountInfoRoute = { controller.navigateTo(AccountInfoRoute, listOf(FriendsRoute)) }
                    )
                }
            )
        }
        composable(ChatRoute) { state, paddingValues ->
            ChatView(
                state.windowSize.widthSizeClass, paddingValues, onBack = { controller.pop() },
                onLookFile = onLook,
                navigateToChatSettingsRoute = {
                    controller.navigateTo(ChatSettingsRoute)
                },
                navigateChatRoute = { controller.navigateTo(ChatRoute) },
                navigateAccountInfoRoute = { controller.navigateTo(AccountInfoRoute) }
            )
        }
        composable(ChatRoute, ICWindowWidthSizeClass.Expanded) { state, paddingValues ->
            TwoPanel(
                {
                    MessagesView(
                        paddingValues,
                        state.windowSize,
                        navigateToChatRoute = { controller.navigateTo(ChatRoute, listOf(MessagesRoute)) })
                },
                {
                    ChatView(
                        state.windowSize.widthSizeClass,
                        paddingValues,
                        onBack = { controller.pop() },
                        onLookFile = onLook,
                        navigateToChatSettingsRoute = {
                        },
                        navigateChatRoute = { controller.navigateTo(ChatRoute, listOf(MessagesRoute)) },
                        navigateAccountInfoRoute = { controller.navigateTo(AccountInfoRoute, listOf(FriendsRoute)) }
                    )
                }
            )
        }
        composable(ChatSettingsRoute) { _, paddingValues ->
            ChatSettingsView(Modifier.padding(paddingValues))
        }
        dynamicComposition(snackbarHostState, controller, onLook = onLook)
        friendComposition(snackbarHostState, controller, onLook = onLook)
        composable(AcceptFriendRequestRoute) { _, paddingValues ->
            AcceptFriendRequestView(snackbarHostState, paddingValues, onBack = { controller.pop() })
        }
        composable(AcceptFriendRequestRoute, ICWindowWidthSizeClass.Expanded) { state, paddingValues ->
            TwoPanel(
                {
                    FriendsView(
                        snackbarHostState, state.windowSize, paddingValues,
                        navigateToNewFriendRoute = {
                            controller.navigateTo(NewFriendRoute, listOf(FriendsRoute))
                        },
                        navigateToAccountInfoRoute = {
                            controller.navigateTo(AccountInfoRoute, listOf(FriendsRoute))
                        }
                    )
                },
                { AcceptFriendRequestView(snackbarHostState, paddingValues, onBack = { controller.pop() }) }
            )
        }
        composable(AccountInfoRoute) { _, _ ->
            AccountInfoView(
                onBack = { controller.pop() },
                navigateToChatRoute = { controller.navigateTo(ChatRoute) },
                navigateToChangeAccountInfo = { controller.navigateTo(ChangeAccountInfoRoute) }
            )
        }
        composable(AccountInfoRoute, ICWindowWidthSizeClass.Expanded) { state, paddingValues ->
            TwoPanel(
                {
                    FriendsView(
                        snackbarHostState, state.windowSize, paddingValues,
                        navigateToNewFriendRoute = {
                            controller.navigateTo(NewFriendRoute, listOf(FriendsRoute))
                        },
                        navigateToAccountInfoRoute = {
                            controller.navigateTo(AccountInfoRoute, listOf(FriendsRoute))
                        }
                    )
                },
                {
                    AccountInfoView(
                        onBack = { controller.pop() },
                        navigateToChatRoute = { controller.navigateTo(ChatRoute, listOf(MessagesRoute)) },
                        navigateToChangeAccountInfo = { controller.navigateTo(ChangeAccountInfoRoute) }
                    )
                }
            )
        }
        composable(ChangeAccountInfoRoute) { _, paddingValues ->
            ChangeAccountInfoView(paddingValues) { controller.pop() }
        }
        composable(ScanCodeRoute) { _, _ ->
            ScanCodeView(navigateToAccountInfoRoute = { controller.navigateTo(AccountInfoRoute) })
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