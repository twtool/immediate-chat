package icu.twtool.chat.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * 动态详细信息表
 */
object Dynamics : LongIdTable("tb_dynamic") {
    val uid = long("uid").index()
    val content = varchar("content", 1024)

    val createAt = datetime("create_at")
    val updateAt = datetime("update_at")

    fun verifyContent(content: String): Boolean = content.trim().length <= 1024
}

object DynamicAttachments : LongIdTable("tb_dynamic_attachment") {
    val dynamicId = long("dynamic_id").index()
    val url = varchar("url", 128)

    val createAt = datetime("create_at")
    val updateAt = datetime("update_at")
}