package icu.twtool.chat.server.chat.param

import kotlinx.serialization.Serializable

@Serializable
data class GetMessageRecordParam(
    val lastEpochSeconds: Long,
    val currentEpochSeconds: Long,
)
