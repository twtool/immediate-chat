rootProject.name = "ImmediateChat"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        maven("https://a-w-maven.pkg.coding.net/repository/ktor-cloud/maven/")
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(":app")

include(":library:cache")
include(":library:logger")
include(":library:async-image")
include(":library:cos-kmp")

include(":server:common-interface")
include(":server:account")
include(":server:account:account-interface")
include(":server:account:account-interceptor")
include(":server:gateway")
include(":server:gateway:gateway-interface")
include(":server:chat")
include(":server:chat:chat-interface")
include(":server:dynamic")
include(":server:dynamic:dynamic-interface")
include(":server:cos")
include(":server:cos:cos-interface")
include(":server:notify")
include(":server:notify:notify-interface")