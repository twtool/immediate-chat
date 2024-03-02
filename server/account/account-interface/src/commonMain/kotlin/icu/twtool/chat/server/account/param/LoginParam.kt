package icu.twtool.chat.server.account.param

import kotlinx.serialization.Serializable

@Serializable
data class LoginParam(
    val principal: String,
    val pwd: String
)
