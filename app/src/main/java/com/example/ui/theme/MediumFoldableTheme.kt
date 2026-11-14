package com.example.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Dedicated Material 3 Adaptive Theme File for Foldables & Medium Screens
 * (e.g., 4:3, 1:1 unfolded inner display — 600dp <= widthDp < 840dp).
 * Enables NavigationRail, 2-column project grid, and comfortable side-by-side split panes.
 */
fun buildMediumFoldableThemeSpec(profile: ScreenProfile): AdaptiveThemeSpec {
    return AdaptiveThemeSpec(
        profile = profile,
        tier = ScreenRatioTier.MEDIUM_FOLDABLE,
        themeFileLabel = "MediumFoldableTheme.kt",
        screenPadding = 16.dp,
        cardPadding = 16.dp,
        sectionSpacing = 14.dp,
        topBarHeight = 52.dp,
        railWidth = 72.dp,
        cardCornerRadius = 20.dp,
        pillCornerRadius = 24.dp,
        cardElevation = 5.dp,
        overlayElevation = 12.dp,
        codeFontDeltaSp = 0,
        useCompactTopActions = false,
        useNavigationRail = true,
        gridColumns = 2,
        defaultSplitSideBySide = true,
        showBrandSubtitle = true,
        maxContentWidth = 900.dp
    )
}
