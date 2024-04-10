package icu.twtool.chat.server.notify.topic

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val NOTIFY_MESSAGE_TOPIC = "NOTIFY_MESSAGE_TOPIC"

// 通知方式
enum class NotifyMode {
    EMAIL,  // 邮件
    SMS,    // 短信
}


@Serializable
sealed class NotifyMessage {

    abstract val address: String
    abstract val mode: NotifyMode

    @Serializable
    data class Captcha(
        override val address: String,
        override val mode: NotifyMode,

        val action: String,
        val captcha: String,
        val effectiveTime: String,
    ) : NotifyMessage()

}

fun main() {
    println(Json.decodeFromString<NotifyMessage>("{\"type\":\"icu.twtool.chat.server.notify.topic.NotifyMessage.Captcha\",\"address\":\"1878572503@qq.com\",\"mode\":\"EMAIL\",\"action\":\"注册\",\"captcha\":\"466077\",\"effectiveTime\":\"5 分钟\"}"))
}