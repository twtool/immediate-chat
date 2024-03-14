package icu.twtool.chat.server.gateway.param

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class WebSocketParam(
    val type: WebSocketParamType,
    val content: JsonElement
)
