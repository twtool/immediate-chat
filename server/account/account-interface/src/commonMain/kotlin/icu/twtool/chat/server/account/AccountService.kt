package icu.twtool.chat.server.account

import icu.twtool.chat.server.account.param.AuthParam
import icu.twtool.chat.server.account.param.LoginParam
import icu.twtool.chat.server.account.param.RegisterParam
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.common.Res
import icu.twtool.ktor.cloud.http.core.HttpMethod
import icu.twtool.ktor.cloud.http.core.IServiceCreator
import icu.twtool.ktor.cloud.http.core.annotation.Body
import icu.twtool.ktor.cloud.http.core.annotation.RequestMapping
import icu.twtool.ktor.cloud.http.core.annotation.Service

@Service("AccountService", "account")
interface AccountService {

    @RequestMapping(HttpMethod.Post, "login")
    suspend fun login(@Body param: LoginParam): Res<String>

    @RequestMapping(HttpMethod.Post, "auth")
    suspend fun auth(@Body param: AuthParam): Res<Unit>

    @RequestMapping(HttpMethod.Post, "register")
    suspend fun register(@Body param: RegisterParam): Res<String>

    companion object
}

expect fun AccountService.Companion.create(creator: IServiceCreator): AccountService