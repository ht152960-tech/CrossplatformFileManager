package com.example.cross_platformfilemanager.data.db

fun createTaggoDatabase(
    driverFactory: DatabaseDriverFactory,
): TaggoDatabase = TaggoDatabase(driverFactory.createDriver())
