package icu.twtool.chat.dao

import icu.twtool.chat.server.account.model.Account
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.common.datetime.nowUTC
import icu.twtool.chat.tables.Accounts
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object AccountDao {

    private fun parseAccount(row: ResultRow): Account = Account(
        id = row[Accounts.id].value,
        uid = row[Accounts.uid],
        email = row[Accounts.email],
        nickname = row[Accounts.nickname],
        avatarUrl = row[Accounts.avatarUrl],
        pwd = row[Accounts.pwd],
        createAt = row[Accounts.createAt],
        updateAt = row[Accounts.updateAt],
    )

    fun add(uid: Long, email: String, pwd: String): Boolean =
        Accounts.insert {
            it[Accounts.uid] = uid
            it[Accounts.email] = email
            it[Accounts.pwd] = pwd

            val now = LocalDateTime.nowUTC()
            it[createAt] = now
            it[updateAt] = now
        }.insertedCount == 1

    fun selectByUid(uid: Long): Account? =
        Accounts.selectAll().where(Accounts.uid eq uid).map(::parseAccount).firstOrNull()

    fun selectByEmail(email: String): Account? =
        Accounts.selectAll().where(Accounts.email eq email).map(::parseAccount).firstOrNull()

    fun existsUid(uid: Long): Boolean =
        Accounts.select(Accounts.id).where(Accounts.uid eq uid).limit(1).count() == 1L

    fun selectAvatarUrlByUID(uid: Long) =
        Accounts.select(Accounts.avatarUrl).where(Accounts.uid eq uid).limit(1)
            .map { it[Accounts.avatarUrl] }
            .firstOrNull()

    fun updateInfoByUID(info: AccountInfo) =
        Accounts.update({
            Accounts.uid eq info.uid
        }) {
            it[nickname] = info.nickname
            it[avatarUrl] = info.avatarUrl
        } == 1

    fun selectNicknameByUid(uid: Long): String? =
        Accounts.select(Accounts.nickname)
            .where(Accounts.uid eq uid)
            .map { it[Accounts.nickname] }.firstOrNull()
}