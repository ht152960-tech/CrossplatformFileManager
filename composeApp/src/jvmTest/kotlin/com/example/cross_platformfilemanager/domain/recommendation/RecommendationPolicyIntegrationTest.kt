package com.example.cross_platformfilemanager.domain.recommendation

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.db.TaggoDatabase
import com.example.cross_platformfilemanager.data.db.createTaggoDatabaseRepositories
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationPolicyState
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecommendationPolicyIntegrationTest {
    @Test
    fun defaultPolicyIsInsertedReadAndUpdated() = withPolicyStore { store, repositories, clock ->
        val initial = store.loadOrCreateHomePolicy()
        assertEquals(1.0, initial.weights.sum(), 1e-12)
        assertEquals(0, initial.updateCount)

        clock.now = 2_000
        val updated = store.updateHomePolicy(initial, initial.weights.copy(periodic = 0.30))
        val persisted = store.loadOrCreateHomePolicy()

        assertEquals(1, updated.updateCount)
        assertEquals(updated.weights, persisted.weights)
        assertEquals(2_000, persisted.lastUpdatedAtMs)
        assertEquals(
            1,
            repositories.recommendationPolicy
                .loadPolicy(
                    RecommendationPolicyStore.POLICY_NAME,
                    RecommendationMode.HOME_INITIAL.name,
                    RecommendationPolicyStore.MODEL_VERSION,
                )
                ?.updateCount,
        )
    }

    @Test
    fun malformedWeightsJsonFallsBackWithoutCrashing() = withPolicyStore { store, repositories, _ ->
        repositories.recommendationPolicy.insertPolicy(
            TaggoRecommendationPolicyState(
                id = "broken",
                policyName = RecommendationPolicyStore.POLICY_NAME,
                recommendationMode = RecommendationMode.HOME_INITIAL.name,
                modelVersion = RecommendationPolicyStore.MODEL_VERSION,
                weightsJson = "{not-json",
                learningConfigJson = "{}",
                updateCount = 4,
                createdAtMs = 100,
                lastUpdatedAtMs = 200,
            ),
        )

        val loaded = store.loadOrCreateHomePolicy()

        assertEquals(RecommendationWeights(), loaded.weights)
        assertEquals(4, loaded.updateCount)
        assertEquals("broken", loaded.id)
    }

    @Test
    fun normalizationKeepsEveryWeightBoundedAndSumAtOne() {
        val config = RecommendationLearningConfig()
        val normalized = RecommendationWeights(
            periodic = 10.0,
            manualSearchOpen = -10.0,
            recency = 4.0,
            frequency = -4.0,
        ).normalized(config)

        assertTrue(abs(1.0 - normalized.sum()) < 1e-9)
        assertTrue(normalized.values().values.all { it in config.minWeight..config.maxWeight })
    }

    @Test
    fun feedbackUsesSnapshotFeaturesAndMissingSnapshotSkipsUpdate() =
        withPolicyStore { store, _, _ ->
            val initial = store.loadOrCreateHomePolicy()
            val updater = BanditPolicyUpdater(store)
            val json = Json { encodeDefaults = true }
            val selected = json.encodeToString(
                snapshot(mapOf("recency" to 1.0, "periodic" to 0.0)),
            )
            val skippedBefore = json.encodeToString(
                snapshot(mapOf("periodic" to 1.0, "recency" to 0.0)),
            )

            val updated = updater.updateFromRecommendationFeedback(
                initial,
                selected,
                listOf(skippedBefore),
            )
            val missing = updater.updateFromRecommendationFeedback(initial, null, listOf(skippedBefore))

            assertNotEquals(initial.weights, updated?.weights)
            assertEquals(1, updated?.updateCount)
            assertNull(missing)
        }

    private fun snapshot(features: Map<String, Double>): RecommendationFeatureSnapshot {
        val allFeatures = RecommendationFeatureVector().values().toMutableMap()
        allFeatures.putAll(features)
        return RecommendationFeatureSnapshot(
            mode = RecommendationMode.HOME_INITIAL.name,
            features = allFeatures,
            weights = RecommendationWeights().values(),
            contributions = emptyMap(),
        )
    }

    private fun withPolicyStore(
        block: suspend (
            RecommendationPolicyStore,
            com.example.cross_platformfilemanager.data.db.TaggoDatabaseRepositories,
            MutableClock,
        ) -> Unit,
    ) {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            TaggoDatabase.Schema.create(driver)
            val repositories = createTaggoDatabaseRepositories(TaggoDatabase(driver))
            val clock = MutableClock(1_000)
            runBlocking {
                block(
                    RecommendationPolicyStore(repositories.recommendationPolicy, clock),
                    repositories,
                    clock,
                )
            }
        } finally {
            driver.close()
        }
    }

    private class MutableClock(var now: Long) : TaggoClock {
        override fun nowMs(): Long = now
    }
}
