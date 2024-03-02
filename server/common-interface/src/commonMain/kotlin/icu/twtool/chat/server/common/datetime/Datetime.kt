package icu.twtool.chat.server.common.datetime

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun LocalDateTime.Companion.now(timeZone: TimeZone = TimeZone.UTC): LocalDateTime =
    Clock.System.now().toLocalDateTime(timeZone)

expect fun Instant.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String

fun LocalDateTime.format(timeZone: TimeZone = TimeZone.currentSystemDefault()): String =
    toInstant(timeZone).format()