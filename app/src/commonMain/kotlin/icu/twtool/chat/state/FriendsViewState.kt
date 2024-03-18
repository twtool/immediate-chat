package icu.twtool.chat.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import icu.twtool.chat.database.database
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.common.datetime.currentEpochSeconds
import icu.twtool.chat.service.get
import icu.twtool.chat.utils.tryLockRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

@Stable
class FriendsViewState {

    private val mutex = Mutex()

    var friendList by mutableStateOf(listOf<AccountInfo>())
        private set

    private val accountService: AccountService by lazy { AccountService.get() }

    suspend fun loadFriendList(refresh: Boolean = false, onError: suspend (String) -> Unit) {
        withContext(Dispatchers.IO) {
            mutex.tryLockRun {
                val info = LoggedInState.info ?: return@tryLockRun
                val dbData =
                    database.friendQueries.selectAllByLoggedUid(info.uid).executeAsList().mapTo(mutableListOf()) {
                        AccountInfo(
                            uid = it.uid,
                            nickname = it.nickname,
                            avatarUrl = it.avatarUrl
                        )
                    }
                val flag = refresh || dbData.isEmpty()
                friendList = if (flag) {
                    val res = accountService.getFriendList()
                    val remoteData = res.data ?: emptyList()
                    if (res.success) {
                        val addData = mutableListOf<AccountInfo>()
                        val modifiedData = mutableListOf<AccountInfo>()
                        remoteData.forEach { remote ->
                            val dbIndex = dbData.indexOfFirst { it.uid == remote.uid }
                            if (dbIndex < 0) addData.add(remote)
                            else {
                                val db = dbData[dbIndex]
                                dbData.removeAt(dbIndex)
                                if (db != remote) modifiedData.add(remote)
                            }
                        }
                        database.transaction {
                            database.friendQueries.deleteByLoggedUidAndUid(info.uid, dbData.map { it.uid })
                            val now = currentEpochSeconds()
                            addData.forEach {
                                database.friendQueries.insertOne(info.uid, it.uid, it.nickname, it.avatarUrl, now, now)
                            }
                            modifiedData.forEach {
                                database.friendQueries.updateByLoggedUidAndUid(
                                    nickname = it.nickname,
                                    avatarUrl = it.avatarUrl,
                                    loggedUID = info.uid,
                                    uid = it.uid,
                                    updateAt = now
                                )
                            }
                        }
                    } else onError(res.msg)
                    remoteData
                } else {
                    dbData
                }
            }
        }
    }
}