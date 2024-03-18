package icu.twtool.chat.server.dynamic.meesage

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
sealed class TimelineEvent

@Serializable
data class PublishDynamicEvent(
    val id: Long,
    val uid: Long,
    val time: LocalDateTime
) : TimelineEvent()