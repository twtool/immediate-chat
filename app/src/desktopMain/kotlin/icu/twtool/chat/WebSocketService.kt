package icu.twtool.chat

import androidx.compose.ui.window.TrayState
import icu.twtool.chat.state.WebSocketState
import icu.twtool.chat.utils.DesktopNotification

class WebSocketService {

    fun start(trayState: TrayState) {
        WebSocketState.notification = DesktopNotification(trayState)
        WebSocketState.start()
    }

    fun destroy() {
        WebSocketState.destroy()
    }
}