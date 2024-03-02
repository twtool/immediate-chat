package icu.twtool.chat

import icu.twtool.ktor.cloud.config.core.ConfigKey

val PwdSecretKey = ConfigKey<String>("account.pwd-secret")
val TokenSecretKey = ConfigKey<String>("account.token-secret")