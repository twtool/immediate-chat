package icu.twtool.chat.server.account

import icu.twtool.chat.server.account.param.AuthParam
import icu.twtool.chat.server.account.param.FriendAcceptParam
import icu.twtool.chat.server.account.param.FriendRejectParam
import icu.twtool.chat.server.account.param.FriendRequestParam
import icu.twtool.chat.server.account.param.LoginParam
import icu.twtool.chat.server.account.param.RegisterParam
import icu.twtool.chat.server.account.param.UpdateInfoParam
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.account.vo.FriendRequestVO
import icu.twtool.chat.server.common.Res
import icu.twtool.ktor.cloud.http.core.HttpMethod
import icu.twtool.ktor.cloud.http.core.IServiceCreator
import icu.twtool.ktor.cloud.http.core.annotation.Body
import icu.twtool.ktor.cloud.http.core.annotation.InternalOnly
import icu.twtool.ktor.cloud.http.core.annotation.Query
import icu.twtool.ktor.cloud.http.core.annotation.RequestMapping
import icu.twtool.ktor.cloud.http.core.annotation.Service

@Service("AccountService", "account")
interface AccountService {

    @RequestMapping(HttpMethod.Post, "login")
    suspend fun login(@Body param: LoginParam): Res<String>

    @RequestMapping(HttpMethod.Post, "auth")
    suspend fun auth(@Body param: AuthParam): Res<Long>

    @RequestMapping(HttpMethod.Post, "register")
    suspend fun register(@Body param: RegisterParam): Res<String>

    @RequestMapping(HttpMethod.Get, "info-by-uid")
    suspend fun getInfoByUID(@Query uid: String): Res<AccountInfo>

    @RequestMapping(HttpMethod.Post, "update-info")
    suspend fun updateInfo(@Body param: UpdateInfoParam): Res<AccountInfo>

    /**
     * 发起好友申请
     */
    @RequestMapping(HttpMethod.Post, "friend-request")
    suspend fun sendFriendRequest(@Body param: FriendRequestParam): Res<Unit>

    /**
     * 同意好友申请
     */
    @RequestMapping(HttpMethod.Post, "friend-accept")
    suspend fun acceptFriendRequest(@Body param: FriendAcceptParam): Res<Unit>

    /**
     * 拒绝好友申请
     */
    @RequestMapping(HttpMethod.Post, "friend-reject")
    suspend fun rejectFriendRequest(@Body param: FriendRejectParam): Res<Unit>

    /**
     * 获取好友申请列表
     */
    @RequestMapping(HttpMethod.Get, "friend-request-list")
    suspend fun getFriendRequestList(@Query offset: String): Res<List<FriendRequestVO>>

    /**
     * 获取好友列表
     */
    @RequestMapping(HttpMethod.Get, "friend-list")
    suspend fun getFriendList(): Res<List<AccountInfo>>

    /**
     * 获取好友列表
     */
    @InternalOnly
    @RequestMapping(HttpMethod.Get, "friend-uid-list")
    suspend fun getFriendUIDList(@Query uid: String): Res<List<Long>>

    companion object
}

expect fun AccountService.Companion.create(creator: IServiceCreator): AccountService