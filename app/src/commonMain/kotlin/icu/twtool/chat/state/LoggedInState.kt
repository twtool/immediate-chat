package icu.twtool.chat.state

import icu.twtool.cache.compose.StringCacheState
import icu.twtool.cache.compose.anyCacheState
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.jwt.Jwt
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.service.get

object LoggedInState {

    var info: AccountInfo? by anyCacheState<AccountInfo>("logged:in:info")

    var token: String? by StringCacheState("logged.in.token")
        private set

    suspend fun login(token: String?) {
        if (token != null) {
            val uid = Jwt.parse(token).payload.uid
            info = AccountService.get().getInfoByUID(uid.toString()).data
            WebSocketState.auth()
        }
        this.token = token
    }

    fun logout() {
        this.token = null
    }
}
