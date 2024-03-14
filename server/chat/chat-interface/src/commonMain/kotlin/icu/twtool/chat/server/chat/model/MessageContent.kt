package icu.twtool.chat.server.chat.model

import kotlinx.serialization.Serializable

@Serializable
sealed class MessageContent

@Serializable
data class PlainMessageContent(
    val value: String
) : MessageContent() {

    override fun toString(): String = value
}