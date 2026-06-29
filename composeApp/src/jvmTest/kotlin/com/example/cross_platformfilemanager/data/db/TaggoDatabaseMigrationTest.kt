package com.example.cross_platformfilemanager.data.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.cross_platformfilemanager.data.model.TaggoRecommendationPolicyState
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TaggoDatabaseMigrationTest {
    @Test
    fun schemaOneMigratesToUsablePolicyStateWithUniquePolicyIdentity() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            driver.execute(
                identifier = null,
                sql = "CREATE TABLE schema_one_marker (id INTEGER NOT NULL PRIMARY KEY)",
                parameters = 0,
            )
            driver.execute(
                identifier = null,
                sql = "PRAGMA user_version = 1",
                parameters = 0,
            )

            TaggoDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = 2)
            val repository = createTaggoDatabaseRepositories(TaggoDatabase(driver)).recommendationPolicy

            runBlocking {
                val initial = policyState()
                repository.insertPolicy(initial)
                assertEquals(
                    initial,
                    repository.loadPolicy(POLICY_NAME, HOME_INITIAL, MODEL_VERSION),
                )

                val updated = initial.copy(
                    weightsJson = """{"periodic":0.21}""",
                    updateCount = 1,
                    lastUpdatedAtMs = 2_000,
                )
                repository.updatePolicy(updated)
                assertEquals(
                    updated,
                    repository.loadPolicy(POLICY_NAME, HOME_INITIAL, MODEL_VERSION),
                )

                assertFailsWith<Exception> {
                    repository.insertPolicy(initial.copy(id = "duplicate_policy"))
                }
            }
        } finally {
            driver.close()
        }
    }

    private fun policyState() = TaggoRecommendationPolicyState(
        id = "policy_home_initial_v1",
        policyName = POLICY_NAME,
        recommendationMode = HOME_INITIAL,
        modelVersion = MODEL_VERSION,
        weightsJson = """{"periodic":0.20}""",
        learningConfigJson = """{"positiveLearningRate":0.035}""",
        updateCount = 0,
        createdAtMs = 1_000,
        lastUpdatedAtMs = 1_000,
    )

    private companion object {
        const val POLICY_NAME = "home_dynamic_policy"
        const val HOME_INITIAL = "HOME_INITIAL"
        const val MODEL_VERSION = 1L
    }
}
