package icu.twtool.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import icu.twtool.chat.app.AppBottomNavigationBar
import icu.twtool.chat.app.AppNavHost
import icu.twtool.chat.app.AppNavigationRail
import icu.twtool.chat.material.ICScaffold
import icu.twtool.chat.navigation.rememberNavController
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.view.DynamicRoute
import icu.twtool.chat.view.FriendsRoute
import icu.twtool.chat.view.LoginRoute
import icu.twtool.chat.view.MessagesRoute

@Composable
fun App(
    windowSize: ICWindowSizeClass,
) {
    val controller = rememberNavController(if (LoggedInState.token == null) LoginRoute else MessagesRoute)

    val snackbarHostState = remember { SnackbarHostState() }

    ICScaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        bottomBar = {
            AnimatedVisibility(
                visible = windowSize.widthSizeClass < ICWindowWidthSizeClass.Expanded &&
                        (controller.current == MessagesRoute ||
                                controller.current == FriendsRoute ||
                                controller.current == DynamicRoute),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                AppBottomNavigationBar(controller.current) { controller.navigateTo(it) }
            }
        }
    ) { paddingValues ->
        Row {
            AnimatedVisibility(
                visible = windowSize.widthSizeClass >= ICWindowWidthSizeClass.Expanded &&
                        controller.current != LoginRoute
            ) {
                AppNavigationRail(controller.current) { controller.navigateTo(it) }
            }
            AppNavHost(controller, windowSize, snackbarHostState, paddingValues)
        }
    }
}