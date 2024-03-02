plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)

    application
}

application {
    mainClass.set("icu.twtool.chat.ApplicationKt")
}

dependencies {
    implementation(projects.server.account.accountInterface)

    implementation(libs.ktor.cloud.application)
    implementation(libs.ktor.cloud.discovery.polaris)
    implementation(libs.ktor.cloud.route.gateway)

    implementation(libs.ktor.server.netty)
}
