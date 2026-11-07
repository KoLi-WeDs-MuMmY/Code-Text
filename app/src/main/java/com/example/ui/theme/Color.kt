package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Mode Editorial Studio Palette
val LightBackground = Color(0xFFFAF8F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF2EFE9)
val LightPrimary = Color(0xFF6B3DF0) // Kotlin Violet
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFEDE7FF)
val LightOnPrimaryContainer = Color(0xFF23005C)
val LightSecondary = Color(0xFFE65A37) // Kotlin Coral
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFDBD2)
val LightOnSecondaryContainer = Color(0xFF3A0B00)
val LightTertiary = Color(0xFF0E7490)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFCFFAFE)
val LightOnTertiaryContainer = Color(0xFF001F28)
val LightOnBackground = Color(0xFF18181B)
val LightOnSurface = Color(0xFF18181B)
val LightOnSurfaceVariant = Color(0xFF52525B)
val LightOutline = Color(0xFFD4D4D8)
val LightOutlineVariant = Color(0xFFE4E4E7)

// Dark Mode Obsidian Studio Palette
val DarkBackground = Color(0xFF0F1117)
val DarkSurface = Color(0xFF181B24)
val DarkSurfaceVariant = Color(0xFF222634)
val DarkPrimary = Color(0xFFA37FFF)
val DarkOnPrimary = Color(0xFF1E0052)
val DarkPrimaryContainer = Color(0xFF3A1D7A)
val DarkOnPrimaryContainer = Color(0xFFEDE7FF)
val DarkSecondary = Color(0xFFFF8A6C)
val DarkOnSecondary = Color(0xFF4C1100)
val DarkSecondaryContainer = Color(0xFF6D1E07)
val DarkOnSecondaryContainer = Color(0xFFFFDBD2)
val DarkTertiary = Color(0xFF38BDF8)
val DarkOnTertiary = Color(0xFF003549)
val DarkTertiaryContainer = Color(0xFF004D67)
val DarkOnTertiaryContainer = Color(0xFFCFFAFE)
val DarkOnBackground = Color(0xFFF3F4F6)
val DarkOnSurface = Color(0xFFF3F4F6)
val DarkOnSurfaceVariant = Color(0xFF9CA3AF)
val DarkOutline = Color(0xFF374151)
val DarkOutlineVariant = Color(0xFF282D3C)

@Immutable
data class KotlinSyntaxColors(
    val codeBackground: Color,
    val codeHeaderBackground: Color,
    val codeBorder: Color,
    val lineNumber: Color,
    val plainText: Color,
    val keyword: Color,
    val stringLiteral: Color,
    val comment: Color,
    val numberLiteral: Color,
    val typeName: Color,
    val functionName: Color,
    val annotation: Color,
    val explanationBackground: Color,
    val explanationAccentBorder: Color
)

val LightKotlinSyntaxColors = KotlinSyntaxColors(
    codeBackground = Color(0xFFF4F6FA),
    codeHeaderBackground = Color(0xFFE9EDF5),
    codeBorder = Color(0xFFD5DCE8),
    lineNumber = Color(0xFF8A94A6),
    plainText = Color(0xFF1E293B),
    keyword = Color(0xFF7C3AED),       // Vibrant Violet for fun, val, var, class, etc.
    stringLiteral = Color(0xFF047857), // Deep Emerald for strings
    comment = Color(0xFF64748B),       // Muted Slate for comments
    numberLiteral = Color(0xFFD97706), // Warm Amber for numbers & booleans
    typeName = Color(0xFF0284C7),      // Ocean Blue for types/classes
    functionName = Color(0xFFBE185D),  // Magenta Rose for function declarations/calls
    annotation = Color(0xFFB45309),    // Bronze for @Annotations
    explanationBackground = Color(0xFFFBF9FF),
    explanationAccentBorder = Color(0xFF7C3AED)
)

val DarkKotlinSyntaxColors = KotlinSyntaxColors(
    codeBackground = Color(0xFF12151E),
    codeHeaderBackground = Color(0xFF1A1E2B),
    codeBorder = Color(0xFF2B3245),
    lineNumber = Color(0xFF56617A),
    plainText = Color(0xFFE2E8F0),
    keyword = Color(0xFFC084FC),       // Electric Purple for fun, val, var, class, etc.
    stringLiteral = Color(0xFF34D399), // Mint Emerald for strings
    comment = Color(0xFF64748B),       // Slate for comments
    numberLiteral = Color(0xFFFBBF24), // Gold Amber for numbers & booleans
    typeName = Color(0xFF38BDF8),      // Sky Cyan for types/classes
    functionName = Color(0xFFF472B6),  // Neon Pink for functions
    annotation = Color(0xFFF59E0B),    // Amber for @Annotations
    explanationBackground = Color(0xFF161925),
    explanationAccentBorder = Color(0xFFA37FFF)
)

val LocalKotlinSyntaxColors = staticCompositionLocalOf { LightKotlinSyntaxColors }
