package icu.twtool.chat.service

import icu.twtool.chat.BuildConfig
import io.ktor.http.URLProtocol

actual fun protocol(): URLProtocol = URLProtocol.createOrDefault(BuildConfig.SERVER_PROTOCOL)
actual fun host(): String = BuildConfig.SERVER_HOST
actual fun port(): Int = BuildConfig.SERVER_PORT

actual fun websocketProtocol(): URLProtocol = URLProtocol.createOrDefault(BuildConfig.SERVER_WEBSOCKET_PROTOCOL)