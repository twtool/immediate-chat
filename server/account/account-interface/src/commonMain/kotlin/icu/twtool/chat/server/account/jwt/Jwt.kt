package icu.twtool.chat.server.account.jwt

import icu.twtool.chat.server.account.vo.AccountInfo
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class Jwt(
    val header: Header,
    val payload: Payload,
) {

    @Serializable
    class Header(
        val alg: String,
        val typ: String
    )

    @Serializable
    class Payload(
        // 过期时间
        val exp: LocalDateTime,
        val account: AccountInfo
    )

    companion object {

        @OptIn(ExperimentalEncodingApi::class)
        fun parse(token: String): Jwt {
            val arr = token.split('.')
            return Jwt(
                Json.decodeFromString(Base64.decode(arr[0]).toString(Charsets.UTF_8)),
                Json.decodeFromString(Base64.decode(arr[1]).toString(Charsets.UTF_8))
            )
        }
    }
}