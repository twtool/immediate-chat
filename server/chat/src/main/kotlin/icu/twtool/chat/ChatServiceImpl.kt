package icu.twtool.chat

import icu.twtool.chat.server.account.interceptor.loggedUID
import icu.twtool.chat.server.chat.ChatService
import icu.twtool.chat.server.chat.constants.CHANNEL_MESSAGE_HANDLE_TOPIC
import icu.twtool.chat.server.chat.constants.SEND_MESSAGE_TO_TOPIC
import icu.twtool.chat.server.chat.model.AccountMessageAddressee
import icu.twtool.chat.server.chat.param.GetMessageRecordParam
import icu.twtool.chat.server.chat.param.SendMessageParam
import icu.twtool.chat.server.chat.vo.MessageVO
import icu.twtool.chat.server.common.Res
import icu.twtool.chat.server.common.datetime.epochSeconds
import icu.twtool.chat.server.common.datetime.nowUTC
import icu.twtool.ktor.cloud.JSON
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.plugin.rocketmq.RocketMQPlugin
import icu.twtool.ktor.cloud.plugin.rocketmq.buildMessage
import icu.twtool.ktor.cloud.plugin.rocketmq.filterTag
import icu.twtool.ktor.cloud.redis.expire
import icu.twtool.ktor.cloud.redis.incr
import icu.twtool.ktor.cloud.redis.redis
import icu.twtool.ktor.cloud.redis.zadd
import icu.twtool.ktor.cloud.redis.zrangeByScore
import icu.twtool.ktor.cloud.redis.zremrangeByScore
import icu.twtool.ktor.cloud.route.service.annotation.ServiceImpl
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import org.apache.rocketmq.client.apis.consumer.ConsumeResult
import org.slf4j.LoggerFactory
import java.io.Closeable
import kotlin.time.Duration.Companion.days

@ServiceImpl
class ChatServiceImpl(application: KtorCloudApplication, rocketMQPlugin: RocketMQPlugin) : ChatService, Closeable {

    private val log = LoggerFactory.getLogger(ChatServiceImpl::class.java)

    private val redis = application.redis

    private val sendMessageToProducer = rocketMQPlugin.getProducer(arrayOf(SEND_MESSAGE_TO_TOPIC))

    private val channelMessageHandleConsumer =
        rocketMQPlugin.getPushConsumer(mapOf(CHANNEL_MESSAGE_HANDLE_TOPIC to filterTag("*"))) {
            // 获取群用户列表，向 RocketMQ 推送数据
            ConsumeResult.FAILURE
        }

    override suspend fun getMessageRecord(param: GetMessageRecordParam): Res<List<MessageVO>> {
        val loggerUID = loggedUID()

        val messageKey = "message:uid:${loggerUID}"

        val minScore = param.lastEpochSeconds.toDouble()
        val maxScore = param.currentEpochSeconds.toDouble()
        return Res.success(redis.zrangeByScore(messageKey, minScore, maxScore).map {
            JSON.decodeFromString<MessageVO>(it)
        })
    }

    override suspend fun sendMessage(param: SendMessageParam): Res<Unit> {
        val loggedUID = loggedUID()

        val now = LocalDateTime.nowUTC()

        val epochSeconds = now.epochSeconds()
        val idKey = "uid:$loggedUID:message-id"
        val id = redis.incr(idKey)

        when (param.addressee) {
            is AccountMessageAddressee -> {
                // TODO: 判断是否是好友
                val addressee = (param.addressee as AccountMessageAddressee).uid
                val body = JSON.encodeToString(
                    MessageVO(
                        loggedUID,
                        addressee,
                        param.addressee,
                        param.content,
                        now,

                        id = id
                    )
                ).apply {
                    val messageKey = "message:uid:${addressee}"
                    redis.zadd(messageKey, epochSeconds.toDouble(), this)
                    redis.zremrangeByScore(messageKey, 0.0, (epochSeconds - 7.days.inWholeSeconds).toDouble())
                    redis.expire(messageKey, 7.days.inWholeSeconds)
                }.toByteArray()
                sendMessageToProducer.send(
                    buildMessage(
                        SEND_MESSAGE_TO_TOPIC,
                        body,
                        keys = arrayOf(loggedUID.toString())
                    )
                )

                return Res.success()
            }
        }
    }

    override fun close() {
        channelMessageHandleConsumer.close()
        sendMessageToProducer.close()
    }
}