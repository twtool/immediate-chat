package icu.twtool.chat.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.vo.FriendRequestVO
import icu.twtool.chat.service.get
import icu.twtool.chat.utils.tryLockRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

@Stable
class NewFriendViewState {

    private val mutex = Mutex()

    val friendRequestList = mutableStateListOf<FriendRequestVO>()
    private var more = true

    private val accountService: AccountService by lazy { AccountService.get() }

    suspend fun loadFriendRequestList(onError: suspend (String) -> Unit) {
        withContext(Dispatchers.IO) {
            mutex.tryLockRun {
                if (!more) return@tryLockRun

                val res = accountService.getFriendRequestList(LoggedInState.token!!, friendRequestList.size.toString())
                if (res.success) {
                    val data = res.data ?: emptyList()
                    if (data.isEmpty()) {
                        more = false
                        return@tryLockRun
                    }
                    friendRequestList.addAll(data)
                } else {
                    onError(res.msg)
                }
            }
        }
    }
}