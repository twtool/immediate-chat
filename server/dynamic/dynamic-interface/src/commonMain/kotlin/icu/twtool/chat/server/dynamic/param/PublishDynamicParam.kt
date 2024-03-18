package icu.twtool.chat.server.dynamic.param

import kotlinx.serialization.Serializable

/**
 * 发布动态参数
 */
@Serializable
data class PublishDynamicParam(
    /**
     * 动态内容
     */
    val content: String,
    /**
     * 动态附件
     */
    val attachments: List<String>
)
