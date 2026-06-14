package com.example.cross_platformfilemanager

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 缩略图生成状态。
 *
 * 这个状态既服务文件条目展示，也服务生成流程控制，
 * 避免同一个文件在不支持或正在生成时被重复触发。
 */
@Serializable
enum class ThumbnailStatus {
    @SerialName("NONE")
    NONE,
    @SerialName("GENERATING")
    GENERATING,
    @SerialName("READY")
    READY,
    @SerialName("FAILED")
    FAILED,
    @SerialName("UNSUPPORTED")
    UNSUPPORTED,
}

/**
 * 缩略图生成的通用配置。
 */
object ThumbnailConfig {
    const val MAX_SIZE_PX = 320
    const val WEBP_QUALITY = 75
}

/**
 * 缩略图生成器抽象接口。
 *
 * 各平台可以用自己的图像解码和文件写入方式生成缩略图，
 * 上层只依赖“生成”和“删除”这两个能力。
 */
interface ThumbnailGenerator {
    suspend fun generateThumbnail(reference: FileReference): ThumbnailResult
    fun deleteThumbnail(thumbnailPath: String)
}

/**
 * 单次缩略图生成的结果。
 *
 * 用显式结果类型区分成功、失败和不支持，
 * 便于上层决定是否展示缩略图、记录失败状态或跳过重试。
 */
sealed interface ThumbnailResult {
    data class Ready(val thumbnailPath: String) : ThumbnailResult
    data class Failed(val reason: String) : ThumbnailResult
    data class Unsupported(val reason: String) : ThumbnailResult
}

expect fun createThumbnailGenerator(): ThumbnailGenerator?

@Composable
expect fun rememberThumbnailPainter(thumbnailPath: String?): Painter?
