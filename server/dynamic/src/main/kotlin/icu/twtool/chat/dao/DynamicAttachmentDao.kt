package icu.twtool.chat.dao

import icu.twtool.chat.tables.DynamicAttachments
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert

object DynamicAttachmentDao {

    fun batchAdd(dynamicId: Long, url: List<String>, time: LocalDateTime) {
        DynamicAttachments.batchInsert(url, shouldReturnGeneratedValues = false) {
            this[DynamicAttachments.dynamicId] = dynamicId
            this[DynamicAttachments.url] = it

            this[DynamicAttachments.createAt] = time
            this[DynamicAttachments.updateAt] = time
        }
    }

    fun selectListByDynamicId(id: Long): List<String> =
        DynamicAttachments.select(DynamicAttachments.url)
            .where(DynamicAttachments.dynamicId eq id)
            .map { it[DynamicAttachments.url] }
}