package icu.twtool.chat

import com.tencent.cloud.CosStsClient
import com.tencent.cloud.Policy
import com.tencent.cloud.Statement
import com.tencent.cloud.cos.util.Jackson
import icu.twtool.chat.constants.EffectAllow
import icu.twtool.chat.server.account.interceptor.loggedUID
import icu.twtool.chat.server.cos.CosService
import icu.twtool.chat.server.cos.vo.TmpCredentialVO
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.route.service.annotation.ServiceImpl
import org.slf4j.LoggerFactory
import java.util.*

@ServiceImpl
class CosServiceImpl(application: KtorCloudApplication) : CosService {

    private val log = LoggerFactory.getLogger(CosServiceImpl::class.java)

    private val secretId = System.getenv("QCLOUD_COS_SECRET_ID")
    private val secretKey = System.getenv("QCLOUD_COS_SECRET_KEY")

    private val host: String? = application.config[CosHost]
    private val appId: String = application.config[CosAppid]
    private val bucket = application.config[CosBucket]
    private val region = application.config[CosRegion]

    override suspend fun getTmpCredential(): TmpCredentialVO {
        val loggedUID = loggedUID()
        val response = CosStsClient.getCredential(TreeMap<String?, Any?>().apply {
            put("secretId", secretId)
            put("secretKey", secretKey)

            host?.let { put("host", it) }
            put("bucket", bucket)
            put("region", region)

            // 初始化 policy https://cloud.tencent.com/document/product/436/14048
            val policy = Policy()
            // * 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径
            // * 资源表达式规则分对象存储(cos)和数据万象(ci)两种
            // * 数据处理、审核相关接口需要授予ci资源权限
            // *  cos : qcs::cos:{region}:uid/{appid}:{bucket}/{path}
            // *  ci  : qcs::ci:{region}:uid/{appid}:bucket/{bucket}/{path}
            // * 列举几种典型的{path}授权场景：
            // * 1、允许访问所有对象："*"
            // * 2、允许访问指定的对象："a/a1.txt", "b/b1.txt"
            // * 3、允许访问指定前缀的对象："a*", "a/*", "b/*"
            // *  如果填写了“*”，将允许用户访问所有资源；除非业务需要，否则请按照最小权限原则授予用户相应的访问权限范围。
            // * 示例：授权 example-bucket-1250000000 bucket目录下的所有资源给cos和ci 授权两条Resource

            Statement().let { statement ->
                statement.setEffect(EffectAllow)
                statement.addActions(
                    arrayOf(
                        "name/cos:PutObject" // 简单上传
                    )
                )
                statement.addResources(
                    arrayOf(
                        "qcs::cos:${region}:uid/${appId}:${bucket}/res/${loggedUID}/*"
                    )
                )

                policy.addStatement(statement)
            }

            Statement().let { statement ->
                statement.setEffect(EffectAllow)
                statement.addActions(
                    arrayOf(
                        "cos:GetObject" // 简单下载 https://cloud.tencent.com/document/product/436/7753
                    )
                )
                statement.addResources(
                    arrayOf(
                        "qcs::cos:${region}:uid/${appId}:${bucket}/*"
                    )
                )

                policy.addStatement(statement)
            }

            put("policy", Jackson.toJsonPrettyString(policy))

            println(Jackson.toJsonString(this))
        })

        return TmpCredentialVO(
            response.credentials.tmpSecretId,
            response.credentials.tmpSecretKey,
            response.credentials.sessionToken,
            response.startTime,
            response.expiredTime
        )
    }

    companion object
}

