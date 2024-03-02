package icu.twtool.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import icu.twtool.chat.navigation.NavRoute

val FriendsRoute = NavRoute("Friends")

@Composable
fun FriendsView() {
    Box(Modifier.fillMaxSize().background(Color.Green))
}