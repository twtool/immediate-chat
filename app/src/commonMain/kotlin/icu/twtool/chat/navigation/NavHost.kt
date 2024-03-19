package icu.twtool.chat.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntSize
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass

class NavHostBuilder {

    private val navMap = mutableMapOf<NavRoute, @Composable (NavHostState, PaddingValues) -> Unit>()
    private val windowWidthSizeNavMap = mapOf(
        ICWindowWidthSizeClass.Compact to mutableMapOf<NavRoute, @Composable (NavHostState, PaddingValues) -> Unit>(),
        ICWindowWidthSizeClass.Medium to mutableMapOf<NavRoute, @Composable (NavHostState, PaddingValues) -> Unit>(),
        ICWindowWidthSizeClass.Expanded to mutableMapOf<NavRoute, @Composable (NavHostState, PaddingValues) -> Unit>()
    )

    fun get(
        route: NavRoute,
        windowWidthSize: ICWindowWidthSizeClass? = null
    ): @Composable (NavHostState, PaddingValues) -> Unit {
        return windowWidthSizeNavMap[windowWidthSize]?.get(route) ?: navMap[route] ?: { _, _ -> }
    }

    fun composable(
        route: NavRoute,
        windowWidthSize: ICWindowWidthSizeClass? = null,
        content: @Composable (NavHostState, PaddingValues) -> Unit
    ) {
        if (windowWidthSize == null) navMap[route] = content
        else windowWidthSizeNavMap[windowWidthSize]?.put(route, content)
    }

    fun composable(
        routes: List<NavRoute>,
        windowWidthSize: ICWindowWidthSizeClass,
        content: @Composable (NavHostState, PaddingValues) -> Unit
    ) {
        routes.forEach { route ->
            windowWidthSizeNavMap[windowWidthSize]?.put(route, content)
        }
    }
}

@Immutable
data class NavHostState(
    val route: NavRoute,
    val windowSize: ICWindowSizeClass,
)

@Composable
fun NavHost(
    controller: NavController,
    windowWidthSize: ICWindowSizeClass,
    paddingValues: PaddingValues,
    configure: NavHostBuilder.() -> Unit
) {
    val builder = remember { NavHostBuilder().apply(configure) }

    val state = derivedStateOf { NavHostState(controller.current, windowWidthSize) }

    AnimatedContent(state.value,
        transitionSpec = {
            fadeIn() togetherWith fadeOut() using SizeTransform(false) { _, targetSize ->
                keyframes {
                    IntSize(targetSize.width, targetSize.height) at 0
                }
            }
        }
    ) {
        builder.get(it.route, it.windowSize.widthSizeClass)(it, paddingValues)
    }
}