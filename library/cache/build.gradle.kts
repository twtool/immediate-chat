plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
}


kotlin {
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
            implementation(libs.mmkv)
            implementation(libs.startup.runtime)
        }

        commonMain.dependencies {
            implementation(projects.library.logger)

            implementation(compose.runtime)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.slf4j.api)
        }

        desktopMain.dependencies {
            implementation(libs.mmkv.jvm)
        }
    }
}

android {
    namespace = "icu.twtool.cache"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}