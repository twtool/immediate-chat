package icu.twtool.chat.server.account.param

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfoParam(
    val nickname: String? = null,
    val avatarUrl: String? = null,
)
