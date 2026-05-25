package com.example.cross_platformfilemanager

/**
 * 判断文件条目是否值得进入缩略图生成流程。
 *
 * 第一版只对图像和视频生成缩略图，
 * 这样规则简单、成本可控，也更符合当前文件管理展示需求。
 */
fun FileReference.needsThumbnailGeneration(): Boolean =
    when (FileTypeClassifier.classify(this)) {
        FileTypeCategory.Image,
        FileTypeCategory.Video,
        -> true

        else -> false
    }

/**
 * 计算文件条目的初始缩略图状态。
 *
 * 可生成缩略图的条目从 `NONE` 起步；
 * 不支持的条目直接标为 `UNSUPPORTED`，避免无意义重试。
 */
fun FileReference.initialThumbnailStatus(): ThumbnailStatus =
    if (needsThumbnailGeneration()) ThumbnailStatus.NONE else ThumbnailStatus.UNSUPPORTED
