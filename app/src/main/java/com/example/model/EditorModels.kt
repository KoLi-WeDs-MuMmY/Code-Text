package com.example.model

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RobotoFontFamily

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

/**
 * Controls how the paired Explanation Block is displayed relative to the full-screen Apple `cmd` Code Editor:
 * - FLOATING_WINDOW: Draggable & resizable macOS floating window overlay (like macOS Notes).
 * - SPLIT_SCREEN: Set aside into a two-window resizable split-screen workspace (Code Window + Explanation Window).
 * - HIDDEN: Minimized so the code block has 100% unobstructed screen space.
 */
enum class ExplanationWindowMode(val label: String) {
    FLOATING_WINDOW("Floating Window"),
    SPLIT_SCREEN("2-Window Split"),
    HIDDEN("Minimized")
}

/**
 * Controls how a docked floating window splits the screen with the full-screen `.ktscribe` code block:
 * - SIDE_BY_SIDE: Code block on the left, docked window on the right with a vertical draggable divider.
 * - BOTTOM_SPLIT: Code block on the top, docked window on the bottom with a horizontal draggable divider.
 */
enum class SplitScreenOrientation(val label: String, val badge: String) {
    SIDE_BY_SIDE("Side-by-Side Split", "◫ Side"),
    BOTTOM_SPLIT("Bottom Split", "⬒ Bottom")
}

/**
 * The 4 top-right oval pill floating windows from the user's sketch.
 * Up to 2 floating windows can be open at the same time (excluding the bottom-docked Command Block),
 * dragged anywhere across the entire screen, converted into an adjustable split screen, resized, or canceled (`✕`).
 */
enum class FloatingWindowType(
    val pillLabel: String,
    val windowTitle: String,
    val windowSubtitle: String,
    val minWidthDp: Float,
    val minHeightDp: Float,
    val defaultHorizontalWidthDp: Float,
    val defaultHorizontalHeightDp: Float,
    val defaultVerticalWidthDp: Float,
    val defaultVerticalHeightDp: Float
) {
    EXPLANATION(
        pillLabel = "Explain",
        windowTitle = "Explanation & Notes",
        windowSubtitle = "Bundled inside file • Float or Split",
        minWidthDp = 270f,
        minHeightDp = 220f,
        defaultHorizontalWidthDp = 370f,
        defaultHorizontalHeightDp = 290f,
        defaultVerticalWidthDp = 320f,
        defaultVerticalHeightDp = 420f
    ),
    FOLDERS(
        pillLabel = "Files",
        windowTitle = "Folders & Language Files",
        windowSubtitle = "Directories & files • 1 File = 1 Code Block",
        minWidthDp = 270f,
        minHeightDp = 240f,
        defaultHorizontalWidthDp = 370f,
        defaultHorizontalHeightDp = 310f,
        defaultVerticalWidthDp = 320f,
        defaultVerticalHeightDp = 440f
    ),
    AI_STUDIO(
        pillLabel = "AI Guide",
        windowTitle = "AI Concept & Multi-Language Studio",
        windowSubtitle = "Deep-dive explanation, syntax fix & KDoc",
        minWidthDp = 280f,
        minHeightDp = 250f,
        defaultHorizontalWidthDp = 390f,
        defaultHorizontalHeightDp = 330f,
        defaultVerticalWidthDp = 330f,
        defaultVerticalHeightDp = 460f
    ),
    RUN_OUTPUT(
        pillLabel = "Run ▶",
        windowTitle = "Terminal Output & Console",
        windowSubtitle = "Compiler diagnostics & script evaluation",
        minWidthDp = 270f,
        minHeightDp = 200f,
        defaultHorizontalWidthDp = 380f,
        defaultHorizontalHeightDp = 260f,
        defaultVerticalWidthDp = 320f,
        defaultVerticalHeightDp = 380f
    );

    val defaultWidthDp: Float get() = defaultHorizontalWidthDp
    val defaultHeightDp: Float get() = defaultHorizontalHeightDp

    fun getTierWidthDp(tier: WindowPresetTier, screenWidthDp: Float = 600f): Float = when (tier) {
        WindowPresetTier.MINI -> when (this) {
            EXPLANATION -> 280f
            FOLDERS -> 260f
            AI_STUDIO -> 280f
            RUN_OUTPUT -> 300f
        }
        WindowPresetTier.HALF -> when (this) {
            EXPLANATION -> 380f
            FOLDERS -> 340f
            AI_STUDIO -> 360f
            RUN_OUTPUT -> 420f
        }
        WindowPresetTier.TWO_THIRDS -> when (this) {
            EXPLANATION -> 460f
            FOLDERS -> 440f
            AI_STUDIO -> 460f
            RUN_OUTPUT -> 520f
        }
        WindowPresetTier.FULL -> (screenWidthDp * 0.94f).coerceAtLeast(340f)
    }

    fun getTierHeightDp(tier: WindowPresetTier, screenHeightDp: Float = 800f): Float = when (tier) {
        WindowPresetTier.MINI -> when (this) {
            EXPLANATION -> 200f
            FOLDERS -> 240f
            AI_STUDIO -> 220f
            RUN_OUTPUT -> 180f
        }
        WindowPresetTier.HALF -> when (this) {
            EXPLANATION -> 300f
            FOLDERS -> 360f
            AI_STUDIO -> 340f
            RUN_OUTPUT -> 260f
        }
        WindowPresetTier.TWO_THIRDS -> when (this) {
            EXPLANATION -> 440f
            FOLDERS -> 500f
            AI_STUDIO -> 470f
            RUN_OUTPUT -> 390f
        }
        WindowPresetTier.FULL -> (screenHeightDp * 0.86f).coerceAtLeast(360f)
    }

    fun getClosestTier(currentWidthDp: Float, currentHeightDp: Float, screenWidthDp: Float = 600f, screenHeightDp: Float = 800f): WindowPresetTier {
        return WindowPresetTier.entries.minByOrNull { tier ->
            val targetW = getTierWidthDp(tier, screenWidthDp)
            val targetH = getTierHeightDp(tier, screenHeightDp)
            val dw = currentWidthDp - targetW
            val dh = currentHeightDp - targetH
            dw * dw + dh * dh
        } ?: WindowPresetTier.HALF
    }
}

/**
 * Discrete floating window dimension tiers (MIUI / iOS / HarmonyOS style).
 * Every floating window snaps exclusively to one of these 4 discrete tiers.
 */
enum class WindowPresetTier(val label: String, val badge: String) {
    MINI("Mini", "▫"),
    HALF("Half", "◫"),
    TWO_THIRDS("2/3", "◰"),
    FULL("Full", "◻")
}

/**
 * Controls which modal or bottom-docked IDE panel is open (such as the Bottom Command Block).
 */
enum class IslandDropdownMenu {
    NONE,
    COMMAND_PALETTE,
    FOLDER_AND_SECTIONS,
    AI_STUDIO,
    TERMINAL_OUTPUT
}

/**
 * Filter categories inside the Bottom-Docked IDE Command Block.
 */
enum class CommandPaletteFilter(val label: String) {
    ALL("All"),
    COMMANDS("Commands"),
    FOLDERS("Files & Folders"),
    SYMBOLS("Symbols"),
    CONCEPTS("Concepts")
}

data class TextFormattingStyle(
    val fontChoice: EditorFontChoice = EditorFontChoice.PLUS_JAKARTA,
    val fontSizeSp: Int = 16,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val alignment: TextAlignmentChoice = TextAlignmentChoice.LEFT,
    val learnedConceptTag: String = ""
) {
    fun toJsonString(): String {
        val safeTag = learnedConceptTag
            .replace(";", ",")
            .replace("=", ":")
            .replace("\n", " ")
            .trim()
        return listOf(
            "fontChoice=${fontChoice.name}",
            "fontSizeSp=${fontSizeSp.coerceIn(12, 32)}",
            "isBold=$isBold",
            "isItalic=$isItalic",
            "isUnderline=$isUnderline",
            "isStrikethrough=$isStrikethrough",
            "alignment=${alignment.name}",
            "learnedConceptTag=$safeTag"
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
                        ?: TextAlignmentChoice.LEFT,
                    learnedConceptTag = map["learnedConceptTag"].orEmpty()
                )
            } catch (_: Exception) {
                TextFormattingStyle()
            }
        }
    }
}

enum class LanguageType(
    val id: String,
    val displayName: String,
    val extension: String,
    val badgeLabel: String,
    val defaultFileName: String,
    val directoryPath: String,
    val runCommandPrefix: String,
    val starterCode: String
) {
    KOTLIN(
        id = "kotlin",
        displayName = "Kotlin",
        extension = ".ktscribe",
        badgeLabel = "KT",
        defaultFileName = "Main.ktscribe",
        directoryPath = "src/kotlin/",
        runCommandPrefix = "kotlinc",
        starterCode = "fun main() {\n    println(\"Hello KtScribe!\")\n}"
    ),
    JAVA(
        id = "java",
        displayName = "Java",
        extension = ".java",
        badgeLabel = "JAVA",
        defaultFileName = "Main.java",
        directoryPath = "src/java/",
        runCommandPrefix = "javac",
        starterCode = "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello Java!\");\n    }\n}"
    ),
    PYTHON(
        id = "python",
        displayName = "Python",
        extension = ".py",
        badgeLabel = "PY",
        defaultFileName = "main.py",
        directoryPath = "src/python/",
        runCommandPrefix = "python3",
        starterCode = "def main():\n    print(\"Hello Python!\")\n\nif __name__ == '__main__':\n    main()"
    ),
    DART(
        id = "dart",
        displayName = "Dart",
        extension = ".dart",
        badgeLabel = "DART",
        defaultFileName = "main.dart",
        directoryPath = "src/dart/",
        runCommandPrefix = "dart",
        starterCode = "void main() {\n    print('Hello Dart!');\n}"
    ),
    CPP(
        id = "cpp",
        displayName = "C++",
        extension = ".cpp",
        badgeLabel = "C++",
        defaultFileName = "main.cpp",
        directoryPath = "src/cpp/",
        runCommandPrefix = "g++",
        starterCode = "#include <iostream>\n\nint main() {\n    std::cout << \"Hello C++!\" << std::endl;\n    return 0;\n}"
    ),
    RUST(
        id = "rust",
        displayName = "Rust",
        extension = ".rs",
        badgeLabel = "RS",
        defaultFileName = "main.rs",
        directoryPath = "src/rust/",
        runCommandPrefix = "rustc",
        starterCode = "fn main() {\n    println!(\"Hello Rust!\");\n}"
    );

    val shortLabel: String get() = badgeLabel

    val badgeColor: androidx.compose.ui.graphics.Color get() = when (this) {
        KOTLIN -> androidx.compose.ui.graphics.Color(0xFFD4883A)
        JAVA -> androidx.compose.ui.graphics.Color(0xFFE76F51)
        PYTHON -> androidx.compose.ui.graphics.Color(0xFF457B9D)
        DART -> androidx.compose.ui.graphics.Color(0xFF2A9D8F)
        CPP -> androidx.compose.ui.graphics.Color(0xFF8338EC)
        RUST -> androidx.compose.ui.graphics.Color(0xFFE63946)
    }

    companion object {
        fun fromExtensionOrName(name: String): LanguageType {
            val lower = name.lowercase().trim()
            return when {
                lower.endsWith(".java") -> JAVA
                lower.endsWith(".py") -> PYTHON
                lower.endsWith(".dart") -> DART
                lower.endsWith(".cpp") || lower.endsWith(".cc") || lower.endsWith(".cxx") -> CPP
                lower.endsWith(".rs") -> RUST
                else -> KOTLIN
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
    val showDivider: Boolean = true,
    val language: LanguageType = LanguageType.fromExtensionOrName(sectionTitle),
    val themeOverride: com.example.ui.theme.AppThemeId? = null
) {
    /**
     * Canonical file name for this code block inside its parent Folder.
     * Respects user-defined extensions (.ktscribe, .java, .py, .dart, .cpp, .rs) or defaults to the section's language.
     */
    val ktScribeFileName: String
        get() {
            val raw = sectionTitle.trim()
            val hasKnownExt = raw.endsWith(".ktscribe", ignoreCase = true) ||
                raw.endsWith(".kt", ignoreCase = true) ||
                raw.endsWith(".java", ignoreCase = true) ||
                raw.endsWith(".py", ignoreCase = true) ||
                raw.endsWith(".dart", ignoreCase = true) ||
                raw.endsWith(".cpp", ignoreCase = true) ||
                raw.endsWith(".rs", ignoreCase = true)
            if (hasKnownExt) return raw

            val cleaned = raw
                .replace(Regex("^Section\\s*\\d+\\s*[•\\-:]?\\s*", RegexOption.IGNORE_CASE), "")
                .replace(Regex("[^A-Za-z0-9_\\-]"), "")
                .take(22)
            val stem = when {
                cleaned.isNotBlank() -> cleaned.replaceFirstChar { it.uppercase() }
                else -> {
                    val decl = Regex("""\b(?:class|interface|object|fun|def)\s+([A-Za-z_][A-Za-z0-9_]*)""")
                        .find(codeContent)?.groupValues?.getOrNull(1)
                    if (!decl.isNullOrBlank()) decl.replaceFirstChar { it.uppercase() }
                    else "File${orderIndex + 1}"
                }
            }
            return "$stem${language.extension}"
        }

    val fullFilePath: String
        get() = "${language.directoryPath}$ktScribeFileName"

    val displayTitle: String
        get() = ktScribeFileName

    /**
     * Serializes this single-code-block `.ktscribe` file together with its bundled concept tag and explanation notes.
     */
    fun toKtScribeFilePayload(folderTitle: String = "Workspace"): String = buildString {
        appendLine("// ============================================================================")
        appendLine("// @ktscribe-file: $ktScribeFileName")
        appendLine("// @folder: ${folderTitle.ifBlank { "DefaultFolder" }}")
        appendLine("// @concept: $effectiveLearnedConceptTag")
        appendLine("// @notes-start")
        if (explanationText.isBlank()) {
            appendLine("// (No explanation notes added yet)")
        } else {
            explanationText.lines().forEach { line ->
                appendLine("// $line")
            }
        }
        appendLine("// @notes-end")
        appendLine("// ============================================================================")
        appendLine()
        append(codeContent)
    }

    val effectiveLearnedConceptTag: String
        get() {
            val explicit = explanationStyle.learnedConceptTag.trim()
            if (explicit.isNotBlank()) return explicit
            return inferSmartConceptTagFromCode(codeContent, sectionTitle, orderIndex + 1)
        }

    val lineCount: Int
        get() = codeContent.split('\n').size.coerceAtLeast(1)

    val visibleLineSlots: Int
        get() = lineCount.coerceAtLeast(4) // Always starts at 4 lines and expands automatically

    companion object {
        fun inferSmartConceptTagFromCode(
            code: String,
            sectionTitle: String,
            sectionNumber: Int
        ): String {
            val trimmed = code.trim()
            return when {
                trimmed.contains("sealed interface") || trimmed.contains("sealed class") ->
                    "Sealed Hierarchy"
                trimmed.contains("MutableStateFlow") || trimmed.contains("StateFlow") ->
                    "Reactive StateFlow"
                trimmed.contains("Flow<") || trimmed.contains("flow {") ->
                    "Kotlin Flow Stream"
                trimmed.contains("suspend fun") || trimmed.contains("coroutineScope") || trimmed.contains("withContext") ->
                    "Coroutines & Suspend"
                trimmed.contains("data class") ->
                    "Immutable Data Class"
                trimmed.contains(".filter") || trimmed.contains(".map") || trimmed.contains(".forEach") ->
                    "Collection Pipeline"
                trimmed.contains("when (") || trimmed.contains("when(") ->
                    "Pattern Matching (when)"
                trimmed.contains("try {") && trimmed.contains("catch") ->
                    "Exception Handling"
                trimmed.contains("Color(0x") || trimmed.contains("#") ->
                    "Color Literal Styling"
                trimmed.contains("fun main") ->
                    "Kotlin Entry Point"
                trimmed.contains("fun ") -> {
                    val fnName = Regex("""\bfun\s+([A-Za-z_][A-Za-z0-9_]*)""")
                        .find(trimmed)?.groupValues?.getOrNull(1)
                    if (!fnName.isNullOrBlank()) "Function: $fnName()" else "Kotlin Function"
                }
                trimmed.contains("class ") ->
                    "Class Architecture"
                trimmed.contains("val ") || trimmed.contains("var ") ->
                    "Property & Type Inference"
                sectionTitle.isNotBlank() && !sectionTitle.startsWith("Section ") ->
                    sectionTitle.take(24)
                else -> "Concept #$sectionNumber"
            }
        }
    }
}

data class DocumentSummaryUiModel(
    val id: Long,
    val title: String,
    val previewSnippet: String,
    val sectionCount: Int,
    val updatedAt: Long,
    val wordCount: Int,
    val conceptTags: List<String> = emptyList(),
    val primaryLanguage: LanguageType = LanguageType.fromExtensionOrName(title)
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

    val totalCodeLines: Int
        get() = sections.sumOf { it.lineCount }
}

sealed interface ActiveEditorTarget {
    data object Intro : ActiveEditorTarget
    data class SectionCode(val sectionId: Long, val orderIndex: Int) : ActiveEditorTarget
    data class SectionExplanation(val sectionId: Long, val orderIndex: Int) : ActiveEditorTarget
}
