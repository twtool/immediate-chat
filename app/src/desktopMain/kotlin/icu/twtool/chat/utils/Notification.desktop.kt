package icu.twtool.chat.utils

import androidx.compose.ui.window.TrayState
import icu.twtool.chat.server.account.vo.AccountInfo
import icu.twtool.chat.server.chat.vo.MessageVO

typealias WindowNotification = androidx.compose.ui.window.Notification

class DesktopNotification(private val trayState: TrayState) : Notification {

    override suspend fun message(info: AccountInfo, message: MessageVO) {
//        trayState.sendNotification(WindowNotification(
//            title = info.nickname ?: "未命名用户",
//            message = run {
//                when(val content = message.content) {
//                    is PlainMessageContent -> content.value
//                }
//            },
//        ))
    }

    override suspend fun notify(notificationId: Int, title: String, content: String) {

    }
}