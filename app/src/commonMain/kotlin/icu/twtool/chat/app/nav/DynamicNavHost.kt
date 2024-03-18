package icu.twtool.chat.app.nav

import icu.twtool.chat.app.DynamicRoute
import icu.twtool.chat.app.PublishDynamicRoute
import icu.twtool.chat.app.TwoPanel
import icu.twtool.chat.components.file.FileRes
import icu.twtool.chat.navigation.NavController
import icu.twtool.chat.navigation.NavHostBuilder
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.view.DynamicView
import icu.twtool.chat.view.PublishDynamicView

fun NavHostBuilder.dynamicComposition(controller: NavController, onLook: (FileRes) -> Unit) {
    composable(DynamicRoute) { _, paddingValues ->
        DynamicView(paddingValues, onLook)
    }
    composable(DynamicRoute, ICWindowWidthSizeClass.Expanded) { _, paddingValues ->
        TwoPanel(
            one = {
                DynamicView(paddingValues, onLook)
            },
            two = {
                PublishDynamicView(paddingValues, onBack = { controller.pop() }, onLook)
            }
        )
    }
    composable(PublishDynamicRoute) { _, paddingValues ->
        PublishDynamicView(paddingValues, onBack = { controller.pop() }, onLook)
    }
    composable(PublishDynamicRoute, ICWindowWidthSizeClass.Expanded) { _, paddingValues ->
        TwoPanel(
            one = {
                DynamicView(paddingValues, onLook)
            },
            two = {
                PublishDynamicView(paddingValues, onBack = { controller.pop() }, onLook)
            }
        )
    }
}