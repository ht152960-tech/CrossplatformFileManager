package com.example.cross_platformfilemanager.data.db

import android.content.Context
import com.example.cross_platformfilemanager.data.adapter.DefaultTaggoIdGenerator
import com.example.cross_platformfilemanager.data.adapter.SystemTaggoClock
import com.example.cross_platformfilemanager.data.service.TaggoFileImportService
import com.example.cross_platformfilemanager.domain.recommendation.RecommendationHistoryReader
import com.example.cross_platformfilemanager.domain.recommendation.RecommendationPolicyStore
import com.example.cross_platformfilemanager.domain.recommendation.TaggoRecommendationService
import com.example.cross_platformfilemanager.runtime.TaggoBehaviorRuntime
import com.example.cross_platformfilemanager.runtime.TaggoFileRuntimeStore
import com.example.cross_platformfilemanager.runtime.TaggoRecommendationRuntime

data class AndroidTaggoAppComponents(
    val runtimeStore: TaggoFileRuntimeStore,
    val behaviorRuntime: TaggoBehaviorRuntime,
    val recommendationRuntime: TaggoRecommendationRuntime,
    val recommendationService: TaggoRecommendationService,
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
        val recommendationRuntime = TaggoRecommendationRuntime(
            recommendationRecordRepository = repositories.recommendations,
            idGenerator = DefaultTaggoIdGenerator,
            clock = SystemTaggoClock,
            recommendationModelVersion = 1L,
        )
        val recommendationService = TaggoRecommendationService(
            historyReader = RecommendationHistoryReader(
                behaviorRepository = repositories.behavior,
                recommendationRecordRepository = repositories.recommendations,
            ),
            policyStore = RecommendationPolicyStore(
                repository = repositories.recommendationPolicy,
                clock = SystemTaggoClock,
            ),
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
            recommendationRuntime = recommendationRuntime,
            recommendationService = recommendationService,
        )
    }
}
