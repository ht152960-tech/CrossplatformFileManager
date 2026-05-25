package com.example.cross_platformfilemanager

fun FileReference.needsThumbnailGeneration(): Boolean =
    when (FileTypeClassifier.classify(this)) {
        FileTypeCategory.Image,
        FileTypeCategory.Video,
        -> true

        else -> false
    }

fun FileReference.initialThumbnailStatus(): ThumbnailStatus =
    if (needsThumbnailGeneration()) ThumbnailStatus.NONE else ThumbnailStatus.UNSUPPORTED
