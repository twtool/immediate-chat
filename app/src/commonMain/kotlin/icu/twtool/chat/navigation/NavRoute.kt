package icu.twtool.chat.navigation

import androidx.compose.runtime.Immutable

@Immutable
open class NavRoute(
    val name: String,
    val top: Boolean = false,
    val parent: NavRoute? = null,
    val title: String? = null
) {

    open fun onPop() {}

    open fun clear() {}
}