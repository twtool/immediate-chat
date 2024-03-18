package icu.twtool.cos

import com.qcloud.cos.model.ObjectMetadata

fun CommonObjectMetadata.toObjectMetadata(): ObjectMetadata = ObjectMetadata().let { res ->
    contentLength?.let { res.contentLength = it }
    res
}