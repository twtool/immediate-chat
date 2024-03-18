package icu.twtool.chat.server.dynamic.param

import icu.twtool.chat.server.common.page.PageParam
import kotlinx.serialization.Serializable

@Serializable
data class GetTimelinePageParam(
    /**
     * 好友 UID，如果没转则获取所有好友的
     */
    val friendUID: Long? = null
) : PageParam()
