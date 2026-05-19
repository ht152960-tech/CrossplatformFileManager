package com.example.cross_platformfilemanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecommendationCoreTest {

    @Test
    fun displayTextKeepsChineseTextUnmodified() {
        assertEquals("中文标题", displayText("  中文标题  "))
        assertEquals("标签一, 标签二", displayText("标签一, 标签二"))
    }

    @Test
    fun intervalScoreRisesWhenFileIsOpenedEveryFiveDays() {
        val store = FilePatternStore()
        val fileId = "X"
        val day = 86_400_000L

        store.recordOpen(FileOpenLog(fileId = fileId, openedAtMillis = 0L))
        val beforeLearning = store.intervalScore(fileId, 5L * day)

        store.recordOpen(FileOpenLog(fileId = fileId, openedAtMillis = 5L * day))
        store.recordOpen(FileOpenLog(fileId = fileId, openedAtMillis = 10L * day))
        store.recordOpen(FileOpenLog(fileId = fileId, openedAtMillis = 15L * day))
        store.recordOpen(FileOpenLog(fileId = fileId, openedAtMillis = 20L * day))

        val afterLearning = store.intervalScore(fileId, 25L * day)

        assertTrue(afterLearning > beforeLearning, "intervalScore should increase after repeated 5-day openings")
    }

    @Test
    fun transitionHistoryPushesBToTheTopAfterA() {
        val engine = RecommendationEngine()
        val day = 86_400_000L

        repeat(5) { index ->
            val time = index * day
            engine.recordFileOpen("A", time, previousFileId = null)
            engine.recordFileOpen("B", time + (day / 10), previousFileId = "A")
        }

        val candidates = listOf(
            fileReference(id = "B", title = "B file", lastOpenedAtMillis = 5L * day),
            fileReference(id = "C", title = "C file", lastOpenedAtMillis = 5L * day),
            fileReference(id = "A", title = "A file", lastOpenedAtMillis = 5L * day),
        )

        val recommendations = engine.recommend(
            references = candidates,
            previousFileId = "A",
            nowMillis = 6L * day,
        )

        assertEquals("B", recommendations.first().file.id)
    }

    @Test
    fun sparseTransitionScoreStaysConservative() {
        val store = TransitionStore()
        store.recordTransition("A", "B")

        val score = store.transitionScore("A", "B")

        assertTrue(score < 0.5, "a single transition should remain conservative after smoothing")
        assertTrue(score > 0.0, "the transition score should still be positive after one observation")
    }

    @Test
    fun clickingRecommendationUpdatesLearnedWeight() {
        val engine = RecommendationEngine()
        val day = 86_400_000L

        engine.recordFileOpen("A", 0L, previousFileId = null)
        engine.recordFileOpen("B", day, previousFileId = "A")
        engine.recordFileOpen("A", 2L * day, previousFileId = "B")
        engine.recordFileOpen("B", 3L * day, previousFileId = "A")

        val candidates = listOf(
            fileReference(id = "B", title = "B file", lastOpenedAtMillis = 3L * day),
            fileReference(id = "C", title = "C file", lastOpenedAtMillis = 3L * day),
        )

        val recommendations = engine.recommend(
            references = candidates,
            previousFileId = "A",
            nowMillis = 4L * day,
        )

        val before = engine.weightStore.learnedWeight
        engine.recordRecommendationClick(
            clickedFileId = recommendations.first().file.id,
            shownRecommendations = recommendations,
            openedAtMillis = 4L * day,
            previousFileId = "A",
        )

        assertTrue(engine.weightStore.learnedWeight > before, "learnedWeight should change after a click")
        assertTrue(engine.weightStore.learnedTransitionWeight > 0.0, "transition learning should be updated")
    }

    @Test
    fun recommendationStateSurvivesSnapshotRoundTrip() {
        val engine = RecommendationEngine()
        val day = 86_400_000L

        engine.recordFileOpen("A", 0L, previousFileId = null)
        engine.recordFileOpen("B", day, previousFileId = "A")
        engine.recordFileOpen("A", 2L * day, previousFileId = "B")
        engine.recordFileOpen("B", 3L * day, previousFileId = "A")

        val beforeSnapshot = engine.exportSnapshot()
        val snapshot = AppSnapshot(
            locale = AppLocale.EnUs,
            query = "",
            selectedTag = null,
            selectedFileType = null,
            favoritesOnly = false,
            activeReferenceId = "B",
            references = listOf(
                fileReference(id = "A", title = "A file", lastOpenedAtMillis = 2L * day),
                fileReference(id = "B", title = "B file", lastOpenedAtMillis = 3L * day),
                fileReference(id = "C", title = "C file", lastOpenedAtMillis = 3L * day),
            ),
            recentSearches = emptyList(),
            recommendationLogs = emptyList(),
            recommendationState = beforeSnapshot,
        )

        val encoded = SnapshotCodec.encode(snapshot)
        val decoded = SnapshotCodec.decode(encoded)

        require(decoded != null)
        val restoredEngine = RecommendationEngine()
        restoredEngine.restoreSnapshot(decoded.recommendationState)

        assertEquals(
            engine.weightStore.learnedTransitionWeight,
            restoredEngine.weightStore.learnedTransitionWeight,
        )
        assertEquals(
            engine.transitionStore.transitionCount("A", "B"),
            restoredEngine.transitionStore.transitionCount("A", "B"),
        )

        val originalRecommendation = engine.recommend(
            references = snapshot.references,
            previousFileId = "A",
            nowMillis = 4L * day,
        ).first().file.id
        val restoredRecommendation = restoredEngine.recommend(
            references = decoded.references,
            previousFileId = "A",
            nowMillis = 4L * day,
        ).first().file.id

        assertEquals(originalRecommendation, restoredRecommendation)
    }

    @Test
    fun inMemoryRecommendationRepositoryStoresSnapshot() {
        val repository = InMemoryRecommendationRepository()
        val snapshot = RecommendationEngineSnapshot(
            filePatterns = mapOf(
                "A" to FilePattern(
                    fileId = "A",
                    lastOpenTimeMillis = 123L,
                    estimatedPeriodMillis = 456L,
                    openCount = 3,
                ),
            ),
            transitionSnapshot = TransitionSnapshot(
                counts = mapOf("A" to mapOf("B" to 2)),
                totals = mapOf("A" to 2),
            ),
            weightSnapshot = WeightSnapshot(
                baseIntervalWeight = 1.2,
                baseTransitionWeight = 1.5,
                baseRecencyWeight = 0.4,
                learnedIntervalWeight = 0.3,
                learnedTransitionWeight = 0.2,
                learnedRecencyWeight = 0.1,
            ),
            lastOpenedFileId = "B",
        )

        repository.save(snapshot)

        assertEquals(snapshot, repository.load())
    }

    @Test
    fun inMemoryRecommendationEventDaoStoresEvents() {
        val eventDao = InMemoryRecommendationEventDao()
        val openEvent = FileOpenLog(fileId = "A", openedAtMillis = 100L, previousFileId = "B")
        val clickEvent = RecommendationClickLog(
            clickedFileId = "A",
            openedAtMillis = 120L,
            previousFileId = "B",
            shownFileIds = listOf("A", "C"),
        )

        eventDao.appendOpenEvent(openEvent)
        eventDao.appendClickEvent(clickEvent)

        assertEquals(listOf(openEvent), eventDao.loadOpenEvents())
        assertEquals(listOf(clickEvent), eventDao.loadClickEvents())
    }

    @Test
    fun inMemoryFileRepositoryDeduplicatesConsecutiveSignals() {
        val repository = InMemoryFileRepository()
        repository.addReference(
            fileReference(
                id = "A",
                title = "A file",
                lastOpenedAtMillis = 0L,
            ),
        )

        // 这里验证同一个信号连续触发时不会被重复写入，避免历史记录和最近打开状态被噪声撑大。
        repository.recordSearch("  A  ")
        repository.recordSearch("a")
        assertEquals(listOf("a"), repository.recentSearches)

        val suggestion = Suggestion(
            label = "A file",
            reason = "test",
            kind = SuggestionKind.File,
            score = 1.0,
        )
        repository.recordRecommendation("a", null, listOf(suggestion))
        repository.recordRecommendation("a", null, listOf(suggestion))
        assertEquals(1, repository.recommendationLogs.size)

        repository.replaceRecentSearches(listOf("  Foo  ", "", "foo", "Bar"))
        assertEquals(listOf("foo", "bar"), repository.recentSearches)

        repository.replaceRecommendationLogs(
            listOf(
                RecommendationLog(
                    id = "rec-1",
                    query = "  Foo  ",
                    selectedTag = "  Tag  ",
                    generatedAtMillis = 1L,
                    topSuggestions = listOf(" A ", "", "B"),
                ),
                RecommendationLog(
                    id = "rec-1",
                    query = "Bar",
                    selectedTag = null,
                    generatedAtMillis = 2L,
                    topSuggestions = listOf("C"),
                ),
            ),
        )
        assertEquals(1, repository.recommendationLogs.size)
        assertEquals("bar", repository.recommendationLogs.first().query)
        assertEquals(listOf("C"), repository.recommendationLogs.first().topSuggestions)

        repository.open("A")
        val firstOpenedAt = repository.findReferenceById("A")?.lastOpenedAtMillis
        repository.open("A")
        val secondOpenedAt = repository.findReferenceById("A")?.lastOpenedAtMillis
        assertEquals(firstOpenedAt, secondOpenedAt)
    }

    @Test
    fun recommendationEngineMeetsCoreRequirements() {
        val day = 86_400_000L
        val intervalStore = FilePatternStore()

        intervalStore.recordOpen(FileOpenLog(fileId = "X", openedAtMillis = 0L))
        val intervalBefore = intervalStore.intervalScore("X", 5L * day)
        intervalStore.recordOpen(FileOpenLog(fileId = "X", openedAtMillis = 5L * day))
        intervalStore.recordOpen(FileOpenLog(fileId = "X", openedAtMillis = 10L * day))
        intervalStore.recordOpen(FileOpenLog(fileId = "X", openedAtMillis = 15L * day))
        intervalStore.recordOpen(FileOpenLog(fileId = "X", openedAtMillis = 20L * day))
        val intervalAfter = intervalStore.intervalScore("X", 25L * day)

        val engine = RecommendationEngine()
        repeat(6) { index ->
            val openedAt = index * day
            engine.recordFileOpen("A", openedAt, previousFileId = null)
            engine.recordFileOpen("B", openedAt + (day / 10), previousFileId = "A")
        }

        val candidates = listOf(
            fileReference(id = "B", title = "B file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "X", title = "X file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "C1", title = "C1 file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "C2", title = "C2 file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "C3", title = "C3 file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "C4", title = "C4 file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "C5", title = "C5 file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "C6", title = "C6 file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "C7", title = "C7 file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "C8", title = "C8 file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "C9", title = "C9 file", lastOpenedAtMillis = 24L * day),
            fileReference(id = "C10", title = "C10 file", lastOpenedAtMillis = 24L * day),
        )

        val recommendations = engine.recommend(
            references = candidates,
            previousFileId = "A",
            nowMillis = 25L * day,
        )

        val learnedBeforeClick = engine.weightStore.learnedWeight
        val clickResult = engine.recordRecommendationClick(
            clickedFileId = recommendations.first().file.id,
            shownRecommendations = recommendations,
            openedAtMillis = 25L * day,
            previousFileId = "A",
        )

        assertTrue(intervalAfter > intervalBefore, "5-day opening interval should improve intervalScore")
        assertEquals(10, recommendations.size, "recommendation list should be capped at 10 items")
        assertEquals("B", recommendations.first().file.id, "frequent A -> B transitions should push B to the top")
        assertTrue(clickResult != null, "clicking a recommendation should be recorded")
        assertTrue(engine.weightStore.learnedWeight > learnedBeforeClick, "clicking a recommendation should update learned weights")
    }

    @Test
    fun fileTypeClassifierKeepsTextAndVideoRulesSeparate() {
        assertEquals(
            FileTypeCategory.TextDocument,
            classify("notes.md"),
        )
        assertEquals(
            FileTypeCategory.TextDocument,
            classify("draft.txt"),
        )
        assertEquals(
            FileTypeCategory.TextDocument,
            classify("activity.log"),
        )
        assertEquals(
            FileTypeCategory.TextDocument,
            classify("report.docx"),
        )

        assertEquals(
            FileTypeCategory.Video,
            classify("clip.mp4"),
        )
        assertEquals(
            FileTypeCategory.Video,
            classify("movie.mkv"),
        )
        assertEquals(
            FileTypeCategory.Video,
            classify("trailer.mov"),
        )
    }

    @Test
    fun recommendationFallsBackToRecencyWhenContextIsMissing() {
        val engine = RecommendationEngine()
        val candidates = listOf(
            fileReference(id = "old", title = "Old file", lastOpenedAtMillis = 10L),
            fileReference(id = "new", title = "New file", lastOpenedAtMillis = 20L),
        )

        val recommendations = engine.recommend(
            references = candidates,
            previousFileId = null,
            nowMillis = 30L,
        )

        assertEquals("new", recommendations.first().file.id)
    }

    @Test
    fun recommendationKeepsResultsDiverseWhenScoresAreSimilar() {
        val engine = RecommendationEngine()
        val candidates = listOf(
            fileReference(id = "text-a", title = "Alpha note", fileType = "TXT", lastOpenedAtMillis = 10L),
            fileReference(id = "text-b", title = "Beta note", fileType = "MD", lastOpenedAtMillis = 10L),
            fileReference(id = "text-c", title = "Gamma note", fileType = "DOCX", lastOpenedAtMillis = 10L),
            fileReference(id = "video-a", title = "Delta clip", fileType = "MP4", lastOpenedAtMillis = 10L),
        )

        val recommendations = engine.recommend(
            references = candidates,
            previousFileId = null,
            nowMillis = 30L,
            limit = 2,
        )

        assertEquals(2, recommendations.size)
        assertTrue(
            recommendations.map { FileTypeClassifier.classify(it.file) }.contains(FileTypeCategory.Video),
            "a nearby non-text candidate should survive the diversity pass",
        )
    }

    @Test
    fun recommendFiltersBlankAndDuplicateCandidatesAndHonorsLimitBoundary() {
        val engine = RecommendationEngine()
        val candidates = listOf(
            fileReference(id = "", title = "Blank", lastOpenedAtMillis = 10L),
            fileReference(id = "A", title = "A file", lastOpenedAtMillis = 20L),
            fileReference(id = "A", title = "A duplicate", lastOpenedAtMillis = 30L),
            fileReference(id = "B", title = "B file", lastOpenedAtMillis = 40L),
        )

        // 这里确保推荐结果不会把空 ID 或重复候选带进去，同时 limit 边界能直接返回空列表。
        assertTrue(
            engine.recommend(candidates, previousFileId = null, nowMillis = 100L, limit = 0).isEmpty(),
        )

        val recommendations = engine.recommend(
            references = candidates,
            previousFileId = null,
            nowMillis = 100L,
            limit = 10,
        )

        assertEquals(listOf("B", "A"), recommendations.map { it.file.id })
    }

    @Test
    fun recommendDeduplicatesCandidatesThatShareTheSameSource() {
        val engine = RecommendationEngine()
        val candidates = listOf(
            fileReference(id = "A1", title = "Alpha", source = "/shared/path", lastOpenedAtMillis = 20L),
            fileReference(id = "A2", title = "Alpha duplicate", source = "/shared/path", lastOpenedAtMillis = 10L),
            fileReference(id = "B", title = "Beta", source = "/other/path", lastOpenedAtMillis = 15L),
        )

        val recommendations = engine.recommend(
            references = candidates,
            previousFileId = null,
            nowMillis = 100L,
            limit = 10,
        )

        assertEquals(listOf("A1", "B"), recommendations.map { it.file.id })
    }

    @Test
    fun openReferenceIgnoresMissingFiles() {
        val appState = FileManagerAppState()

        appState.openReference("missing")

        assertEquals(null, appState.activeReferenceId)
    }

    private fun fileReference(
        id: String,
        title: String,
        source: String = "/tmp/$id",
        fileType: String = "TXT",
        lastOpenedAtMillis: Long,
    ): FileReference = FileReference(
        id = id,
        title = title,
        source = source,
        sourceKind = FileSourceKind.ManualPath,
        fileType = fileType,
        fileSizeBytes = null,
        tags = emptyList(),
        notes = "",
        createdAtMillis = 0L,
        lastOpenedAtMillis = lastOpenedAtMillis,
    )

    private fun classify(fileName: String): FileTypeCategory = FileReference(
        id = fileName,
        title = fileName,
        source = "/tmp/$fileName",
        sourceKind = FileSourceKind.ManualPath,
        fileType = fileName.substringAfterLast('.', fileName.uppercase()),
        fileSizeBytes = null,
        tags = emptyList(),
        notes = "",
        createdAtMillis = 0L,
        lastOpenedAtMillis = 0L,
    ).let(FileTypeClassifier::classify)
}
