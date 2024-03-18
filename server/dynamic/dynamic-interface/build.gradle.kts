plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

kotlin {
    jvm("desktop")

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }

        publishLibraryVariants("release")
    }

    sourceSets {

        commonMain.dependencies {
            api(projects.server.commonInterface)
            api(projects.server.account.accountInterface)

            api(libs.ktor.cloud.http.core)
        }
    }
}

dependencies {
    add("kspDesktop", libs.ktor.cloud.http.ksp)
    add("kspAndroid", libs.ktor.cloud.http.ksp)
}

android {
    namespace = "icu.twtool.chat.server.dynamic"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}