package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Mode: Nordic Alabaster & Executive Emerald-Sapphire Studio Palette
val LightBackground = Color(0xFFF3F6FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE8EEF5)
val LightPrimary = Color(0xFF059669) // Executive Emerald Teal
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD1FAE5)
val LightOnPrimaryContainer = Color(0xFF064E3B)
val LightSecondary = Color(0xFF0284C7) // Precision Sapphire Cyan
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE0F2FE)
val LightOnSecondaryContainer = Color(0xFF0C4A6E)
val LightTertiary = Color(0xFF6366F1) // Indigo Concept Tag Accent
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFE0E7FF)
val LightOnTertiaryContainer = Color(0xFF1E1B4B)
val LightOnBackground = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFCBD5E1)
val LightOutlineVariant = Color(0xFFE2E8F0)

// Dark Mode: Midnight Carbon & Luminous Emerald-Cyan Pro IDE Palette
val DarkBackground = Color(0xFF090D14)
val DarkSurface = Color(0xFF111722)
val DarkSurfaceVariant = Color(0xFF1A2232)
val DarkPrimary = Color(0xFF10B981) // Luminous Emerald
val DarkOnPrimary = Color(0xFF022C22)
val DarkPrimaryContainer = Color(0xFF064E3B)
val DarkOnPrimaryContainer = Color(0xFFA7F3D0)
val DarkSecondary = Color(0xFF38BDF8) // Electric Sky Cyan
val DarkOnSecondary = Color(0xFF082F49)
val DarkSecondaryContainer = Color(0xFF075985)
val DarkOnSecondaryContainer = Color(0xFFE0F2FE)
val DarkTertiary = Color(0xFFA78BFA) // Soft Violet for Learned Concept Badges
val DarkOnTertiary = Color(0xFF2E1065)
val DarkTertiaryContainer = Color(0xFF4C1D95)
val DarkOnTertiaryContainer = Color(0xFFEDE9FE)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)
val DarkOutlineVariant = Color(0xFF1E293B)

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
    codeBackground = Color(0xFFF1F5F9),
    codeHeaderBackground = Color(0xFFE2E8F0),
    codeBorder = Color(0xFFCBD5E1),
    lineNumber = Color(0xFF64748B),
    plainText = Color(0xFF0F172A),
    keyword = Color(0xFF7C3AED),       // Crisp Violet for fun, val, var, class, etc.
    stringLiteral = Color(0xFF059669), // Emerald for strings
    comment = Color(0xFF64748B),       // Muted Slate for comments
    numberLiteral = Color(0xFFD97706), // Amber for numbers & booleans
    typeName = Color(0xFF0284C7),      // Sapphire for types/classes
    functionName = Color(0xFFDB2777),  // Rose for function declarations/calls
    annotation = Color(0xFFB45309),    // Bronze for @Annotations
    explanationBackground = Color(0xFFF8FAFC),
    explanationAccentBorder = Color(0xFF059669)
)

val DarkKotlinSyntaxColors = KotlinSyntaxColors(
    codeBackground = Color(0xFF0D1117),
    codeHeaderBackground = Color(0xFF161B22),
    codeBorder = Color(0xFF30363D),
    lineNumber = Color(0xFF6E7681),
    plainText = Color(0xFFE6EDF3),
    keyword = Color(0xFFFF7B72),       // Apple Xcode / GitHub Dark Coral-Pink for keywords
    stringLiteral = Color(0xFF7EE787), // Apple Terminal Bright Green for strings
    comment = Color(0xFF8B949E),       // Muted Grey-Blue for comments
    numberLiteral = Color(0xFF79C0FF), // Crisp Sky Blue for numbers & booleans
    typeName = Color(0xFFFFA657),      // Warm Orange for types/classes
    functionName = Color(0xFFD2A8FF),  // Soft Lavender for functions
    annotation = Color(0xFFE3B341),    // Gold for @Annotations
    explanationBackground = Color(0xFF111722),
    explanationAccentBorder = Color(0xFF10B981)
)

val LocalKotlinSyntaxColors = staticCompositionLocalOf { LightKotlinSyntaxColors }
