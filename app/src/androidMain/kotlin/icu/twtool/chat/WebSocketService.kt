package icu.twtool.chat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import icu.twtool.chat.state.WebSocketState

class WebSocketService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        WebSocketState.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketState.destroy()
    }
}