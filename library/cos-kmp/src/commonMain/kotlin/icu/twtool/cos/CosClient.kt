package icu.twtool.cos

import java.io.InputStream

enum class TaskState {
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    CANCELED,
    FAILED,
    UNKNOWN
}

interface TaskResult {

    val path: String

    fun parse()
    fun resume()
    fun cancel()
}

interface CosClient {

    fun putObject(key: String, input: InputStream, metadata: CommonObjectMetadata): String?

    fun getObject(key: String, queryParameter: String? = null): InputStream?

    fun getObject(
        key: String, id: Long, filename: String? = null, extension: String? = null,
        onChangePercentage: (percentage: Float) -> Unit,
        onChangeState: (state: TaskState) -> Unit,
    ): TaskResult
}

expect fun getCosClient(): CosClient