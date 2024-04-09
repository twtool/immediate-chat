package icu.twtool.chat.server.chat.vo

import icu.twtool.chat.server.chat.model.MessageAddressee
import icu.twtool.chat.server.chat.model.MessageContent
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class MessageVO(
    val sender: Long, // 发送人
    val addressee: Long, // 收信人 UID
    val originAddressee: MessageAddressee, // 用户 UID 或者频道 CID（字符串）
    val content: MessageContent,
    val createTime: LocalDateTime,

    val id: Long = -1
)
