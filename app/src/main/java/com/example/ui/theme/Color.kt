package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Mode: Warm Parchment & Espresso Darcula Studio Palette (Zero Neon)
val LightBackground = Color(0xFFF5F3EF)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEBE7E0)
val LightPrimary = Color(0xFF9A5B25) // Warm Amber Bronze
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFF5E6D3)
val LightOnPrimaryContainer = Color(0xFF3E2208)
val LightSecondary = Color(0xFF4A6B82) // Muted Steel Slate
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFDCE7F0)
val LightOnSecondaryContainer = Color(0xFF162B3A)
val LightTertiary = Color(0xFF5D7052) // Muted Olive Sage
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFE1E9DC)
val LightOnTertiaryContainer = Color(0xFF1E2918)
val LightOnBackground = Color(0xFF1C1A17)
val LightOnSurface = Color(0xFF1C1A17)
val LightOnSurfaceVariant = Color(0xFF5C564E)
val LightOutline = Color(0xFFC9C2B8)
val LightOutlineVariant = Color(0xFFE0DBD1)

// Dark Mode: Matte JetBrains Darcula & Warm Mocha 4-Tier Depth Palette (Zero Neon)
val DarkBackground = Color(0xFF11100F)         // Tier 0: Recessed Outer Workspace Trench
val DarkSurface = Color(0xFF1B1A19)            // Tier 1: Beveled Code Block Canvas
val DarkSurfaceVariant = Color(0xFF252320)     // Tier 2: Cursor IDE Completion & Surface Card
val DarkPrimary = Color(0xFFD4883A)            // Warm Amber Ochre (No Neon)
val DarkOnPrimary = Color(0xFF241404)
val DarkPrimaryContainer = Color(0xFF75420E)   // Exact IDE Completion Selection Highlight (#75420E)
val DarkOnPrimaryContainer = Color(0xFFF5E6D3)
val DarkSecondary = Color(0xFF8CA6B8)          // Muted Steel Blue
val DarkOnSecondary = Color(0xFF13232E)
val DarkSecondaryContainer = Color(0xFF2B3842)
val DarkOnSecondaryContainer = Color(0xFFDCE7F0)
val DarkTertiary = Color(0xFF8AA87B)           // Muted Sage Olive
val DarkOnTertiary = Color(0xFF162410)
val DarkTertiaryContainer = Color(0xFF2A3824)
val DarkOnTertiaryContainer = Color(0xFFDCE8D5)
val DarkOnBackground = Color(0xFFE6E1DC)
val DarkOnSurface = Color(0xFFE6E1DC)
val DarkOnSurfaceVariant = Color(0xFFA39C93)
val DarkOutline = Color(0xFF423D38)
val DarkOutlineVariant = Color(0xFF2E2A27)

// 4-Tier Physical Window Depth Tokens
val Tier0OuterTrenchBg = Color(0xFF11100F)
val Tier1CodeCanvasBg = Color(0xFF1B1A19)
val Tier1CodeGutterBg = Color(0xFF151413)
val Tier1CodeHeaderBg = Color(0xFF22201E)
val Tier1CodeBevelTop = Color(0xFF3A3530)
val Tier1CodeOuterRim = Color(0xFF080707)

val Tier2CompletionBg = Color(0xFF252320)
val Tier2CompletionDocBg = Color(0xFF2C2926)
val Tier2CompletionSelectedBg = Color(0xFF75420E) // Exact warm amber-brown from IDE screenshot
val Tier2CompletionBorder = Color(0xFF4C463F)

val Tier3FloatingWindowActiveBg = Color(0xFF2C2825)
val Tier3FloatingWindowActiveHeader = Color(0xFF36312D)
val Tier3FloatingWindowActiveBevel = Color(0xFF63594F)
val Tier3FloatingWindowInactiveBg = Color(0xFF23201E)
val Tier3FloatingWindowInactiveHeader = Color(0xFF2B2724)
val Tier3FloatingWindowInactiveBevel = Color(0xFF47413B)

val IdeInlineErrorPillBg = Color(0xFF4E2624)
val IdeInlineErrorPillBorder = Color(0xFF823D39)
val IdeInlineErrorPillText = Color(0xFFF2B8B5)

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
    codeBackground = Color(0xFFF5F3EF),
    codeHeaderBackground = Color(0xFFEBE7E0),
    codeBorder = Color(0xFFC9C2B8),
    lineNumber = Color(0xFF7D756B),
    plainText = Color(0xFF1C1A17),
    keyword = Color(0xFFB35012),       // Classic JetBrains Warm Terracotta for keywords
    stringLiteral = Color(0xFF4B7332), // Muted Olive Green for strings
    comment = Color(0xFF7D756B),       // Warm Grey for comments
    numberLiteral = Color(0xFF2E6B8E), // Muted Steel Blue for numbers
    typeName = Color(0xFF6B4C8C),      // Muted Plum for types/classes
    functionName = Color(0xFF8C5A1C),  // Warm Ochre for function declarations
    annotation = Color(0xFF946B1E),    // Muted Gold for @Annotations
    explanationBackground = Color(0xFFFAF8F5),
    explanationAccentBorder = Color(0xFF9A5B25)
)

// Authentic JetBrains Darcula Syntax Colors (matching Screenshots 3 & 4)
val DarkKotlinSyntaxColors = KotlinSyntaxColors(
    codeBackground = Tier1CodeCanvasBg,
    codeHeaderBackground = Tier1CodeHeaderBg,
    codeBorder = Color(0xFF332F2B),
    lineNumber = Color(0xFF6E6862),
    plainText = Color(0xFFA9B7C6),
    keyword = Color(0xFFCC7832),       // Authentic Darcula Warm Terracotta Orange (`fun`, `val`, `var`)
    stringLiteral = Color(0xFF6A8759), // Authentic Darcula Sage Green (`"..."`)
    comment = Color(0xFF808080),       // Authentic Darcula Muted Grey (`// ...`)
    numberLiteral = Color(0xFF6897BB), // Authentic Darcula Steel Blue (`42`, `true`)
    typeName = Color(0xFFB5B6E3),      // Soft Periwinkle Slate (`String`, `Function0`)
    functionName = Color(0xFFFFC66D),  // Authentic Darcula Warm Gold (`main()`, `println()`)
    annotation = Color(0xFFBBB529),    // Authentic Darcula Olive Gold (`@FunctionalInterface`)
    explanationBackground = Tier3FloatingWindowActiveBg,
    explanationAccentBorder = Color(0xFFD4883A)
)

val LocalKotlinSyntaxColors = staticCompositionLocalOf { DarkKotlinSyntaxColors }
