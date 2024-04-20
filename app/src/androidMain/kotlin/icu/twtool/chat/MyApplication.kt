package icu.twtool.chat

import android.app.Application
import icu.twtool.chat.constants.COS_CONFIG
import icu.twtool.chat.constants.CacheDirFile
import icu.twtool.chat.database.DriverFactory
import icu.twtool.chat.utils.initNotification
import icu.twtool.cos.AndroidCosClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CacheDirFile = cacheDir

        DriverFactory.initialize(this)
        AndroidCosClient.initialize(this, COS_CONFIG)

        initNotification(this)
    }
}