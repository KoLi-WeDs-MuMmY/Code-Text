package com.example.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Dedicated Material 3 Adaptive Theme File for Narrow / Standard Portrait Phones
 * (e.g., 9:20, 9:19.5, 9:16 — widthDp < 600dp).
 * Uses compact single-row top bars, modal bottom sheets, 1-column cards, and clamped overlays.
 */
fun buildCompactPortraitThemeSpec(profile: ScreenProfile): AdaptiveThemeSpec {
    val veryNarrow = profile.widthDp < 370
    return AdaptiveThemeSpec(
        profile = profile,
        tier = ScreenRatioTier.COMPACT_PORTRAIT,
        themeFileLabel = "CompactPortraitTheme.kt",
        screenPadding = if (veryNarrow) 10.dp else 14.dp,
        cardPadding = if (veryNarrow) 12.dp else 14.dp,
        sectionSpacing = 12.dp,
        topBarHeight = 52.dp,
        railWidth = 0.dp,
        cardCornerRadius = 18.dp,
        pillCornerRadius = 24.dp,
        cardElevation = 4.dp,
        overlayElevation = 10.dp,
        codeFontDeltaSp = if (veryNarrow) -2 else -1,
        useCompactTopActions = true,
        useNavigationRail = false,
        gridColumns = 1,
        defaultSplitSideBySide = false,
        showBrandSubtitle = !veryNarrow,
        maxContentWidth = 600.dp
    )
}
