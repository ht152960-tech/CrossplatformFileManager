# 项目学习指南

这份文档面向 Kotlin Multiplatform 和 Compose 的初学者，用来快速看懂这个文件管理项目的结构、启动流程和核心数据流。  
本项目当前重点是本地推荐算法，UI 是配合业务流程的，不是第一优先级。

## 1. 项目整体目标

这个项目是一个 Kotlin Multiplatform 文件管理工具，核心目标有两件：

1. 管理本地文件数据，包括新增、打开、搜索、详情页展示、标签筛选。
2. 在本地内存里完成推荐算法，不依赖服务器、不依赖深度学习框架。

推荐算法的设计说明在 [RECOMMENDATION_ALGORITHM.md](../RECOMMENDATION_ALGORITHM.md).

当前实现原则：

- 只用 Kotlin 标准库和内存数据结构做第一版。
- 算法类尽量放在 `composeApp/src/commonMain`。
- 以后要方便替换成数据库存储。
- 不走服务器。

## 2. 目录结构说明

先记住几个最重要的目录：

- `composeApp/src/commonMain`：共享业务层，推荐算法、状态、模型、页面逻辑都在这里。
- `composeApp/src/androidMain`：Android 平台实现。
- `composeApp/src/iosMain`：iOS 平台实现。
- `composeApp/src/jvmMain`：桌面端实现。
- `composeApp/src/jsMain`：浏览器 JS 平台实现。
- `composeApp/src/wasmJsMain`：Wasm 浏览器资源和入口。
- `composeApp/src/webMain`：浏览器共享入口层，JS 和 Wasm 共用。
- `composeApp/src/commonMain/composeResources`：图片、字体等共享资源。
- `tools/`：本地验收和辅助脚本。
- `docs/`：项目说明文档。

如果你是新手，优先从 `commonMain` 看起，不要先钻到平台目录里。

## 3. 应用启动流程

浏览器端的启动链路最能体现整个项目的结构：

1. `composeApp/src/wasmJsMain/resources/index.html`
2. `composeApp/src/webMain/kotlin/com/example/cross_platformfilemanager/main.kt`
3. `App()`
4. `AppStartupGate`
5. `AppMainSurface()`
6. `HomePage()`

对应的文件路径：

- [index.html](../composeApp/src/wasmJsMain/resources/index.html)
- [main.kt](../composeApp/src/webMain/kotlin/com/example/cross_platformfilemanager/main.kt)
- [App.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/App.kt)
- [AppStartupGate.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/AppStartupGate.kt)
- [StartupScreens.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/StartupScreens.kt)

启动时先显示启动页，再进入主界面。  
如果快照还没加载完，`AppStartupGate` 会先渲染 `StartupSplashScreen`。  
等快照和字体状态准备好后，才切到 `AppMainSurface()`，再由 `currentPage` 决定首页、标签页、文件页、详情页或搜索页。

## 4. 页面导航流程

页面导航主要集中在 [App.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/App.kt).

### 页面状态放在哪里

页面切换状态不是单独的导航框架，而是 `App()` 里面的 Compose 状态：

- `currentPage`
- `selectedNavigationPage`
- `detailBackTarget`

### 页面有哪些

`AppPage` 里定义了这些页面：

- `Home`
- `Tags`
- `AllFiles`
- `Detail`
- `Search`

### 页面怎么切换

- 顶部导航点击会改 `currentPage`。
- 打开文件会进入 `Detail`。
- 从详情页返回时，会回到 `detailBackTarget`。
- 搜索会进入 `Search`。
- 新增文件后通常回到 `Home`。

## 5. 各页面职责

还是看 [App.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/App.kt).

### HomePage

首页是推荐入口，主要展示：

- 最近新增文件
- 推荐文件
- 搜索框
- 导入文件入口

首页不直接算推荐分数，它只消费 `appState` 已经准备好的结果。

### TagsPage

标签页负责标签浏览和标签筛选。

### AllFilesPage

文件总览页，负责按排序方式、类型过滤展示全部文件。

### SearchResultsPage

搜索页负责展示搜索结果和搜索反馈。

### DetailPage

详情页负责展示单个文件的详细信息，也负责打开、替换、删除等操作。

## 6. 核心数据结构

新手先认这几个类：

- [FileManagerAppState.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/state/FileManagerAppState.kt)
- [RecommendationModels.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/RecommendationModels.kt)
- [SearchModels.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/SearchModels.kt)
- [AppSnapshot.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/AppSnapshot.kt)
- [SnapshotCodec.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/SnapshotCodec.kt)

### FileManagerAppState

这是应用状态总入口，里面放了：

- 当前语言
- 搜索条件
- 草稿数据
- 当前激活文件
- 快照版本号
- repository
- recommendationEngine

它负责把 UI 层和业务层连起来。

### AppSnapshot

这是快照数据，保存当前工作区状态，方便恢复。

## 7. 文件导入、打开、详情页流程

这一条线最重要，业务主流程就是它。

### 导入文件

导入入口一般在首页：

1. 首页点“上传文件”。
2. 调用浏览器文件选择器。
3. 选中文件后调用 `applyBrowserDraft()`。
4. 再调用 `addDraftReference()`。
5. 写入 `repository`。
6. 生成缩略图。
7. 回到首页。

相关代码在 [App.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/App.kt) 和 [FileManagerAppState.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/state/FileManagerAppState.kt).

### 打开文件

打开文件的统一入口在 `openReference(...)`。

流程是：

1. 保存当前页作为返回目标。
2. 调用 `appState.openReference(reference.id)`。
3. 写入打开历史和推荐学习信号。
4. 切到 `Detail` 页。

### 详情页返回

详情页返回会读取 `detailBackTarget`，再跳回之前的页面。

## 8. 搜索流程

搜索逻辑同样主要在 [App.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/App.kt) 和 [FileManagerAppState.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/state/FileManagerAppState.kt).

### 搜索怎么开始

1. 用户输入关键词。
2. 调用 `startSearchFromHome()` 或 `startSearchFromSearchPage()`。
3. 先做校验。
4. 通过后调用 `appState.submitSearch(...)`。
5. 搜索条件会变成 `searchTags`。
6. 跳转到 `Search` 页。

### 搜索结果从哪里来

搜索页的结果不是 UI 自己拼的，而是直接从 `appState.searchResults` 来。

`searchResults` 的底层数据来源是 `repository.search(searchTags)`。

## 9. 推荐算法流程

推荐算法是本项目当前的重点，入口和状态分两层看。

### 入口在什么地方

- [TaggoRecommendationService.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/domain/recommendation/TaggoRecommendationService.kt)
- [FileManagerAppState.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/state/FileManagerAppState.kt)

### 推荐数据从哪里来

推荐引擎主要使用这些输入：

- 文件打开时间间隔规律
- 文件之间的后继关系
- 最近打开时间
- 文件打开次数

### 推荐怎么产生

1. `FileManagerAppState.recommendedReferences` 调用推荐引擎。
2. `TaggoRecommendationService.recommendHome()` 读取历史和当前 policy 并计算候选分数。
3. 分数来源主要是：
   - 动态打开时间间隔
   - 最近打开与打开频率
   - 推荐反馈与弱行为信号
   - 当前 policy 里的权重
4. 排序后返回 `min(fileCount, limit)` 个候选，不做 source/id 去重或类型多样性重排。

### 文件打开如何影响推荐

打开文件时，`FileManagerAppState.openReference()` 会调用：

- `repository.open(referenceId)`
- `recommendationEngine.recordFileOpen(...)`

这一步会同时更新：

- 打开日志
- 时间规律样本
- 前后继关系
- 在线学习权重

所以“打开文件”就是推荐系统最重要的学习信号。

## 10. Web / Desktop / Android 平台差异

这个项目是 KMP，所以同一份业务代码会在不同平台上跑。

### 共享层

绝大多数业务代码都在 `commonMain`，这是你最该先看的地方。

### 平台入口

- Android：`androidMain`
- iOS：`iosMain`
- Desktop：`jvmMain`
- Web/JS：`webMain` 和 `jsMain`
- Wasm：`wasmJsMain`

### 平台实际实现

像字体加载、文件选择器、浏览器引用解析这些能力，常常通过 `expect/actual` 分到各平台去实现。

所以你看到同名函数在不同目录各有一个版本，这是正常的。

## 11. 初学者阅读顺序

如果你第一次看这个项目，建议按下面顺序：

1. [composeApp/build.gradle.kts](../composeApp/build.gradle.kts)
2. [App.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/App.kt)
3. [FileManagerAppState.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/state/FileManagerAppState.kt)
4. [TaggoRecommendationService.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/domain/recommendation/TaggoRecommendationService.kt)
5. [RecommendationModels.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/RecommendationModels.kt)
6. [SearchModels.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/SearchModels.kt)
7. [AppSnapshot.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/AppSnapshot.kt)
8. [SnapshotCodec.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/SnapshotCodec.kt)
9. [StartupFontLoadState.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/StartupFontLoadState.kt)
10. [index.html](../composeApp/src/wasmJsMain/resources/index.html)

这个顺序的好处是：

- 先看入口
- 再看状态
- 再看推荐算法
- 最后看页面和平台差异

## 12. 常见修改入口

如果你以后要改功能，可以先找这些文件：

- 改首页、搜索页、详情页：看 [App.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/App.kt)
- 改推荐算法：看 [TaggoRecommendationService.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/domain/recommendation/TaggoRecommendationService.kt)
- 改数据存取：看 [FileManagerAppState.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/state/FileManagerAppState.kt) 和 repository 相关文件
- 改快照恢复：看 [AppSnapshot.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/AppSnapshot.kt) 和 [SnapshotCodec.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/SnapshotCodec.kt)
- 改启动页或字体加载：看 [StartupScreens.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/StartupScreens.kt) 和 [StartupFontLoadState.kt](../composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/StartupFontLoadState.kt)
- 改浏览器启动：看 [main.kt](../composeApp/src/webMain/kotlin/com/example/cross_platformfilemanager/main.kt) 和 [index.html](../composeApp/src/wasmJsMain/resources/index.html)

## 13. 一句话总结

这个项目的核心是：

**`App()` 负责把启动、页面、状态和推荐算法串起来，`FileManagerAppState` 负责保存业务状态，`TaggoRecommendationService` 负责把文件与行为历史转成推荐结果。**
