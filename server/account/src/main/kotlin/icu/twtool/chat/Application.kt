package icu.twtool.chat

import icu.twtool.chat.server.account.interceptor.installTokenInterceptor
import icu.twtool.chat.server.common.BizException
import icu.twtool.chat.server.common.Res
import icu.twtool.chat.tables.Accounts
import icu.twtool.chat.tables.FriendRequests
import icu.twtool.chat.tables.Friends
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.discovery.polaris.PolarisRegistry
import icu.twtool.ktor.cloud.exposed.initExposed
import icu.twtool.ktor.cloud.plugin.rocketmq.RocketMQPlugin
import icu.twtool.ktor.cloud.redis.initRedis
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.SchemaUtils
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
        installTokenInterceptor()

        val rocketMQPlugin = RocketMQPlugin()
        install(rocketMQPlugin)

        initExposed {
            SchemaUtils.createMissingTablesAndColumns(
                Accounts,
                Friends,
                FriendRequests
            )
        }
        initRedis()

        val accountService = AccountServiceImpl(this, rocketMQPlugin)
        accountService.register()

        application.environment.monitor.subscribe(ApplicationStopped) {
            accountService.close()
        }
    }
}