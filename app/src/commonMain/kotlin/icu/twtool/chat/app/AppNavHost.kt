package icu.twtool.chat.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import icu.twtool.chat.navigation.NavController
import icu.twtool.chat.navigation.NavHost
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.view.DynamicRoute
import icu.twtool.chat.view.DynamicView
import icu.twtool.chat.view.FriendsRoute
import icu.twtool.chat.view.FriendsView
import icu.twtool.chat.view.LoginRoute
import icu.twtool.chat.view.LoginView
import icu.twtool.chat.view.MessagesRoute
import icu.twtool.chat.view.MessagesView


@Composable
fun AppNavHost(
    controller: NavController,
    windowSize: ICWindowSizeClass,
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
) {
    NavHost(controller, windowSize.widthSizeClass) {
        composable(LoginRoute) {
            LoginView(snackbarHostState, paddingValues) {
                controller.navigateTo(MessagesRoute)
            }
        }
        composable(MessagesRoute) {
            MessagesView()
        }
        composable(FriendsRoute) {
            FriendsView()
        }
        composable(DynamicRoute) {
            DynamicView()
        }
    }
}