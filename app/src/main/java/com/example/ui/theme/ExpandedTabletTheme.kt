package com.example.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Dedicated Material 3 Adaptive Theme File for Expanded Tablets & Desktops
 * (widthDp >= 840dp and heightDp >= 500dp).
 * Enables spacious M3 surface cards, 3-column project grids, persistent NavigationRail, and elevated shadows.
 */
fun buildExpandedTabletThemeSpec(profile: ScreenProfile): AdaptiveThemeSpec {
    return AdaptiveThemeSpec(
        profile = profile,
        tier = ScreenRatioTier.EXPANDED_TABLET,
        themeFileLabel = "ExpandedTabletTheme.kt",
        screenPadding = 20.dp,
        cardPadding = 18.dp,
        sectionSpacing = 16.dp,
        topBarHeight = 56.dp,
        railWidth = 80.dp,
        cardCornerRadius = 22.dp,
        pillCornerRadius = 28.dp,
        cardElevation = 6.dp,
        overlayElevation = 14.dp,
        codeFontDeltaSp = 1,
        useCompactTopActions = false,
        useNavigationRail = true,
        gridColumns = 3,
        defaultSplitSideBySide = true,
        showBrandSubtitle = true,
        maxContentWidth = 1320.dp
    )
}
