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

@Serializable
data class DeleteDynamicEvent(
    val id: Long
) : TimelineEvent()

@Serializable
data class AddFriendEvent(
    val uid: Long,
    val friendUID: Long,
) : TimelineEvent()