package icu.twtool.chat.service

import icu.twtool.chat.server.account.AccountService
import io.ktor.http.URLProtocol

actual fun protocol(): URLProtocol = URLProtocol.HTTP

actual fun host(): String = "localhost"

actual fun port(): Int = 20000