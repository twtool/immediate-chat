package icu.twtool.chat.server.dynamic.param

import kotlinx.serialization.Serializable

@Serializable
data class CommentDynamicParam(
    val dynamicId: Long,
    val content: String,
    val replyId: Long? = null,
)
