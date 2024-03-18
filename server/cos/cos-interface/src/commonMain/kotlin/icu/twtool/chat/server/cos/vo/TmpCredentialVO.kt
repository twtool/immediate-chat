package icu.twtool.chat.server.cos.vo

import kotlinx.serialization.Serializable

@Serializable
data class TmpCredentialVO(
    val secretId: String,
    val secretKey: String,
    val sessionToken: String,
    val startTime: Long,
    val expiredTime: Long,
)
