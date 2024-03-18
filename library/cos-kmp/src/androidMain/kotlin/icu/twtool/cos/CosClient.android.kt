package icu.twtool.cos

import android.content.Context
import android.util.Log
import com.tencent.cos.xml.CosXmlService
import com.tencent.cos.xml.CosXmlServiceConfig
import com.tencent.cos.xml.model.`object`.GetObjectBytesRequest
import com.tencent.cos.xml.model.`object`.GetObjectRequest
import com.tencent.cos.xml.model.`object`.PutObjectRequest
import java.io.File
import java.io.InputStream
import kotlin.math.log

object AndroidCosClient : CosClient {

    private const val TAG = "AndroidCosClient"

    private lateinit var service: CosXmlService
    private lateinit var config: CosConfig
    private lateinit var cacheDir: File

    fun initialize(context: Context, config: CosConfig) {
        this.config = config
        val credentialsProvider = CredentialsProviderImpl(config.getTmpCredentials)

        val serviceConfig = CosXmlServiceConfig.Builder()
            .setRegion(config.region)
            .isHttps(true)
            .builder()

        cacheDir = context.externalCacheDir!!
        service = CosXmlService(context, serviceConfig, credentialsProvider)
    }

    override fun putObject(key: String, input: InputStream, metadata: CommonObjectMetadata): String {
        val request = PutObjectRequest(config.bucket, key, input)

        service.putObject(request)
        return key
    }

    override fun getObject(key: String, queryParameter: String?): InputStream? {
        val file = cacheDir.resolve(key)
        val index = file.absolutePath.lastIndexOf('/')
        Log.d(TAG, "getObject: key = $key, queryParameter: = $queryParameter")
        val request = GetObjectRequest(config.bucket, key, file.absolutePath.substring(0, index))

        queryParameter?.let { request.addQuery(it, null) }

        service.getObject(request)
        return file.inputStream()
    }
}

actual fun getCosClient(): CosClient = AndroidCosClient