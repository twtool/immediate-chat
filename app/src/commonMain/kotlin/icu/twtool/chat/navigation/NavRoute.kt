package icu.twtool.chat.navigation

import androidx.compose.runtime.Immutable

@Immutable
open class NavRoute(val name: String, val top: Boolean = false) {

    open fun onPop() {}
}