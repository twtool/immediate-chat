package icu.twtool.chat.utils

import icu.twtool.chat.server.common.datetime.currentTimeZone
import icu.twtool.chat.server.common.datetime.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char

private val TimeFormat: DateTimeFormat<LocalDateTime> by lazy {
    LocalDateTime.Format {
        hour()
        char(':')
        minute()
    }
}

private val DateFormat: DateTimeFormat<LocalDateTime> by lazy {
    LocalDateTime.Format {
        monthNumber()
        char('-')
        dayOfMonth()
    }
}

fun LocalDateTime.formatLocal(originTimeZone: TimeZone = TimeZone.UTC): String {
    val systemTimeZone = TimeZone.currentSystemDefault()
    val now = LocalDateTime.now(systemTimeZone)
    val systemTime = currentTimeZone(systemTimeZone, originTimeZone)
    return if (systemTime.year == now.year &&
        systemTime.month == now.month &&
        systemTime.dayOfMonth == now.dayOfMonth
    ) systemTime.format(TimeFormat)
    else systemTime.format(DateFormat)
}