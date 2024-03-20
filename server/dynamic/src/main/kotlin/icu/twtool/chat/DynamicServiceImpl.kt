package icu.twtool.chat

import icu.twtool.chat.dao.DynamicAttachmentDao
import icu.twtool.chat.dao.DynamicCommentDao
import icu.twtool.chat.dao.DynamicDao
import icu.twtool.chat.dao.DynamicLikeDao
import icu.twtool.chat.dao.TimelineDao
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.interceptor.loggedUID
import icu.twtool.chat.server.common.Res
import icu.twtool.chat.server.common.checkParam
import icu.twtool.chat.server.common.datetime.nowUTC
import icu.twtool.chat.server.common.page.PageVO
import icu.twtool.chat.server.common.result
import icu.twtool.chat.server.dynamic.DynamicService
import icu.twtool.chat.server.dynamic.constants.DYNAMIC_TIMELINE_HANDLE_TOPIC
import icu.twtool.chat.server.dynamic.meesage.PublishDynamicEvent
import icu.twtool.chat.server.dynamic.meesage.TimelineEvent
import icu.twtool.chat.server.dynamic.model.Timeline
import icu.twtool.chat.server.dynamic.param.CommentDynamicParam
import icu.twtool.chat.server.dynamic.param.GetTimelinePageParam
import icu.twtool.chat.server.dynamic.param.LikeDynamicParam
import icu.twtool.chat.server.dynamic.param.PublishDynamicParam
import icu.twtool.chat.server.dynamic.vo.DynamicDetailsVO
import icu.twtool.chat.tables.DynamicComments
import icu.twtool.chat.tables.Dynamics
import icu.twtool.ktor.cloud.JSON
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.client.service.getService
import icu.twtool.ktor.cloud.exposed.database
import icu.twtool.ktor.cloud.exposed.transaction
import icu.twtool.ktor.cloud.plugin.rocketmq.RocketMQPlugin
import icu.twtool.ktor.cloud.plugin.rocketmq.buildMessage
import icu.twtool.ktor.cloud.plugin.rocketmq.filterTag
import icu.twtool.ktor.cloud.redis.lock
import icu.twtool.ktor.cloud.redis.redis
import icu.twtool.ktor.cloud.route.service.annotation.ServiceImpl
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import org.apache.rocketmq.client.apis.consumer.ConsumeResult
import org.slf4j.LoggerFactory
import java.io.Closeable

@ServiceImpl
class DynamicServiceImpl(
    application: KtorCloudApplication,
    rocketMQPlugin: RocketMQPlugin
) : DynamicService, Closeable {

    private val log = LoggerFactory.getLogger(DynamicServiceImpl::class.java)

    private val db = application.database
    private val redis = application.redis

    private val accountService: AccountService by lazy { application.getService() }

    private val timelineHandleProducer = rocketMQPlugin.getProducer(arrayOf(DYNAMIC_TIMELINE_HANDLE_TOPIC))
    private val timelineHandlePushCustomer = rocketMQPlugin.getPushConsumer(
        mapOf(DYNAMIC_TIMELINE_HANDLE_TOPIC to filterTag())
    ) {
        try {
            runBlocking {
                val data = ByteArray(it.body.remaining())
                it.body.get(data)
                val messageStr = String(data)
                if (log.isDebugEnabled)
                    log.debug("consume message: {}", messageStr)
                val message = try {
                    JSON.decodeFromString<TimelineEvent>(messageStr)
                } catch (e: Throwable) {
                    log.error("decoding message exception", e)
                    return@runBlocking ConsumeResult.SUCCESS
                }
                when (message) {
                    is PublishDynamicEvent -> {
                        // 加分布式锁
                        val lockKey = "timeline:handle:uid:${message.uid}"
                        val success = redis.lock(lockKey) {
                            val res = accountService.getFriendUIDList(message.uid.toString())
                            if (!res.success) return@lock false
                            val models = mutableListOf(
                                Timeline(message.uid, message.uid, message.id, message.time)
                            )
                            res.data?.forEach {
                                models.add(Timeline(it, message.uid, message.id, message.time))
                            }
                            db.transaction {
                                TimelineDao.batchInsert(models)
                            }
                            true
                        }
                        if (!success) return@runBlocking ConsumeResult.FAILURE
                    }
                }
                ConsumeResult.SUCCESS
            }
        } catch (e: Throwable) {
            log.error("Error while receiving message", e)
            ConsumeResult.FAILURE
        }
    }

    override fun close() {
        timelineHandleProducer.close()
        timelineHandlePushCustomer.close()
    }

    override suspend fun publish(param: PublishDynamicParam): Res<Unit> {
        val loggedUID = loggedUID()
        checkParam(Dynamics.verifyContent(param.content)) { "内容最长为 1024 个字符" }

        db.transaction {
            val time = LocalDateTime.nowUTC()
            val dynamicId = DynamicDao.add(loggedUID, param.content, time)
            DynamicAttachmentDao.batchAdd(dynamicId, param.attachments, time)

            // 推送到 MQ 处理好友和自己的时间线
            timelineHandleProducer.send(
                buildMessage(
                    DYNAMIC_TIMELINE_HANDLE_TOPIC,
                    JSON.encodeToString<TimelineEvent>(PublishDynamicEvent(dynamicId, loggedUID, time)).toByteArray(),
                    messageGroup = "$loggedUID",
                    keys = arrayOf(loggedUID.toString())
                )
            )
        }

        return Res.success()
    }

    override suspend fun getTimelines(param: GetTimelinePageParam): Res<PageVO<DynamicDetailsVO>> {
        val loggedUID = loggedUID()

        return db.transaction {
            TimelineDao.page(loggedUID, param)
        }.result()
    }

    override suspend fun details(dynamicId: String): Res<DynamicDetailsVO> {
        val id = dynamicId.toLongOrNull().run {
            checkParam(this != null)
            this!!
        }

        return db.transaction {
            DynamicDao.detailsById(id)
        }.result()
    }

    override suspend fun like(param: LikeDynamicParam): Res<Unit> {
        val loggedUID = loggedUID()

        return db.transaction {
            if (param.cancel) DynamicLikeDao.cancelLike(param.dynamicId, loggedUID)
            else DynamicLikeDao.addLike(param.dynamicId, loggedUID)
        }.result()
    }

    override suspend fun comment(param: CommentDynamicParam): Res<Unit> {
        checkParam(DynamicComments.verifyContent(param.content)) { "评论长度需要小于 255 个字符" }
        val loggedUID = loggedUID()

        // TODO: 验证是否有动态的访问权限，验证是否是回复的好友

        return db.transaction {
            DynamicCommentDao.add(param.dynamicId, loggedUID, param.content, param.replyId)
        }.result()
    }
}