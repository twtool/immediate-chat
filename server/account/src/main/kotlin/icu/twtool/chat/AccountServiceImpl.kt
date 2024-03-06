package icu.twtool.chat

import icu.twtool.chat.dao.AccountDao
import icu.twtool.chat.dao.FriendDao
import icu.twtool.chat.dao.FriendRequestDao
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.jwt.Jwt
import icu.twtool.chat.server.account.model.FriendRequestStatus
import icu.twtool.chat.server.account.param.AuthParam
import icu.twtool.chat.server.account.param.FriendAcceptParam
import icu.twtool.chat.server.account.param.FriendRejectParam
import icu.twtool.chat.server.account.param.FriendRequestParam
import icu.twtool.chat.server.account.param.LoginParam
import icu.twtool.chat.server.account.param.RegisterParam
import icu.twtool.chat.server.account.status.AccountStatus
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.account.vo.FriendRequestVO
import icu.twtool.chat.server.common.CommonStatus
import icu.twtool.chat.server.common.Res
import icu.twtool.chat.server.common.result
import icu.twtool.chat.tables.FriendRequests
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.exposed.database
import icu.twtool.ktor.cloud.exposed.transaction
import icu.twtool.ktor.cloud.redis.incr
import icu.twtool.ktor.cloud.redis.redis
import icu.twtool.ktor.cloud.route.service.annotation.ServiceImpl
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@ServiceImpl
class AccountServiceImpl(application: KtorCloudApplication) : AccountService {

    private val pwdSecret = application.config[PwdSecretKey]
    private val tokenSecret = application.config[TokenSecretKey]
    private val tokenHmacUtils = HmacUtils(HmacAlgorithms.HMAC_SHA_256, tokenSecret)

    private val redis = application.redis
    private val db = application.database

    override suspend fun login(param: LoginParam): Res<String> = db.transaction {
        val uid = param.principal.toLongOrNull()
        val account = if (uid == null) AccountDao.selectByEmail(param.principal) else AccountDao.selectByUid(uid)
        if (account == null || digestPwd(param.pwd) != account.pwd) Res.error(msg = "账号或者密码错误")
        else Res.success(generateToken(account.toInfo()))
    }

    override suspend fun auth(param: AuthParam): Res<Unit> {
        return verifyToken(param.token).result()
    }

    override suspend fun register(param: RegisterParam): Res<String> = db.transaction {
        // TODO: 验证码邮箱是否存在或者验证码正确

        val uid = generateUID()
        val pwd = digestPwd(param.pwd)
        val result = AccountDao.add(uid, param.email, pwd)

        if (result) Res.success(generateToken(AccountInfo(uid, null, null)))
        else Res.error(msg = "注册失败，请联系开发人员")
    }

    override suspend fun sendFriendRequest(token: String, param: FriendRequestParam): Res<Unit> = db.transaction {
        if (!FriendRequests.verifyMsg(param.msg))
            return@transaction Res.error(CommonStatus.ParamErr, "验证信息不能大于 255 字符")

        val account = Jwt.parse(token).payload.account
        if (!AccountDao.existsUid(param.uid)) return@transaction Res.error(AccountStatus.AccountNotExists)

        val record = FriendRequestDao.last(account.uid, param.uid)
        if (record != null && record.status == FriendRequestStatus.REQUEST && record.isValid()) {
            return@transaction Res.error(msg = "请等待对方验证")
        }
        // TODO: 发送好友申请推送
        FriendRequestDao.add(account.uid, param.uid, param.msg.trim()).result()
    }

    override suspend fun getFriendList(token: String): Res<List<AccountInfo>> {
        val account = Jwt.parse(token).payload.account

        return db.transaction {
            FriendDao.selectListByUID(account.uid)
        }.result()
    }

    override suspend fun acceptFriendRequest(token: String, param: FriendAcceptParam): Res<Unit> = db.transaction {
        val account = Jwt.parse(token).payload.account

        val record =
            FriendRequestDao.selectById(param.id) ?: return@transaction Res.error(CommonStatus.ParamErr, "请求不存在")

        // 验证是否有操作权限
        if (record.requestUID != account.uid) return@transaction Res.error(CommonStatus.Unauthorized)

        // 验证是否已处理或者已过期
        if (record.status != FriendRequestStatus.REQUEST || !record.isValid())
            return@transaction Res.error(msg = "请求已过期，请邀请好友重新申请")

        val result = FriendRequestDao.updateStatusById(FriendRequestStatus.ACCEPT, record.id)
        if (result) {
            if (!FriendDao.exists(record.createUID, record.requestUID))
                FriendDao.add(record.createUID, record.requestUID)
            if (!FriendDao.exists(record.requestUID, record.createUID))
                FriendDao.add(record.requestUID, record.createUID)
        }

        result.result()
    }

    override suspend fun rejectFriendRequest(token: String, param: FriendRejectParam): Res<Unit> = db.transaction {
        val account = Jwt.parse(token).payload.account

        val record =
            FriendRequestDao.selectById(param.id) ?: return@transaction Res.error(CommonStatus.ParamErr, "请求不存在")

        // 验证是否有操作权限
        if (record.requestUID != account.uid) return@transaction Res.error(CommonStatus.Unauthorized)

        // 验证是否已处理或者已过期
        if (record.status != FriendRequestStatus.REQUEST || !record.isValid())
            return@transaction Res.error(msg = "请求已过期")

        val result = FriendRequestDao.updateStatusById(FriendRequestStatus.REJECT, record.id)

        result.result()
    }

    override suspend fun getFriendRequestList(token: String, offset: String): Res<List<FriendRequestVO>> {
        val account = Jwt.parse(token).payload.account

        return db.transaction {
            FriendRequestDao.selectList(account.uid, offset.toLongOrNull() ?: 0)
        }.result()
    }

    private fun digestPwd(pwd: String): String {
        return DigestUtils.sha3_512Hex(pwd + pwdSecret)
    }

    private fun verifyToken(token: String): Boolean {
        val arr = token.split('.')
        if (arr.size != 3) return false
        return tokenHmacUtils.hmacHex("${arr[0]}.${arr[1]}") == arr[2]
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun generateToken(accountInfo: AccountInfo): String {
        val header = Base64.encode(Json.encodeToString(Jwt.Header("HS256", "JWT")).toByteArray())
        val payload = Base64.encode(
            Json.encodeToString(
                Jwt.Payload(
                    Clock.System.now().plus(30.days).toLocalDateTime(TimeZone.UTC),
                    accountInfo,
                )
            ).toByteArray()
        )

        val signature = tokenHmacUtils.hmacHex("$header.$payload")

        return "$header.$payload.$signature"
    }

    private fun generateUID(): Long {
        val incr = redis.incr("uid:generate")
        val length = incr.toString().length
        val max = 10.0.pow(length).toInt()
        return incr * max + Random.nextInt(max)
    }

    companion object
}