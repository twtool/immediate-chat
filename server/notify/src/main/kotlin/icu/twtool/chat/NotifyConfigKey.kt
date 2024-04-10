package icu.twtool.chat

import icu.twtool.ktor.cloud.config.core.configKey

val NOTIFY_MAIL_SMTP_HOST = configKey<String>("notify.mail.smtp.host", nullable = false)
val NOTIFY_MAIL_PORT = configKey<Int>("notify.mail.smtp.port", 465)

val NOTIFY_MAIL_USER = configKey<String>("notify.mail.user", nullable = false)
val NOTIFY_MAIL_PASSWORD = configKey<String>("notify.mail.password", nullable = false)

