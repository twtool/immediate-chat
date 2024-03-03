package icu.twtool.chat.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import icu.twtool.chat.navigation.NavController
import icu.twtool.chat.navigation.NavHost
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.view.AcceptFriendRequestView
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
    NavHost(controller, windowSize.widthSizeClass, paddingValues) {
        composable(LoginRoute) {
            LoginView(snackbarHostState, paddingValues) {
                controller.navigateTo(MessagesRoute)
            }
        }
        composable(MessagesRoute) {
            MessagesView()
        }
        composable(FriendsRoute) {
            FriendsView(it, controller::navigateTo)
        }
        composable(DynamicRoute) {
            DynamicView()
        }
        composable(NewFriendRoute) {
            NewFriendView(snackbarHostState, it, { controller.pop() }, controller::navigateTo)
        }
        composable(AcceptFriendRequestRoute) {
            AcceptFriendRequestView(snackbarHostState, it, onBack = { controller.pop() })
        }
    }
}