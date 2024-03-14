package icu.twtool.chat

import icu.twtool.chat.server.account.interceptor.loggedUID
import icu.twtool.chat.server.chat.ChatService
import icu.twtool.chat.server.chat.constants.CHANNEL_MESSAGE_HANDLE_TOPIC
import icu.twtool.chat.server.chat.constants.SEND_MESSAGE_TO_TOPIC
import icu.twtool.chat.server.chat.model.AccountMessageAddressee
import icu.twtool.chat.server.chat.param.SendMessageParam
import icu.twtool.chat.server.chat.vo.MessageVO
import icu.twtool.chat.server.common.Res
import icu.twtool.chat.server.common.datetime.nowUTC
import icu.twtool.ktor.cloud.JSON
import icu.twtool.ktor.cloud.plugin.rocketmq.RocketMQPlugin
import icu.twtool.ktor.cloud.plugin.rocketmq.buildMessage
import icu.twtool.ktor.cloud.plugin.rocketmq.filterTag
import icu.twtool.ktor.cloud.route.service.annotation.ServiceImpl
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import org.apache.rocketmq.client.apis.consumer.ConsumeResult
import org.slf4j.LoggerFactory
import java.io.Closeable

@ServiceImpl
class ChatServiceImpl(rocketMQPlugin: RocketMQPlugin) : ChatService, Closeable {

    private val log = LoggerFactory.getLogger(ChatServiceImpl::class.java)

    private val sendMessageToProducer = rocketMQPlugin.getProducer(arrayOf(SEND_MESSAGE_TO_TOPIC))

    private val channelMessageHandleConsumer =
        rocketMQPlugin.getPushConsumer(mapOf(CHANNEL_MESSAGE_HANDLE_TOPIC to filterTag("*"))) {
            // 获取群用户列表，向 RocketMQ 推送数据
            ConsumeResult.FAILURE
        }

    override suspend fun sendMessage(param: SendMessageParam): Res<Unit> {
        val loggedUID = loggedUID()

        val now = LocalDateTime.nowUTC()

        when (param.addressee) {
            is AccountMessageAddressee -> {
                // TODO: 判断是否是好友
                val body = JSON.encodeToString(
                    MessageVO(
                        loggedUID,
                        (param.addressee as AccountMessageAddressee).uid,
                        param.addressee,
                        param.content,
                        now,
                    )
                ).toByteArray()
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