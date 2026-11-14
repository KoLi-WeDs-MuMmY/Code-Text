package com.example.ui.theme

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ScreenRatioTier(
    val displayName: String,
    val themeFileName: String,
    val shortBadge: String
) {
    COMPACT_PORTRAIT(
        displayName = "Compact Portrait",
        themeFileName = "CompactPortraitTheme.kt",
        shortBadge = "Portrait"
    ),
    COMPACT_LANDSCAPE(
        displayName = "Compact Landscape",
        themeFileName = "CompactLandscapeTheme.kt",
        shortBadge = "Landscape"
    ),
    MEDIUM_FOLDABLE(
        displayName = "Medium Foldable",
        themeFileName = "MediumFoldableTheme.kt",
        shortBadge = "Foldable"
    ),
    EXPANDED_TABLET(
        displayName = "Expanded Tablet",
        themeFileName = "ExpandedTabletTheme.kt",
        shortBadge = "Tablet"
    )
}

@Immutable
data class ScreenProfile(
    val widthDp: Int,
    val heightDp: Int,
    val densityDpi: Int,
    val aspectRatio: Float,
    val aspectRatioLabel: String,
    val isLandscape: Boolean,
    val tier: ScreenRatioTier,
    val sourceLabel: String = "WindowMetrics (Zero Permission)"
)

@Immutable
data class AdaptiveThemeSpec(
    val profile: ScreenProfile,
    val tier: ScreenRatioTier,
    val themeFileLabel: String,
    val screenPadding: Dp,
    val cardPadding: Dp,
    val sectionSpacing: Dp,
    val topBarHeight: Dp,
    val railWidth: Dp,
    val cardCornerRadius: Dp,
    val pillCornerRadius: Dp,
    val cardElevation: Dp,
    val overlayElevation: Dp,
    val codeFontDeltaSp: Int,
    val useCompactTopActions: Boolean,
    val useNavigationRail: Boolean,
    val gridColumns: Int,
    val defaultSplitSideBySide: Boolean,
    val showBrandSubtitle: Boolean,
    val maxContentWidth: Dp
)

val LocalAdaptiveThemeSpec = staticCompositionLocalOf {
    buildCompactPortraitThemeSpec(
        classifyScreenProfile(widthDp = 411, heightDp = 915, densityDpi = 420)
    )
}

object ScreenRatioClassifier {

    fun classify(
        widthDp: Int,
        heightDp: Int,
        densityDpi: Int = 420,
        sourceLabel: String = "WindowMetrics (Zero Permission)"
    ): ScreenProfile {
        val safeW = widthDp.coerceAtLeast(240)
        val safeH = heightDp.coerceAtLeast(240)
        val isLandscape = safeW > safeH
        val longEdge = maxOf(safeW, safeH).toFloat()
        val shortEdge = minOf(safeW, safeH).toFloat().coerceAtLeast(1f)
        val ratio = longEdge / shortEdge

        val tier = when {
            // Landscape phone: wide widthDp but short heightDp (< 500dp)
            isLandscape && safeH < 500 -> ScreenRatioTier.COMPACT_LANDSCAPE
            // Tablets / desktop windows
            safeW >= 840 && safeH >= 500 -> ScreenRatioTier.EXPANDED_TABLET
            // Foldables / medium split panes
            safeW >= 600 && safeH >= 480 -> ScreenRatioTier.MEDIUM_FOLDABLE
            isLandscape -> ScreenRatioTier.COMPACT_LANDSCAPE
            else -> ScreenRatioTier.COMPACT_PORTRAIT
        }

        val ratioLabel = formatFriendlyAspectRatio(safeW, safeH)
        return ScreenProfile(
            widthDp = safeW,
            heightDp = safeH,
            densityDpi = densityDpi,
            aspectRatio = ratio,
            aspectRatioLabel = ratioLabel,
            isLandscape = isLandscape,
            tier = tier,
            sourceLabel = sourceLabel
        )
    }

    fun readDeviceDisplayProfile(
        context: Context,
        containerWidthDp: Int,
        containerHeightDp: Int
    ): ScreenProfile {
        val metrics = context.resources.displayMetrics
        val densityDpi = metrics.densityDpi.coerceAtLeast(120)
        val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val wm = context.getSystemService(WindowManager::class.java)
                val bounds = wm?.currentWindowMetrics?.bounds
                if (bounds != null && bounds.width() > 0 && bounds.height() > 0) {
                    "WindowMetrics API (Zero Permission)"
                } else {
                    "DisplayMetrics (Zero Permission)"
                }
            } catch (_: Exception) {
                "DisplayMetrics (Zero Permission)"
            }
        } else {
            "DisplayMetrics (Zero Permission)"
        }
        return classify(
            widthDp = containerWidthDp,
            heightDp = containerHeightDp,
            densityDpi = densityDpi,
            sourceLabel = source
        )
    }

    fun resolveThemeSpec(profile: ScreenProfile): AdaptiveThemeSpec = when (profile.tier) {
        ScreenRatioTier.COMPACT_PORTRAIT -> buildCompactPortraitThemeSpec(profile)
        ScreenRatioTier.COMPACT_LANDSCAPE -> buildCompactLandscapeThemeSpec(profile)
        ScreenRatioTier.MEDIUM_FOLDABLE -> buildMediumFoldableThemeSpec(profile)
        ScreenRatioTier.EXPANDED_TABLET -> buildExpandedTabletThemeSpec(profile)
    }

    private fun formatFriendlyAspectRatio(w: Int, h: Int): String {
        val longSide = maxOf(w, h).toFloat()
        val shortSide = minOf(w, h).toFloat().coerceAtLeast(1f)
        val r = longSide / shortSide
        val knownRatios = listOf(
            21f / 9f to "21:9",
            20f / 9f to "20:9",
            19.5f / 9f to "19.5:9",
            16f / 9f to "16:9",
            16f / 10f to "16:10",
            3f / 2f to "3:2",
            4f / 3f to "4:3",
            1f to "1:1"
        )
        val closest = knownRatios.minByOrNull { abs(it.first - r) }
        val base = if (closest != null && abs(closest.first - r) <= 0.11f) {
            closest.second
        } else {
            "${((r * 10f).roundToInt() / 10f)}:1"
        }
        return if (w >= h) "$base Landscape" else "$base Portrait"
    }
}

fun classifyScreenProfile(
    widthDp: Int,
    heightDp: Int,
    densityDpi: Int = 420
): ScreenProfile = ScreenRatioClassifier.classify(widthDp, heightDp, densityDpi)
