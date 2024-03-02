package icu.twtool.chat

import icu.twtool.chat.dao.AccountDao
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.jwt.Jwt
import icu.twtool.chat.server.account.param.AuthParam
import icu.twtool.chat.server.account.param.LoginParam
import icu.twtool.chat.server.account.param.RegisterParam
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.common.Res
import icu.twtool.chat.server.common.datetime.now
import icu.twtool.chat.server.common.result
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.applicationOr
import icu.twtool.ktor.cloud.redis.incr
import icu.twtool.ktor.cloud.redis.redis
import icu.twtool.ktor.cloud.route.service.annotation.ServiceImpl
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
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

    override suspend fun login(param: LoginParam): Res<String> {
        val uid = param.principal.toLongOrNull()
        val account = if (uid == null) AccountDao.selectByEmail(param.principal) else AccountDao.selectByUid(uid)
        if (account == null || digestPwd(param.pwd) != account.pwd) return Res.error(msg = "账号或者密码错误")
        return Res.success(generateToken(account.toInfo()))
    }

    override suspend fun auth(param: AuthParam): Res<Unit> {
        return verifyToken(param.token).result()
    }

    override suspend fun register(param: RegisterParam): Res<String> {
        // TODO: 验证码邮箱是否存在或者验证码正确

        val uid = generateUID()
        val pwd = digestPwd(param.pwd)
        val result = AccountDao.add(uid, param.email, pwd)

        return if (result) Res.success(
            generateToken(AccountInfo(uid, null, null))
        )
        else Res.error(msg = "注册失败，请联系开发人员")
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