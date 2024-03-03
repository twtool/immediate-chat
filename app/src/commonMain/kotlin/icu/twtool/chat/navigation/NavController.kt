package icu.twtool.chat.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import icu.twtool.chat.app.MessagesRoute
import icu.twtool.logger.getLogger

private val log = getLogger("icu.twtool.chat.navigation.NavController")

@Stable
class NavController(initial: NavRoute) {

    var current by mutableStateOf(initial)
        private set

    private val stack = mutableListOf<NavRoute>()

    fun navigateTo(route: NavRoute) {
        if (current == route) return
        if (!route.top) stack.add(current)
        else stack.clear()
        current = route
    }

    fun pop() {
        log.info("===== start =====")
        log.info("current = $current")
        log.info("stack = $stack")
        val pop = current
        current = stack.removeLastOrNull() ?: MessagesRoute
        pop.onPop()
        log.info("current = $current")
        log.info("stack = $stack")
        log.info("===== end =====")
    }

}

@Composable
fun rememberNavController(initial: NavRoute): NavController = remember { NavController(initial) }