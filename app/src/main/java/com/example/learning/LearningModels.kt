package com.example.learning

import androidx.compose.ui.graphics.Color
import com.example.model.LanguageType

enum class LearningTrack(
    val id: String,
    val title: String,
    val shortBadge: String,
    val subtitle: String,
    val accentHex: Long,
    val defaultLanguage: LanguageType
) {
    KOTLIN_ANDROID(
        id = "kotlin_android",
        title = "Kotlin & Android Compose",
        shortBadge = "Kotlin",
        subtitle = "Functions, Compose, XML & KtVM bytecode",
        accentHex = 0xFFD4883A,
        defaultLanguage = LanguageType.KOTLIN
    ),
    FLUTTER_DART(
        id = "flutter_dart",
        title = "Flutter & Dart UI",
        shortBadge = "Flutter",
        subtitle = "Dart state logic & declarative Widget trees",
        accentHex = 0xFF2A9D8F,
        defaultLanguage = LanguageType.DART
    ),
    PYTHON_DSA(
        id = "python_dsa",
        title = "Python & Algorithms",
        shortBadge = "Python",
        subtitle = "Loops, recursion, stdin I/O & problem solving",
        accentHex = 0xFF457B9D,
        defaultLanguage = LanguageType.PYTHON
    ),
    SYSTEMS_CPP_RUST(
        id = "systems_cpp_rust",
        title = "C++ & Rust Systems",
        shortBadge = "Systems",
        subtitle = "Loops, memory discipline & pattern matching",
        accentHex = 0xFF8338EC,
        defaultLanguage = LanguageType.CPP
    ),
    AI_PROMPT_LOGIC(
        id = "ai_prompt_logic",
        title = "Applied AI & Logic",
        shortBadge = "AI Lab",
        subtitle = "Rule engines, classifiers & Socratic tutoring",
        accentHex = 0xFFE76F51,
        defaultLanguage = LanguageType.KOTLIN
    );

    val accentColor: Color get() = Color(accentHex)
}

data class RealLifeAnalogy(
    val headline: String,
    val story: String,
    val mappingSummary: String
)

data class WordMeaning(
    val term: String,
    val partOfSpeech: String,
    val simpleMeaning: String,
    val everydayExample: String
)

data class LineTranslation(
    val lineNumber: Int,
    val codeLine: String,
    val plainEnglish: String
)

sealed interface LessonTestAssertion {
    val label: String

    data class ExitCodeZero(
        override val label: String = "Program compiles & exits cleanly (exit code 0)"
    ) : LessonTestAssertion

    data class ContainsStdout(
        val expected: String,
        override val label: String
    ) : LessonTestAssertion

    data class VariableEquals(
        val variableName: String,
        val expectedValue: String,
        override val label: String
    ) : LessonTestAssertion

    data class NonEmptyOutput(
        override val label: String = "Produces non-empty stdout output"
    ) : LessonTestAssertion
}

data class CodingLesson(
    val id: String,
    val track: LearningTrack,
    val title: String,
    val conceptTag: String,
    val difficulty: String,
    val language: LanguageType,
    val instructions: String,
    val hint: String,
    val starterCode: String,
    val solutionCode: String,
    val stdinInput: String = "",
    val assertions: List<LessonTestAssertion>,
    val xpReward: Int = 50,
    val supportsUiPreview: Boolean = false,
    val realLifeAnalogy: RealLifeAnalogy = RealLifeAnalogy(
        headline = "Everyday Mental Model",
        story = "Code breaks big tasks into tiny clear steps just like following a cooking recipe.",
        mappingSummary = "Each statement executes in order from top to bottom."
    ),
    val wordDictionary: List<WordMeaning> = emptyList(),
    val lineWalkthrough: List<LineTranslation> = emptyList()
)

data class LessonTestCaseResult(
    val label: String,
    val passed: Boolean,
    val detail: String
)

data class AutoGradeResult(
    val lessonId: String,
    val passedCount: Int,
    val totalCount: Int,
    val testResults: List<LessonTestCaseResult>,
    val earnedXp: Int,
    val summaryBanner: String,
    val stdoutPreview: String = ""
) {
    val isAllPassed: Boolean get() = totalCount > 0 && passedCount == totalCount
}

data class ConceptQuizQuestion(
    val id: String,
    val track: LearningTrack,
    val conceptTag: String,
    val question: String,
    val codeSnippet: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ConceptMasteryState(
    val conceptTag: String,
    val trackId: String,
    val xp: Int = 0,
    val masteryPercent: Int = 0,
    val completedLessons: Int = 0,
    val passedQuizzes: Int = 0
)

data class LessonProgressState(
    val lessonId: String,
    val trackId: String,
    val passedTests: Int = 0,
    val totalTests: Int = 0,
    val isCompleted: Boolean = false,
    val lastCodeContent: String = ""
)
