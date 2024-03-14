package icu.twtool.chat.server.chat.model

import kotlinx.serialization.Serializable

@Serializable
sealed class MessageAddressee

@Serializable
data class AccountMessageAddressee(
    val uid: Long
) : MessageAddressee()