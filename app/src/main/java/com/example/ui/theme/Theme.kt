package com.example.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun KtScribeTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    activeTheme: AppThemeId = AppThemeId.DARCULA_ESPRESSO,
    codeAssistAppTheme: CodeAssistAppTheme = CodeAssistAppTheme.CODEASSIST_DARK,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = codeAssistAppTheme.forcesDark ?: when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = codeAssistAppTheme.resolveColorScheme(
        context = context,
        isDarkEffective = useDarkTheme
    )
    val syntaxColors = activeTheme.toSyntaxColors()

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
        codeAssistAppTheme = if (darkTheme) CodeAssistAppTheme.CODEASSIST_DARK else CodeAssistAppTheme.CODEASSIST_LIGHT,
        content = content
    )
}
