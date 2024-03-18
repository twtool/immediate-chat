package icu.twtool.chat.server.common.page

import kotlinx.serialization.Serializable

@Serializable
data class PageVO<T>(
    val record: List<T>,
    val currentPage: Int,
    val pageSize: Int,
    var total: Long?
)