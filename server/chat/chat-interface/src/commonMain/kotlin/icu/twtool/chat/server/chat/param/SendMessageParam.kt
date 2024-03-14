package icu.twtool.chat.server.chat.param

import icu.twtool.chat.server.chat.model.AccountMessageAddressee
import icu.twtool.chat.server.chat.model.MessageAddressee
import icu.twtool.chat.server.chat.model.MessageContent
import icu.twtool.chat.server.chat.model.PlainMessageContent
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SendMessageParam(
    val addressee: MessageAddressee,

    val content: MessageContent
)
