package icu.twtool.chat.app.nav

import androidx.compose.material3.SnackbarHostState
import icu.twtool.chat.app.DynamicRoute
import icu.twtool.chat.app.PublishDynamicRoute
import icu.twtool.chat.app.TwoPanel
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.navigation.NavController
import icu.twtool.chat.navigation.NavHostBuilder
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.view.DynamicView
import icu.twtool.chat.view.PublishDynamicView

fun NavHostBuilder.dynamicComposition(
    snackbarHostState: SnackbarHostState,
    controller: NavController,
    onLook: (FileRes) -> Unit
) {
    composable(DynamicRoute) { _, paddingValues ->
        DynamicView(paddingValues, onLook)
    }
    composable(DynamicRoute, ICWindowWidthSizeClass.Expanded) { _, paddingValues ->
        TwoPanel(
            one = {
                DynamicView(paddingValues, onLook)
            },
            two = {
                PublishDynamicView(snackbarHostState, paddingValues, onBack = { controller.pop() }, onLook)
            }
        )
    }
    composable(PublishDynamicRoute) { _, paddingValues ->
        PublishDynamicView(snackbarHostState, paddingValues, onBack = { controller.pop() }, onLook)
    }
    composable(PublishDynamicRoute, ICWindowWidthSizeClass.Expanded) { _, paddingValues ->
        TwoPanel(
            one = {
                DynamicView(paddingValues, onLook)
            },
            two = {
                PublishDynamicView(snackbarHostState, paddingValues, onBack = { controller.pop() }, onLook)
            }
        )
    }
}