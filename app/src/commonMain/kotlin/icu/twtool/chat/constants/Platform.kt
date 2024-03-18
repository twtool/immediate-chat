package icu.twtool.chat.constants

enum class Platform {
    Android, Desktop
}

expect val platform: Platform

fun getPlatform(): Platform = platform