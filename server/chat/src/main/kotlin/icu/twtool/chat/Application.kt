package icu.twtool.chat

import icu.twtool.chat.server.account.interceptor.installTokenInterceptor
import icu.twtool.chat.server.common.BizException
import icu.twtool.chat.server.common.Res
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.discovery.polaris.PolarisRegistry
import icu.twtool.ktor.cloud.exposed.initExposed
import icu.twtool.ktor.cloud.plugin.rocketmq.RocketMQPlugin
import icu.twtool.ktor.cloud.plugin.rocketmq.filterTag
import icu.twtool.ktor.cloud.redis.initRedis
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.ApplicationStopPreparing
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
        val rocketMQPlugin = RocketMQPlugin()
        install(rocketMQPlugin)

//        initExposed {
//            SchemaUtils.createMissingTablesAndColumns(
//            )
//        }
//        initRedis()

        installTokenInterceptor()


        val chatService = ChatServiceImpl(rocketMQPlugin)

        chatService.register()

        application.environment.monitor.subscribe(ApplicationStopPreparing) {
            chatService.close()
        }
    }
}