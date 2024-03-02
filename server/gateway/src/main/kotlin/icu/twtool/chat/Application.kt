package icu.twtool.chat

import icu.twtool.chat.server.account.AccountService
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.discovery.polaris.PolarisRegistry
import icu.twtool.ktor.cloud.route.gateway.route
import io.ktor.server.netty.Netty

fun main() {
    KtorCloudApplication.start(Netty) {
        install(PolarisRegistry())

        route(AccountService::class)
    }
}