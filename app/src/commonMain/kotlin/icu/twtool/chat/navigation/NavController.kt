package icu.twtool.chat.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import icu.twtool.chat.app.MessagesRoute

@Stable
class NavController(initial: NavRoute) {

    var current by mutableStateOf(initial)
        private set

    var empty by mutableStateOf(false)
        private set

    private val stack = mutableListOf<NavRoute>()

    fun navigateTo(route: NavRoute, customStack: List<NavRoute>? = null) {
        if (current == route) return
        if (customStack != null) {
            stack.removeAll {
                it.onPop()
                true
            }
            stack.addAll(customStack)
        } else if (!route.top) stack.add(current)
        else {
            stack.removeAll {
                it.onPop()
                true
            }
        }
        current = route

        empty = stack.isEmpty()
    }

    fun pop() {
        val pop = current
        current = stack.removeLastOrNull() ?: MessagesRoute
        pop.onPop()

        empty = stack.isEmpty()
    }

}

@Composable
fun rememberNavController(initial: NavRoute): NavController = remember { NavController(initial) }