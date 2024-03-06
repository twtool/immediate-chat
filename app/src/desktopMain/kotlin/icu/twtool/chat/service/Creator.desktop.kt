package icu.twtool.chat.service

import io.ktor.http.URLProtocol

actual fun protocol(): URLProtocol = URLProtocol.HTTP

actual fun host(): String = "localhost"

actual fun port(): Int = 20000