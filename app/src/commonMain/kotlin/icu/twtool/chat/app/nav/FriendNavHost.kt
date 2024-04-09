package icu.twtool.chat.app.nav

import androidx.compose.material3.SnackbarHostState
import icu.twtool.chat.app.AccountInfoRoute
import icu.twtool.chat.app.AddFriendRoute
import icu.twtool.chat.app.FriendsRoute
import icu.twtool.chat.app.NewFriendRoute
import icu.twtool.chat.app.ScanCodeRoute
import icu.twtool.chat.app.TwoPanel
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.navigation.NavController
import icu.twtool.chat.navigation.NavHostBuilder
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.view.AddFriendView
import icu.twtool.chat.view.FriendsView
import icu.twtool.chat.view.NewFriendView


fun NavHostBuilder.friendComposition(
    snackbarHostState: SnackbarHostState,
    controller: NavController,
    onLook: (FileRes) -> Unit
) {
    composable(FriendsRoute) { state, paddingValues ->
        FriendsView(snackbarHostState, state.windowSize, paddingValues,
            navigateToNewFriendRoute = {
                controller.navigateTo(NewFriendRoute)
            },
            navigateToAccountInfoRoute = {
                controller.navigateTo(AccountInfoRoute)
            }
        )
    }
    composable(listOf(FriendsRoute, AddFriendRoute), ICWindowWidthSizeClass.Expanded) { state, paddingValues ->
        TwoPanel(
            {
                FriendsView(snackbarHostState, state.windowSize, paddingValues,
                    navigateToNewFriendRoute = {
                        controller.navigateTo(NewFriendRoute, listOf(FriendsRoute))
                    },
                    navigateToAccountInfoRoute = {
                        controller.navigateTo(AccountInfoRoute, listOf(FriendsRoute))
                    }
                )
            },
            {
                AddFriendView(
                    snackbarHostState,
                    windowSize = state.windowSize,
                    onBack = { controller.pop() },
                    navigateToAccountInfoRoute = { controller.navigateTo(AccountInfoRoute, listOf(FriendsRoute)) },
                    navigateToScanCodeRoute = { controller.navigateTo(ScanCodeRoute) }
                )
            }
        )
    }
    composable(AddFriendRoute) { state, _ ->
        AddFriendView(snackbarHostState,
            windowSize = state.windowSize,
            onBack = { controller.pop() },
            navigateToAccountInfoRoute = { controller.navigateTo(AccountInfoRoute) },
            navigateToScanCodeRoute = { controller.navigateTo(ScanCodeRoute) }
        )
    }
    composable(NewFriendRoute) { _, paddingValues ->
        NewFriendView(snackbarHostState, paddingValues, controller::navigateTo)
    }
    composable(NewFriendRoute, ICWindowWidthSizeClass.Expanded) { state, paddingValues ->
        TwoPanel(
            {
                FriendsView(snackbarHostState, state.windowSize, paddingValues,
                    navigateToNewFriendRoute = {
                        controller.navigateTo(NewFriendRoute, listOf(FriendsRoute))
                    },
                    navigateToAccountInfoRoute = {
                        controller.navigateTo(AccountInfoRoute, listOf(FriendsRoute))
                    }
                )
            },
            {
                NewFriendView(snackbarHostState, paddingValues) {
                    controller.navigateTo(it, listOf(FriendsRoute, NewFriendRoute))
                }
            }
        )
    }
}