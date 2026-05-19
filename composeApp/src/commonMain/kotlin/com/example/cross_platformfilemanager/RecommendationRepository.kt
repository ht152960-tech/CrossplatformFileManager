package com.example.cross_platformfilemanager

interface RecommendationStateDao {
    fun loadState(): RecommendationEngineSnapshot?
    fun saveState(snapshot: RecommendationEngineSnapshot)
    fun clearState()
}

interface RecommendationEventDao {
    fun appendOpenEvent(event: FileOpenLog)
    fun appendClickEvent(event: RecommendationClickLog)
    fun loadOpenEvents(): List<FileOpenLog>
    fun loadClickEvents(): List<RecommendationClickLog>
    fun clearEvents()
}

interface RecommendationRepository : RecommendationStateDao, RecommendationEventDao

class InMemoryRecommendationRepository(
    initialSnapshot: RecommendationEngineSnapshot? = null,
) : RecommendationRepository {
    private var snapshot: RecommendationEngineSnapshot? = initialSnapshot?.copy()
    private val openEvents = mutableListOf<FileOpenLog>()
    private val clickEvents = mutableListOf<RecommendationClickLog>()

    override fun loadState(): RecommendationEngineSnapshot? = snapshot?.copy()

    override fun saveState(snapshot: RecommendationEngineSnapshot) {
        this.snapshot = snapshot.copy()
    }

    override fun clearState() {
        snapshot = null
    }

    override fun appendOpenEvent(event: FileOpenLog) {
        // 如果同样的打开事件已经存在，就直接忽略，避免重试或事件重放把历史写重。
        if (openEvents.any { it.isSameAs(event) }) return
        openEvents += event.copy()
    }

    override fun appendClickEvent(event: RecommendationClickLog) {
        // 点击事件同样做全局去重，防止同一条点击在后续流程里被重复记账。
        if (clickEvents.any { it.isSameAs(event) }) return
        clickEvents += event.copy(shownFileIds = event.shownFileIds.toList())
    }

    override fun loadOpenEvents(): List<FileOpenLog> = openEvents.map { it.copy() }

    override fun loadClickEvents(): List<RecommendationClickLog> = clickEvents.map {
        it.copy(shownFileIds = it.shownFileIds.toList())
    }

    override fun clearEvents() {
        openEvents.clear()
        clickEvents.clear()
    }

    fun load(): RecommendationEngineSnapshot? = loadState()

    fun save(snapshot: RecommendationEngineSnapshot) {
        saveState(snapshot)
    }

    fun clear() {
        clearState()
        clearEvents()
    }
}

class InMemoryRecommendationEventDao : RecommendationEventDao {
    private val openEvents = mutableListOf<FileOpenLog>()
    private val clickEvents = mutableListOf<RecommendationClickLog>()

    override fun appendOpenEvent(event: FileOpenLog) {
        if (openEvents.any { it.isSameAs(event) }) return
        openEvents += event.copy()
    }

    override fun appendClickEvent(event: RecommendationClickLog) {
        if (clickEvents.any { it.isSameAs(event) }) return
        clickEvents += event.copy(shownFileIds = event.shownFileIds.toList())
    }

    override fun loadOpenEvents(): List<FileOpenLog> = openEvents.map { it.copy() }

    override fun loadClickEvents(): List<RecommendationClickLog> = clickEvents.map {
        it.copy(shownFileIds = it.shownFileIds.toList())
    }

    override fun clearEvents() {
        openEvents.clear()
        clickEvents.clear()
    }
}

private fun FileOpenLog.isSameAs(other: FileOpenLog): Boolean =
    fileId == other.fileId &&
        openedAtMillis == other.openedAtMillis &&
        previousFileId == other.previousFileId

private fun RecommendationClickLog.isSameAs(other: RecommendationClickLog): Boolean =
    clickedFileId == other.clickedFileId &&
        openedAtMillis == other.openedAtMillis &&
        previousFileId == other.previousFileId &&
        shownFileIds == other.shownFileIds
