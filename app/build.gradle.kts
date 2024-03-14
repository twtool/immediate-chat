import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.sqldelight)
}


kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        optIn.add("org.jetbrains.compose.resources.ExperimentalResourceApi")
    }
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.window)

            implementation(libs.sqldelight.driver.android)
        }

        commonMain.dependencies {
            implementation(projects.server.account.accountInterface)
            implementation(projects.server.chat.chatInterface)
            implementation(projects.server.gateway.gatewayInterface)
            implementation(projects.library.cache)
            implementation(projects.library.logger)
            implementation(projects.library.asyncImage)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
//            implementation(compose.material)

            implementation(compose.components.resources)

            implementation(libs.ktor.cloud.client.kmp)
            implementation(libs.ktor.cloud.client.kmp.websocket)
            implementation(libs.sqldelight.runtime)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.logback)
            implementation(libs.sqldelight.driver.sqlite)
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("icu.twtool.chat.database")
        }
    }
}

android {
    namespace = "icu.twtool.chat"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
//    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "icu.twtool.chat"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("immediate-chat") {
            storeFile = file("${System.getProperty("user.home")}/immediate-chat.jks")
            storePassword = "immediate-chat"
            keyAlias = "immediate-chat"
            keyPassword = "immediate-chat"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("immediate-chat")
            isMinifyEnabled = false
            buildConfigField("String", "SERVER_PROTOCOL", "\"https\"")
            buildConfigField("String", "SERVER_WEBSOCKET_PROTOCOL", "\"wss\"")
            buildConfigField("String", "SERVER_HOST", "\"ic.twtool.icu\"")
            buildConfigField("Integer", "SERVER_PORT", "443")
        }
        debug {
            signingConfig = signingConfigs.getByName("immediate-chat")
            buildConfigField("String", "SERVER_WEBSOCKET_PROTOCOL", "\"ws\"")
            buildConfigField("String", "SERVER_PROTOCOL", "\"http\"")
            buildConfigField("String", "SERVER_HOST", "\"10.0.2.2\"")
            buildConfigField("Integer", "SERVER_PORT", "20000")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
//        debugImplementation(libs.compose.ui.tooling)
    }
}


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "icu.twtool.chat"
            packageVersion = "1.0.0"

            // https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md#adding-files-to-packaged-application
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
        }
    }
}