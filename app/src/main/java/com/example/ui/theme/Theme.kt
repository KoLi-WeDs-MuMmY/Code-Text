package com.example.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceDim = Color(0xFF0E0D0C),
    surfaceBright = Color(0xFF36322E),
    surfaceContainerLowest = Color(0xFF0D0C0B),
    surfaceContainerLow = Color(0xFF171514),
    surfaceContainer = Color(0xFF1E1C1A),
    surfaceContainerHigh = Color(0xFF272421),
    surfaceContainerHighest = Color(0xFF322E2A),
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceDim = Color(0xFFE6E1D9),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F6F2),
    surfaceContainer = Color(0xFFF1EDE6),
    surfaceContainerHigh = Color(0xFFEBE6DD),
    surfaceContainerHighest = Color(0xFFE3DDD3),
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun KtScribeTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    activeTheme: AppThemeId = AppThemeId.DARCULA_ESPRESSO,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (!useDarkTheme) {
        LightColorScheme
    } else {
        activeTheme.toColorScheme()
    }
    val syntaxColors = if (!useDarkTheme) {
        LightKotlinSyntaxColors
    } else {
        activeTheme.toSyntaxColors()
    }
    val context = LocalContext.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(300)
        val heightDp = maxHeight.value.toInt().coerceAtLeast(300)
        val profile = ScreenRatioClassifier.readDeviceDisplayProfile(
            context = context,
            containerWidthDp = widthDp,
            containerHeightDp = heightDp
        )
        val adaptiveSpec = ScreenRatioClassifier.resolveThemeSpec(profile)
        val adaptiveShapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(adaptiveSpec.cardCornerRadius),
            large = RoundedCornerShape((adaptiveSpec.cardCornerRadius.value + 4f).dp),
            extraLarge = RoundedCornerShape((adaptiveSpec.cardCornerRadius.value + 8f).dp)
        )

        CompositionLocalProvider(
            LocalKotlinSyntaxColors provides syntaxColors,
            LocalAdaptiveThemeSpec provides adaptiveSpec
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography,
                shapes = adaptiveShapes,
                content = content
            )
        }
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    KtScribeTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        content = content
    )
}
