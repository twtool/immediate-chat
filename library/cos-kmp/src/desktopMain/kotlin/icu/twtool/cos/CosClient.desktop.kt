package icu.twtool.cos

import com.qcloud.cos.COSClient
import com.qcloud.cos.ClientConfig
import com.qcloud.cos.http.HttpProtocol
import com.qcloud.cos.model.GetObjectRequest
import com.qcloud.cos.model.PutObjectRequest
import com.qcloud.cos.region.Region
import org.slf4j.LoggerFactory
import java.io.InputStream

object DesktopCosClient : CosClient {

    private val log = LoggerFactory.getLogger(DesktopCosClient::class.java)

    private lateinit var client: COSClient
    private lateinit var config: CosConfig

    fun initialize(config: CosConfig) {
        this.config = config
        val credentialsProvider = CredentialsProviderImpl(config.getTmpCredentials)

        val clientConfig = ClientConfig(Region(config.region))
        clientConfig.httpProtocol = HttpProtocol.https

        client = COSClient(credentialsProvider, clientConfig)
    }

    fun close() {
        client.shutdown()
    }

    override fun putObject(key: String, input: InputStream, metadata: CommonObjectMetadata): String? {
        val request = PutObjectRequest(config.bucket, key, input, metadata.toObjectMetadata())

        if (runCatching {
                client.putObject(request)
            }.isSuccess) return key
        return null
    }

    override fun getObject(key: String, queryParameter: String?): InputStream? {
        log.debug("getObject: key = $key, queryParameter: = $queryParameter")
        val request = GetObjectRequest(config.bucket, key)

        queryParameter?.let { request.putCustomQueryParameter(it, null) }

        return client.getObject(request).objectContent
    }
}

actual fun getCosClient(): CosClient = DesktopCosClient