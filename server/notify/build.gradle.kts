plugins {
    alias(libs.plugins.kotlinJvm)

    application
}

version = "0.0.1"

application {
    mainClass.set("icu.twtool.chat.ApplicationKt")
}

dependencies {
    implementation(projects.server.notify.notifyInterface)

    implementation(libs.ktor.cloud.application)
    implementation(libs.ktor.cloud.discovery.polaris)
    implementation(libs.ktor.cloud.opentelemetry)
    implementation(libs.ktor.cloud.plugin.rocketmq)

    implementation(libs.jakarta.mail)

    implementation(libs.ktor.server.netty)
}