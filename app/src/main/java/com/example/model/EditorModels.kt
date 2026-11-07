package com.example.model

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RobotoFontFamily
import org.json.JSONObject

enum class EditorFontChoice(val displayName: String, val badge: String) {
    PLUS_JAKARTA("Jakarta Sans", "Sans"),
    MERRIWEATHER("Merriweather", "Serif"),
    ROBOTO("Roboto", "Clean"),
    JETBRAINS_MONO("JetBrains Mono", "Mono");

    fun toComposeFontFamily(): FontFamily = when (this) {
        PLUS_JAKARTA -> PlusJakartaSansFontFamily
        MERRIWEATHER -> MerriweatherFontFamily
        ROBOTO -> RobotoFontFamily
        JETBRAINS_MONO -> JetBrainsMonoFontFamily
    }
}

enum class TextAlignmentChoice(val displayName: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right");

    fun toComposeTextAlign(): TextAlign = when (this) {
        LEFT -> TextAlign.Start
        CENTER -> TextAlign.Center
        RIGHT -> TextAlign.End
    }
}

data class TextFormattingStyle(
    val fontChoice: EditorFontChoice = EditorFontChoice.PLUS_JAKARTA,
    val fontSizeSp: Int = 16,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val alignment: TextAlignmentChoice = TextAlignmentChoice.LEFT
) {
    fun toJsonString(): String {
        return listOf(
            "fontChoice=${fontChoice.name}",
            "fontSizeSp=${fontSizeSp.coerceIn(12, 32)}",
            "isBold=$isBold",
            "isItalic=$isItalic",
            "isUnderline=$isUnderline",
            "isStrikethrough=$isStrikethrough",
            "alignment=${alignment.name}"
        ).joinToString(";")
    }

    companion object {
        val AVAILABLE_FONT_SIZES = listOf(12, 14, 16, 18, 20, 24, 28, 32)

        fun fromJsonString(raw: String?): TextFormattingStyle {
            if (raw.isNullOrBlank()) return TextFormattingStyle()
            return try {
                val map = raw.split(";")
                    .mapNotNull { part ->
                        val idx = part.indexOf('=')
                        if (idx > 0) part.substring(0, idx).trim() to part.substring(idx + 1).trim()
                        else null
                    }
                    .toMap()

                val fontName = map["fontChoice"] ?: EditorFontChoice.PLUS_JAKARTA.name
                val alignName = map["alignment"] ?: TextAlignmentChoice.LEFT.name
                TextFormattingStyle(
                    fontChoice = EditorFontChoice.entries.find { it.name == fontName }
                        ?: EditorFontChoice.PLUS_JAKARTA,
                    fontSizeSp = (map["fontSizeSp"]?.toIntOrNull() ?: 16).coerceIn(12, 32),
                    isBold = map["isBold"]?.toBooleanStrictOrNull() ?: false,
                    isItalic = map["isItalic"]?.toBooleanStrictOrNull() ?: false,
                    isUnderline = map["isUnderline"]?.toBooleanStrictOrNull() ?: false,
                    isStrikethrough = map["isStrikethrough"]?.toBooleanStrictOrNull() ?: false,
                    alignment = TextAlignmentChoice.entries.find { it.name == alignName }
                        ?: TextAlignmentChoice.LEFT
                )
            } catch (_: Exception) {
                TextFormattingStyle()
            }
        }
    }
}

data class CodeSectionUiModel(
    val id: Long = 0L,
    val documentId: Long = 0L,
    val orderIndex: Int = 0,
    val sectionTitle: String = "",
    val isAiNamed: Boolean = false,
    val isNamingInProgress: Boolean = false,
    val lastNamedHash: String = "",
    val codeContent: String = "",
    val explanationText: String = "",
    val explanationStyle: TextFormattingStyle = TextFormattingStyle(),
    val showDivider: Boolean = true
) {
    val displayTitle: String
        get() = sectionTitle.ifBlank { "Section ${orderIndex + 1} • Untitled Kotlin Block" }

    val lineCount: Int
        get() = codeContent.split('\n').size.coerceAtLeast(1)

    val visibleLineSlots: Int
        get() = lineCount.coerceAtLeast(4) // Always starts at 4 lines and expands automatically
}

data class DocumentSummaryUiModel(
    val id: Long,
    val title: String,
    val previewSnippet: String,
    val sectionCount: Int,
    val updatedAt: Long,
    val wordCount: Int
)

data class DocumentDetailUiModel(
    val id: Long,
    val title: String,
    val introText: String,
    val introStyle: TextFormattingStyle,
    val sections: List<CodeSectionUiModel>,
    val updatedAt: Long
) {
    val totalWordCount: Int
        get() {
            val introWords = introText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
            val sectionWords = sections.sumOf { sec ->
                val codeTokens = sec.codeContent.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                val expWords = sec.explanationText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                codeTokens + expWords
            }
            return introWords + sectionWords
        }

    val totalCharacterCount: Int
        get() = introText.length + sections.sumOf { it.codeContent.length + it.explanationText.length }
}

sealed interface ActiveEditorTarget {
    data object Intro : ActiveEditorTarget
    data class SectionCode(val sectionId: Long, val orderIndex: Int) : ActiveEditorTarget
    data class SectionExplanation(val sectionId: Long, val orderIndex: Int) : ActiveEditorTarget
}
