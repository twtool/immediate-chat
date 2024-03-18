import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.distsDirectory

plugins {
    alias(libs.plugins.kotlinJvm)

    application
}

version = "0.0.1"

application {
    mainClass.set("icu.twtool.chat.ApplicationKt")
}

dependencies {
    implementation(projects.server.account.accountInterface)
    implementation(projects.server.gateway.gatewayInterface)
    implementation(projects.server.chat.chatInterface)
    implementation(projects.server.dynamic.dynamicInterface)
    implementation(projects.server.cos.cosInterface)

    implementation(libs.ktor.cloud.application)
    implementation(libs.ktor.cloud.discovery.polaris)
    implementation(libs.ktor.cloud.route.gateway)
    implementation(libs.ktor.cloud.client.service)
    implementation(libs.ktor.cloud.route.websocket)
    implementation(libs.ktor.cloud.plugin.rocketmq)

    implementation(libs.ktor.server.netty)
}

//ktor {
//}

//
//tasks.withType<ShadowJar> {
//    this.configurations
//}
//


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