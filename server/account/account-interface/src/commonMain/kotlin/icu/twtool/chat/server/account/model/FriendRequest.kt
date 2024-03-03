package icu.twtool.chat.server.account.model

import icu.twtool.chat.server.common.datetime.now
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

enum class FriendRequestStatus {
    REQUEST,
    ACCEPT,
    REJECT
}

@Serializable
data class FriendRequest(
    val id: Long,
    val createUID: Long,
    val requestUID: Long,
    val msg: String,
    val status: FriendRequestStatus,
    val createAt: LocalDateTime,
    val updateAt: LocalDateTime
) {

    companion object {

        const val VALID_SECONDS: Int = 7 * 24 * 60 * 60
    }

    fun isValid(): Boolean {
        return (LocalDateTime.now().second - createAt.second) < VALID_SECONDS
    }
}
