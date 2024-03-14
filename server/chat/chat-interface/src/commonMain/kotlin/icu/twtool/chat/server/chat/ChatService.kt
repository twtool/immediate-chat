package icu.twtool.chat.server.chat

import icu.twtool.chat.server.chat.param.SendMessageParam
import icu.twtool.chat.server.common.Res
import icu.twtool.ktor.cloud.http.core.HttpMethod
import icu.twtool.ktor.cloud.http.core.IServiceCreator
import icu.twtool.ktor.cloud.http.core.annotation.Body
import icu.twtool.ktor.cloud.http.core.annotation.RequestMapping
import icu.twtool.ktor.cloud.http.core.annotation.Service

@Service("ChatService", "chat")
interface ChatService {

    @RequestMapping(HttpMethod.Post, "send-message")
    suspend fun sendMessage(@Body param: SendMessageParam): Res<Unit>

    companion object
}

expect fun ChatService.Companion.create(creator: IServiceCreator): ChatService