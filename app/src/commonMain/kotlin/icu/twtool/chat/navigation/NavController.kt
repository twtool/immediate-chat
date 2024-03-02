package icu.twtool.chat.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class NavController(initial: NavRoute) {

    var current by mutableStateOf(initial)
        private set


    fun navigateTo(route: NavRoute) {
        if (current == route) return
        // TODO: Stack
        current = route
    }

}

@Composable
fun rememberNavController(initial: NavRoute): NavController = remember { NavController(initial) }