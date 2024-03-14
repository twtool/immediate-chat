package icu.twtool.chat.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.logs.LogSqliteDriver
import icu.twtool.chat.constants.ApplicationDir
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

private val log = LoggerFactory.getLogger("Database")

val dbFile: String by lazy { "$ApplicationDir/$DB_NAME" }
val driver: SqlDriver by lazy {
    LogSqliteDriver(JdbcSqliteDriver("jdbc:sqlite:$ApplicationDir/$DB_NAME")) {
        it.lines().forEach(log::info)
    }
}

actual fun createDriver(): SqlDriver {
    if (!File(dbFile).exists()) Database.Schema.create(driver)
    return driver
}