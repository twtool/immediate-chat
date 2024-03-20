package icu.twtool.chat.server.dynamic.vo

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class DynamicCommentVO(
    val id: Long,
    val uid: Long,
    val content: String,
    val replyId: Long?,
    val time: LocalDateTime
)
