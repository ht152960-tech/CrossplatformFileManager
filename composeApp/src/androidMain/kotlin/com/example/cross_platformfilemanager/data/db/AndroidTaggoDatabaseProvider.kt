package com.example.cross_platformfilemanager.data.db

import android.content.Context
import com.example.cross_platformfilemanager.data.adapter.DefaultTaggoIdGenerator
import com.example.cross_platformfilemanager.data.adapter.SystemTaggoClock
import com.example.cross_platformfilemanager.data.service.TaggoFileImportService
import com.example.cross_platformfilemanager.runtime.TaggoBehaviorRuntime
import com.example.cross_platformfilemanager.runtime.TaggoFileRuntimeStore

data class AndroidTaggoAppComponents(
    val runtimeStore: TaggoFileRuntimeStore,
    val behaviorRuntime: TaggoBehaviorRuntime,
)

object AndroidTaggoDatabaseProvider {
    private var appComponents: AndroidTaggoAppComponents? = null

    fun getAppComponents(context: Context): AndroidTaggoAppComponents =
        appComponents ?: createAppComponents(context.applicationContext).also {
            appComponents = it
        }

    fun getRuntimeStore(context: Context): TaggoFileRuntimeStore = getAppComponents(context).runtimeStore

    private fun createAppComponents(context: Context): AndroidTaggoAppComponents {
        val database = createTaggoDatabase(AndroidDatabaseDriverFactory(context))
        val repositories = createTaggoDatabaseRepositories(database)
        val behaviorRuntime = TaggoBehaviorRuntime(
            behaviorRepository = repositories.behavior,
            idGenerator = DefaultTaggoIdGenerator,
            clock = SystemTaggoClock,
            platform = "android",
            appVersion = null,
            databaseVersion = 1L,
            recommendationModelVersion = 1L,
        )
        val importService = TaggoFileImportService(
            fileEntries = repositories.fileEntries,
            tags = repositories.tags,
            idGenerator = DefaultTaggoIdGenerator,
            clock = SystemTaggoClock,
            platform = "android",
        )
        val runtimeStore = TaggoFileRuntimeStore(
            repositories = repositories,
            importService = importService,
            idGenerator = DefaultTaggoIdGenerator,
            clock = SystemTaggoClock,
            behaviorRuntime = behaviorRuntime,
        )
        return AndroidTaggoAppComponents(
            runtimeStore = runtimeStore,
            behaviorRuntime = behaviorRuntime,
        )
    }
}
