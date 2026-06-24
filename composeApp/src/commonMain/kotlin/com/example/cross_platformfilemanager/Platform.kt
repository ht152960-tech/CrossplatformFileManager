package com.example.cross_platformfilemanager

/**
 * 平台信息抽象。
 *
 * 这个接口只暴露当前平台名称，
 * 供界面或日志层做轻量展示，不承载复杂业务能力。
 */
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun debugLog(tag: String, message: String)

/**
 * 外部打开文件的结果。
 *
 * 除了是否成功，还允许平台返回失败原因，
 * 便于上层在不支持或打开失败时提示用户。
 */
data class OpenReferenceResult(
    val opened: Boolean,
    val message: String? = null,
)

/**
 * 尝试由当前平台直接打开一个文件条目。
 *
 * 详情页里的“打开此文件”入口最终会走到这里，
 * 具体是否支持本地路径、URL 或浏览器句柄，由各平台实现自行决定。
 */
expect suspend fun openReferenceExternally(reference: FileReference): Boolean

expect suspend fun openReferenceExternallyWithResult(reference: FileReference): OpenReferenceResult

/**
 * 判断当前平台是否应该把该文件条目展示为可直接打开。
 *
 * 该判断只服务 UI 可用性，不执行真正打开动作。
 */
expect fun isReferenceExternallyOpenable(reference: FileReference): Boolean
