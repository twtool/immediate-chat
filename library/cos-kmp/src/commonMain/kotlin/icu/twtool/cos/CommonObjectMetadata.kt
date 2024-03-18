package icu.twtool.cos

/**
 * ObjectMetadata 类用于记录对象的元信息，其主要成员说明如下：
 *
 * httpExpiresDate 缓存的超时时间，为 HTTP 响应头部中 Expires 字段的值。 Date
 * ongoingRestore 正在从归档存储类型恢复该对象。 Boolean
 * userMetadata 前缀为 x-cos-meta- 的用户自定义元信息。 Map<String, String>
 * metadata 除用户自定义元信息以外的其他头部。Map<String, String>
 * restoreExpirationTime 归档对象恢复副本的过期时间。Date
 */
data class CommonObjectMetadata(
    val contentLength: Long? = null,
    val metadata: Map<String, String> = hashMapOf()
)
