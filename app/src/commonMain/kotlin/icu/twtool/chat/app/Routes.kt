package icu.twtool.chat.app

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import icu.twtool.chat.navigation.NavRoute
import icu.twtool.chat.server.account.vo.FriendRequestVO

val LoginRoute = NavRoute("Login", true)

val MessagesRoute = NavRoute("Messages", true)

val FriendsRoute = NavRoute("Friends", true)
val NewFriendRoute = NavRoute("NewFriendRoute", false)

@Stable
object AcceptFriendRequestRoute : NavRoute("AcceptFriendRequestRoute", false) {

    var request: FriendRequestVO? by mutableStateOf(null)

    override fun onPop() {
        request = null
    }
}

val DynamicRoute = NavRoute("Dynamic", true)
