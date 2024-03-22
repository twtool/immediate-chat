package icu.twtool.chat.app

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import icu.twtool.chat.database.database
import icu.twtool.chat.navigation.NavRoute
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.account.vo.FriendRequestVO
import icu.twtool.chat.server.dynamic.vo.DynamicDetailsVO
import icu.twtool.chat.state.LoggedInState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

val LoginRoute = NavRoute("Login", true)

val MessagesRoute = NavRoute("Messages", true)

@Stable
object ChatRoute : NavRoute("Chat", false, MessagesRoute) {
    var info by mutableStateOf<AccountInfo?>(null)
        private set
    private val stack = mutableListOf<AccountInfo>()

    override fun onPop() {
        info = stack.removeLastOrNull()
    }

    suspend fun open(info: AccountInfo, opened: suspend () -> Unit) {
        withContext(Dispatchers.IO) {
            val loggedUID = LoggedInState.info?.uid ?: return@withContext
            database.messageQueries.upsertByLoggedUIDAndID(
                now = Clock.System.now().epochSeconds,
                loggedUID = loggedUID,
                uid = info.uid
            )
            withContext(Dispatchers.Default) {
                ChatRoute.info?.let { stack.add(it) }
                ChatRoute.info = info
                opened()
            }
        }
    }
}

val ChatSettingsRoute = NavRoute("ChatSettings", false, title = "聊天设置")

val FriendsRoute = NavRoute("Friends", true)
val NewFriendRoute = NavRoute("NewFriend", false, FriendsRoute)
val AddFriendRoute = NavRoute("AddFriend", false, FriendsRoute)

@Stable
object AcceptFriendRequestRoute : NavRoute("AcceptFriendRequest", false, FriendsRoute) {
    var request: FriendRequestVO? by mutableStateOf(null)
    override fun onPop() {
        request = null
    }
}

val DynamicRoute = NavRoute("Dynamic", true)
val PublishDynamicRoute = NavRoute("Dynamic", false, DynamicRoute)

object DynamicDetailsRoute : NavRoute("DynamicDetails", false, DynamicRoute) {

    var details by mutableStateOf<DynamicDetailsVO?>(null)
        private set

    private val stack = mutableListOf<DynamicDetailsVO>()

    override fun onPop() {
        details = stack.removeLastOrNull()
    }

    fun open(details: DynamicDetailsVO, opened: () -> Unit) {
        this.details?.let(stack::add)
        this.details = details
        opened()
    }
}

@Stable
object AccountInfoRoute : NavRoute("AccountInfo", false, FriendsRoute) {
    var info: AccountInfo? by mutableStateOf(null)

    fun open(info: AccountInfo, opened: () -> Unit) {
        this.info = info
        opened()
    }
}

val ChangeAccountInfoRoute = NavRoute("ChangeAccountInfo", false, title = "编辑资料")

val ScanCodeRoute = NavRoute("ScanCode", false)