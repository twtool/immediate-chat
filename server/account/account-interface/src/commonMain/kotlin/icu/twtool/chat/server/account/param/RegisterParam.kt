package icu.twtool.chat.server.account.param

import kotlinx.serialization.Serializable

@Serializable
data class RegisterParam(
    val email: String,
    val captcha: String,
    val pwd: String,
)
