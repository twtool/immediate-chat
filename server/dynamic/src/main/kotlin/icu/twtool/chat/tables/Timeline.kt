package icu.twtool.chat.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Timelines : LongIdTable("tb_timeline") {
    val uid = long("uid").index()
    val friendUID = long("friend_uid")
    val dynamicId = long("dynamic_id")

    val publishAt = datetime("publish_at")
}