plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktor)

    application
}

application {
    mainClass.set("icu.twtool.chat.ApplicationKt")
}

dependencies {
    implementation(projects.server.account.accountInterface)

    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)

    implementation(libs.ktor.cloud.application)
    implementation(libs.ktor.cloud.discovery.polaris)
    implementation(libs.ktor.cloud.exposed)
    implementation(libs.ktor.cloud.redis)
    implementation(libs.ktor.cloud.route.service)
    implementation(libs.ktor.server.netty)
    implementation(libs.mysql)

    ksp(libs.ktor.cloud.route.service.ksp)
}
