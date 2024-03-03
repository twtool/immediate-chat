package icu.twtool.chat.server.account.vo

import icu.twtool.chat.server.account.model.FriendRequestStatus
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * 好友申请数据对象
 */
@Serializable
data class FriendRequestVO(
    val id: Long,
    val msg: String,
    val status: FriendRequestStatus,
    val info: AccountInfo,
    val createAt: LocalDateTime
)