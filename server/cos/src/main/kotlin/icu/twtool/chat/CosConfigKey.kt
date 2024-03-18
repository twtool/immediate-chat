package icu.twtool.chat

import icu.twtool.ktor.cloud.config.core.configKey

val CosAppid = configKey<String>("cos.app-id")
val CosHost = configKey<String?>("cos.host") // 如果您使用了腾讯云 cvm，可以设置内部域名: sts.internal.tencentcloudapi.com
val CosBucket = configKey<String>("cos.bucket", nullable = false)
val CosRegion = configKey<String>("cos.region", nullable = false)