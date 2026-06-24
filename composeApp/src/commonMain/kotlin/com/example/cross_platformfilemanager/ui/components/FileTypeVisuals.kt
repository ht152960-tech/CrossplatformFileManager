package com.example.cross_platformfilemanager.ui.components

import com.example.cross_platformfilemanager.FileTypeCategory
import org.jetbrains.compose.resources.DrawableResource
import taggo.composeapp.generated.resources.Res
import taggo.composeapp.generated.resources.taggo_hero_archive
import taggo.composeapp.generated.resources.taggo_hero_audio
import taggo.composeapp.generated.resources.taggo_hero_code
import taggo.composeapp.generated.resources.taggo_hero_doc
import taggo.composeapp.generated.resources.taggo_hero_generic
import taggo.composeapp.generated.resources.taggo_hero_image
import taggo.composeapp.generated.resources.taggo_hero_pdf
import taggo.composeapp.generated.resources.taggo_hero_presentation
import taggo.composeapp.generated.resources.taggo_hero_spreadsheet
import taggo.composeapp.generated.resources.taggo_hero_video

internal object FileTypeVisuals {
    fun iconDrawableForCategory(category: FileTypeCategory): DrawableResource =
        when (category) {
            FileTypeCategory.Image -> Res.drawable.taggo_hero_image
            FileTypeCategory.Video -> Res.drawable.taggo_hero_video
            FileTypeCategory.Audio -> Res.drawable.taggo_hero_audio
            FileTypeCategory.TextDocument -> Res.drawable.taggo_hero_doc
            FileTypeCategory.PdfDocument -> Res.drawable.taggo_hero_pdf
            FileTypeCategory.Archive -> Res.drawable.taggo_hero_archive
            FileTypeCategory.Code -> Res.drawable.taggo_hero_code
            FileTypeCategory.Spreadsheet -> Res.drawable.taggo_hero_spreadsheet
            FileTypeCategory.Presentation -> Res.drawable.taggo_hero_presentation
            FileTypeCategory.Folder,
            FileTypeCategory.Unknown,
            -> Res.drawable.taggo_hero_generic
        }

    fun heroFallbackDrawableForCategory(category: FileTypeCategory): DrawableResource =
        iconDrawableForCategory(category)
}
