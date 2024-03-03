package icu.twtool.chat.tables

import icu.twtool.chat.server.account.model.FriendRequestStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object FriendRequests : LongIdTable("tb_friend_request") {
    val createUID = long("create_uid")
    val requestUID = long("request_uid")
    val msg = varchar("msg", 255)
    val status = enumeration<FriendRequestStatus>("status")

    val createAt = datetime("create_at")
    val updateAt = datetime("update_at")

    fun verifyMsg(msg: String): Boolean = msg.length <= 255
}