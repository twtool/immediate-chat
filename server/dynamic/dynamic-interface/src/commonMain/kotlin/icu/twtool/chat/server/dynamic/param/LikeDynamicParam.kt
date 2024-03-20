package icu.twtool.chat.server.dynamic.param

import kotlinx.serialization.Serializable

@Serializable
data class LikeDynamicParam(
    val dynamicId: Long,
    val cancel: Boolean = false
)
