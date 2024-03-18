package icu.twtool.chat

import icu.twtool.chat.server.account.interceptor.installTokenInterceptor
import icu.twtool.chat.server.common.BizException
import icu.twtool.chat.server.common.Res
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.discovery.polaris.PolarisRegistry
import io.ktor.server.netty.Netty
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

        installTokenInterceptor()

        CosServiceImpl(this).register()
    }
}