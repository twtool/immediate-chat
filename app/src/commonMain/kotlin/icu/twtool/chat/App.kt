package icu.twtool.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import icu.twtool.chat.app.AppBottomNavigationBar
import icu.twtool.chat.app.AppNavHost
import icu.twtool.chat.app.AppNavigationRail
import icu.twtool.chat.app.AppTopBar
import icu.twtool.chat.app.DynamicRoute
import icu.twtool.chat.app.FriendsRoute
import icu.twtool.chat.app.LoginRoute
import icu.twtool.chat.app.MessagesRoute
import icu.twtool.chat.material.ICScaffold
import icu.twtool.chat.navigation.rememberNavController
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.utils.ICBackHandler

@Composable
fun App(
    windowSize: ICWindowSizeClass,
) {
    val controller = rememberNavController(if (LoggedInState.token == null) LoginRoute else MessagesRoute)

    ICBackHandler(!controller.empty) {
        controller.pop()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    ICScaffold(
        Modifier.imePadding(),
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            AppTopBar(controller, windowSize)
        },
        bottomBar = {
            val visible by derivedStateOf {
                windowSize.widthSizeClass < ICWindowWidthSizeClass.Expanded &&
                        (controller.current == MessagesRoute ||
                                controller.current == FriendsRoute ||
                                controller.current == DynamicRoute)
            }
            AnimatedVisibility(
                visible = visible,
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