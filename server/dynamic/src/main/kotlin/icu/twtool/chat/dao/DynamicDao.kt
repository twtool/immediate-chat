package icu.twtool.chat.dao

import icu.twtool.chat.server.dynamic.vo.DynamicDetailsVO
import icu.twtool.chat.tables.Dynamics
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId

object DynamicDao {

    /**
     * 添加动态数据
     */
    fun add(uid: Long, content: String, time: LocalDateTime): Long = Dynamics.insertAndGetId {
        it[Dynamics.uid] = uid
        it[Dynamics.content] = content

        it[createAt] = time
        it[updateAt] = time
    }.value

    fun detailsById(id: Long): DynamicDetailsVO? =
        Dynamics.select(Dynamics.uid, Dynamics.content, Dynamics.createAt)
            .where(Dynamics.id eq id)
            .map {
                val attachments = DynamicAttachmentDao.selectListByDynamicId(id)
                val likeIds = DynamicLikeDao.selectUIDListByDynamicID(id)
                val comments = DynamicCommentDao.selectListByDynamicId(id)
                DynamicDetailsVO(
                    id = id,
                    uid = it[Dynamics.uid],
                    content = it[Dynamics.content],
                    attachments = attachments,
                    likeIds = likeIds,
                    time = it[Dynamics.createAt],
                    comments = comments
                )
            }.firstOrNull()

    fun deleteById(loggedUID: Long, id: Long): Boolean {
        return (Dynamics.select(Dynamics.id).where { (Dynamics.uid eq loggedUID) and (Dynamics.id eq id) }.forUpdate()
            .map { row ->
                Dynamics.deleteWhere { Dynamics.id eq row[Dynamics.id] }
            }.firstOrNull() ?: 0) > 0
    }

    fun selectDynamicIdAndPublishTimeByUID(uid: Long): List<Pair<Long, LocalDateTime>> =
        Dynamics.select(Dynamics.id, Dynamics.createAt)
            .where(Dynamics.uid eq uid)
            .map { it[Dynamics.id].value to it[Dynamics.createAt] }
}