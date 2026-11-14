package com.example.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Dedicated Material 3 Adaptive Theme File for Landscape Handheld Phones
 * (e.g., 20:9, 16:9 Landscape — wide horizontal span with constrained < 500dp height).
 * Replaces stacked vertical chrome with a slim 44dp top bar and side NavigationRail.
 */
fun buildCompactLandscapeThemeSpec(profile: ScreenProfile): AdaptiveThemeSpec {
    return AdaptiveThemeSpec(
        profile = profile,
        tier = ScreenRatioTier.COMPACT_LANDSCAPE,
        themeFileLabel = "CompactLandscapeTheme.kt",
        screenPadding = 12.dp,
        cardPadding = 12.dp,
        sectionSpacing = 10.dp,
        topBarHeight = 44.dp,
        railWidth = 60.dp,
        cardCornerRadius = 16.dp,
        pillCornerRadius = 20.dp,
        cardElevation = 4.dp,
        overlayElevation = 12.dp,
        codeFontDeltaSp = -1,
        useCompactTopActions = false,
        useNavigationRail = true,
        gridColumns = 2,
        defaultSplitSideBySide = true,
        showBrandSubtitle = false,
        maxContentWidth = 1000.dp
    )
}
