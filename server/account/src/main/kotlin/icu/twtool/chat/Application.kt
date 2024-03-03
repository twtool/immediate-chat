package icu.twtool.chat

import icu.twtool.chat.tables.Accounts
import icu.twtool.chat.tables.FriendRequests
import icu.twtool.chat.tables.Friends
import icu.twtool.ktor.cloud.KtorCloudApplication
import icu.twtool.ktor.cloud.discovery.polaris.PolarisRegistry
import icu.twtool.ktor.cloud.exposed.initExposed
import icu.twtool.ktor.cloud.redis.initRedis
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.SchemaUtils

fun main() {
    KtorCloudApplication.start(Netty) {
        install(PolarisRegistry())

        initExposed {
            SchemaUtils.createMissingTablesAndColumns(
                Accounts,
                Friends,
                FriendRequests
            )
        }
        initRedis()

        AccountServiceImpl(this).register()
    }
}