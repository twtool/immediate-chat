package icu.twtool.chat.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass

class NavHostBuilder {

    private val navMap = mutableMapOf<NavRoute, @Composable () -> Unit>()
    private val windowWidthSizeNavMap = mapOf(
        ICWindowWidthSizeClass.Compact to mutableMapOf<NavRoute, @Composable () -> Unit>(),
        ICWindowWidthSizeClass.Medium to mutableMapOf<NavRoute, @Composable () -> Unit>(),
        ICWindowWidthSizeClass.Expanded to mutableMapOf<NavRoute, @Composable () -> Unit>()
    )

    fun get(route: NavRoute, windowWidthSize: ICWindowWidthSizeClass? = null): @Composable () -> Unit {
        return windowWidthSizeNavMap[windowWidthSize]?.get(route) ?: navMap[route] ?: {}
    }

    fun composable(route: NavRoute, windowWidthSize: ICWindowWidthSizeClass? = null, content: @Composable () -> Unit) {
        if (windowWidthSize == null) navMap[route] = content
        else windowWidthSizeNavMap[windowWidthSize]?.put(route, content)
    }
}

@Immutable
data class NavHostState(
    val route: NavRoute,
    val windowWidthSize: ICWindowWidthSizeClass? = null
)

@Composable
fun NavHost(
    controller: NavController,
    windowWidthSize: ICWindowWidthSizeClass? = null,
    configure: NavHostBuilder.() -> Unit
) {
    val builder = remember { NavHostBuilder().apply(configure) }

    val state = derivedStateOf { NavHostState(controller.current, windowWidthSize) }

    AnimatedContent(state.value) {
        builder.get(it.route, it.windowWidthSize)()
    }
}