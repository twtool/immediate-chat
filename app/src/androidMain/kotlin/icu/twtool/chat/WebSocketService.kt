package icu.twtool.chat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import icu.twtool.chat.state.WebSocketState
import icu.twtool.chat.utils.AndroidNotification

class WebSocketService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        WebSocketState.notification = AndroidNotification(this)
        WebSocketState.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        WebSocketState.notification = null
        super.onDestroy()
        WebSocketState.destroy()
    }
}