package icu.twtool.chat

import android.app.Application
import icu.twtool.chat.constants.COS_CONFIG
import icu.twtool.chat.database.DriverFactory
import icu.twtool.cos.AndroidCosClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DriverFactory.initialize(this)
        AndroidCosClient.initialize(this, COS_CONFIG)
    }
}