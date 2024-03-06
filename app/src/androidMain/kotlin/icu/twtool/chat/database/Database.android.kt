package icu.twtool.chat.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

object DriverFactory {

    lateinit var driver: SqlDriver
        private set

    fun initialize(context: Context) {
        driver = AndroidSqliteDriver(Database.Schema, context, DB_NAME)
    }
}

actual fun createDriver(): SqlDriver {
    return DriverFactory.driver
}