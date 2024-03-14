package icu.twtool.chat

import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.create
import icu.twtool.chat.server.chat.ChatService
import icu.twtool.chat.server.common.BizException
import icu.twtool.chat.server.common.Res
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.client.service.ServiceCreator
import icu.twtool.ktor.cloud.discovery.polaris.PolarisRegistry
import icu.twtool.ktor.cloud.plugin.rocketmq.RocketMQPlugin
import icu.twtool.ktor.cloud.route.gateway.route
import icu.twtool.ktor.cloud.route.websockets.WebSocketsPlugin
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory

fun main() {
    val log = LoggerFactory.getLogger("application")

    KtorCloudApplication.start(Netty,
        exceptionHandler = handler@{ err ->
            if (err is BizException) return@handler Res.error(err.status, err.msg)

            log.error(err.message, err)
            Res.error<Unit>()
        }
    ) {
        install(PolarisRegistry())
        install(ServiceCreator(listOf(AccountService::create)))

        val rocketMQPlugin = RocketMQPlugin()
        install(rocketMQPlugin)

        install(WebSocketsPlugin)

        route(AccountService::class)
        route(ChatService::class)

        val service = GatewayWebSocketService(this, rocketMQPlugin)

        application.environment.monitor.subscribe(ApplicationStopped) {
            service.close()
        }
    }
}