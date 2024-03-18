package icu.twtool.cos

data class CosConfig(
    val region: String,
    val bucket: String,
    val getTmpCredentials: () -> Credentials
)
