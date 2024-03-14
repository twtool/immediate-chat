package icu.twtool.chat.service

import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.create
import icu.twtool.chat.server.chat.ChatService
import icu.twtool.chat.server.chat.create
import icu.twtool.chat.server.common.CommonStatus
import icu.twtool.chat.server.common.Res
import icu.twtool.chat.state.LoggedInState
import icu.twtool.ktor.cloud.client.kmp.ServiceCreator
import icu.twtool.ktor.cloud.client.kmp.websocket.websocket
import icu.twtool.logger.getLogger
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol

expect fun protocol(): URLProtocol
expect fun websocketProtocol(): URLProtocol
expect fun host(): String
expect fun port(): Int

val log = getLogger("icu.twtool.chat.service.CreatorKt")

val creator = ServiceCreator(
    protocol(), websocketProtocol(), host(), port(),
    preHandlers = listOf { builder ->
        LoggedInState.token?.let {
            builder.header(HttpHeaders.Authorization, it)
        }
    },
    configure = {
        websocket()
    }
) {
    if (it is ConnectTimeoutException) return@ServiceCreator Res.error<Any>(CommonStatus.Timeout)
    log.error("error: ${it.message}", it)
    Res.error<Any>()
}

fun AccountService.Companion.get() = creator.get(AccountService::create)
fun ChatService.Companion.get() = creator.get(ChatService::create)