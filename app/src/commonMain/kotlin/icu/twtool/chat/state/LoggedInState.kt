package icu.twtool.chat.state

import icu.twtool.cache.compose.StringCacheState
import icu.twtool.cache.compose.anyCacheState
import icu.twtool.chat.server.account.jwt.Jwt
import icu.twtool.chat.server.account.vo.AccountInfo

object LoggedInState {

    var info: AccountInfo? by anyCacheState<AccountInfo>("logged:in:info")

    var token: String? by StringCacheState("logged.in.token")
        private set

    fun login(token: String?) {
        if (token != null) info = Jwt.parse(token).payload.account
        this.token = token
    }
}
