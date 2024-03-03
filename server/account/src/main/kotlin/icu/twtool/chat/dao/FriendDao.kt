package icu.twtool.chat.dao

import icu.twtool.chat.server.common.datetime.now
import icu.twtool.chat.tables.Friends
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert

object FriendDao {
    fun exists(uid: Long, friendUID: Long): Boolean =
        Friends.select(Friends.id)
            .where((Friends.uid eq uid) and (Friends.friendUID eq friendUID))
            .limit(1)
            .count() == 1L

    fun add(uid: Long, friendUID: Long): Boolean =
        Friends.insert {
            it[Friends.uid] = uid
            it[Friends.friendUID] = friendUID

            val now = LocalDateTime.now()
            it[createAt] = now
            it[updateAt] = now
        }.insertedCount == 1
}