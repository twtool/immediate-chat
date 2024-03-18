package icu.twtool.chat.dao

import icu.twtool.chat.server.common.datetime.nowUTC
import icu.twtool.chat.tables.Dynamics
import kotlinx.datetime.LocalDateTime
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
}