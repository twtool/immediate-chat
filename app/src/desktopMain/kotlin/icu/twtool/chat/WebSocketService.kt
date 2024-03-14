package icu.twtool.chat

import icu.twtool.chat.state.WebSocketState

class WebSocketService {

    fun start() {
        WebSocketState.start()
    }

    fun destroy() {
        WebSocketState.destroy()
    }
}