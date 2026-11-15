package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
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
 * 1:1 CodeAssist App Chrome Themes (Material You Dynamic, CodeAssist Dark, Light, and AMOLED Black).
 */
enum class CodeAssistAppTheme(
    val displayName: String,
    val subtitle: String,
    val forcesDark: Boolean?, // null = follows system/ThemeMode toggle
    val previewSurfaceHex: String,
    val previewPrimaryHex: String
) {
    MATERIAL_YOU(
        displayName = "Material You",
        subtitle = "Android 12+ dynamic wallpaper colors with M3 tonal fallback",
        forcesDark = null,
        previewSurfaceHex = "#1B1B1F",
        previewPrimaryHex = "#A8C7FA"
    ),
    CODEASSIST_DARK(
        displayName = "CodeAssist Dark",
        subtitle = "Official CodeAssist Material 3 dark slate workspace",
        forcesDark = true,
        previewSurfaceHex = "#121316",
        previewPrimaryHex = "#A8C7FA"
    ),
    CODEASSIST_LIGHT(
        displayName = "CodeAssist Light",
        subtitle = "Crisp daylight CodeAssist Material 3 workspace",
        forcesDark = false,
        previewSurfaceHex = "#FAF9FD",
        previewPrimaryHex = "#0B57D0"
    ),
    CODEASSIST_AMOLED(
        displayName = "AMOLED Black",
        subtitle = "Pitch-black OLED studio surfaces for zero-glare coding",
        forcesDark = true,
        previewSurfaceHex = "#000000",
        previewPrimaryHex = "#8AB4F8"
    );

    fun resolveColorScheme(context: Context, isDarkEffective: Boolean): ColorScheme {
        return when (this) {
            MATERIAL_YOU -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (isDarkEffective) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                } else {
                    if (isDarkEffective) CODEASSIST_DARK.staticDarkScheme() else CODEASSIST_LIGHT.staticLightScheme()
                }
            }
            CODEASSIST_DARK -> if (isDarkEffective) staticDarkScheme() else CODEASSIST_LIGHT.staticLightScheme()
            CODEASSIST_LIGHT -> if (!isDarkEffective) staticLightScheme() else staticDarkScheme()
            CODEASSIST_AMOLED -> if (isDarkEffective) amoledDarkScheme() else CODEASSIST_LIGHT.staticLightScheme()
        }
    }

    private fun staticDarkScheme(): ColorScheme = darkColorScheme(
        primary = Color(0xFFA8C7FA),
        onPrimary = Color(0xFF062E6F),
        primaryContainer = Color(0xFF0842A0),
        onPrimaryContainer = Color(0xFFD3E3FD),
        secondary = Color(0xFFC2C7CE),
        onSecondary = Color(0xFF2B3137),
        secondaryContainer = Color(0xFF3E4758),
        onSecondaryContainer = Color(0xFFDCE2EE),
        tertiary = Color(0xFF81C995),
        onTertiary = Color(0xFF0A3818),
        tertiaryContainer = Color(0xFF1E4D2B),
        onTertiaryContainer = Color(0xFFC4EED0),
        background = Color(0xFF121316),
        onBackground = Color(0xFFE3E2E6),
        surface = Color(0xFF121316),
        onSurface = Color(0xFFE3E2E6),
        surfaceVariant = Color(0xFF44474E),
        onSurfaceVariant = Color(0xFFC4C6D0),
        surfaceDim = Color(0xFF0E0F12),
        surfaceBright = Color(0xFF38393E),
        surfaceContainerLowest = Color(0xFF0D0E11),
        surfaceContainerLow = Color(0xFF1A1C20),
        surfaceContainer = Color(0xFF1E2025),
        surfaceContainerHigh = Color(0xFF282A30),
        surfaceContainerHighest = Color(0xFF33353B),
        outline = Color(0xFF8E9099),
        outlineVariant = Color(0xFF2F3239),
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color(0xFFF9DEDC)
    )

    private fun staticLightScheme(): ColorScheme = lightColorScheme(
        primary = Color(0xFF0B57D0),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD3E3FD),
        onPrimaryContainer = Color(0xFF041E49),
        secondary = Color(0xFF565F71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDAE2F9),
        onSecondaryContainer = Color(0xFF131C2B),
        tertiary = Color(0xFF146C2E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFC4EED0),
        onTertiaryContainer = Color(0xFF072711),
        background = Color(0xFFFAF9FD),
        onBackground = Color(0xFF1A1B1F),
        surface = Color(0xFFFAF9FD),
        onSurface = Color(0xFF1A1B1F),
        surfaceVariant = Color(0xFFE1E2EC),
        onSurfaceVariant = Color(0xFF44474E),
        surfaceDim = Color(0xFFDAD9DF),
        surfaceBright = Color(0xFFFFFFFF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF4F3F8),
        surfaceContainer = Color(0xFFEEEDF4),
        surfaceContainerHigh = Color(0xFFE8E7EF),
        surfaceContainerHighest = Color(0xFFE2E2EA),
        outline = Color(0xFF74777F),
        outlineVariant = Color(0xFFC4C6D0),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B)
    )

    private fun amoledDarkScheme(): ColorScheme = darkColorScheme(
        primary = Color(0xFF8AB4F8),
        onPrimary = Color(0xFF041E49),
        primaryContainer = Color(0xFF133262),
        onPrimaryContainer = Color(0xFFD3E3FD),
        secondary = Color(0xFFB0BEC5),
        onSecondary = Color(0xFF1C262B),
        secondaryContainer = Color(0xFF1F2733),
        onSecondaryContainer = Color(0xFFDCE5F2),
        tertiary = Color(0xFF81C995),
        onTertiary = Color(0xFF04260C),
        tertiaryContainer = Color(0xFF12331C),
        onTertiaryContainer = Color(0xFFC4EED0),
        background = Color(0xFF000000),
        onBackground = Color(0xFFF1F3F4),
        surface = Color(0xFF000000),
        onSurface = Color(0xFFF1F3F4),
        surfaceVariant = Color(0xFF16181D),
        onSurfaceVariant = Color(0xFF9AA0A6),
        surfaceDim = Color(0xFF000000),
        surfaceBright = Color(0xFF202229),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceContainerLow = Color(0xFF08090C),
        surfaceContainer = Color(0xFF0E1015),
        surfaceContainerHigh = Color(0xFF161920),
        surfaceContainerHighest = Color(0xFF1F232C),
        outline = Color(0xFF3C4043),
        outlineVariant = Color(0xFF1E222A),
        error = Color(0xFFF28B82),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF5C1512),
        onErrorContainer = Color(0xFFF9DEDC)
    )
}

/**
 * Represents the 1:1 CodeAssist editor syntax & workspace color schemes (both Dark & Light schemes).
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
        displayName = "Darcula (CodeAssist)",
        tagline = "Classic JetBrains & CodeAssist dark editor scheme",
        isDark = true,
        previewPrimaryHex = "#CC7832",
        previewBgHex = "#2B2B2B",
        previewSurfaceHex = "#313335"
    ),
    QUIET_LIGHT(
        displayName = "QuietLight",
        tagline = "Official JetBrains & CodeAssist clean daylight scheme",
        isDark = false,
        previewPrimaryHex = "#3333AA",
        previewBgHex = "#F5F5F5",
        previewSurfaceHex = "#FFFFFF"
    ),
    DRACULA(
        displayName = "Dracula",
        tagline = "Vibrant violet, cyan & pink on deep slate",
        isDark = true,
        previewPrimaryHex = "#BD93F9",
        previewBgHex = "#282A36",
        previewSurfaceHex = "#21222C"
    ),
    GITHUB_DARK(
        displayName = "GitHub Dark",
        tagline = "Crisp GitHub Dark obsidian & azure tokens",
        isDark = true,
        previewPrimaryHex = "#58A6FF",
        previewBgHex = "#0D1117",
        previewSurfaceHex = "#161B22"
    ),
    GITHUB_LIGHT(
        displayName = "GitHub Light",
        tagline = "Bright GitHub Light syntax highlighting",
        isDark = false,
        previewPrimaryHex = "#0969DA",
        previewBgHex = "#FFFFFF",
        previewSurfaceHex = "#F6F8FA"
    ),
    MONOKAI_PRO(
        displayName = "Monokai",
        tagline = "Classic Monokai neon magenta, yellow & lime",
        isDark = true,
        previewPrimaryHex = "#FFD866",
        previewBgHex = "#272822",
        previewSurfaceHex = "#221F22"
    ),
    SOLARIZED_DARK(
        displayName = "Solarized Dark",
        tagline = "Ethan Schoonover's iconic deep teal palette",
        isDark = true,
        previewPrimaryHex = "#268BD2",
        previewBgHex = "#002B36",
        previewSurfaceHex = "#073642"
    ),
    SOLARIZED_LIGHT(
        displayName = "Solarized Light",
        tagline = "Warm paper-cream low-eyestrain daylight scheme",
        isDark = false,
        previewPrimaryHex = "#268BD2",
        previewBgHex = "#FDF6E3",
        previewSurfaceHex = "#EEE8D5"
    ),
    ABYSS(
        displayName = "Abyss",
        tagline = "Deep midnight navy with crisp pastel tokens",
        isDark = true,
        previewPrimaryHex = "#FFEEBB",
        previewBgHex = "#000C18",
        previewSurfaceHex = "#051336"
    ),
    NORD_FROST(
        displayName = "Nord",
        tagline = "Arctic polar night & frost cyan-blue",
        isDark = true,
        previewPrimaryHex = "#88C0D0",
        previewBgHex = "#2E3440",
        previewSurfaceHex = "#3B4252"
    ),
    MACOS_MIDNIGHT(
        displayName = "Midnight Ocean",
        tagline = "Deep space midnight & electric sky cyan",
        isDark = true,
        previewPrimaryHex = "#38BDF8",
        previewBgHex = "#0B0F19",
        previewSurfaceHex = "#111827"
    );

    val title: String get() = displayName
    val paletteSwatches: List<ColorSwatchInfo> get() = getSwatches()

    fun getSwatches(): List<ColorSwatchInfo> = when (this) {
        DARCULA_ESPRESSO -> listOf(
            ColorSwatchInfo("Editor BG", "#2B2B2B", Color(0xFF2B2B2B)),
            ColorSwatchInfo("Gutter", "#313335", Color(0xFF313335)),
            ColorSwatchInfo("Keyword", "#CC7832", Color(0xFFCC7832)),
            ColorSwatchInfo("String", "#6A8759", Color(0xFF6A8759)),
            ColorSwatchInfo("Function", "#FFC66D", Color(0xFFFFC66D)),
            ColorSwatchInfo("Number", "#6897BB", Color(0xFF6897BB))
        )
        QUIET_LIGHT -> listOf(
            ColorSwatchInfo("Editor BG", "#F5F5F5", Color(0xFFF5F5F5)),
            ColorSwatchInfo("Gutter", "#ECECEC", Color(0xFFECECEC)),
            ColorSwatchInfo("Keyword", "#4B83CD", Color(0xFF4B83CD)),
            ColorSwatchInfo("String", "#448C27", Color(0xFF448C27)),
            ColorSwatchInfo("Function", "#AA3731", Color(0xFFAA3731)),
            ColorSwatchInfo("Number", "#AB6526", Color(0xFFAB6526))
        )
        DRACULA -> listOf(
            ColorSwatchInfo("Editor BG", "#282A36", Color(0xFF282A36)),
            ColorSwatchInfo("Gutter", "#21222C", Color(0xFF21222C)),
            ColorSwatchInfo("Keyword", "#FF79C6", Color(0xFFFF79C6)),
            ColorSwatchInfo("String", "#F1FA8C", Color(0xFFF1FA8C)),
            ColorSwatchInfo("Function", "#50FA7B", Color(0xFF50FA7B)),
            ColorSwatchInfo("Type", "#8BE9FD", Color(0xFF8BE9FD))
        )
        GITHUB_DARK -> listOf(
            ColorSwatchInfo("Editor BG", "#0D1117", Color(0xFF0D1117)),
            ColorSwatchInfo("Gutter", "#161B22", Color(0xFF161B22)),
            ColorSwatchInfo("Keyword", "#FF7B72", Color(0xFFFF7B72)),
            ColorSwatchInfo("String", "#A5D6FF", Color(0xFFA5D6FF)),
            ColorSwatchInfo("Function", "#D2A8FF", Color(0xFFD2A8FF)),
            ColorSwatchInfo("Accent", "#58A6FF", Color(0xFF58A6FF))
        )
        GITHUB_LIGHT -> listOf(
            ColorSwatchInfo("Editor BG", "#FFFFFF", Color(0xFFFFFFFF)),
            ColorSwatchInfo("Gutter", "#F6F8FA", Color(0xFFF6F8FA)),
            ColorSwatchInfo("Keyword", "#CF222E", Color(0xFFCF222E)),
            ColorSwatchInfo("String", "#0A3069", Color(0xFF0A3069)),
            ColorSwatchInfo("Function", "#8250DF", Color(0xFF8250DF)),
            ColorSwatchInfo("Accent", "#0969DA", Color(0xFF0969DA))
        )
        MONOKAI_PRO -> listOf(
            ColorSwatchInfo("Editor BG", "#272822", Color(0xFF272822)),
            ColorSwatchInfo("Gutter", "#221F22", Color(0xFF221F22)),
            ColorSwatchInfo("Keyword", "#F92672", Color(0xFFF92672)),
            ColorSwatchInfo("String", "#E6DB74", Color(0xFFE6DB74)),
            ColorSwatchInfo("Function", "#A6E22E", Color(0xFFA6E22E)),
            ColorSwatchInfo("Type", "#66D9EF", Color(0xFF66D9EF))
        )
        SOLARIZED_DARK -> listOf(
            ColorSwatchInfo("Deep Teal", "#002B36", Color(0xFF002B36)),
            ColorSwatchInfo("Surface", "#073642", Color(0xFF073642)),
            ColorSwatchInfo("Keyword", "#859900", Color(0xFF859900)),
            ColorSwatchInfo("String", "#2AA198", Color(0xFF2AA198)),
            ColorSwatchInfo("Function", "#268BD2", Color(0xFF268BD2)),
            ColorSwatchInfo("Type", "#B58900", Color(0xFFB58900))
        )
        SOLARIZED_LIGHT -> listOf(
            ColorSwatchInfo("Paper Cream", "#FDF6E3", Color(0xFFFDF6E3)),
            ColorSwatchInfo("Surface", "#EEE8D5", Color(0xFFEEE8D5)),
            ColorSwatchInfo("Keyword", "#859900", Color(0xFF859900)),
            ColorSwatchInfo("String", "#2AA198", Color(0xFF2AA198)),
            ColorSwatchInfo("Function", "#268BD2", Color(0xFF268BD2)),
            ColorSwatchInfo("Type", "#B58900", Color(0xFFB58900))
        )
        ABYSS -> listOf(
            ColorSwatchInfo("Abyss Navy", "#000C18", Color(0xFF000C18)),
            ColorSwatchInfo("Gutter", "#051336", Color(0xFF051336)),
            ColorSwatchInfo("Keyword", "#225588", Color(0xFF569CD6)),
            ColorSwatchInfo("String", "#22AA44", Color(0xFF4EC9B0)),
            ColorSwatchInfo("Function", "#DDBB88", Color(0xFFFFEEBB)),
            ColorSwatchInfo("Number", "#F280D0", Color(0xFFF280D0))
        )
        NORD_FROST -> listOf(
            ColorSwatchInfo("Polar Night", "#2E3440", Color(0xFF2E3440)),
            ColorSwatchInfo("Surface", "#3B4252", Color(0xFF3B4252)),
            ColorSwatchInfo("Keyword", "#81A1C1", Color(0xFF81A1C1)),
            ColorSwatchInfo("String", "#A3BE8C", Color(0xFFA3BE8C)),
            ColorSwatchInfo("Function", "#88C0D0", Color(0xFF88C0D0)),
            ColorSwatchInfo("Number", "#B48EAD", Color(0xFFB48EAD))
        )
        MACOS_MIDNIGHT -> listOf(
            ColorSwatchInfo("Midnight Void", "#0B0F19", Color(0xFF0B0F19)),
            ColorSwatchInfo("Surface", "#111827", Color(0xFF111827)),
            ColorSwatchInfo("Sky Cyan", "#38BDF8", Color(0xFF38BDF8)),
            ColorSwatchInfo("Indigo", "#818CF8", Color(0xFF818CF8)),
            ColorSwatchInfo("Rose", "#F43F5E", Color(0xFFF43F5E)),
            ColorSwatchInfo("Emerald", "#34D399", Color(0xFF34D399))
        )
    }

    fun toColorScheme(): ColorScheme = if (isDark) {
        when (this) {
            DARCULA_ESPRESSO -> defaultDarculaScheme()
            DRACULA -> darkColorScheme(
                primary = Color(0xFFBD93F9),
                onPrimary = Color(0xFF21133A),
                primaryContainer = Color(0xFF44475A),
                onPrimaryContainer = Color(0xFFF8F8F2),
                secondary = Color(0xFF8BE9FD),
                background = Color(0xFF21222C),
                onBackground = Color(0xFFF8F8F2),
                surface = Color(0xFF282A36),
                onSurface = Color(0xFFF8F8F2),
                surfaceContainer = Color(0xFF282A36),
                surfaceContainerHigh = Color(0xFF343746),
                outlineVariant = Color(0xFF44475A)
            )
            ABYSS -> darkColorScheme(
                primary = Color(0xFFFFEEBB),
                onPrimary = Color(0xFF16213E),
                primaryContainer = Color(0xFF102A5C),
                onPrimaryContainer = Color(0xFFFFEEBB),
                secondary = Color(0xFF6688CC),
                background = Color(0xFF000C18),
                onBackground = Color(0xFFDDDDDD),
                surface = Color(0xFF051336),
                onSurface = Color(0xFFDDDDDD),
                surfaceContainer = Color(0xFF071A40),
                surfaceContainerHigh = Color(0xFF0E2554),
                outlineVariant = Color(0xFF1B366B)
            )
            MACOS_MIDNIGHT -> darkColorScheme(
                primary = Color(0xFF38BDF8),
                onPrimary = Color(0xFF082F49),
                primaryContainer = Color(0xFF0369A1),
                onPrimaryContainer = Color(0xFFE0F2FE),
                secondary = Color(0xFF818CF8),
                background = Color(0xFF0B0F19),
                onBackground = Color(0xFFF1F5F9),
                surface = Color(0xFF111827),
                onSurface = Color(0xFFF1F5F9),
                surfaceContainer = Color(0xFF151E2E),
                surfaceContainerHigh = Color(0xFF1D283A),
                outlineVariant = Color(0xFF1E293B)
            )
            MONOKAI_PRO -> darkColorScheme(
                primary = Color(0xFFFFD866),
                onPrimary = Color(0xFF332A00),
                primaryContainer = Color(0xFF7A6400),
                onPrimaryContainer = Color(0xFFFFF3CC),
                secondary = Color(0xFF78DCE8),
                background = Color(0xFF1E1F1C),
                onBackground = Color(0xFFFCFCFA),
                surface = Color(0xFF272822),
                onSurface = Color(0xFFFCFCFA),
                surfaceContainer = Color(0xFF2D2E27),
                surfaceContainerHigh = Color(0xFF383930),
                outlineVariant = Color(0xFF3E3D32)
            )
            GITHUB_DARK -> darkColorScheme(
                primary = Color(0xFF58A6FF),
                onPrimary = Color(0xFF051D4D),
                primaryContainer = Color(0xFF1F6FEB),
                onPrimaryContainer = Color(0xFFDDF4FF),
                secondary = Color(0xFFBC8CFF),
                background = Color(0xFF010409),
                onBackground = Color(0xFFF0F6FC),
                surface = Color(0xFF0D1117),
                onSurface = Color(0xFFF0F6FC),
                surfaceContainer = Color(0xFF11161E),
                surfaceContainerHigh = Color(0xFF181E28),
                outlineVariant = Color(0xFF21262D)
            )
            SOLARIZED_DARK -> darkColorScheme(
                primary = Color(0xFF268BD2),
                onPrimary = Color(0xFF00223D),
                primaryContainer = Color(0xFF104A73),
                onPrimaryContainer = Color(0xFFB3E5FC),
                secondary = Color(0xFFB58900),
                background = Color(0xFF002B36),
                onBackground = Color(0xFF93A1A1),
                surface = Color(0xFF073642),
                onSurface = Color(0xFFEEE8D5),
                surfaceContainer = Color(0xFF093B47),
                surfaceContainerHigh = Color(0xFF0E4958),
                outlineVariant = Color(0xFF093E4B)
            )
            NORD_FROST -> darkColorScheme(
                primary = Color(0xFF88C0D0),
                onPrimary = Color(0xFF122C34),
                primaryContainer = Color(0xFF4C566A),
                onPrimaryContainer = Color(0xFFECEFF4),
                secondary = Color(0xFF81A1C1),
                background = Color(0xFF242933),
                onBackground = Color(0xFFECEFF4),
                surface = Color(0xFF2E3440),
                onSurface = Color(0xFFECEFF4),
                surfaceContainer = Color(0xFF303744),
                surfaceContainerHigh = Color(0xFF394150),
                outlineVariant = Color(0xFF3B4252)
            )
            else -> defaultDarculaScheme()
        }
    } else {
        when (this) {
            QUIET_LIGHT -> lightColorScheme(
                primary = Color(0xFF3333AA),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE0E2FA),
                onPrimaryContainer = Color(0xFF10104E),
                secondary = Color(0xFF4B83CD),
                background = Color(0xFFF5F5F5),
                onBackground = Color(0xFF222222),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF222222),
                surfaceContainer = Color(0xFFECECEC),
                surfaceContainerHigh = Color(0xFFE2E2E2),
                outlineVariant = Color(0xFFD0D0D0)
            )
            GITHUB_LIGHT -> lightColorScheme(
                primary = Color(0xFF0969DA),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFDDF4FF),
                onPrimaryContainer = Color(0xFF051D4D),
                secondary = Color(0xFF8250DF),
                background = Color(0xFFFFFFFF),
                onBackground = Color(0xFF1F2328),
                surface = Color(0xFFF6F8FA),
                onSurface = Color(0xFF1F2328),
                surfaceContainer = Color(0xFFF6F8FA),
                surfaceContainerHigh = Color(0xFFEAECEF),
                outlineVariant = Color(0xFFD0D7DE)
            )
            SOLARIZED_LIGHT -> lightColorScheme(
                primary = Color(0xFF268BD2),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFDCEBF5),
                onPrimaryContainer = Color(0xFF002B36),
                secondary = Color(0xFFB58900),
                background = Color(0xFFFDF6E3),
                onBackground = Color(0xFF586E75),
                surface = Color(0xFFEEE8D5),
                onSurface = Color(0xFF586E75),
                surfaceContainer = Color(0xFFEEE8D5),
                surfaceContainerHigh = Color(0xFFE5DEC8),
                outlineVariant = Color(0xFFD3CBB8)
            )
            else -> defaultDarculaScheme()
        }
    }

    private fun androidContextOrNull(): Context? = null

    private fun defaultDarculaScheme(): ColorScheme = darkColorScheme(
        primary = Color(0xFFA8C7FA),
        onPrimary = Color(0xFF062E6F),
        primaryContainer = Color(0xFF0842A0),
        onPrimaryContainer = Color(0xFFD3E3FD),
        secondary = Color(0xFFC2C7CE),
        background = Color(0xFF121316),
        onBackground = Color(0xFFE3E2E6),
        surface = Color(0xFF1B1C20),
        onSurface = Color(0xFFE3E2E6),
        surfaceContainer = Color(0xFF1E2025),
        surfaceContainerHigh = Color(0xFF282A30),
        outlineVariant = Color(0xFF2F3239)
    )

    fun toSyntaxColors(): KotlinSyntaxColors = when (this) {
        DARCULA_ESPRESSO -> KotlinSyntaxColors(
            codeBackground = Color(0xFF2B2B2B),
            codeHeaderBackground = Color(0xFF313335),
            codeBorder = Color(0xFF3C3F41),
            lineNumber = Color(0xFF606366),
            plainText = Color(0xFFA9B7C6),
            keyword = Color(0xFFCC7832),
            stringLiteral = Color(0xFF6A8759),
            comment = Color(0xFF808080),
            numberLiteral = Color(0xFF6897BB),
            typeName = Color(0xFFB5B6E3),
            functionName = Color(0xFFFFC66D),
            annotation = Color(0xFFBBB529),
            explanationBackground = Color(0xFF252830),
            explanationAccentBorder = Color(0xFFA8C7FA)
        )
        QUIET_LIGHT -> KotlinSyntaxColors(
            codeBackground = Color(0xFFF5F5F5),
            codeHeaderBackground = Color(0xFFECECEC),
            codeBorder = Color(0xFFD6D6D6),
            lineNumber = Color(0xFF999999),
            plainText = Color(0xFF333333),
            keyword = Color(0xFF4B83CD),
            stringLiteral = Color(0xFF448C27),
            comment = Color(0xFF8C8C8C),
            numberLiteral = Color(0xFFAB6526),
            typeName = Color(0xFF7A3E9D),
            functionName = Color(0xFFAA3731),
            annotation = Color(0xFF9C5D27),
            explanationBackground = Color(0xFFFFFFFF),
            explanationAccentBorder = Color(0xFF0B57D0)
        )
        DRACULA -> KotlinSyntaxColors(
            codeBackground = Color(0xFF282A36),
            codeHeaderBackground = Color(0xFF21222C),
            codeBorder = Color(0xFF44475A),
            lineNumber = Color(0xFF6272A4),
            plainText = Color(0xFFF8F8F2),
            keyword = Color(0xFFFF79C6),
            stringLiteral = Color(0xFFF1FA8C),
            comment = Color(0xFF6272A4),
            numberLiteral = Color(0xFFBD93F9),
            typeName = Color(0xFF8BE9FD),
            functionName = Color(0xFF50FA7B),
            annotation = Color(0xFFFFB86C),
            explanationBackground = Color(0xFF21222C),
            explanationAccentBorder = Color(0xFFBD93F9)
        )
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
            codeBackground = Color(0xFF272822),
            codeHeaderBackground = Color(0xFF221F22),
            codeBorder = Color(0xFF49483E),
            lineNumber = Color(0xFF8F908A),
            plainText = Color(0xFFF8F8F2),
            keyword = Color(0xFFF92672),
            stringLiteral = Color(0xFFE6DB74),
            comment = Color(0xFF75715E),
            numberLiteral = Color(0xFFAE81FF),
            typeName = Color(0xFF66D9EF),
            functionName = Color(0xFFA6E22E),
            annotation = Color(0xFFFD971F),
            explanationBackground = Color(0xFF221F22),
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
        GITHUB_LIGHT -> KotlinSyntaxColors(
            codeBackground = Color(0xFFFFFFFF),
            codeHeaderBackground = Color(0xFFF6F8FA),
            codeBorder = Color(0xFFD0D7DE),
            lineNumber = Color(0xFF8C959F),
            plainText = Color(0xFF1F2328),
            keyword = Color(0xFFCF222E),
            stringLiteral = Color(0xFF0A3069),
            comment = Color(0xFF6E7781),
            numberLiteral = Color(0xFF0550AE),
            typeName = Color(0xFF953800),
            functionName = Color(0xFF8250DF),
            annotation = Color(0xFF116329),
            explanationBackground = Color(0xFFF6F8FA),
            explanationAccentBorder = Color(0xFF0969DA)
        )
        SOLARIZED_DARK -> KotlinSyntaxColors(
            codeBackground = Color(0xFF002B36),
            codeHeaderBackground = Color(0xFF073642),
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
            explanationBackground = Color(0xFF073642),
            explanationAccentBorder = Color(0xFF268BD2)
        )
        SOLARIZED_LIGHT -> KotlinSyntaxColors(
            codeBackground = Color(0xFFFDF6E3),
            codeHeaderBackground = Color(0xFFEEE8D5),
            codeBorder = Color(0xFFD3CBB8),
            lineNumber = Color(0xFF93A1A1),
            plainText = Color(0xFF586E75),
            keyword = Color(0xFF859900),
            stringLiteral = Color(0xFF2AA198),
            comment = Color(0xFF93A1A1),
            numberLiteral = Color(0xFFD33682),
            typeName = Color(0xFFB58900),
            functionName = Color(0xFF268BD2),
            annotation = Color(0xFFCB4B16),
            explanationBackground = Color(0xFFEEE8D5),
            explanationAccentBorder = Color(0xFF268BD2)
        )
        ABYSS -> KotlinSyntaxColors(
            codeBackground = Color(0xFF000C18),
            codeHeaderBackground = Color(0xFF051336),
            codeBorder = Color(0xFF1B366B),
            lineNumber = Color(0xFF406385),
            plainText = Color(0xFF6688CC),
            keyword = Color(0xFF2299DD),
            stringLiteral = Color(0xFF22AA44),
            comment = Color(0xFF384887),
            numberLiteral = Color(0xFFF280D0),
            typeName = Color(0xFFFFEEBB),
            functionName = Color(0xFFDDBB88),
            annotation = Color(0xFF9966B8),
            explanationBackground = Color(0xFF051336),
            explanationAccentBorder = Color(0xFFFFEEBB)
        )
        NORD_FROST -> KotlinSyntaxColors(
            codeBackground = Color(0xFF2E3440),
            codeHeaderBackground = Color(0xFF3B4252),
            codeBorder = Color(0xFF4C566A),
            lineNumber = Color(0xFF616E88),
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
