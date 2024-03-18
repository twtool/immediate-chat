package icu.twtool.chat.server.dynamic.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Timeline(
    val uid: Long,
    val friendUID: Long,
    val dynamicId: Long,
    val publishAt: LocalDateTime,
)
