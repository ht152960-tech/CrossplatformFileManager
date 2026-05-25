package com.example.cross_platformfilemanager

/**
 * 推荐状态仓储接口。
 *
 * 这个接口抽象的是“推荐引擎当前状态”的读取、保存和清空能力，
 * 让算法层不依赖内存结构、文件存储或数据库细节。
 */
interface RecommendationStateDao {
    fun loadState(): RecommendationEngineSnapshot?
    fun saveState(snapshot: RecommendationEngineSnapshot)
    fun clearState()
}

/**
 * 推荐事件仓储接口。
 *
 * 这里保存的是推荐学习链路中的行为事件，而不是推荐状态本身。
 * 后续更换存储实现时，需要保持追加事件、读取事件和清空事件的语义一致。
 */
interface RecommendationEventDao {
    fun appendOpenEvent(event: FileOpenLog)
    fun appendClickEvent(event: RecommendationClickLog)
    fun loadOpenEvents(): List<FileOpenLog>
    fun loadClickEvents(): List<RecommendationClickLog>
    fun clearEvents()
}

/**
 * 推荐仓储聚合接口。
 *
 * 第一版直接把状态仓储和事件仓储合并在一起，方便内存实现快速验证算法；
 * 未来替换为数据库时，仍可以继续通过这个边界对上层保持兼容。
 */
interface RecommendationRepository : RecommendationStateDao, RecommendationEventDao

/**
 * 基于内存数据结构的推荐仓储实现。
 *
 * 这个实现适合第一版验证推荐算法，不承担跨进程持久化能力；
 * 如果以后改为数据库实现，上层应继续依赖接口而不是这里的具体集合结构。
 */
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
        // 同一打开事件只保留一份，避免重试或事件重放把历史样本重复写入。
        if (openEvents.any { it.isSameAs(event) }) return
        openEvents += event.copy()
    }

    override fun appendClickEvent(event: RecommendationClickLog) {
        // 点击事件同样按全量字段去重，避免一次反馈被重复计入学习链路。
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

/**
 * 独立的内存事件仓储实现。
 *
 * 当状态仓储和事件仓储不需要绑定在同一个实现里时，
 * 推荐引擎可以退回到这个事件仓储，仅负责追加和读取行为事件。
 */
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
