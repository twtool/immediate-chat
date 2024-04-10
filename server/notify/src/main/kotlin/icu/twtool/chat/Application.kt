package icu.twtool.chat

import icu.twtool.chat.handler.CaptchaHandler
import icu.twtool.chat.handler.Handler
import icu.twtool.chat.sender.MailSender
import icu.twtool.chat.server.common.BizException
import icu.twtool.chat.server.common.Res
import icu.twtool.chat.server.notify.topic.NOTIFY_MESSAGE_TOPIC
import icu.twtool.chat.server.notify.topic.NotifyMessage
import icu.twtool.ktor.cloud.JSON
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.discovery.polaris.PolarisRegistry
import icu.twtool.ktor.cloud.opentelemetry.OpenTelemetryPlugin
import icu.twtool.ktor.cloud.plugin.rocketmq.RocketMQPlugin
import icu.twtool.ktor.cloud.plugin.rocketmq.filterTag
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.netty.Netty
import org.apache.rocketmq.client.apis.consumer.ConsumeResult
import org.slf4j.LoggerFactory

fun main() {
    val log = LoggerFactory.getLogger("application")

    KtorCloudApplication.start(
        Netty,
        exceptionHandler = handler@{ err ->
            if (err is BizException) return@handler Res.error(err.status, err.msg)

            log.error(err.message, err)
            Res.error<Unit>()
        }
    ) {
        install(PolarisRegistry())
        val mailSender = MailSender()
        install(mailSender)

        OpenTelemetryPlugin.install()

        val rocketMQPlugin = RocketMQPlugin()
        install(rocketMQPlugin)

        val handlers = listOf<Handler>(
            CaptchaHandler(mailSender)
        )

        val rocketMqConsumer = rocketMQPlugin.getPushConsumer(
            mapOf(NOTIFY_MESSAGE_TOPIC to filterTag())
        ) { mv ->
            log.info("Receive: ${mv.topic}, id = ${mv.messageId}")
            try {
                val result = if (NOTIFY_MESSAGE_TOPIC == mv.topic) {
                    val body = ByteArray(mv.body.remaining())
                    mv.body.get(body)
                    val message = JSON.decodeFromString<NotifyMessage>(String(body))
                    handlers.find { it.isSupport(message) }?.handle(message) ?: false
                } else false
                if (result) {
                    log.info("handle success, id = ${mv.messageId}")
                    ConsumeResult.SUCCESS
                } else ConsumeResult.FAILURE
            } catch (e: Exception) {
                log.error(e.message, e)
                ConsumeResult.FAILURE
            }
        }

        application.environment.monitor.subscribe(ApplicationStopped) {
            rocketMqConsumer.close()
        }
    }
}