package icu.twtool.chat.database

import app.cash.sqldelight.db.SqlDriver

const val DB_NAME = "ic.db"

expect fun createDriver(): SqlDriver

val database: Database by lazy { Database(createDriver()) }
