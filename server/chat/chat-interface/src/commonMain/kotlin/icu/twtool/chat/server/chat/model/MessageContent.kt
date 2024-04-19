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

@Serializable
data class FileMessageContent(
    val url: String,
    val filename: String,
    val extension: String,
    val size: String? = null,
    val origin: String? = null
) : MessageContent() {

    override fun renderText(): String = "[文件]$filename"
}