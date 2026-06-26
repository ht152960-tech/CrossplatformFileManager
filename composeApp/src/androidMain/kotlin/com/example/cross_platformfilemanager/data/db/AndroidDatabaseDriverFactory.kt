package com.example.cross_platformfilemanager.data.db

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class AndroidDatabaseDriverFactory(
    private val context: Context,
) : DatabaseDriverFactory {
    override fun createDriver() =
        AndroidSqliteDriver(
            schema = TaggoDatabase.Schema,
            context = context,
            name = "taggo.db",
        )
}