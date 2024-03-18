import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.distsDirectory

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktor)

    application
}

version = "0.0.1"

application {
    mainClass.set("icu.twtool.chat.ApplicationKt")
}

dependencies {
    implementation(projects.server.cos.cosInterface)
    implementation(projects.server.account.accountInterceptor)

    implementation(libs.ktor.cloud.application)
    implementation(libs.ktor.cloud.discovery.polaris)
    implementation(libs.ktor.cloud.route.service)
    implementation(libs.ktor.server.netty)
    implementation(libs.qcloud.cos.sts.api)
    implementation(libs.jackson.databing)

    ksp(libs.ktor.cloud.route.service.ksp)
}

tasks.create("upload") {
    dependsOn("distTar")
    doLast {
        exec {
            commandLine(
                "scp",
                distsDirectory.file(project.name + "-" + project.version + ".tar").get().asFile.absolutePath,
                "root@cloud.pc:/root/workspace/immediate-chat/${project.name}/"
            )
        }
    }
}