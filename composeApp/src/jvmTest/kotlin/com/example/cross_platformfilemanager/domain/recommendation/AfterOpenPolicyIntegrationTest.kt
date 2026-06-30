package com.example.cross_platformfilemanager.domain.recommendation

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.db.TaggoDatabase
import com.example.cross_platformfilemanager.data.db.createTaggoDatabaseRepositories
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AfterOpenPolicyIntegrationTest {
    @Test
    fun afterOpenPolicyIsSeparateNormalizedAndUpdatedFromAfterOpenSnapshot() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            TaggoDatabase.Schema.create(driver)
            val repositories = createTaggoDatabaseRepositories(TaggoDatabase(driver))
            val store = RecommendationPolicyStore(repositories.recommendationPolicy, FixedClock)
            val service = TaggoRecommendationService(
                historyReader = RecommendationHistoryReader(
                    repositories.behavior,
                    repositories.recommendations,
                ),
                policyStore = store,
            )
            runBlocking {
                val home = store.loadOrCreateHomePolicy()
                val afterOpen = store.loadOrCreateAfterOpenPolicy()

                assertNotEquals(home.id, afterOpen.id)
                assertEquals(RecommendationPolicyStore.AFTER_OPEN_POLICY_NAME, afterOpen.policyName)
                assertEquals(RecommendationMode.AFTER_OPEN, afterOpen.mode)
                assertTrue(abs(1.0 - afterOpen.weights.sum()) < 1e-12)
                assertEquals(0.34, afterOpen.weights.sequencePath)
                assertEquals(0.18, afterOpen.weights.directSuccessor)

                val snapshot = Json { encodeDefaults = true }.encodeToString(
                    RecommendationFeatureSnapshot(
                        mode = RecommendationMode.AFTER_OPEN.name,
                        features = RecommendationFeatureVector(sequencePath = 1.0).values(),
                        weights = afterOpen.weights.values(),
                        contributions = mapOf("sequencePath" to afterOpen.weights.sequencePath),
                        pathContext = RecommendationPathContext("A", listOf("A"), 1),
                    ),
                )
                val updated = service.updatePolicyFromFeedback(
                    mode = RecommendationMode.AFTER_OPEN,
                    selectedFeaturesJson = snapshot,
                    skippedBeforeFeaturesJson = emptyList(),
                )
                val persistedHome = store.loadOrCreateHomePolicy()
                val persistedAfterOpen = store.loadOrCreateAfterOpenPolicy()

                assertEquals(0, persistedHome.updateCount)
                assertEquals(home.weights, persistedHome.weights)
                assertEquals(1, updated?.updateCount)
                assertEquals(1, persistedAfterOpen.updateCount)
                assertNotEquals(afterOpen.weights, persistedAfterOpen.weights)

                val homeSnapshot = Json { encodeDefaults = true }.encodeToString(
                    RecommendationFeatureSnapshot(
                        mode = RecommendationMode.HOME_INITIAL.name,
                        features = RecommendationFeatureVector(recency = 1.0).values(),
                        weights = persistedHome.weights.values(),
                        contributions = mapOf("recency" to persistedHome.weights.recency),
                    ),
                )
                service.updatePolicyFromFeedback(
                    mode = RecommendationMode.HOME_INITIAL,
                    selectedFeaturesJson = homeSnapshot,
                    skippedBeforeFeaturesJson = emptyList(),
                )
                val afterHomeFeedback = store.loadOrCreateAfterOpenPolicy()
                val homeAfterHomeFeedback = store.loadOrCreateHomePolicy()
                assertEquals(1, homeAfterHomeFeedback.updateCount)
                assertEquals(1, afterHomeFeedback.updateCount)
                assertEquals(persistedAfterOpen.weights, afterHomeFeedback.weights)

                assertNull(
                    service.updatePolicyFromFeedback(
                        mode = RecommendationMode.AFTER_OPEN,
                        selectedFeaturesJson = null,
                        skippedBeforeFeaturesJson = emptyList(),
                    ),
                )
            }
        } finally {
            driver.close()
        }
    }

    private object FixedClock : TaggoClock {
        override fun nowMs(): Long = 10_000
    }
}
