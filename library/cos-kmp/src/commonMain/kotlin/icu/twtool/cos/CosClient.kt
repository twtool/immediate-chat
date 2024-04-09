package icu.twtool.cos

import java.io.InputStream


interface CosClient {

    fun putObject(key: String, input: InputStream, metadata: CommonObjectMetadata): String?

    fun getObject(key: String, queryParameter: String? = null): InputStream?

}

expect fun getCosClient(): CosClient