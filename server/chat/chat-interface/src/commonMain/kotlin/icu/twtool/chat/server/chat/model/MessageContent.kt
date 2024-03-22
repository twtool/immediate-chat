package icu.twtool.chat.server.chat.model

import kotlinx.serialization.Serializable

@Serializable
sealed class MessageContent {

    abstract fun renderText(): String
}

@Serializable
data class PlainMessageContent(
    val value: String
) : MessageContent() {

    override fun renderText(): String = value
}

@Serializable
data class ImageMessageContent(
    val url: String
) : MessageContent() {

    override fun renderText(): String = "[图片]"
}