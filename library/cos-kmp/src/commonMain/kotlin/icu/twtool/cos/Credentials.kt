package icu.twtool.cos

data class Credentials(
    val secretId: String,
    val secretKey: String,
    val sessionToken: String,
    val startTime: Long,
    val expireTime: Long
)
