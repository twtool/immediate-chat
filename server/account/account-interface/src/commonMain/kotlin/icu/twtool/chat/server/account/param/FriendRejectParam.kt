package icu.twtool.chat.server.account.param

import kotlinx.serialization.Serializable

/**
 * 拒绝好友申请参数
 */
@Serializable
class FriendRejectParam(
    /**
     * 好友申请记录的 ID
     */
    val id: Long
)
