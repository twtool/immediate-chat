package icu.twtool.chat.server.dynamic

import icu.twtool.chat.server.common.Res
import icu.twtool.chat.server.common.page.PageParam
import icu.twtool.chat.server.common.page.PageVO
import icu.twtool.chat.server.dynamic.param.GetTimelinePageParam
import icu.twtool.chat.server.dynamic.param.PublishDynamicParam
import icu.twtool.chat.server.dynamic.vo.DynamicDetailsVO
import icu.twtool.ktor.cloud.http.core.HttpMethod
import icu.twtool.ktor.cloud.http.core.IServiceCreator
import icu.twtool.ktor.cloud.http.core.annotation.Body
import icu.twtool.ktor.cloud.http.core.annotation.RequestMapping
import icu.twtool.ktor.cloud.http.core.annotation.Service

@Service("DynamicService", "dynamic")
interface DynamicService {

    @RequestMapping(HttpMethod.Post, "publish")
    suspend fun publish(@Body param: PublishDynamicParam): Res<Unit>

    @RequestMapping(HttpMethod.Post, "timelines")
    suspend fun getTimelines(@Body param: GetTimelinePageParam): Res<PageVO<DynamicDetailsVO>>

    companion object
}

expect fun DynamicService.Companion.create(creator: IServiceCreator): DynamicService