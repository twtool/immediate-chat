package icu.twtool.chat.server.account.param

import kotlinx.serialization.Serializable

@Serializable
data class CheckFriendParam(
    val uid: Long
)
