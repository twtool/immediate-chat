package icu.twtool.chat

import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.create
import icu.twtool.chat.server.account.interceptor.installTokenInterceptor
import icu.twtool.chat.server.common.BizException
import icu.twtool.chat.server.common.Res
import icu.twtool.chat.tables.DynamicAttachments
import icu.twtool.chat.tables.DynamicComments
import icu.twtool.chat.tables.DynamicLikes
import icu.twtool.chat.tables.Dynamics
import icu.twtool.chat.tables.Timelines
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.client.service.ServiceCreator
import icu.twtool.ktor.cloud.discovery.polaris.PolarisRegistry
import icu.twtool.ktor.cloud.exposed.initExposed
import icu.twtool.ktor.cloud.opentelemetry.OpenTelemetryPlugin
import icu.twtool.ktor.cloud.plugin.rocketmq.RocketMQPlugin
import icu.twtool.ktor.cloud.redis.initRedis
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.SchemaUtils
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
        OpenTelemetryPlugin.install()

        installTokenInterceptor()

        val rocketMQPlugin = RocketMQPlugin()
        install(rocketMQPlugin)

        initExposed {
            SchemaUtils.createMissingTablesAndColumns(
                Dynamics, DynamicAttachments, DynamicLikes, DynamicComments,
                Timelines
            )
        }

        initRedis()

        install(
            ServiceCreator(
                listOf(
                    AccountService::create
                )
            )
        )

        val dynamicService = DynamicServiceImpl(this, rocketMQPlugin)
        dynamicService.register()

        application.environment.monitor.subscribe(ApplicationStopped) {
            dynamicService.close()
        }
    }
}