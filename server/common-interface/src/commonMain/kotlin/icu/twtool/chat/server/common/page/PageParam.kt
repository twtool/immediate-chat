package icu.twtool.chat.server.common.page

import kotlinx.serialization.Serializable

/**
 * 获取分页数据参数
 */
@Serializable
open class PageParam {
    /**
     * 当前页面
     */
    var currentPage: Int = 1

    /**
     * 一页的数量
     */
    var pageSize: Int = 10

    /**
     * 计算 Count
     */
    var count: Boolean = true
}