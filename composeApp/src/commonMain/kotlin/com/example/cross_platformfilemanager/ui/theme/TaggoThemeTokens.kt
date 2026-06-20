package com.example.cross_platformfilemanager.ui.theme

/**
 * HomeWide 命名空间是当前首页宽屏定稿版的样式 token 初稿。
 * HomeWide.* 只允许服务首页宽屏当前实现，不代表全项目通用 token。
 * 后续中屏、窄屏和其他页面重构时，不得直接照搬 HomeWide.*。
 * 如果某些 token 被证明可以跨页面复用，未来再提升为全局 TaggoColors / TaggoSpacing / TaggoRadius / TaggoTypography。
 * 当前阶段禁止为了“统一”而大规模改名、迁移或重组 token。
 */
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cross_platformfilemanager.FileTypeCategory

object TaggoGlobalColors {
    val PageBackground = Color(0xFF080811)
    val PanelBackground = Color(0xEE1A1F28)
    val PanelBackgroundSoft = Color(0xC0131720)
    val ItemBackground = Color(0xE617192A)
    val Border = Color(0xFF2A2E37)
    val BorderStrong = Color(0xFF3B4252)
    val SurfaceVariant = Color(0xFF222936)
    val PrimaryAccent = Color(0xFF7C5CFF)
    val PrimaryAccentSoft = Color(0xFF2B235A)
    val TextPrimary = Color(0xFFF2F4F8)
    val TextSecondary = Color(0xFFB2BBCB)
    val TextMuted = Color(0xFF8690A0)
    val Danger = Color(0xFFFF5C7A)
    val Success = Color(0xFF43D17A)
    val Warning = Color(0xFFF5B84B)
}

object TaggoGlobalRadius {
    val Card = 18.dp
    val Item = 12.dp
    val Chip = 12.dp
    val Button = 10.dp
    val Badge = 100.dp
    val Search = 12.dp
}

object TaggoGlobalSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 20.dp
    val Xxl = 24.dp
}

object TaggoGlobalTypography {
    val TitleLarge = 27.sp
    val TitleMedium = 15.sp
    val Body = 13.sp
    val BodySmall = 12.sp
    val Caption = 10.sp
    val Button = 11.sp
}

object TaggoGlobalAlpha {
    val Disabled = 0.42f
    val Hover = 0.07f
    val Pressed = 0.14f
    val Subtle = 0.22f
    val Strong = 0.82f
}

object TaggoFileCoverTokens {
    val CompactFileTileCoverSize = 50.dp
    val CompactFileTileIconSize = 30.dp
    val CompactRecommendationCoverSize = 48.dp
    val CompactRecommendationIconSize = 25.dp
    val MediumFileTileCoverSize = 68.dp
    val MediumFileTileIconSize = 38.dp
    val MediumRecommendationCoverSize = 36.dp
    val MediumRecommendationIconSize = 24.dp
}

object TaggoCompactTokens {
    val PageHorizontalInsetExtra = TaggoGlobalSpacing.Xs
    val PageSectionGap = 14.dp
    val BottomNavigationClearance = 96.dp

    val CardRadius = 20.dp
    val SoftCardRadius = TaggoGlobalRadius.Card
    val ItemRadius = 16.dp
    val HeroCoverRadius = 24.dp
    val BorderWidth = 1.dp

    val SearchCardPadding = 13.dp
    val SearchFieldHeight = 56.dp
    val SearchButtonHeight = 40.dp
    val SearchButtonMinWidth = 68.dp
    val SearchInlineThreshold = 300.dp

    val FileItemHeight = 82.dp
    val FileItemGap = TaggoGlobalSpacing.Sm
    val FileItemHorizontalPadding = 16.dp
    val FileItemVerticalPadding = 14.dp
    val FileCoverSize = TaggoFileCoverTokens.CompactFileTileCoverSize
    val FileIconSize = TaggoFileCoverTokens.CompactFileTileIconSize

    val DetailHeroPadding = TaggoGlobalSpacing.Md
    val DetailCoverAspectRatio = 16f / 9f
    val DetailCoverMaxHeight = 220.dp
    val DetailIconSize = 72.dp

    val ButtonRadius = 13.dp
    val SearchButtonHorizontalPadding = TaggoGlobalSpacing.Md

    val CaptionSmall = TaggoGlobalTypography.Caption
    val Caption = TaggoGlobalTypography.BodySmall
    val Placeholder = TaggoGlobalTypography.Body
    val Body = 14.sp
    val DetailTitle = TaggoGlobalTypography.TitleMedium
    val SearchLineHeight = 20.sp

    val ScrimAlpha = TaggoGlobalAlpha.Pressed
    val PrimaryButtonAlpha = 0.84f
    val DisabledSurfaceAlpha = 0.55f

    val AmbientBaseBackground = Color(0xFF070A16)
    val AmbientGlowPurple = Color(0xFF8F63FF).copy(alpha = 0.13f)
    val AmbientGlowBluePurple = Color(0xFF536DFF).copy(alpha = 0.10f)
    val AmbientGlowCyanBlue = Color(0xFF35D2FF).copy(alpha = 0.06f)
    val AmbientGlowBottomPurple = Color(0xFF9C63FF).copy(alpha = 0.08f)

    val GlassCardBorder = Color(0x66A9B8FF)
    val GlassCardSubtleHighlight = Color(0x1CFFFFFF)
    val GlassListItemBorder = Color(0x38A9B8FF)
    val GlassListItemHighlight = Color(0x14FFFFFF)

    fun glassCardBackgroundBrush(): Brush =
        Brush.linearGradient(
            listOf(
                Color(0x9A171B33),
                Color(0x7A11162A),
                Color(0x5C0B1022),
            ),
        )

    fun glassListItemBackgroundBrush(): Brush =
        Brush.linearGradient(
            listOf(
                Color(0x661E2440),
                Color(0x4C151A32),
                Color(0x36101526),
            ),
        )

    val DockBorder = Color(0x78B7C4FF)
    val DockSelectedIcon = Color(0xFFA98BFF)
    val DockUnselectedContent = Color(0xA0AEB8D0)
    val DockSelectedIndicator = Color(0xFF9C6BFF)
    val DockBottomGlow = Color(0xFF8B5CFF).copy(alpha = 0.11f)

    fun dockGlassBackgroundBrush(): Brush =
        Brush.linearGradient(
            listOf(
                Color(0xD51C2138),
                Color(0xB814192E),
                Color(0xA40D1122),
            ),
        )

    fun fabGradient(): Brush =
        Brush.linearGradient(
            listOf(
                Color(0xFFB58CFF),
                Color(0xFF8B5CFF),
                Color(0xFF6F46F5),
            ),
        )

    val FabGlow = Color(0xFF9C63FF).copy(alpha = 0.24f)
    val FabIcon = Color.White
    val FabBorder = Color(0x80D8C7FF)
    val FabHighlight = Color(0x36FFFFFF)
}

data class FileTypeColorTokens(
    val iconColor: Color,
    val avatarBackgroundColor: Color,
    val progressColor: Color,
    val badgeBackground: Color,
    val badgeContentColor: Color,
    val weakGlowColor: Color,
) {
    fun avatarBrush(baseColor: Color): Brush =
        Brush.linearGradient(
            listOf(
                baseColor,
                avatarBackgroundColor,
                weakGlowColor,
            ),
        )
}

object TaggoFileTypeColorTokens {
    val Document = FileTypeColorTokens(
        iconColor = Color(0xFFFFB45F),
        avatarBackgroundColor = Color(0x33FFB45F),
        progressColor = Color(0xFFFFA94A),
        badgeBackground = Color(0x24FFB45F),
        badgeContentColor = Color(0xFFFFD3A0),
        weakGlowColor = Color(0x18FFB45F),
    )
    val Image = FileTypeColorTokens(
        iconColor = Color(0xFF6FD3FF),
        avatarBackgroundColor = Color(0x336FD3FF),
        progressColor = Color(0xFF5CC9F5),
        badgeBackground = Color(0x246FD3FF),
        badgeContentColor = Color(0xFFB9EAFF),
        weakGlowColor = Color(0x186FD3FF),
    )
    val Video = FileTypeColorTokens(
        iconColor = Color(0xFFFF78C8),
        avatarBackgroundColor = Color(0x33FF78C8),
        progressColor = Color(0xFFE95AAE),
        badgeBackground = Color(0x24FF78C8),
        badgeContentColor = Color(0xFFFFB9E4),
        weakGlowColor = Color(0x18FF78C8),
    )
    val Audio = FileTypeColorTokens(
        iconColor = Color(0xFF6CE8B7),
        avatarBackgroundColor = Color(0x336CE8B7),
        progressColor = Color(0xFF55D6A5),
        badgeBackground = Color(0x246CE8B7),
        badgeContentColor = Color(0xFFB9F6DD),
        weakGlowColor = Color(0x186CE8B7),
    )
    val Archive = FileTypeColorTokens(
        iconColor = Color(0xFFFFC861),
        avatarBackgroundColor = Color(0x33FFC861),
        progressColor = Color(0xFFFFBD4A),
        badgeBackground = Color(0x24FFC861),
        badgeContentColor = Color(0xFFFFE2A6),
        weakGlowColor = Color(0x18FFC861),
    )
    val Code = FileTypeColorTokens(
        iconColor = Color(0xFF78E6A0),
        avatarBackgroundColor = Color(0x3378E6A0),
        progressColor = Color(0xFF5ED88B),
        badgeBackground = Color(0x2478E6A0),
        badgeContentColor = Color(0xFFBDF4CF),
        weakGlowColor = Color(0x1878E6A0),
    )
    val Other = FileTypeColorTokens(
        iconColor = Color(0xFFA8B4D0),
        avatarBackgroundColor = Color(0x2EA8B4D0),
        progressColor = Color(0xFF8F9BB8),
        badgeBackground = Color(0x20A8B4D0),
        badgeContentColor = Color(0xFFD4DBEC),
        weakGlowColor = Color(0x14A8B4D0),
    )

    fun forCategory(category: FileTypeCategory): FileTypeColorTokens =
        when (category) {
            FileTypeCategory.TextDocument,
            FileTypeCategory.PdfDocument,
            FileTypeCategory.Spreadsheet,
            FileTypeCategory.Presentation,
            -> Document
            FileTypeCategory.Image -> Image
            FileTypeCategory.Video -> Video
            FileTypeCategory.Audio -> Audio
            FileTypeCategory.Archive -> Archive
            FileTypeCategory.Code -> Code
            FileTypeCategory.Folder,
            FileTypeCategory.Unknown,
            -> Other
        }
}

object TaggoThemeTokens {
    object HomeWide {
        object Colors {
            val PageBackground = TaggoGlobalColors.PageBackground
            val DashboardPanelBackground = Color(0xE610111D)
            val DashboardPanelBorder = Color(0x802B2346)
            val DashboardItemBackground = TaggoGlobalColors.ItemBackground
            val DashboardItemBorder = Color(0x802B2346)
            val DashboardBadgeBackground = Color(0xFF242447)
            val DashboardBadgeText = Color(0xFFCFCBFF)
            val DashboardAccent = Color(0xFF8B5CFF)
            val DashboardProgressTrack = Color(0xFF23253A)
            val DashboardProgressFill = Color(0xFF8B5CFF)
            val DashboardProgressFillImage = Color(0xFF6E7DFF)
            val DashboardProgressFillVideo = Color(0xFFD95FFF)
            val DashboardProgressFillAudio = Color(0xFFFF9A57)
            val PanelBackground = TaggoGlobalColors.PanelBackground
            val PanelBackgroundSoft = TaggoGlobalColors.PanelBackgroundSoft
            val SurfaceVariant = TaggoGlobalColors.SurfaceVariant
            val PanelBorder = Color(0x80404A5D)
            val Border = TaggoGlobalColors.Border
            val BorderStrong = TaggoGlobalColors.BorderStrong
            val PrimaryAccent = TaggoGlobalColors.PrimaryAccent
            val PrimaryAccentSoft = TaggoGlobalColors.PrimaryAccentSoft
            val TextPrimary = TaggoGlobalColors.TextPrimary
            val TextSecondary = TaggoGlobalColors.TextSecondary
            val TextMuted = TaggoGlobalColors.TextMuted
            val Danger = TaggoGlobalColors.Danger
            val Success = TaggoGlobalColors.Success
            val Warning = TaggoGlobalColors.Warning
        }

        object Radius {
            val HeroSearch = TaggoGlobalRadius.Search
            val ToolButton = TaggoGlobalRadius.Button
            val DashboardPanelCompact = TaggoGlobalRadius.Card
            val DashboardPanelProminent = 22.dp
            val EmptyState = TaggoGlobalRadius.Item
            val RecentRow = 13.dp
            val RecentIcon = 8.dp
            val RecentIconInner = 18.dp
            val TagButton = TaggoGlobalRadius.Chip
            val TypeRow = TaggoGlobalRadius.Item
            val TypeIcon = 7.dp
            val Badge = TaggoGlobalRadius.Badge
        }

        object Spacing {
            val HeroHorizontal = 18.dp
            val HeroTop = 2.dp
            val HeroBottom = 4.dp
            val HeroTitleGap = 6.dp
            val PanelGap = 14.dp
            val SearchBarGap = TaggoGlobalSpacing.Sm
            val SearchFieldPaddingX = 13.dp
            val SearchFieldGap = 10.dp
            val ToolButtonPaddingX = 13.dp
            val PanelContentGapProminent = 15.dp
            val PanelContentGapRegular = 10.dp
            val PanelPaddingProminent = 18.dp
            val PanelPaddingRegular = 13.dp
            val EmptyStatePaddingX = 12.dp
            val EmptyStatePaddingY = 9.dp
            val EmptyStatePaddingXTransparent = 10.dp
            val EmptyStatePaddingYTransparent = 5.dp
            val EmptyStateContentGap = 5.dp
            val RecentRowPaddingX = 9.dp
            val RecentRowPaddingY = 5.dp
            val RecentRowGap = 9.dp
            val RecentTextGap = 2.dp
            val RecentListGap = 6.dp
            val TagGridGap = TaggoGlobalSpacing.Sm
            val TagButtonPaddingX = 11.dp
            val TagButtonPaddingY = 8.dp
            val TypeRowPaddingX = 8.dp
            val TypeRowPaddingY = 6.dp
            val TypeRowGap = 10.dp
            val TypeContentGap = 6.dp
            val TypeBarHeight = 3.dp
            val BadgePaddingX = 7.dp
            val BadgePaddingY = 3.dp
            val LinkButtonPaddingY = 2.dp
        }

        object Size {
            val HeroHeaderMinHeight = 58.dp
            val HeroSearchMinWidth = 330.dp
            val HeroSearchMaxWidth = 430.dp
            val SearchBarHeight = 46.dp
            val SearchIcon = 17.dp
            val ToolButtonHeight = 36.dp
            val PanelHeight = 248.dp
            val EmptyStateMaxWidth = 360.dp
            val RecentRowHeight = 48.dp
            val RecentIconSize = 34.dp
            val RecentIconInner = 18.dp
            val RecentTimeWidth = 72.dp
            val TagButtonHeight = 45.dp
            val TypeIconSize = 26.dp
            val TypeIconInner = 17.dp
            val LinkButtonHeight = 22.dp
        }

        object Typography {
            val HeroTitle = TaggoGlobalTypography.TitleLarge
            val HeroTitleLineHeight = 32.sp
            val HeroSubtitle = TaggoGlobalTypography.Body
            val HeroSubtitleLineHeight = 18.sp
            val SearchText = TaggoGlobalTypography.Body
            val SearchLineHeight = 20.sp
            val SearchPlaceholder = TaggoGlobalTypography.Body
            val ToolButton = TaggoGlobalTypography.Button
            val PanelTitle = TaggoGlobalTypography.TitleMedium
            val PanelTitleProminent = 17.sp
            val PanelMeta = TaggoGlobalTypography.Button
            val EmptyTitle = TaggoGlobalTypography.BodySmall
            val EmptyBody = TaggoGlobalTypography.Caption
            val EmptyBodyLineHeight = 14.sp
            val RecentTitle = TaggoGlobalTypography.BodySmall
            val RecentMeta = 9.sp
            val RecentMetaLineHeight = 11.sp
            val RecentTime = 10.sp
            val TagButton = TaggoGlobalTypography.Body
            val TypeName = TaggoGlobalTypography.BodySmall
            val TypeCount = TaggoGlobalTypography.Button
            val Badge = TaggoGlobalTypography.Caption
            val Link = 10.5.sp
        }

        object Alpha {
            val ButtonPrimary = 0.64f
            val ButtonSecondary = 0.70f
            val ButtonDisabled = TaggoGlobalAlpha.Disabled
            val SearchBorderFocused = 0.62f
            val SearchBorderIdle = TaggoGlobalAlpha.Subtle
            val SearchTextFieldFocused = 0.76f
            val SearchTextFieldIdle = 0.58f
            val SearchIndicatorFocused = TaggoGlobalAlpha.Strong
            val SearchIndicatorIdle = 0.34f
            val PanelProminentBorder = 0.28f
            val PanelSubtleBorder = TaggoGlobalAlpha.Hover
            val CompactEmptyBorder = TaggoGlobalAlpha.Hover
            val CompactEmptyBody = 0.72f
            val RecentMeta = 0.78f
            val DashboardTypeAccent = 0.08f
            val DashboardPanelMeta = 0.78f
            val DashboardPanelMetaSecondary = 0.82f
        }

        fun searchBorderColor(focused: Boolean): Color =
            if (focused) {
                Color(0xFFA68DFF).copy(alpha = Alpha.SearchBorderFocused)
            } else {
                Color(0xFF9A84FF).copy(alpha = Alpha.SearchBorderIdle)
            }

        fun searchFieldBackground(): Color = Color(0x76151125)

        fun searchFieldContainerColor(focused: Boolean): Color =
            if (focused) {
                Colors.PanelBackgroundSoft.copy(alpha = Alpha.SearchTextFieldFocused)
            } else {
                Colors.PanelBackgroundSoft.copy(alpha = Alpha.SearchTextFieldIdle)
            }

        fun searchFieldIndicatorColor(focused: Boolean): Color =
            if (focused) {
                Colors.PrimaryAccent.copy(alpha = Alpha.SearchIndicatorFocused)
            } else {
                Colors.PanelBorder.copy(alpha = Alpha.SearchIndicatorIdle)
            }

        fun toolButtonContainerColor(primary: Boolean): Color =
            if (primary) {
                Color(0xFF8B68FF).copy(alpha = Alpha.ButtonPrimary)
            } else {
                Color(0xFF241A42).copy(alpha = Alpha.ButtonSecondary)
            }

        fun dashboardPanelContainerColor(prominent: Boolean): Color =
            if (prominent) {
                Color(0xC3151028)
            } else {
                Color(0x360E0B17)
            }

        fun dashboardPanelBorderColor(prominent: Boolean): Color =
            if (prominent) {
                Color(0xFFC0AFFF).copy(alpha = Alpha.PanelProminentBorder)
            } else {
                Color(0xFFA997FF).copy(alpha = Alpha.PanelSubtleBorder)
            }

        fun dashboardPanelSurfaceBrush(prominent: Boolean): Brush =
            if (prominent) {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x76200F44),
                        Color(0x55170E31),
                        Color(0x3B120D25),
                        Color(0x20080612),
                    ),
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x10170D2A),
                        Color(0x08100A1A),
                        Color(0x04080610),
                    ),
                )
            }

        fun compactEmptyStateBackground(): Color = Color(0x2B17112B)

        fun compactEmptyStateBorder(): Color =
            Color(0xFFB09CFF).copy(alpha = Alpha.CompactEmptyBorder)

        fun dashboardTypeAccentBackground(accent: Color): Color =
            accent.copy(alpha = Alpha.DashboardTypeAccent)
    }
}
