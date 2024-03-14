package icu.twtool.chat

import icu.twtool.ktor.cloud.config.core.configKey

val GatewayIDKey = configKey<String>("gateway.id", nullable = false)