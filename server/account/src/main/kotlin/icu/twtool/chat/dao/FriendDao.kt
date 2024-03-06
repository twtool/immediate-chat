package icu.twtool.chat.dao

import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.common.datetime.nowUTC
import icu.twtool.chat.tables.Accounts
import icu.twtool.chat.tables.Friends
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert

object FriendDao {
    fun exists(uid: Long, friendUID: Long): Boolean =
        Friends.select(Friends.id)
            .where((Friends.uid eq uid) and (Friends.friendUID eq friendUID))
            .limit(1)
            .count() == 1L

    fun selectListByUID(uid: Long): List<AccountInfo> =
        Friends.join(Accounts, JoinType.LEFT, Friends.friendUID, Accounts.uid)
            .select(Accounts.uid, Accounts.nickname, Accounts.avatarUrl)
            .where(Friends.uid eq uid)
            .map {
                AccountInfo(
                    uid = it[Accounts.uid],
                    nickname = it[Accounts.nickname],
                    avatarUrl = it[Accounts.avatarUrl]
                )
            }

    fun add(uid: Long, friendUID: Long): Boolean =
        Friends.insert {
            it[Friends.uid] = uid
            it[Friends.friendUID] = friendUID

            val now = LocalDateTime.nowUTC()
            it[createAt] = now
            it[updateAt] = now
        }.insertedCount == 1
}