package icu.twtool.chat

import icu.twtool.chat.dao.AccountDao
import icu.twtool.chat.dao.FriendDao
import icu.twtool.chat.dao.FriendRequestDao
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.interceptor.loggedUID
import icu.twtool.chat.server.account.jwt.Jwt
import icu.twtool.chat.server.account.model.FriendRequestStatus
import icu.twtool.chat.server.account.param.AuthParam
import icu.twtool.chat.server.account.param.CheckFriendParam
import icu.twtool.chat.server.account.param.FriendAcceptParam
import icu.twtool.chat.server.account.param.FriendRejectParam
import icu.twtool.chat.server.account.param.FriendRequestParam
import icu.twtool.chat.server.account.param.LoginParam
import icu.twtool.chat.server.account.param.RegisterParam
import icu.twtool.chat.server.account.param.UpdateInfoParam
import icu.twtool.chat.server.account.status.AccountStatus
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.account.vo.FriendRequestVO
import icu.twtool.chat.server.common.CommonStatus
import icu.twtool.chat.server.common.Res
import icu.twtool.chat.server.common.assertNotNull
import icu.twtool.chat.server.common.assertTrue
import icu.twtool.chat.server.common.checkParam
import icu.twtool.chat.server.common.result
import icu.twtool.chat.server.dynamic.constants.DYNAMIC_TIMELINE_HANDLE_TOPIC
import icu.twtool.chat.server.dynamic.meesage.AddFriendEvent
import icu.twtool.chat.server.dynamic.meesage.TimelineEvent
import icu.twtool.chat.server.gateway.topic.FriendRequestPushMessage
import icu.twtool.chat.server.gateway.topic.PUSH_MESSAGE_TOPIC
import icu.twtool.chat.server.gateway.topic.PushMessage
import icu.twtool.chat.tables.FriendRequests
import icu.twtool.ktor.cloud.JSON
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.exposed.database
import icu.twtool.ktor.cloud.exposed.transaction
import icu.twtool.ktor.cloud.plugin.rocketmq.RocketMQPlugin
import icu.twtool.ktor.cloud.plugin.rocketmq.buildMessage
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
import java.io.Closeable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@ServiceImpl
class AccountServiceImpl(application: KtorCloudApplication, rocketMQPlugin: RocketMQPlugin) : AccountService,
    Closeable {

    private val pwdSecret = application.config[PwdSecretKey]
    private val tokenSecret = application.config[TokenSecretKey]
    private val tokenHmacUtils = HmacUtils(HmacAlgorithms.HMAC_SHA_256, tokenSecret)

    private val redis = application.redis
    private val db = application.database

    private val rocketMQProducer =
        rocketMQPlugin.getProducer(arrayOf(PUSH_MESSAGE_TOPIC, DYNAMIC_TIMELINE_HANDLE_TOPIC))

    override fun close() {
        rocketMQProducer.close()
    }

    override suspend fun login(param: LoginParam): Res<String> = db.transaction {
        val uid = param.principal.toLongOrNull()
        val account = if (uid == null) AccountDao.selectByEmail(param.principal) else AccountDao.selectByUid(uid)
        if (account == null || digestPwd(param.pwd) != account.pwd) Res.error(msg = "账号或者密码错误")
        else Res.success(generateToken(account.toInfo()))
    }

    override suspend fun auth(param: AuthParam): Res<Long> {
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

    override suspend fun checkFriend(param: CheckFriendParam): Res<Boolean> {
        val loggedUID = loggedUID()
        return Res.success(db.transaction {
            FriendDao.exists(loggedUID, param.uid)
        })
    }

    override suspend fun updateInfo(param: UpdateInfoParam): Res<AccountInfo> {
        checkParam((param.nickname?.length ?: 0) <= 16) { "用户昵称需要小于 16 个字符" }
        val loggedUID = loggedUID()

        val info = AccountInfo(
            uid = loggedUID,
            nickname = param.nickname,
            avatarUrl = param.avatarUrl
        )

        val result = db.transaction { AccountDao.updateInfoByUID(info) }

        return if (result) Res.success(info)
        else Res.error()
    }

    override suspend fun getInfoByUID(uid: String): Res<AccountInfo> {
        val uidLong = uid.toLongOrNull() ?: return Res.error(CommonStatus.ParamErr)
        return db.transaction { AccountDao.selectByUid(uidLong)?.toInfo() }.result()
    }

    override suspend fun sendFriendRequest(param: FriendRequestParam): Res<Unit> {
        checkParam(FriendRequests.verifyMsg(param.msg)) { "验证信息不能大于 255 字符" }
        val loggedUID = loggedUID()

        return db.transaction {
            checkParam(!FriendDao.exists(loggedUID, param.uid)) { "对方已是好友" }
            val nickname = assertNotNull(AccountDao.selectNicknameByUid(param.uid), AccountStatus.AccountNotExists)

            val record = FriendRequestDao.last(loggedUID, param.uid)
            if (record != null && record.status == FriendRequestStatus.REQUEST && record.isValid()) {
                return@transaction Res.error(msg = "请等待对方验证")
            }

            val msg = param.msg.trim()
            val id = FriendRequestDao.add(loggedUID, param.uid, msg)
            rocketMQProducer.send(
                buildMessage(
                    PUSH_MESSAGE_TOPIC,
                    JSON.encodeToString<PushMessage>(FriendRequestPushMessage(id, nickname, msg, param.uid))
                        .toByteArray(),
                    keys = arrayOf(loggedUID.toString())
                )
            )
            Res.success()
        }
    }

    override suspend fun getFriendList(): Res<List<AccountInfo>> {
        val loggedUID = loggedUID()

        return db.transaction {
            FriendDao.selectListByUID(loggedUID)
        }.result()
    }

    override suspend fun getFriendUIDList(uid: String): Res<List<Long>> {
        return db.transaction {
            FriendDao.selectUIDListByUID(uid.toLong())
        }.result()
    }

    override suspend fun acceptFriendRequest(param: FriendAcceptParam): Res<Unit> {
        val loggedUID = loggedUID()

        return db.transaction {
            val record =
                FriendRequestDao.selectById(param.id) ?: return@transaction Res.error(
                    CommonStatus.ParamErr,
                    "请求不存在"
                )

            // 验证是否有操作权限
            assertTrue(record.requestUID == loggedUID, CommonStatus.Unauthorized)

            // 验证是否已处理或者已过期
            assertTrue(record.status == FriendRequestStatus.REQUEST && record.isValid()) { "请求已过期，请邀请好友重新申请" }

            val result = FriendRequestDao.updateStatusById(FriendRequestStatus.ACCEPT, record.id)
            if (result) {
                if (!FriendDao.exists(record.createUID, record.requestUID))
                    FriendDao.add(record.createUID, record.requestUID)
                if (!FriendDao.exists(record.requestUID, record.createUID))
                    FriendDao.add(record.requestUID, record.createUID)

                rocketMQProducer.send(
                    buildMessage(
                        DYNAMIC_TIMELINE_HANDLE_TOPIC,
                        JSON.encodeToString<TimelineEvent>(AddFriendEvent(record.createUID, record.requestUID))
                            .toByteArray(),
                        messageGroup = "${record.createUID}",
                        keys = arrayOf(loggedUID.toString())
                    )
                )

                rocketMQProducer.send(
                    buildMessage(
                        DYNAMIC_TIMELINE_HANDLE_TOPIC,
                        JSON.encodeToString<TimelineEvent>(AddFriendEvent(record.requestUID, record.createUID))
                            .toByteArray(),
                        messageGroup = "${record.requestUID}",
                        keys = arrayOf(loggedUID.toString())
                    )
                )
            }

            result.result()
        }
    }

    override suspend fun rejectFriendRequest(param: FriendRejectParam): Res<Unit> {
        val loggedUID = loggedUID()

        return db.transaction {
            val record = assertNotNull(FriendRequestDao.selectById(param.id)) { "请求不存在" }

            // 验证是否有操作权限
            assertTrue(record.requestUID == loggedUID, CommonStatus.Unauthorized)

            // 验证是否已处理或者已过期
            assertTrue(record.status == FriendRequestStatus.REQUEST && record.isValid()) { "请求已过期" }

            val result = FriendRequestDao.updateStatusById(FriendRequestStatus.REJECT, record.id)

            assertTrue(result)

            Res.success()
        }
    }

    override suspend fun getFriendRequestList(offset: String): Res<List<FriendRequestVO>> {
        val loggedUID = loggedUID()

        return db.transaction {
            FriendRequestDao.selectList(loggedUID, offset.toLongOrNull() ?: 0)
        }.result()
    }

    private fun digestPwd(pwd: String): String {
        return DigestUtils.sha3_512Hex(pwd + pwdSecret)
    }

    private fun verifyToken(token: String): Long? {
        val arr = token.split('.')
        if (arr.size != 3) return null
        val result = tokenHmacUtils.hmacHex("${arr[0]}.${arr[1]}") == arr[2]
        if (!result) return null
        return Jwt.parse(token).payload.uid
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun generateToken(accountInfo: AccountInfo): String {
        val header = Base64.encode(Json.encodeToString(Jwt.Header("HS256", "JWT")).toByteArray())
        val payload = Base64.encode(
            Json.encodeToString(
                Jwt.Payload(
                    Clock.System.now().plus(30.days).toLocalDateTime(TimeZone.UTC),
                    accountInfo.uid,
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