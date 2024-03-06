package icu.twtool.chat.server.common.datetime

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun LocalDateTime.Companion.nowUTC() = Clock.System.now().toLocalDateTime(TimeZone.UTC)
fun LocalDateTime.Companion.now(timeZone: TimeZone) = Clock.System.now().toLocalDateTime(timeZone)

fun LocalDateTime.epochSeconds(timeZone: TimeZone = TimeZone.UTC): Long = toInstant(timeZone).epochSeconds

fun currentEpochSeconds(): Long = Clock.System.now().epochSeconds