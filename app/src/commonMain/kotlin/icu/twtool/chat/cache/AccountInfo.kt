package icu.twtool.chat.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import icu.twtool.cache.Cache
import icu.twtool.cache.get
import icu.twtool.cache.getCache
import icu.twtool.cache.set
import icu.twtool.chat.database.database
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.common.datetime.currentEpochSeconds
import icu.twtool.chat.service.get
import icu.twtool.chat.state.LoggedInState

private fun parseKey(uid: Long): String = "ACCOUNT:$uid"

suspend fun loadAccountInfo(uid: Long, cacheable: Boolean = true): AccountInfo? {
    val key = parseKey(uid)
    val cache = getCache()
    val cacheValue = if (cacheable) cache.get<AccountInfo>(key) else null
    val result = cacheValue ?: (AccountService.get().getInfoByUID(uid.toString()).data)
    if (result != null) {
        if (!cacheable) updateAccountInfo(result, cache, key)
    }
    return result
}

fun updateAccountInfo(info: AccountInfo, cache: Cache = getCache(), key: String = parseKey(info.uid)) {
    cache[key] = info
    val loggedUID = LoggedInState.info?.uid
    if (loggedUID != null) {
        database.friendQueries.updateByLoggedUidAndUid(
            nickname = info.nickname,
            avatarUrl = info.avatarUrl,
            updateAt = currentEpochSeconds(),
            loggedUID = loggedUID,
            uid = info.uid
        )
    }
}

@Composable
fun produceAccountInfoState(info: AccountInfo, cacheable: Boolean = false) = produceState(info) {
    val res = AccountService.get().getInfoByUID(info.uid.toString())
    if (res.success) value = res.data!!.apply {
        if (info != this) updateAccountInfo(this)
    }
}

@Composable
fun produceAccountInfoState(uid: Long, cacheable: Boolean = false) =
    produceState<AccountInfo?>(null, uid) {
        loadAccountInfo(uid, cacheable)?.let {
            value = it
        }
    }