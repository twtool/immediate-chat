package icu.twtool.chat.dao

import icu.twtool.chat.server.common.page.PageVO
import icu.twtool.chat.server.dynamic.model.Timeline
import icu.twtool.chat.server.dynamic.param.GetTimelinePageParam
import icu.twtool.chat.server.dynamic.vo.DynamicDetailsVO
import icu.twtool.chat.tables.DynamicAttachments
import icu.twtool.chat.tables.Dynamics
import icu.twtool.chat.tables.Timelines
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

object TimelineDao {

    fun insert(timeline: Timeline): Boolean {
        return Timelines.insert {
            it[uid] = timeline.uid
            it[friendUID] = timeline.friendUID
            it[dynamicId] = timeline.dynamicId
            it[publishAt] = timeline.publishAt
        }.insertedCount > 0
    }

    fun batchInsert(models: List<Timeline>) {
        Timelines.batchInsert(models, shouldReturnGeneratedValues = false) {
            this[Timelines.uid] = it.uid
            this[Timelines.friendUID] = it.friendUID
            this[Timelines.dynamicId] = it.dynamicId
            this[Timelines.publishAt] = it.publishAt
        }
    }

    fun page(loggedUID: Long, param: GetTimelinePageParam): PageVO<DynamicDetailsVO> {
        val timelineAllQuery = Timelines.select(Timelines.dynamicId, Timelines.friendUID)
            .where {
                var op = Timelines.uid eq loggedUID
                param.friendUID?.let {
                    op = op and (Timelines.friendUID eq it)
                }
                op
            }
        val timelineQuery = timelineAllQuery
            .limit(param.pageSize, ((param.currentPage - 1) * param.pageSize).toLong())
            .orderBy(Timelines.publishAt, SortOrder.DESC)
            .alias("tq")

        val resultQuery = timelineQuery
            .join(
                Dynamics,
                onColumn = timelineQuery[Timelines.dynamicId],
                otherColumn = Dynamics.id,
                joinType = JoinType.LEFT,
            )
            .join(DynamicAttachments, JoinType.LEFT, Dynamics.id, DynamicAttachments.dynamicId)
            .select(Dynamics.id, Dynamics.uid, Dynamics.content, DynamicAttachments.url, Dynamics.createAt)

        return PageVO(
            resultQuery.fold(mutableMapOf<Long, Pair<DynamicDetailsVO, MutableList<String>>>()) { acc, row ->
                val id = row[Dynamics.id].value
                val pair = acc.getOrPut(id) {
                    val attachments = mutableListOf<String>()
                    val detail = DynamicDetailsVO(
                        id = id,
                        uid = row[Dynamics.uid],
                        content = row[Dynamics.content],
                        attachments = attachments,
                        time = row[Dynamics.createAt],
                        likeIds = DynamicLikeDao.selectUIDListByDynamicID(id)
                    )
                    detail to attachments
                }
                row.getOrNull(DynamicAttachments.url)?.let(pair.second::add)
                acc
            }.values.map { it.first },
            param.currentPage,
            param.pageSize,
            timelineAllQuery.count()
        )
    }

    fun deleteByDynamicId(dynamicId: Long): Boolean {
        return Timelines.deleteWhere { Timelines.dynamicId eq dynamicId } > 0
    }
}