package icu.twtool.chat.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Accounts : LongIdTable("tb_account") {
    val uid = long("uid")
    val email = varchar("email", 64).uniqueIndex()
    val nickname = varchar("nickname", 16).nullable()
    val avatarUrl = varchar("avatar_url", 128).nullable()
    val pwd = varchar("pwd", 128)

    val createAt = datetime("create_at")
    val updateAt = datetime("update_at")
}