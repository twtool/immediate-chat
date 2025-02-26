package icu.twtool.chat.server.dynamic.vo

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class DynamicDetailsVO(
    val id: Long,
    val uid: Long,
    val content: String,
    val attachments: List<String>,
    val likeIds: List<Long>,
    val time: LocalDateTime,

    val comments: List<DynamicCommentVO> = emptyList()
)
