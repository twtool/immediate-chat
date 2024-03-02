package icu.twtool.chat.server.common.datetime

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.format.DateTimeFormatter

actual fun Instant.format(pattern: String): String = DateTimeFormatter.ofPattern(pattern).format(toJavaInstant())