package icu.twtool.chat.dao

import icu.twtool.chat.server.common.datetime.nowUTC
import icu.twtool.chat.server.dynamic.vo.DynamicCommentVO
import icu.twtool.chat.tables.DynamicComments
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert

object DynamicCommentDao {

    fun add(dynamicId: Long, uid: Long, content: String, replyId: Long?): Boolean =
        DynamicComments.insert {
            it[DynamicComments.dynamicId] = dynamicId
            it[DynamicComments.uid] = uid
            it[DynamicComments.content] = content
            it[DynamicComments.replyId] = replyId

            val now = LocalDateTime.nowUTC()
            it[createAt] = now
            it[updateAt] = now
        }.insertedCount > 0

    fun selectListByDynamicId(dynamicId: Long): List<DynamicCommentVO> =
        DynamicComments.select(
            DynamicComments.id,
            DynamicComments.uid,
            DynamicComments.content,
            DynamicComments.replyId,
            DynamicComments.createAt
        ).where(DynamicComments.dynamicId eq dynamicId).map {
            DynamicCommentVO(
                it[DynamicComments.id].value,
                it[DynamicComments.uid],
                it[DynamicComments.content],
                it[DynamicComments.replyId],
                it[DynamicComments.createAt],
            )
        }
}