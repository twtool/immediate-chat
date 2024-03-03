package icu.twtool.chat.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Friends : LongIdTable("tb_friend") {
    val uid = long("uid")
    val friendUID = long("friend_uid")

    val createAt = datetime("create_at")
    val updateAt = datetime("update_at")

    init {
        uniqueIndex(uid, friendUID)
    }
}