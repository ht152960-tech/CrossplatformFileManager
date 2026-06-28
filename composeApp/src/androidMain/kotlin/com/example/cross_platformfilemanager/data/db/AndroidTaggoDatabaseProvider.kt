package com.example.cross_platformfilemanager.data.db

import android.content.Context
import com.example.cross_platformfilemanager.data.adapter.DefaultTaggoIdGenerator
import com.example.cross_platformfilemanager.data.adapter.SystemTaggoClock
import com.example.cross_platformfilemanager.data.service.TaggoFileImportService
import com.example.cross_platformfilemanager.runtime.TaggoFileRuntimeStore

object AndroidTaggoDatabaseProvider {
    private var runtimeStore: TaggoFileRuntimeStore? = null

    fun getRuntimeStore(context: Context): TaggoFileRuntimeStore =
        runtimeStore ?: createRuntimeStore(context.applicationContext).also {
            runtimeStore = it
        }

    private fun createRuntimeStore(context: Context): TaggoFileRuntimeStore {
        val database = createTaggoDatabase(AndroidDatabaseDriverFactory(context))
        val repositories = createTaggoDatabaseRepositories(database)
        val importService = TaggoFileImportService(
            fileEntries = repositories.fileEntries,
            tags = repositories.tags,
            idGenerator = DefaultTaggoIdGenerator,
            clock = SystemTaggoClock,
            platform = "android",
        )
        return TaggoFileRuntimeStore(
            repositories = repositories,
            importService = importService,
            idGenerator = DefaultTaggoIdGenerator,
            clock = SystemTaggoClock,
        )
    }
}
