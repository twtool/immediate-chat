package icu.twtool.chat.server.account.param

import kotlinx.serialization.Serializable

/**
 * 同意好友申请参数
 */
@Serializable
class FriendAcceptParam(
    /**
     * 好友申请记录的 ID
     */
    val id: Long
)
