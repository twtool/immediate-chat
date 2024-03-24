package icu.twtool.chat.server.gateway.topic

import kotlinx.serialization.Serializable

const val PUSH_MESSAGE_TOPIC = "PUSH_MESSAGE_TOPIC"

@Serializable
sealed class PushMessage {

    abstract val uid: Long

}

@Serializable
data class FriendRequestPushMessage(
    val id: Long,
    val nickname: String?,
    val message: String,

    override val uid: Long
) : PushMessage()