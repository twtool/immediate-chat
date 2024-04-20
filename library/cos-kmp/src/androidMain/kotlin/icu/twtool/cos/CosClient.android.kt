package icu.twtool.cos

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.tencent.cos.xml.CosXmlService
import com.tencent.cos.xml.CosXmlServiceConfig
import com.tencent.cos.xml.model.`object`.GetObjectRequest
import com.tencent.cos.xml.model.`object`.PutObjectRequest
import com.tencent.cos.xml.transfer.COSXMLDownloadTask
import com.tencent.cos.xml.transfer.TransferConfig
import com.tencent.cos.xml.transfer.TransferManager
import com.tencent.cos.xml.transfer.TransferState
import icu.twtool.cache.Cache
import icu.twtool.cache.getCache
import java.io.File
import java.io.InputStream


class AndroidTaskResult(
    private val task: COSXMLDownloadTask,
    override val path: String,
    onChangePercentage: (percentage: Float) -> Unit,
    onChangeState: (state: TaskState) -> Unit,
) : TaskResult {

    private fun mappingState(state: TransferState) = when (state) {
        TransferState.IN_PROGRESS -> TaskState.IN_PROGRESS
        TransferState.PAUSED -> TaskState.PAUSED
        TransferState.COMPLETED -> TaskState.COMPLETED
        TransferState.CANCELED -> TaskState.CANCELED
        TransferState.FAILED -> TaskState.FAILED
        else -> TaskState.UNKNOWN
    }

    init {
        onChangePercentage(0f)
        task.setCosXmlProgressListener { complete, target ->
            onChangePercentage((complete.toFloat() / target.toFloat()) * 100f)
        }
        onChangeState(mappingState(task.taskState))
        task.setTransferStateListener {
            onChangeState(mappingState(it))
        }
    }

    override fun parse() {
        task.pause()
    }

    override fun resume() {
        task.resume()
    }

    override fun cancel() {
        task.cancel()
    }
}


@SuppressLint("StaticFieldLeak")
object AndroidCosClient : CosClient {

    private const val TAG = "AndroidCosClient"

    private lateinit var context: Context
    private lateinit var service: CosXmlService
    private lateinit var config: CosConfig
    private lateinit var cacheDir: File

    private lateinit var transferManager: TransferManager

    private val cache: Cache by lazy { getCache() }

    fun initialize(context: Context, config: CosConfig) {
        this.context = context
        this.config = config
        val credentialsProvider = CredentialsProviderImpl(config.getTmpCredentials)

        val serviceConfig = CosXmlServiceConfig.Builder()
            .setRegion(config.region)
            .isHttps(true)
            .builder()

        cacheDir = context.externalCacheDir!!
        service = CosXmlService(context, serviceConfig, credentialsProvider)

        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        val transferConfig: TransferConfig = TransferConfig.Builder().build()

        //初始化 TransferManager
        transferManager = TransferManager(
            service,
            transferConfig
        )
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

    override fun getObject(
        key: String, id: Long, filename: String?, extension: String?,
        onChangePercentage: (percentage: Float) -> Unit,
        onChangeState: (state: TaskState) -> Unit,
    ): TaskResult {
        val fileName = (filename ?: "").ifBlank { key }
        val file: File = cache.getString("download:$id")?.let { File(it) } ?: cacheDir.run {
            if (!extension.isNullOrBlank()) resolve(extension) else this
        }.let {
            var i = 0
            val suffix = if (!extension.isNullOrBlank()) ".$extension" else ""
            var file: File
            while (true) {
                val offset = if (i++ > 0) "($i)" else ""
                file = it.resolve("$fileName$offset$suffix")
                if (file.exists()) continue
                break
            }
            cache["download:$id"] = file.absolutePath
            file
        }

        Log.d(TAG, "getObject: file = $file, cache = ${cache.getString("download:$id")}")

        // context
        val downloadTask: COSXMLDownloadTask =
            transferManager.download(
                context,
                config.bucket, key, file.parent, file.name
            )

        return AndroidTaskResult(downloadTask, file.absolutePath, onChangePercentage, onChangeState)
    }
}

actual fun getCosClient(): CosClient = AndroidCosClient