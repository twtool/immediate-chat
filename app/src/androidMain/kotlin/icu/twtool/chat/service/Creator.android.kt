package icu.twtool.chat.service

import icu.twtool.chat.BuildConfig
import icu.twtool.chat.server.account.AccountService
import io.ktor.http.URLProtocol

actual fun protocol(): URLProtocol = URLProtocol.createOrDefault(BuildConfig.SERVER_PROTOCOL)
actual fun host(): String = BuildConfig.SERVER_HOST
actual fun port(): Int = BuildConfig.SERVER_PORT

