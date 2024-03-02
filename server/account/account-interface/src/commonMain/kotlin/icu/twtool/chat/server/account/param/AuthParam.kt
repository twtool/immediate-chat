package icu.twtool.chat.server.account.param

import kotlinx.serialization.Serializable

@Serializable
data class AuthParam(
    val token: String
)
