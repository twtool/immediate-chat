package icu.twtool.chat.server.account.param

import kotlinx.serialization.Serializable

/**
 * 好友申请请求参数
 */
@Serializable
class FriendRequestParam(
    /**
     * 对方的 UID
     */
    val uid: Long,
    /**
     * 请求信息
     */
    val msg: String
)
