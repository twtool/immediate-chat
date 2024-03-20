package icu.twtool.chat.dao

import icu.twtool.chat.server.common.datetime.nowUTC
import icu.twtool.chat.tables.DynamicLikeStatus
import icu.twtool.chat.tables.DynamicLikes
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

object DynamicLikeDao {

    fun selectUIDListByDynamicID(dynamicID: Long): List<Long> =
        DynamicLikes.select(DynamicLikes.uid)
            .where((DynamicLikes.dynamicId eq dynamicID) and (DynamicLikes.status eq DynamicLikeStatus.LIKED))
            .map { it[DynamicLikes.uid] }

    fun addLike(dynamicId: Long, uid: Long): Boolean {
        val id = DynamicLikes.select(DynamicLikes.id).where {
            (DynamicLikes.dynamicId eq dynamicId) and (DynamicLikes.uid eq uid)
        }.forUpdate().map { it[DynamicLikes.id].value }.firstOrNull()

        val now = LocalDateTime.nowUTC()

        return if (id == null) {
            DynamicLikes.insert {
                it[DynamicLikes.dynamicId] = dynamicId
                it[DynamicLikes.uid] = uid

                it[status] = DynamicLikeStatus.LIKED

                it[createAt] = now
                it[updateAt] = now
            }.insertedCount
        } else {
            DynamicLikes.update({ DynamicLikes.id eq id }) {
                it[status] = DynamicLikeStatus.LIKED
                it[updateAt] = now
            }
        } >= 1
    }

    fun cancelLike(dynamicId: Long, uid: Long): Boolean {
        val id = DynamicLikes.select(DynamicLikes.id).where {
            (DynamicLikes.dynamicId eq dynamicId) and (DynamicLikes.uid eq uid)
        }.forUpdate().map { it[DynamicLikes.id].value }.firstOrNull()

        return if (id != null) {
            val now = LocalDateTime.nowUTC()

            DynamicLikes.update({ DynamicLikes.id eq id }) {
                it[status] = DynamicLikeStatus.CANCEL

                it[updateAt] = now
            } >= 1
        } else true
    }
}