package icu.twtool.chat.dao

import icu.twtool.chat.server.account.model.FriendRequest
import icu.twtool.chat.server.account.model.FriendRequestStatus
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.account.vo.FriendRequestVO
import icu.twtool.chat.server.common.datetime.now
import icu.twtool.chat.tables.Accounts
import icu.twtool.chat.tables.FriendRequests
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object FriendRequestDao {

    private fun parse(row: ResultRow): FriendRequest = FriendRequest(
        id = row[FriendRequests.id].value,
        createUID = row[FriendRequests.createUID],
        requestUID = row[FriendRequests.requestUID],
        msg = row[FriendRequests.msg],
        status = row[FriendRequests.status],
        createAt = row[FriendRequests.createAt],
        updateAt = row[FriendRequests.updateAt],
    )

    fun add(createUID: Long, requestUID: Long, msg: String): Boolean =
        FriendRequests.insert {
            it[FriendRequests.createUID] = createUID
            it[FriendRequests.requestUID] = requestUID
            it[FriendRequests.msg] = msg
            it[status] = FriendRequestStatus.REQUEST

            val now = LocalDateTime.now()
            it[createAt] = now
            it[updateAt] = now
        }.insertedCount == 1

    fun last(createUID: Long, requestUID: Long): FriendRequest? =
        FriendRequests.selectAll().where {
            (FriendRequests.createUID eq createUID) and (FriendRequests.requestUID eq requestUID)
        }.orderBy(FriendRequests.id, SortOrder.DESC).limit(1).map(::parse).firstOrNull()

    fun selectById(id: Long): FriendRequest? =
        FriendRequests.selectAll().where {
            (FriendRequests.id eq id)
        }.limit(1).map(::parse).firstOrNull()

    fun updateStatusById(status: FriendRequestStatus, id: Long): Boolean =
        FriendRequests.update({ FriendRequests.id eq id }, 1) {
            it[FriendRequests.status] = status
        } == 1

    fun selectList(uid: Long, offset: Long): List<FriendRequestVO> =
        FriendRequests.join(Accounts, JoinType.LEFT, FriendRequests.createUID, Accounts.uid)
            .select(
                FriendRequests.id,
                FriendRequests.msg,
                FriendRequests.status,
                FriendRequests.createAt,
                Accounts.uid,
                Accounts.nickname,
                Accounts.avatarUrl
            ).where(FriendRequests.requestUID eq uid)
            .orderBy(FriendRequests.id, SortOrder.DESC)
            .limit(10, offset)
            .map {
                FriendRequestVO(
                    id = it[FriendRequests.id].value,
                    msg = it[FriendRequests.msg],
                    status = it[FriendRequests.status],
                    info = AccountInfo(
                        uid = it[Accounts.uid],
                        nickname = it[Accounts.nickname],
                        avatarUrl = it[Accounts.avatarUrl]
                    ),
                    createAt = it[FriendRequests.createAt]
                )
            }
}