plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(projects.server.account.accountInterface)

    implementation(libs.ktor.cloud.application)
}
