package icu.twtool.chat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import icu.twtool.chat.state.WebSocketState
import icu.twtool.chat.utils.AndroidNotification

private const val TAG = "WebSocketService"

class WebSocketService : Service() {

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        WebSocketState.notification = AndroidNotification(this)
        WebSocketState.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        WebSocketState.notification = null
        super.onDestroy()
        WebSocketState.destroy()
    }
}