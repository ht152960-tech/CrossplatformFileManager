package com.example.cross_platformfilemanager.data.db

import android.content.Context
import com.example.cross_platformfilemanager.data.adapter.DefaultTaggoIdGenerator
import com.example.cross_platformfilemanager.data.adapter.SystemTaggoClock
import com.example.cross_platformfilemanager.data.service.TaggoFileImportService

object AndroidTaggoDatabaseProvider {
    private var fileImportService: TaggoFileImportService? = null

    fun getFileImportService(context: Context): TaggoFileImportService =
        fileImportService ?: createFileImportService(context.applicationContext).also {
            fileImportService = it
        }

    private fun createFileImportService(context: Context): TaggoFileImportService {
        val database = createTaggoDatabase(AndroidDatabaseDriverFactory(context))
        val repositories = createTaggoDatabaseRepositories(database)
        return TaggoFileImportService(
            fileEntries = repositories.fileEntries,
            tags = repositories.tags,
            idGenerator = DefaultTaggoIdGenerator,
            clock = SystemTaggoClock,
            platform = "android",
        )
    }
}
