package icu.twtool.chat.server.dynamic

import icu.twtool.chat.server.common.Res
import icu.twtool.chat.server.common.page.PageVO
import icu.twtool.chat.server.dynamic.param.CommentDynamicParam
import icu.twtool.chat.server.dynamic.param.GetTimelinePageParam
import icu.twtool.chat.server.dynamic.param.LikeDynamicParam
import icu.twtool.chat.server.dynamic.param.PublishDynamicParam
import icu.twtool.chat.server.dynamic.vo.DynamicDetailsVO
import icu.twtool.ktor.cloud.http.core.HttpMethod
import icu.twtool.ktor.cloud.http.core.IServiceCreator
import icu.twtool.ktor.cloud.http.core.annotation.Body
import icu.twtool.ktor.cloud.http.core.annotation.Query
import icu.twtool.ktor.cloud.http.core.annotation.RequestMapping
import icu.twtool.ktor.cloud.http.core.annotation.Service

@Service("DynamicService", "dynamic")
interface DynamicService {

    @RequestMapping(HttpMethod.Post, "publish")
    suspend fun publish(@Body param: PublishDynamicParam): Res<Unit>

    @RequestMapping(HttpMethod.Post, "timelines")
    suspend fun getTimelines(@Body param: GetTimelinePageParam): Res<PageVO<DynamicDetailsVO>>

    @RequestMapping(HttpMethod.Get, "details")
    suspend fun details(@Query dynamicId: String): Res<DynamicDetailsVO>

    @RequestMapping(HttpMethod.Post, "like")
    suspend fun like(@Body param: LikeDynamicParam): Res<Unit>

    @RequestMapping(HttpMethod.Post, "comment")
    suspend fun comment(@Body param: CommentDynamicParam): Res<Unit>

    @RequestMapping(HttpMethod.Delete, "delete")
    suspend fun delete(@Query dynamicId: String): Res<Unit>

    companion object
}

expect fun DynamicService.Companion.create(creator: IServiceCreator): DynamicService