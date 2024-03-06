package icu.twtool.chat.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import icu.twtool.chat.constants.ApplicationDir
import java.io.File

val dbFile: String by lazy { "$ApplicationDir/$DB_NAME" }
val driver: JdbcSqliteDriver by lazy { JdbcSqliteDriver("jdbc:sqlite:$ApplicationDir/$DB_NAME") }

actual fun createDriver(): SqlDriver {
    if (!File(dbFile).exists()) Database.Schema.create(driver)
    return driver
}