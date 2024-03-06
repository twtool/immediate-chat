package icu.twtool.chat

import android.app.Application
import icu.twtool.chat.database.DriverFactory

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DriverFactory.initialize(this)
    }
}