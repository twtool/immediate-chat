package icu.twtool.chat.server.account.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Friend(
    val id: Long,
    val uid: Long,
    val friendUID: Long,
    val createAt: LocalDateTime,
    val updateAt: LocalDateTime
)
