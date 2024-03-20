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

enum class DynamicLikeStatus {
    LIKED,
    CANCEL
}

/**
 * 点赞记录表
 */
object DynamicLikes : LongIdTable("tb_dynamic_like") {
    val dynamicId = long("dynamic_id")
    val uid = long("uid")
    val status = enumeration<DynamicLikeStatus>("status")

    val createAt = datetime("create_at")
    val updateAt = datetime("update_at")

    init {
        uniqueIndex(dynamicId, uid)
    }
}

/**
 * 动态评论表
 */
object DynamicComments : LongIdTable("tb_dynamic_comment") {
    val dynamicId = long("dynamic_id").index()
    val uid = long("uid")
    val replyId = long("reply_id").nullable()
    val content = varchar("content", 255)

    val createAt = datetime("create_at")
    val updateAt = datetime("update_at")

    fun verifyContent(content: String): Boolean = content.length <= 255
}