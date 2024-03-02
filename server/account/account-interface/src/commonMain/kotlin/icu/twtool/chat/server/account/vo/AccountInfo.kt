package icu.twtool.chat.server.account.vo

import kotlinx.serialization.Serializable

@Serializable
data class AccountInfo(
    val uid: Long,
    val nickname: String?,
    val avatarUrl: String?
)
