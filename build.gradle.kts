import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false

    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktor) apply false

    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false

    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.sqldelight) apply false
}

allprojects {
    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(0, TimeUnit.SECONDS)
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs += "-Xcontext-receivers"
        }
    }
}

