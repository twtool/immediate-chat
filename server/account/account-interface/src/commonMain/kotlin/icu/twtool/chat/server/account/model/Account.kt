package icu.twtool.chat.server.account.model

import icu.twtool.chat.server.account.vo.AccountInfo
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: Long,
    val uid: Long,
    val email: String,
    val nickname: String?,
    val avatarUrl: String?,
    val pwd: String,
    val createAt: LocalDateTime,
    val updateAt: LocalDateTime
) {

    fun toInfo(): AccountInfo = AccountInfo(
        uid = this.uid,
        nickname = this.nickname,
        avatarUrl = this.avatarUrl,
    )
}