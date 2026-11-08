package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Color Palette entry for display with swatches and exact hex codes.
 */
data class ColorSwatchInfo(
    val name: String,
    val hexCode: String,
    val color: Color
)

/**
 * Represents one of the 6 aesthetic IDE themes.
 */
enum class AppThemeId(
    val displayName: String,
    val tagline: String,
    val isDark: Boolean,
    val previewPrimaryHex: String,
    val previewBgHex: String,
    val previewSurfaceHex: String
) {
    DARCULA_ESPRESSO(
        displayName = "Darcula Espresso",
        tagline = "Warm matte espresso & amber ochre (JetBrains Classic)",
        isDark = true,
        previewPrimaryHex = "#D4883A",
        previewBgHex = "#11100F",
        previewSurfaceHex = "#1B1A19"
    ),
    MACOS_MIDNIGHT(
        displayName = "macOS Midnight",
        tagline = "Deep space midnight & electric steel cyan",
        isDark = true,
        previewPrimaryHex = "#38BDF8",
        previewBgHex = "#0B0F19",
        previewSurfaceHex = "#111827"
    ),
    MONOKAI_PRO(
        displayName = "Monokai Pro",
        tagline = "Warm charcoal with radiant pastel accents",
        isDark = true,
        previewPrimaryHex = "#FFD866",
        previewBgHex = "#19181A",
        previewSurfaceHex = "#221F22"
    ),
    GITHUB_DARK(
        displayName = "GitHub Dark High-Contrast",
        tagline = "Crisp obsidian with icy slate & azure",
        isDark = true,
        previewPrimaryHex = "#58A6FF",
        previewBgHex = "#010409",
        previewSurfaceHex = "#0D1117"
    ),
    SOLARIZED_DARK(
        displayName = "Solarized Dark",
        tagline = "Deep teal-cyan base with warm ochre & amber",
        isDark = true,
        previewPrimaryHex = "#B58900",
        previewBgHex = "#002B36",
        previewSurfaceHex = "#073642"
    ),
    NORD_FROST(
        displayName = "Nord Frost",
        tagline = "Arctic polar night & frost cyan-blue",
        isDark = true,
        previewPrimaryHex = "#88C0D0",
        previewBgHex = "#242933",
        previewSurfaceHex = "#2E3440"
    );

    val title: String get() = displayName
    val paletteSwatches: List<ColorSwatchInfo> get() = getSwatches()

    fun getSwatches(): List<ColorSwatchInfo> = when (this) {
        DARCULA_ESPRESSO -> listOf(
            ColorSwatchInfo("Background", "#11100F", Color(0xFF11100F)),
            ColorSwatchInfo("Surface Canvas", "#1B1A19", Color(0xFF1B1A19)),
            ColorSwatchInfo("Primary Amber", "#D4883A", Color(0xFFD4883A)),
            ColorSwatchInfo("Secondary Steel", "#8CA6B8", Color(0xFF8CA6B8)),
            ColorSwatchInfo("Keyword Ochre", "#CC7832", Color(0xFFCC7832)),
            ColorSwatchInfo("String Sage", "#6A8759", Color(0xFF6A8759))
        )
        MACOS_MIDNIGHT -> listOf(
            ColorSwatchInfo("Midnight Void", "#0B0F19", Color(0xFF0B0F19)),
            ColorSwatchInfo("Surface Navy", "#111827", Color(0xFF111827)),
            ColorSwatchInfo("Sky Cyan", "#38BDF8", Color(0xFF38BDF8)),
            ColorSwatchInfo("Electric Indigo", "#818CF8", Color(0xFF818CF8)),
            ColorSwatchInfo("Keyword Rose", "#F43F5E", Color(0xFFF43F5E)),
            ColorSwatchInfo("String Emerald", "#34D399", Color(0xFF34D399))
        )
        MONOKAI_PRO -> listOf(
            ColorSwatchInfo("Charcoal Base", "#19181A", Color(0xFF19181A)),
            ColorSwatchInfo("Surface Ash", "#221F22", Color(0xFF221F22)),
            ColorSwatchInfo("Amber Gold", "#FFD866", Color(0xFFFFD866)),
            ColorSwatchInfo("Coral Rose", "#FF6188", Color(0xFFFF6188)),
            ColorSwatchInfo("Mint Green", "#A9DC76", Color(0xFFA9DC76)),
            ColorSwatchInfo("Cyan Blue", "#78DCE8", Color(0xFF78DCE8))
        )
        GITHUB_DARK -> listOf(
            ColorSwatchInfo("Obsidian Black", "#010409", Color(0xFF010409)),
            ColorSwatchInfo("Slate Gray", "#0D1117", Color(0xFF0D1117)),
            ColorSwatchInfo("GitHub Azure", "#58A6FF", Color(0xFF58A6FF)),
            ColorSwatchInfo("Lilac Purple", "#BC8CFF", Color(0xFFBC8CFF)),
            ColorSwatchInfo("Keyword Coral", "#FF7B72", Color(0xFFFF7B72)),
            ColorSwatchInfo("String Seafoam", "#7EE787", Color(0xFF7EE787))
        )
        SOLARIZED_DARK -> listOf(
            ColorSwatchInfo("Deep Teal", "#002B36", Color(0xFF002B36)),
            ColorSwatchInfo("Surface Cyan", "#073642", Color(0xFF073642)),
            ColorSwatchInfo("Ochre Gold", "#B58900", Color(0xFFB58900)),
            ColorSwatchInfo("Slate Blue", "#268BD2", Color(0xFF268BD2)),
            ColorSwatchInfo("Keyword Orange", "#CB4B16", Color(0xFFCB4B16)),
            ColorSwatchInfo("String Green", "#859900", Color(0xFF859900))
        )
        NORD_FROST -> listOf(
            ColorSwatchInfo("Polar Night", "#242933", Color(0xFF242933)),
            ColorSwatchInfo("Nordic Surface", "#2E3440", Color(0xFF2E3440)),
            ColorSwatchInfo("Frost Cyan", "#88C0D0", Color(0xFF88C0D0)),
            ColorSwatchInfo("Ice Blue", "#81A1C1", Color(0xFF81A1C1)),
            ColorSwatchInfo("Keyword Magenta", "#B48EAD", Color(0xFFB48EAD)),
            ColorSwatchInfo("String Mint", "#A3BE8C", Color(0xFFA3BE8C))
        )
    }

    fun toColorScheme(): ColorScheme = when (this) {
        DARCULA_ESPRESSO -> darkColorScheme(
            primary = Color(0xFFD4883A),
            onPrimary = Color(0xFF241404),
            primaryContainer = Color(0xFF75420E),
            onPrimaryContainer = Color(0xFFF5E6D3),
            secondary = Color(0xFF8CA6B8),
            onSecondary = Color(0xFF13232E),
            secondaryContainer = Color(0xFF2B3842),
            onSecondaryContainer = Color(0xFFDCE7F0),
            tertiary = Color(0xFF8AA87B),
            onTertiary = Color(0xFF162410),
            background = Color(0xFF11100F),
            onBackground = Color(0xFFE6E1DC),
            surface = Color(0xFF1B1A19),
            onSurface = Color(0xFFE6E1DC),
            surfaceVariant = Color(0xFF252320),
            onSurfaceVariant = Color(0xFFA39C93),
            outline = Color(0xFF423D38),
            outlineVariant = Color(0xFF2E2A27)
        )
        MACOS_MIDNIGHT -> darkColorScheme(
            primary = Color(0xFF38BDF8),
            onPrimary = Color(0xFF082F49),
            primaryContainer = Color(0xFF0369A1),
            onPrimaryContainer = Color(0xFFE0F2FE),
            secondary = Color(0xFF818CF8),
            onSecondary = Color(0xFF1E1B4B),
            secondaryContainer = Color(0xFF312E81),
            onSecondaryContainer = Color(0xFFE0E7FF),
            tertiary = Color(0xFF34D399),
            onTertiary = Color(0xFF022C22),
            background = Color(0xFF0B0F19),
            onBackground = Color(0xFFF1F5F9),
            surface = Color(0xFF111827),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF1F2937),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF374151),
            outlineVariant = Color(0xFF1E293B)
        )
        MONOKAI_PRO -> darkColorScheme(
            primary = Color(0xFFFFD866),
            onPrimary = Color(0xFF332A00),
            primaryContainer = Color(0xFF7A6400),
            onPrimaryContainer = Color(0xFFFFF3CC),
            secondary = Color(0xFF78DCE8),
            onSecondary = Color(0xFF00363D),
            secondaryContainer = Color(0xFF1A383D),
            onSecondaryContainer = Color(0xFFE0F7FA),
            tertiary = Color(0xFFA9DC76),
            onTertiary = Color(0xFF1C3A00),
            background = Color(0xFF19181A),
            onBackground = Color(0xFFFCFCFA),
            surface = Color(0xFF221F22),
            onSurface = Color(0xFFFCFCFA),
            surfaceVariant = Color(0xFF2D2A2E),
            onSurfaceVariant = Color(0xFF939293),
            outline = Color(0xFF49464E),
            outlineVariant = Color(0xFF322F35)
        )
        GITHUB_DARK -> darkColorScheme(
            primary = Color(0xFF58A6FF),
            onPrimary = Color(0xFF051D4D),
            primaryContainer = Color(0xFF1F6FEB),
            onPrimaryContainer = Color(0xFFDDF4FF),
            secondary = Color(0xFFBC8CFF),
            onSecondary = Color(0xFF2E1065),
            secondaryContainer = Color(0xFF3B1E78),
            onSecondaryContainer = Color(0xFFF3E8FF),
            tertiary = Color(0xFF7EE787),
            onTertiary = Color(0xFF04260C),
            background = Color(0xFF010409),
            onBackground = Color(0xFFF0F6FC),
            surface = Color(0xFF0D1117),
            onSurface = Color(0xFFF0F6FC),
            surfaceVariant = Color(0xFF161B22),
            onSurfaceVariant = Color(0xFF8B949E),
            outline = Color(0xFF30363D),
            outlineVariant = Color(0xFF21262D)
        )
        SOLARIZED_DARK -> darkColorScheme(
            primary = Color(0xFFB58900),
            onPrimary = Color(0xFF291E00),
            primaryContainer = Color(0xFF5C4500),
            onPrimaryContainer = Color(0xFFFFE082),
            secondary = Color(0xFF268BD2),
            onSecondary = Color(0xFF00223D),
            secondaryContainer = Color(0xFF104A73),
            onSecondaryContainer = Color(0xFFB3E5FC),
            tertiary = Color(0xFF859900),
            onTertiary = Color(0xFF1B2400),
            background = Color(0xFF002B36),
            onBackground = Color(0xFF93A1A1),
            surface = Color(0xFF073642),
            onSurface = Color(0xFFEEE8D5),
            surfaceVariant = Color(0xFF0A4452),
            onSurfaceVariant = Color(0xFF839496),
            outline = Color(0xFF586E75),
            outlineVariant = Color(0xFF093E4B)
        )
        NORD_FROST -> darkColorScheme(
            primary = Color(0xFF88C0D0),
            onPrimary = Color(0xFF122C34),
            primaryContainer = Color(0xFF4C566A),
            onPrimaryContainer = Color(0xFFECEFF4),
            secondary = Color(0xFF81A1C1),
            onSecondary = Color(0xFF152638),
            secondaryContainer = Color(0xFF3B4252),
            onSecondaryContainer = Color(0xFFE5E9F0),
            tertiary = Color(0xFFA3BE8C),
            onTertiary = Color(0xFF1E2E14),
            background = Color(0xFF242933),
            onBackground = Color(0xFFECEFF4),
            surface = Color(0xFF2E3440),
            onSurface = Color(0xFFECEFF4),
            surfaceVariant = Color(0xFF3B4252),
            onSurfaceVariant = Color(0xFFD8DEE9),
            outline = Color(0xFF4C566A),
            outlineVariant = Color(0xFF2E3440)
        )
    }

    fun toSyntaxColors(): KotlinSyntaxColors = when (this) {
        DARCULA_ESPRESSO -> DarkKotlinSyntaxColors
        MACOS_MIDNIGHT -> KotlinSyntaxColors(
            codeBackground = Color(0xFF111827),
            codeHeaderBackground = Color(0xFF1F2937),
            codeBorder = Color(0xFF374151),
            lineNumber = Color(0xFF64748B),
            plainText = Color(0xFFF1F5F9),
            keyword = Color(0xFF38BDF8),
            stringLiteral = Color(0xFF34D399),
            comment = Color(0xFF64748B),
            numberLiteral = Color(0xFFF472B6),
            typeName = Color(0xFF818CF8),
            functionName = Color(0xFFFBBF24),
            annotation = Color(0xFFA78BFA),
            explanationBackground = Color(0xFF1E293B),
            explanationAccentBorder = Color(0xFF38BDF8)
        )
        MONOKAI_PRO -> KotlinSyntaxColors(
            codeBackground = Color(0xFF221F22),
            codeHeaderBackground = Color(0xFF2D2A2E),
            codeBorder = Color(0xFF49464E),
            lineNumber = Color(0xFF727072),
            plainText = Color(0xFFFCFCFA),
            keyword = Color(0xFFFF6188),
            stringLiteral = Color(0xFFFFD866),
            comment = Color(0xFF727072),
            numberLiteral = Color(0xFFAB9DF2),
            typeName = Color(0xFF78DCE8),
            functionName = Color(0xFFA9DC76),
            annotation = Color(0xFFFF9B5E),
            explanationBackground = Color(0xFF2D2A2E),
            explanationAccentBorder = Color(0xFFFFD866)
        )
        GITHUB_DARK -> KotlinSyntaxColors(
            codeBackground = Color(0xFF0D1117),
            codeHeaderBackground = Color(0xFF161B22),
            codeBorder = Color(0xFF30363D),
            lineNumber = Color(0xFF6E7681),
            plainText = Color(0xFFC9D1D9),
            keyword = Color(0xFFFF7B72),
            stringLiteral = Color(0xFFA5D6FF),
            comment = Color(0xFF8B949E),
            numberLiteral = Color(0xFF79C0FF),
            typeName = Color(0xFFFFA657),
            functionName = Color(0xFFD2A8FF),
            annotation = Color(0xFF7EE787),
            explanationBackground = Color(0xFF161B22),
            explanationAccentBorder = Color(0xFF58A6FF)
        )
        SOLARIZED_DARK -> KotlinSyntaxColors(
            codeBackground = Color(0xFF073642),
            codeHeaderBackground = Color(0xFF0A4452),
            codeBorder = Color(0xFF586E75),
            lineNumber = Color(0xFF657B83),
            plainText = Color(0xFF93A1A1),
            keyword = Color(0xFF859900),
            stringLiteral = Color(0xFF2AA198),
            comment = Color(0xFF586E75),
            numberLiteral = Color(0xFFD33682),
            typeName = Color(0xFFB58900),
            functionName = Color(0xFF268BD2),
            annotation = Color(0xFFCB4B16),
            explanationBackground = Color(0xFF0A4452),
            explanationAccentBorder = Color(0xFFB58900)
        )
        NORD_FROST -> KotlinSyntaxColors(
            codeBackground = Color(0xFF2E3440),
            codeHeaderBackground = Color(0xFF3B4252),
            codeBorder = Color(0xFF4C566A),
            lineNumber = Color(0xFF4C566A),
            plainText = Color(0xFFECEFF4),
            keyword = Color(0xFF81A1C1),
            stringLiteral = Color(0xFFA3BE8C),
            comment = Color(0xFF616E88),
            numberLiteral = Color(0xFFB48EAD),
            typeName = Color(0xFF8FBCBB),
            functionName = Color(0xFF88C0D0),
            annotation = Color(0xFFEBCB8B),
            explanationBackground = Color(0xFF3B4252),
            explanationAccentBorder = Color(0xFF88C0D0)
        )
    }
}
