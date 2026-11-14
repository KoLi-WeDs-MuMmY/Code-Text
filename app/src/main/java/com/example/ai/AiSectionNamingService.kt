package com.example.ai

import android.util.Log
import com.example.code.CodeLanguageEngine
import com.example.code.KotlinCodeEngine
import com.example.model.CodeSectionUiModel
import com.example.model.LanguageType
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

private const val TAG = "AiSectionNamingService"

sealed interface SectionNamingResult {
    data class Success(
        val sectionTitle: String,
        val learnedConceptTag: String,
        val contentHash: String,
        val remainingRpmQuota: Int,
        val isCloudAi: Boolean = false
    ) : SectionNamingResult

    data class RateLimited(
        val retryAfterSeconds: Int,
        val fallbackTitle: String,
        val learnedConceptTag: String,
        val contentHash: String
    ) : SectionNamingResult

    data class Error(
        val message: String,
        val fallbackTitle: String,
        val learnedConceptTag: String,
        val contentHash: String
    ) : SectionNamingResult
}

sealed interface AiAssistOutput {
    val outputText: String

    data class Success(
        override val outputText: String,
        val summaryBanner: String,
        val remainingRpmQuota: Int,
        val learnedConceptTag: String = ""
    ) : AiAssistOutput

    data class FallbackApplied(
        override val outputText: String,
        val summaryBanner: String,
        val remainingRpmQuota: Int,
        val learnedConceptTag: String = ""
    ) : AiAssistOutput
}

/**
 * On-Device & Zero-Server AST Studio Assistant for KtScribe:
 * - Runs 100% locally on-device by default (`preferOfflineLocalEngine = true`) with zero server cost and 0ms network delay.
 * - Supports all 6 languages: Kotlin, Java, Python, C++, Dart, and Rust.
 * - Provides 4 capabilities:
 *   1. Smart Section Naming + Learned Concept Tagging
 *   2. Thorough 3-Part Code Explanation (Concept Learned, Step-by-Step Mechanics, Language Best Practices)
 *   3. Idiomatic Refactor, Auto-Import & Syntax Auto-Fix
 *   4. Native Docstring / KDoc / Javadoc / Doxygen / Rustdoc Generator
 */
class AiSectionNamingService(
    private val maxRequestsPerMinute: Int = FREE_TIER_SAFE_RPM_LIMIT,
    private val clockMillis: () -> Long = { System.currentTimeMillis() },
    var preferOfflineLocalEngine: Boolean = true
) {
    private val rateLimitMutex = Mutex()
    private val requestTimestampsMs = ArrayDeque<Long>()
    private val hashToTitleAndConceptCache = ConcurrentHashMap<String, Pair<String, String>>()

    private val sectionSchema = Schema.obj(
        properties = mapOf(
            "sectionTitle" to Schema.string(
                description = "A concise, descriptive 3 to 6 word title summarizing the code block and its explanation."
            ),
            "learnedConceptTag" to Schema.string(
                description = "A 2 to 4 word tag naming the specific programming concept or pattern learned in this code block."
            )
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = sectionSchema
            }
        )
    }

    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val fallbackTextModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite"
        )
    }

    fun computeContentHash(code: String, explanation: String): String {
        val normalized = "CODE:${code.trim()}\nEXP:${explanation.trim()}"
        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(24)
    }

    suspend fun getRemainingRpmSlots(): Int = rateLimitMutex.withLock {
        pruneExpiredTimestampsLocked(clockMillis())
        (maxRequestsPerMinute - requestTimestampsMs.size).coerceAtLeast(0)
    }

    suspend fun generateSectionTitle(
        codeContent: String,
        explanationText: String,
        sectionNumber: Int,
        language: LanguageType = LanguageType.KOTLIN
    ): SectionNamingResult {
        val trimmedCode = codeContent.trim()
        val trimmedExplanation = explanationText.trim()
        val contentHash = computeContentHash(trimmedCode, trimmedExplanation)
        val smartLocalTitle = deriveSmartLocalFallbackTitle(trimmedCode, trimmedExplanation, sectionNumber, language)
        val smartLocalConcept = deriveLearnedConceptTag(trimmedCode, trimmedExplanation, sectionNumber)

        if (trimmedCode.isBlank() && trimmedExplanation.isBlank()) {
            return SectionNamingResult.Error(
                message = "Write ${language.displayName} code or an explanation first to name and tag this file.",
                fallbackTitle = smartLocalTitle,
                learnedConceptTag = smartLocalConcept,
                contentHash = contentHash
            )
        }

        hashToTitleAndConceptCache[contentHash]?.let { (cachedTitle, cachedConcept) ->
            val remaining = getRemainingRpmSlots()
            return SectionNamingResult.Success(
                sectionTitle = cachedTitle,
                learnedConceptTag = cachedConcept,
                contentHash = contentHash,
                remainingRpmQuota = remaining,
                isCloudAi = !preferOfflineLocalEngine
            )
        }

        val reservation = tryReserveRpmSlot()
        if (!reservation.allowed) {
            return SectionNamingResult.RateLimited(
                retryAfterSeconds = reservation.retryAfterSeconds,
                fallbackTitle = smartLocalTitle,
                learnedConceptTag = smartLocalConcept,
                contentHash = contentHash
            )
        }

        // Instant 100% On-Device AST mode (Zero server cost, zero network latency)
        if (preferOfflineLocalEngine) {
            hashToTitleAndConceptCache[contentHash] = smartLocalTitle to smartLocalConcept
            return SectionNamingResult.Success(
                sectionTitle = smartLocalTitle,
                learnedConceptTag = smartLocalConcept,
                contentHash = contentHash,
                remainingRpmQuota = reservation.remainingQuota,
                isCloudAi = false
            )
        }

        val prompt = buildString {
            appendLine("You are naming a logical section in a ${language.displayName} developer notebook and tagging the core concept learned.")
            appendLine("Return JSON with:")
            appendLine("1. sectionTitle: A clear, specific 3 to 6 word section title.")
            appendLine("2. learnedConceptTag: A concise 2 to 4 word tag for the core concept learned.")
            appendLine()
            appendLine("${language.displayName} Code:")
            appendLine(trimmedCode.ifBlank { "// (No code provided)" }.take(1500))
            appendLine()
            appendLine("Explanation / Context:")
            appendLine(trimmedExplanation.ifBlank { "(No explanation text written yet)" }.take(1000))
        }

        return try {
            val (generatedTitle, generatedConcept) = withTimeout(8_000L) {
                try {
                    val response = structuredModel.generateContent(prompt)
                    parseTitleAndConceptFromModelOutput(response.text.orEmpty(), smartLocalConcept)
                } catch (inner: Exception) {
                    if (inner is CancellationException) throw inner
                    val fallbackResponse = fallbackTextModel.generateContent(prompt)
                    parseTitleAndConceptFromModelOutput(fallbackResponse.text.orEmpty(), smartLocalConcept)
                }
            }

            val finalTitle = generatedTitle.ifBlank { smartLocalTitle }
            val finalConcept = generatedConcept.ifBlank { smartLocalConcept }
            hashToTitleAndConceptCache[contentHash] = finalTitle to finalConcept
            SectionNamingResult.Success(
                sectionTitle = finalTitle,
                learnedConceptTag = finalConcept,
                contentHash = contentHash,
                remainingRpmQuota = reservation.remainingQuota,
                isCloudAi = generatedTitle.isNotBlank()
            )
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "AI section title timed out, using local AST analyzer")
            hashToTitleAndConceptCache[contentHash] = smartLocalTitle to smartLocalConcept
            SectionNamingResult.Success(
                sectionTitle = smartLocalTitle,
                learnedConceptTag = smartLocalConcept,
                contentHash = contentHash,
                remainingRpmQuota = reservation.remainingQuota,
                isCloudAi = false
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Using local on-device AST analyzer for section naming")
            hashToTitleAndConceptCache[contentHash] = smartLocalTitle to smartLocalConcept
            SectionNamingResult.Success(
                sectionTitle = smartLocalTitle,
                learnedConceptTag = smartLocalConcept,
                contentHash = contentHash,
                remainingRpmQuota = reservation.remainingQuota,
                isCloudAi = false
            )
        }
    }

    /**
     * Studio Action 2: Generates a thorough, structured 3-part developer explanation for any supported language
     * AND identifies the core "Learned Concept Tag" for instant navigation.
     */
    suspend fun generateCodeExplanation(
        codeContent: String,
        existingExplanation: String,
        sectionNumber: Int = 1,
        language: LanguageType = LanguageType.KOTLIN
    ): AiAssistOutput {
        val cleanCode = codeContent.trim()
        val learnedConcept = deriveLearnedConceptTag(cleanCode, existingExplanation, sectionNumber)
        val localExplanation = synthesizeLocalCodeExplanation(cleanCode, existingExplanation, language)
        if (cleanCode.isBlank()) {
            return AiAssistOutput.FallbackApplied(
                outputText = localExplanation,
                summaryBanner = "Write ${language.displayName} code in the editor to generate a thorough explanation.",
                remainingRpmQuota = getRemainingRpmSlots(),
                learnedConceptTag = learnedConcept
            )
        }

        val reservation = tryReserveRpmSlot()
        if (!reservation.allowed || preferOfflineLocalEngine) {
            return AiAssistOutput.Success(
                outputText = localExplanation,
                summaryBanner = "⚡ On-Device AST Explanation & tag [$learnedConcept] generated for ${language.displayName}",
                remainingRpmQuota = reservation.remainingQuota,
                learnedConceptTag = learnedConcept
            )
        }

        val prompt = buildString {
            appendLine("Analyze the ${language.displayName} code below and write a structured explanation in plain text using these 3 sections:")
            appendLine("🎯 Concept Learned: Explain the core ${language.displayName} paradigm or pattern demonstrated.")
            appendLine("⚙️ Execution & Data Flow: Walk through the code step-by-step.")
            appendLine("💡 Idiomatic Takeaways & Best Practices: Highlight why this pattern is effective in ${language.displayName}.")
            appendLine()
            appendLine(cleanCode.take(2200))
        }

        return try {
            val responseText = withTimeout(8_000L) {
                generalModel.generateContent(prompt).text.orEmpty().trim()
            }
            if (responseText.isNotBlank()) {
                AiAssistOutput.Success(
                    outputText = responseText,
                    summaryBanner = "✨ Thorough ${language.displayName} explanation & tag [$learnedConcept] ready",
                    remainingRpmQuota = reservation.remainingQuota,
                    learnedConceptTag = learnedConcept
                )
            } else {
                AiAssistOutput.Success(
                    outputText = localExplanation,
                    summaryBanner = "⚡ On-Device AST explanation & tag [$learnedConcept] generated",
                    remainingRpmQuota = reservation.remainingQuota,
                    learnedConceptTag = learnedConcept
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AiAssistOutput.Success(
                outputText = localExplanation,
                summaryBanner = "⚡ On-Device AST explanation & tag [$learnedConcept] generated",
                remainingRpmQuota = getRemainingRpmSlots(),
                learnedConceptTag = learnedConcept
            )
        }
    }

    /**
     * Studio Action 3: Refactors code across all 6 languages to fix syntax errors, auto-import missing symbols,
     * optimize imports, and apply clean formatting.
     */
    suspend fun refactorAndFixKotlinCode(
        codeContent: String,
        language: LanguageType = LanguageType.KOTLIN
    ): AiAssistOutput {
        val cleanCode = codeContent.trim()
        val learnedConcept = deriveLearnedConceptTag(cleanCode, "", 1)
        val locallyRefactored = applyLocalIdiomaticRefactor(cleanCode, language)
        if (cleanCode.isBlank()) {
            return AiAssistOutput.FallbackApplied(
                outputText = codeContent,
                summaryBanner = "Code block is empty.",
                remainingRpmQuota = getRemainingRpmSlots(),
                learnedConceptTag = learnedConcept
            )
        }

        val reservation = tryReserveRpmSlot()
        if (!reservation.allowed || preferOfflineLocalEngine) {
            return AiAssistOutput.Success(
                outputText = locallyRefactored,
                summaryBanner = "⚡ On-Device AST auto-fixed syntax, imports & formatting (${language.displayName})",
                remainingRpmQuota = reservation.remainingQuota,
                learnedConceptTag = deriveLearnedConceptTag(locallyRefactored, "", 1)
            )
        }

        val prompt = buildString {
            appendLine("Refactor and fix the following ${language.displayName} code so it is valid, clean, and idiomatic.")
            appendLine("Return ONLY the raw source code without markdown fences.")
            appendLine()
            appendLine(cleanCode.take(2000))
        }

        return try {
            val rawReply = withTimeout(8_000L) {
                generalModel.generateContent(prompt).text.orEmpty().trim()
            }
            val strippedCode = stripMarkdownCodeFences(rawReply)
            if (strippedCode.isNotBlank()) {
                val formatted = CodeLanguageEngine.formatCodeForLanguage(strippedCode, language)
                AiAssistOutput.Success(
                    outputText = formatted,
                    summaryBanner = "✨ Refactored & formatted ${language.displayName} code",
                    remainingRpmQuota = reservation.remainingQuota,
                    learnedConceptTag = deriveLearnedConceptTag(formatted, "", 1)
                )
            } else {
                AiAssistOutput.Success(
                    outputText = locallyRefactored,
                    summaryBanner = "⚡ On-Device AST auto-fixed & formatted ${language.displayName} code",
                    remainingRpmQuota = reservation.remainingQuota,
                    learnedConceptTag = learnedConcept
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AiAssistOutput.Success(
                outputText = locallyRefactored,
                summaryBanner = "⚡ On-Device AST auto-fixed & formatted ${language.displayName} code",
                remainingRpmQuota = getRemainingRpmSlots(),
                learnedConceptTag = learnedConcept
            )
        }
    }

    /**
     * Studio Action 4: Adds language-appropriate documentation comments (KDoc, Javadoc, Python Docstrings,
     * Doxygen, Dartdoc, or Rustdoc) to the code block.
     */
    suspend fun addKDocAndCommentsToCode(
        codeContent: String,
        language: LanguageType = LanguageType.KOTLIN
    ): AiAssistOutput {
        val cleanCode = codeContent.trim()
        val learnedConcept = deriveLearnedConceptTag(cleanCode, "", 1)
        val locallyDocumented = synthesizeLocalKDocCode(cleanCode, language)
        if (cleanCode.isBlank()) {
            return AiAssistOutput.FallbackApplied(
                outputText = codeContent,
                summaryBanner = "Code block is empty.",
                remainingRpmQuota = getRemainingRpmSlots(),
                learnedConceptTag = learnedConcept
            )
        }

        val docStyleName = when (language) {
            LanguageType.KOTLIN -> "KDoc"
            LanguageType.JAVA -> "Javadoc"
            LanguageType.PYTHON -> "PEP-257 Docstrings"
            LanguageType.CPP -> "Doxygen"
            LanguageType.DART -> "Dartdoc ///"
            LanguageType.RUST -> "Rustdoc ///"
        }

        val reservation = tryReserveRpmSlot()
        if (!reservation.allowed || preferOfflineLocalEngine) {
            return AiAssistOutput.Success(
                outputText = locallyDocumented,
                summaryBanner = "⚡ Added on-device $docStyleName & inline documentation (${language.displayName})",
                remainingRpmQuota = reservation.remainingQuota,
                learnedConceptTag = learnedConcept
            )
        }

        val prompt = buildString {
            appendLine("Add concise $docStyleName comments to the following ${language.displayName} code.")
            appendLine("Do not alter the logic. Return ONLY the raw code without markdown code fences.")
            appendLine()
            appendLine(cleanCode.take(2000))
        }

        return try {
            val rawReply = withTimeout(8_000L) {
                generalModel.generateContent(prompt).text.orEmpty().trim()
            }
            val stripped = stripMarkdownCodeFences(rawReply)
            if (stripped.isNotBlank()) {
                AiAssistOutput.Success(
                    outputText = CodeLanguageEngine.formatCodeForLanguage(stripped, language),
                    summaryBanner = "✨ Added $docStyleName comments (${language.displayName})",
                    remainingRpmQuota = reservation.remainingQuota,
                    learnedConceptTag = learnedConcept
                )
            } else {
                AiAssistOutput.Success(
                    outputText = locallyDocumented,
                    summaryBanner = "⚡ Added on-device $docStyleName comments",
                    remainingRpmQuota = reservation.remainingQuota,
                    learnedConceptTag = learnedConcept
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AiAssistOutput.Success(
                outputText = locallyDocumented,
                summaryBanner = "⚡ Added on-device $docStyleName comments",
                remainingRpmQuota = getRemainingRpmSlots(),
                learnedConceptTag = learnedConcept
            )
        }
    }

    private fun stripMarkdownCodeFences(raw: String): String {
        val lines = raw.trim().lines()
        if (lines.isEmpty()) return ""
        val withoutFirst = if (lines.first().trim().startsWith("```")) lines.drop(1) else lines
        val withoutLast = if (withoutFirst.lastOrNull()?.trim()?.startsWith("```") == true) {
            withoutFirst.dropLast(1)
        } else {
            withoutFirst
        }
        return withoutLast.joinToString("\n").trim()
    }

    /**
     * Multi-language on-device AST auto-fixer & formatter:
     * Applies diagnostic quick-fixes (missing colons, semicolons, quotes, imports) and formats cleanly.
     */
    fun applyLocalIdiomaticRefactor(
        code: String,
        language: LanguageType = LanguageType.KOTLIN
    ): String {
        var current = code
        repeat(6) {
            val diagWithFix = CodeLanguageEngine.getDiagnosticsForLanguage(current, language)
                .firstOrNull { it.fixedCode != null }
            if (diagWithFix?.fixedCode != null && diagWithFix.fixedCode != current) {
                current = diagWithFix.fixedCode
            } else {
                return@repeat
            }
        }
        return CodeLanguageEngine.formatCodeForLanguage(current, language)
    }

    /**
     * Synthesizes a thorough, 3-part developer explanation for any supported language:
     * 1. 🎯 Concept Learned
     * 2. ⚙️ Step-by-Step Execution & Data Flow
     * 3. 💡 Idiomatic Language Takeaways & Best Practices
     */
    fun synthesizeLocalCodeExplanation(
        code: String,
        existingExplanation: String,
        language: LanguageType = LanguageType.KOTLIN
    ): String {
        if (code.isBlank()) return existingExplanation
        val conceptTag = deriveLearnedConceptTag(code, existingExplanation, 1)
        val symbols = CodeLanguageEngine.extractSymbolsForSection(code, language)
        val functions = symbols.filter { it.kind == "Function" }.map { "${it.name}()" }
        val types = symbols.filter { it.kind == "Class/Type" || it.kind == "Interface/Enum" }.map { it.name }
        val properties = symbols.filter { it.kind == "Property" }.map { it.name }
        val diagnostics = CodeLanguageEngine.getDiagnosticsForLanguage(code, language)
        val errors = diagnostics.count { it.severity == com.example.code.DiagnosticSeverity.ERROR }
        val metrics = KotlinCodeEngine.analyzeCodeMetrics(code)

        val mechanics = mutableListOf<String>()
        if (types.isNotEmpty()) {
            mechanics.add("Declares ${language.displayName} type structure(s) ${types.joinToString(", ")} to encapsulate domain state and behavior.")
        }
        if (functions.isNotEmpty()) {
            mechanics.add("Defines callable routine(s) ${functions.joinToString(", ")} with structured control flow and scoped variables.")
        }
        if (properties.isNotEmpty()) {
            mechanics.add("Binds local/member symbol(s) ${properties.take(6).joinToString(", ")} (${metrics.immutabilityPercent}% immutable binding ratio).")
        }
        when (language) {
            LanguageType.KOTLIN -> {
                if (code.contains("Flow") || code.contains("StateFlow")) {
                    mechanics.add("Streams state updates reactively using Kotlin Coroutines Flow/StateFlow for deterministic emissions.")
                }
                if (code.contains("filter") || code.contains("map") || code.contains("forEach")) {
                    mechanics.add("Chains functional collection operators (.filter / .map / .forEach) without mutating the source collection.")
                }
            }
            LanguageType.PYTHON -> {
                if (code.contains("if __name__")) {
                    mechanics.add("Uses the standard Python `__name__ == '__main__'` entry guard so the module can be both imported and executed directly.")
                }
                if (code.contains("[") && code.contains("for ") && code.contains(" in ")) {
                    mechanics.add("Applies Pythonic comprehension/iteration syntax for concise sequence transformation.")
                }
            }
            LanguageType.JAVA -> {
                if (code.contains("public static void main")) {
                    mechanics.add("Defines the JVM entry point `public static void main(String[] args)` for standard class execution.")
                }
            }
            LanguageType.CPP -> {
                if (code.contains("std::cout") || code.contains("std::cin")) {
                    mechanics.add("Streams formatted I/O through standard C++ `<iostream>` operators (`<<` / `>>`).")
                }
            }
            LanguageType.DART -> {
                if (code.contains("async") || code.contains("Future")) {
                    mechanics.add("Coordinates non-blocking isolate execution using Dart `Future` / `async-await` primitives.")
                }
            }
            LanguageType.RUST -> {
                if (code.contains("let mut ")) {
                    mechanics.add("Uses explicit `let mut` ownership bindings to satisfy the Rust borrow checker safely.")
                }
                if (code.contains("println!")) {
                    mechanics.add("Invokes the compile-time checked `println!` macro for formatted standard output.")
                }
            }
        }
        if (errors > 0) {
            mechanics.add("Note: Static analyzer detected $errors syntax diagnostic(s) — tap 'Refactor & Fix' or the inline Quick Fix chip to repair automatically.")
        }
        if (mechanics.isEmpty()) {
            mechanics.add("Executes this ${code.lines().size}-line ${language.displayName} snippet sequentially and evaluates its expressions.")
        }

        val takeaway = when (language) {
            LanguageType.KOTLIN -> when {
                code.contains("sealed ") ->
                    "Using sealed hierarchies enables exhaustive `when` expressions without a catch-all `else` branch."
                code.contains("data class") ->
                    "Kotlin `data class` synthesizes `equals()`, `hashCode()`, `toString()`, and `copy()` for clean immutable state updates."
                metrics.varCount > 0 ->
                    "Prefer `val` over mutable `var` where possible to maximize thread safety and predictability."
                else ->
                    "Preferring immutable `val` bindings and small single-responsibility functions keeps complexity (${metrics.complexityLabel}) easy to test."
            }
            LanguageType.PYTHON ->
                "Follow PEP-8 4-space indentation, add type hints (`def fn(x: int) -> str:`), and use context managers (`with`) for deterministic resource cleanup."
            LanguageType.JAVA ->
                "Favor `final` fields, Java 21 `record` types for immutable data carriers, and interfaces over concrete collection types."
            LanguageType.CPP ->
                "Prefer RAII stack allocation, `const` references (`const auto&`), and `<vector>`/`<string>` over raw pointers for memory safety."
            LanguageType.DART ->
                "Use `final` or `const` constructors whenever possible and leverage Dart 3 sound null safety (`?`, `??`) to prevent runtime null errors."
            LanguageType.RUST ->
                "Prefer immutable `let` bindings by default, borrow slices (`&str`, `&[T]`) instead of cloning, and handle `Result`/`Option` via `match` or `?`."
        }

        return buildString {
            appendLine("🎯 Concept Learned — $conceptTag (${language.displayName}):")
            appendLine("This file demonstrates $conceptTag in ${language.displayName} across ${code.lines().size} lines of code.")
            appendLine()
            appendLine("⚙️ Execution & Data Flow:")
            mechanics.forEach { item ->
                appendLine("• $item")
            }
            appendLine()
            append("💡 Idiomatic Takeaways & Best Practices (${language.displayName}): $takeaway")
        }
    }

    /**
     * Synthesizes native documentation comments per language:
     * - Python: `"""Docstring"""` inside functions/classes
     * - Rust / Dart: `///` doc comments above declarations
     * - Kotlin / Java / C++: `/** ... */` block doc comments above declarations
     */
    fun synthesizeLocalKDocCode(
        code: String,
        language: LanguageType = LanguageType.KOTLIN
    ): String {
        val formatted = applyLocalIdiomaticRefactor(code, language)
        val lines = formatted.lines()
        val result = mutableListOf<String>()

        if (language == LanguageType.PYTHON) {
            var i = 0
            while (i < lines.size) {
                val line = lines[i]
                val trimmed = line.trim()
                val indent = line.takeWhile { it == ' ' }
                result.add(line)
                if ((trimmed.startsWith("def ") || trimmed.startsWith("class ")) && trimmed.endsWith(":")) {
                    val nextTrimmed = lines.getOrNull(i + 1)?.trim().orEmpty()
                    if (!nextTrimmed.startsWith("\"\"\"") && !nextTrimmed.startsWith("'''")) {
                        val name = Regex("""\b(?:def|class)\s+([A-Za-z_][A-Za-z0-9_]*)""")
                            .find(trimmed)?.groupValues?.getOrNull(1) ?: "block"
                        result.add("$indent    \"\"\"Executes ${camelToTitle(name)} logic.\"\"\"")
                    }
                }
                i++
            }
            return result.joinToString("\n")
        }

        if (language == LanguageType.RUST || language == LanguageType.DART) {
            for (line in lines) {
                val trimmed = line.trim()
                val indent = line.takeWhile { it == ' ' }
                val isDecl = trimmed.startsWith("fn ") || trimmed.startsWith("pub fn ") ||
                    trimmed.startsWith("struct ") || trimmed.startsWith("void main") ||
                    trimmed.startsWith("class ")
                if (isDecl && result.lastOrNull()?.trim()?.startsWith("///") != true) {
                    val name = Regex("""\b(?:fn|struct|class|void)\s+([A-Za-z_][A-Za-z0-9_]*)""")
                        .find(trimmed)?.groupValues?.getOrNull(1) ?: "component"
                    result.add("$indent/// Executes ${camelToTitle(name)} routine.")
                }
                result.add(line)
            }
            return result.joinToString("\n")
        }

        // Kotlin, Java, C++
        for (line in lines) {
            val trimmed = line.trim()
            val indent = line.takeWhile { it == ' ' }
            val isDecl = trimmed.startsWith("fun ") || trimmed.startsWith("suspend fun ") ||
                trimmed.startsWith("data class ") || trimmed.startsWith("sealed ") ||
                trimmed.startsWith("class ") || trimmed.startsWith("public class ") ||
                trimmed.startsWith("public static void ") || trimmed.startsWith("int main")
            if (isDecl && result.lastOrNull()?.trim()?.endsWith("*/") != true) {
                val name = Regex("""\b(?:fun|class|interface|object|void|int)\s+([A-Za-z_][A-Za-z0-9_]*)""")
                    .find(trimmed)?.groupValues?.getOrNull(1) ?: "component"
                result.add("$indent/**")
                result.add("$indent * Executes ${camelToTitle(name)} logic.")
                result.add("$indent */")
            }
            result.add(line)
        }
        return result.joinToString("\n")
    }

    fun deriveLearnedConceptTag(
        code: String,
        explanation: String,
        sectionNumber: Int
    ): String {
        return CodeSectionUiModel.inferSmartConceptTagFromCode(
            code = code,
            sectionTitle = explanation.take(28),
            sectionNumber = sectionNumber
        )
    }

    private fun parseTitleAndConceptFromModelOutput(
        rawOutput: String,
        defaultConcept: String
    ): Pair<String, String> {
        val trimmed = rawOutput.trim()
        if (trimmed.isEmpty()) return "" to defaultConcept

        val titleMatch = Regex(""""sectionTitle"\s*:\s*"([^"]+)"""").find(trimmed)
        val conceptMatch = Regex(""""learnedConceptTag"\s*:\s*"([^"]+)"""").find(trimmed)

        val parsedTitle = if (titleMatch != null) {
            cleanTitleText(titleMatch.groupValues[1])
        } else {
            val firstLine = trimmed.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotBlank() && !it.startsWith("{") && !it.startsWith("}") && !it.startsWith("```") }
                ?: ""
            cleanTitleText(firstLine)
        }

        val parsedConcept = if (conceptMatch != null) {
            cleanTitleText(conceptMatch.groupValues[1]).take(28)
        } else {
            defaultConcept
        }

        return parsedTitle to parsedConcept
    }

    private fun cleanTitleText(raw: String): String {
        return raw
            .removePrefix("Title:")
            .removePrefix("Section Title:")
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .removeSurrounding("**")
            .trim()
            .take(64)
    }

    fun deriveSmartLocalFallbackTitle(
        code: String,
        explanation: String,
        sectionNumber: Int,
        language: LanguageType = LanguageType.KOTLIN
    ): String {
        val cleanCode = code.trim()
        val cleanExp = explanation.trim()

        val codeConstruct = when {
            Regex("""\bsealed\s+(?:interface|class)\s+([A-Za-z_][A-Za-z0-9_]*)""").containsMatchIn(cleanCode) -> {
                val name = Regex("""\bsealed\s+(?:interface|class)\s+([A-Za-z_][A-Za-z0-9_]*)""").find(cleanCode)!!.groupValues[1]
                "Sealed Hierarchy: ${camelToTitle(name)}"
            }
            Regex("""\bdata\s+class\s+([A-Za-z_][A-Za-z0-9_]*)""").containsMatchIn(cleanCode) -> {
                val name = Regex("""\bdata\s+class\s+([A-Za-z_][A-Za-z0-9_]*)""").find(cleanCode)!!.groupValues[1]
                "Data Model: ${camelToTitle(name)}"
            }
            Regex("""\bsuspend\s+fun\s+([A-Za-z_][A-Za-z0-9_]*)""").containsMatchIn(cleanCode) -> {
                val name = Regex("""\bsuspend\s+fun\s+([A-Za-z_][A-Za-z0-9_]*)""").find(cleanCode)!!.groupValues[1]
                "Async Coroutine: ${camelToTitle(name)}"
            }
            Regex("""\bdef\s+([A-Za-z_][A-Za-z0-9_]*)""").containsMatchIn(cleanCode) -> {
                val allDefs = Regex("""\bdef\s+([A-Za-z_][A-Za-z0-9_]*)""").findAll(cleanCode).map { it.groupValues[1] }.toList()
                val primaryDef = allDefs.firstOrNull { it != "main" && it != "__init__" } ?: allDefs.first()
                "Python Function: ${camelToTitle(primaryDef)}"
            }
            Regex("""\bfn\s+([A-Za-z_][A-Za-z0-9_]*)""").containsMatchIn(cleanCode) -> {
                val allFns = Regex("""\bfn\s+([A-Za-z_][A-Za-z0-9_]*)""").findAll(cleanCode).map { it.groupValues[1] }.toList()
                val primaryFn = allFns.firstOrNull { it != "main" } ?: allFns.first()
                "Rust Routine: ${camelToTitle(primaryFn)}"
            }
            Regex("""\bfun\s+([A-Za-z_][A-Za-z0-9_]*)""").containsMatchIn(cleanCode) -> {
                val allFuns = Regex("""\bfun\s+([A-Za-z_][A-Za-z0-9_]*)""").findAll(cleanCode)
                    .map { it.groupValues[1] }
                    .toList()
                val primaryFun = allFuns.firstOrNull { it != "main" } ?: allFuns.first()
                if (primaryFun == "main") {
                    inferMainBlockTopic(cleanCode)
                } else {
                    "Function: ${camelToTitle(primaryFun)}"
                }
            }
            Regex("""\b(?:class|interface|object|struct)\s+([A-Za-z_][A-Za-z0-9_]*)""").containsMatchIn(cleanCode) -> {
                val name = Regex("""\b(?:class|interface|object|struct)\s+([A-Za-z_][A-Za-z0-9_]*)""").find(cleanCode)!!.groupValues[1]
                "Component: ${camelToTitle(name)}"
            }
            Regex("""\b(?:val|var|let)\s+([A-Za-z_][A-Za-z0-9_]*)""").containsMatchIn(cleanCode) -> {
                val name = Regex("""\b(?:val|var|let)\s+([A-Za-z_][A-Za-z0-9_]*)""").find(cleanCode)!!.groupValues[1]
                "Property: ${camelToTitle(name)}"
            }
            else -> null
        }

        val expSummary = if (cleanExp.isNotBlank()) {
            val firstSentence = cleanExp.split(Regex("""[.!?\n]""")).firstOrNull()?.trim().orEmpty()
            val stopWords = setOf("this", "that", "here", "the", "and", "for", "with", "from", "code", "block", "section", "explains", "shows", "how")
            val meaningfulWords = firstSentence
                .split(Regex("""\s+"""))
                .map { it.trim(',', ':', ';', '(', ')', '"', '\'') }
                .filter { it.length > 2 && it.lowercase() !in stopWords }
                .take(4)
            if (meaningfulWords.size >= 2) {
                meaningfulWords.joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercaseChar() }
                }
            } else {
                null
            }
        } else {
            null
        }

        return when {
            codeConstruct != null && expSummary != null -> "$codeConstruct • $expSummary".take(60)
            codeConstruct != null -> codeConstruct
            expSummary != null -> "Section $sectionNumber • $expSummary"
            else -> "Section $sectionNumber • ${language.displayName} Snippet"
        }
    }

    private fun inferMainBlockTopic(code: String): String = when {
        code.contains("Flow") || code.contains("StateFlow") -> "Reactive Flow Entry Point"
        code.contains("coroutineScope") || code.contains("launch") -> "Coroutine Scope Execution"
        code.contains("filter") || code.contains("map") -> "Collection Pipeline in Main"
        code.contains("Color(") || code.contains("0xFF") -> "Color Palette Configuration"
        code.contains("when ") || code.contains("when(") -> "Pattern Matching Entry Point"
        code.contains("println") -> "Console Output Entry Point"
        else -> "Kotlin Main Entry Point"
    }

    private fun camelToTitle(identifier: String): String {
        return identifier
            .replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
            .replace("_", " ")
            .trim()
            .replaceFirstChar { it.uppercaseChar() }
    }

    private data class SlotReservation(
        val allowed: Boolean,
        val remainingQuota: Int,
        val retryAfterSeconds: Int
    )

    private suspend fun tryReserveRpmSlot(): SlotReservation = rateLimitMutex.withLock {
        val now = clockMillis()
        pruneExpiredTimestampsLocked(now)
        if (requestTimestampsMs.size >= maxRequestsPerMinute) {
            val oldest = requestTimestampsMs.first()
            val waitMs = (WINDOW_MS - (now - oldest)).coerceAtLeast(1000L)
            val waitSeconds = ((waitMs + 999L) / 1000L).toInt()
            return@withLock SlotReservation(
                allowed = false,
                remainingQuota = 0,
                retryAfterSeconds = waitSeconds
            )
        }
        requestTimestampsMs.addLast(now)
        val remaining = (maxRequestsPerMinute - requestTimestampsMs.size).coerceAtLeast(0)
        SlotReservation(
            allowed = true,
            remainingQuota = remaining,
            retryAfterSeconds = 0
        )
    }

    private fun pruneExpiredTimestampsLocked(now: Long) {
        while (requestTimestampsMs.isNotEmpty() && now - requestTimestampsMs.first() >= WINDOW_MS) {
            requestTimestampsMs.removeFirst()
        }
    }

    companion object {
        const val FREE_TIER_SAFE_RPM_LIMIT = 10
        private const val WINDOW_MS = 60_000L
    }
}
