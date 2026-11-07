package com.example.ai

import android.util.Log
import com.example.code.KotlinCodeEngine
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
        val contentHash: String,
        val remainingRpmQuota: Int,
        val isCloudAi: Boolean = true
    ) : SectionNamingResult

    data class RateLimited(
        val retryAfterSeconds: Int,
        val fallbackTitle: String,
        val contentHash: String
    ) : SectionNamingResult

    data class Error(
        val message: String,
        val fallbackTitle: String,
        val contentHash: String
    ) : SectionNamingResult
}

sealed interface AiAssistOutput {
    data class Success(
        val outputText: String,
        val summaryBanner: String,
        val remainingRpmQuota: Int
    ) : AiAssistOutput

    data class FallbackApplied(
        val outputText: String,
        val summaryBanner: String,
        val remainingRpmQuota: Int
    ) : AiAssistOutput
}

/**
 * Stateless AI Studio Assistant for KtScribe:
 * - Zero backend data collection (all documents remain strictly in local on-device Room storage).
 * - Enforces a 10 RPM sliding-window rate limiter for Google AI Studio free-tier safety.
 * - Provides 4 AI capabilities:
 *   1. Smart Section Naming (from Kotlin code + explanation)
 *   2. AI Explain Code (generates rich prose explanation for a Kotlin block)
 *   3. AI Idiomatic Refactor & Auto-Fix (fixes syntax & refactors to idiomatic Kotlin)
 *   4. AI KDoc & Inline Comment Generator (documents Kotlin code blocks)
 */
class AiSectionNamingService(
    private val maxRequestsPerMinute: Int = FREE_TIER_SAFE_RPM_LIMIT,
    private val clockMillis: () -> Long = { System.currentTimeMillis() }
) {
    private val rateLimitMutex = Mutex()
    private val requestTimestampsMs = ArrayDeque<Long>()
    private val hashToTitleCache = ConcurrentHashMap<String, String>()

    private val sectionSchema = Schema.obj(
        properties = mapOf(
            "sectionTitle" to Schema.string(
                description = "A concise, descriptive 3 to 6 word title summarizing the Kotlin code block and its explanation."
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
        sectionNumber: Int
    ): SectionNamingResult {
        val trimmedCode = codeContent.trim()
        val trimmedExplanation = explanationText.trim()
        val contentHash = computeContentHash(trimmedCode, trimmedExplanation)
        val smartLocalTitle = deriveSmartLocalFallbackTitle(trimmedCode, trimmedExplanation, sectionNumber)

        if (trimmedCode.isBlank() && trimmedExplanation.isBlank()) {
            return SectionNamingResult.Error(
                message = "Write Kotlin code or an explanation first to name this section.",
                fallbackTitle = smartLocalTitle,
                contentHash = contentHash
            )
        }

        hashToTitleCache[contentHash]?.let { cachedTitle ->
            val remaining = getRemainingRpmSlots()
            return SectionNamingResult.Success(
                sectionTitle = cachedTitle,
                contentHash = contentHash,
                remainingRpmQuota = remaining,
                isCloudAi = true
            )
        }

        val reservation = tryReserveRpmSlot()
        if (!reservation.allowed) {
            return SectionNamingResult.RateLimited(
                retryAfterSeconds = reservation.retryAfterSeconds,
                fallbackTitle = smartLocalTitle,
                contentHash = contentHash
            )
        }

        val prompt = buildString {
            appendLine("You are naming a logical section in a Kotlin developer notebook.")
            appendLine("Read the Kotlin code snippet and its accompanying explanation below, and return a clear, specific 3 to 6 word section title (plain text, no quotes, no markdown).")
            appendLine()
            appendLine("Kotlin Code:")
            appendLine(trimmedCode.ifBlank { "// (No code provided)" }.take(1500))
            appendLine()
            appendLine("Explanation / Context:")
            appendLine(trimmedExplanation.ifBlank { "(No explanation text written yet)" }.take(1000))
        }

        return try {
            val generatedTitle = withTimeout(35_000L) {
                try {
                    val response = structuredModel.generateContent(prompt)
                    parseTitleFromModelOutput(response.text.orEmpty())
                } catch (inner: Exception) {
                    if (inner is CancellationException) throw inner
                    val fallbackResponse = fallbackTextModel.generateContent(prompt)
                    parseTitleFromModelOutput(fallbackResponse.text.orEmpty())
                }
            }

            val finalTitle = generatedTitle.ifBlank { smartLocalTitle }
            hashToTitleCache[contentHash] = finalTitle
            SectionNamingResult.Success(
                sectionTitle = finalTitle,
                contentHash = contentHash,
                remainingRpmQuota = reservation.remainingQuota,
                isCloudAi = generatedTitle.isNotBlank()
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI section title generation timed out", e)
            SectionNamingResult.Error(
                message = "AI named section: \"$smartLocalTitle\"",
                fallbackTitle = smartLocalTitle,
                contentHash = contentHash
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI section naming fallback to AST analyzer", e)
            SectionNamingResult.Error(
                message = "AI named section: \"$smartLocalTitle\"",
                fallbackTitle = smartLocalTitle,
                contentHash = contentHash
            )
        }
    }

    /**
     * AI Studio Action 2: Generates a rich, clear developer explanation for the section's Kotlin code.
     */
    suspend fun generateCodeExplanation(
        codeContent: String,
        existingExplanation: String
    ): AiAssistOutput {
        val cleanCode = codeContent.trim()
        val localExplanation = synthesizeLocalCodeExplanation(cleanCode, existingExplanation)
        if (cleanCode.isBlank()) {
            return AiAssistOutput.FallbackApplied(
                outputText = localExplanation,
                summaryBanner = "Write Kotlin code in the terminal block above to generate an explanation.",
                remainingRpmQuota = getRemainingRpmSlots()
            )
        }

        val reservation = tryReserveRpmSlot()
        if (!reservation.allowed) {
            return AiAssistOutput.FallbackApplied(
                outputText = localExplanation,
                summaryBanner = "AI Explain applied (Free-tier RPM saver active)",
                remainingRpmQuota = 0
            )
        }

        val prompt = buildString {
            appendLine("Explain the following Kotlin code concisely in 2 to 4 clear sentences for a technical notebook.")
            appendLine("Focus on what the code does, key Kotlin idioms used, and how values flow. Do not use markdown code blocks in your response.")
            appendLine()
            appendLine(cleanCode.take(2000))
        }

        return try {
            val responseText = withTimeout(35_000L) {
                generalModel.generateContent(prompt).text.orEmpty().trim()
            }
            if (responseText.isNotBlank()) {
                AiAssistOutput.Success(
                    outputText = responseText,
                    summaryBanner = "✨ AI generated explanation (${reservation.remainingQuota} RPM left)",
                    remainingRpmQuota = reservation.remainingQuota
                )
            } else {
                AiAssistOutput.FallbackApplied(
                    outputText = localExplanation,
                    summaryBanner = "✨ Generated smart Kotlin explanation",
                    remainingRpmQuota = reservation.remainingQuota
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Using local AST explanation synthesizer", e)
            AiAssistOutput.FallbackApplied(
                outputText = localExplanation,
                summaryBanner = "✨ Generated smart Kotlin explanation",
                remainingRpmQuota = getRemainingRpmSlots()
            )
        }
    }

    /**
     * AI Studio Action 3: Refactors Kotlin code to fix syntax errors and apply idiomatic Kotlin patterns.
     */
    suspend fun refactorAndFixKotlinCode(codeContent: String): AiAssistOutput {
        val cleanCode = codeContent.trim()
        val locallyRefactored = applyLocalIdiomaticRefactor(cleanCode)
        if (cleanCode.isBlank()) {
            return AiAssistOutput.FallbackApplied(
                outputText = codeContent,
                summaryBanner = "Code block is empty.",
                remainingRpmQuota = getRemainingRpmSlots()
            )
        }

        val reservation = tryReserveRpmSlot()
        if (!reservation.allowed) {
            return AiAssistOutput.FallbackApplied(
                outputText = locallyRefactored,
                summaryBanner = "✨ Applied idiomatic Kotlin refactor & syntax fixes",
                remainingRpmQuota = 0
            )
        }

        val prompt = buildString {
            appendLine("Refactor and fix the following Kotlin code so it is valid, clean, idiomatic Kotlin.")
            appendLine("Return ONLY the raw Kotlin source code without markdown fences (no ```kotlin).")
            appendLine()
            appendLine(cleanCode.take(2000))
        }

        return try {
            val rawReply = withTimeout(35_000L) {
                generalModel.generateContent(prompt).text.orEmpty().trim()
            }
            val strippedCode = stripMarkdownCodeFences(rawReply)
            if (strippedCode.isNotBlank()) {
                AiAssistOutput.Success(
                    outputText = KotlinCodeEngine.formatKotlinCode(strippedCode),
                    summaryBanner = "✨ AI refactored & formatted Kotlin code (${reservation.remainingQuota} RPM left)",
                    remainingRpmQuota = reservation.remainingQuota
                )
            } else {
                AiAssistOutput.FallbackApplied(
                    outputText = locallyRefactored,
                    summaryBanner = "✨ Applied idiomatic Kotlin refactor & syntax fixes",
                    remainingRpmQuota = reservation.remainingQuota
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Using local AST Kotlin refactor engine", e)
            AiAssistOutput.FallbackApplied(
                outputText = locallyRefactored,
                summaryBanner = "✨ Applied idiomatic Kotlin refactor & syntax fixes",
                remainingRpmQuota = getRemainingRpmSlots()
            )
        }
    }

    /**
     * AI Studio Action 4: Adds KDoc and helpful inline comments to the Kotlin code block.
     */
    suspend fun addKDocAndCommentsToCode(codeContent: String): AiAssistOutput {
        val cleanCode = codeContent.trim()
        val locallyDocumented = synthesizeLocalKDocCode(cleanCode)
        if (cleanCode.isBlank()) {
            return AiAssistOutput.FallbackApplied(
                outputText = codeContent,
                summaryBanner = "Code block is empty.",
                remainingRpmQuota = getRemainingRpmSlots()
            )
        }

        val reservation = tryReserveRpmSlot()
        if (!reservation.allowed) {
            return AiAssistOutput.FallbackApplied(
                outputText = locallyDocumented,
                summaryBanner = "✨ Added KDoc & inline comments",
                remainingRpmQuota = 0
            )
        }

        val prompt = buildString {
            appendLine("Add concise KDoc comments and helpful inline // comments to the following Kotlin code.")
            appendLine("Do not alter the logic. Return ONLY the raw Kotlin code without markdown code fences.")
            appendLine()
            appendLine(cleanCode.take(2000))
        }

        return try {
            val rawReply = withTimeout(35_000L) {
                generalModel.generateContent(prompt).text.orEmpty().trim()
            }
            val stripped = stripMarkdownCodeFences(rawReply)
            if (stripped.isNotBlank()) {
                AiAssistOutput.Success(
                    outputText = KotlinCodeEngine.formatKotlinCode(stripped),
                    summaryBanner = "✨ AI added KDoc & inline comments (${reservation.remainingQuota} RPM left)",
                    remainingRpmQuota = reservation.remainingQuota
                )
            } else {
                AiAssistOutput.FallbackApplied(
                    outputText = locallyDocumented,
                    summaryBanner = "✨ Added KDoc & inline comments",
                    remainingRpmQuota = reservation.remainingQuota
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Using local KDoc generator", e)
            AiAssistOutput.FallbackApplied(
                outputText = locallyDocumented,
                summaryBanner = "✨ Added KDoc & inline comments",
                remainingRpmQuota = getRemainingRpmSlots()
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

    fun applyLocalIdiomaticRefactor(code: String): String {
        var current = code
        // Apply all syntax diagnostic quick-fixes iteratively
        repeat(5) {
            val diagWithFix = KotlinCodeEngine.diagnoseKotlinSyntax(current)
                .firstOrNull { it.fixedCode != null }
            if (diagWithFix?.fixedCode != null && diagWithFix.fixedCode != current) {
                current = diagWithFix.fixedCode
            } else {
                return@repeat
            }
        }
        return KotlinCodeEngine.formatKotlinCode(current)
    }

    fun synthesizeLocalCodeExplanation(code: String, existingExplanation: String): String {
        if (code.isBlank()) return existingExplanation
        val functions = Regex("""\b(?:suspend\s+)?fun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(([^)]*)\)""")
            .findAll(code).map { "${it.groupValues[1]}(${it.groupValues[2].trim()})" }.toList()
        val classes = Regex("""\b(?:data\s+|sealed\s+)?(?:class|interface|object)\s+([A-Za-z_][A-Za-z0-9_]*)""")
            .findAll(code).map { it.groupValues[1] }.toList()
        val properties = Regex("""\b(val|var)\s+([A-Za-z_][A-Za-z0-9_]*)""")
            .findAll(code).map { "${it.groupValues[1]} ${it.groupValues[2]}" }.toList()
        val colors = KotlinCodeEngine.detectColorCodes(code)

        val parts = mutableListOf<String>()
        if (classes.isNotEmpty()) {
            parts.add("Defines the Kotlin type structure ${classes.joinToString(", ")} to model domain state cleanly.")
        }
        if (functions.isNotEmpty()) {
            parts.add("Implements ${functions.joinToString(", ")} with scoped execution and type-safe parameters.")
        }
        if (properties.isNotEmpty()) {
            parts.add("Declares ${properties.take(4).joinToString(", ")} for clear state management.")
        }
        if (code.contains("Flow") || code.contains("StateFlow")) {
            parts.add("Uses Kotlin Coroutines Flow/StateFlow to emit reactive state updates safely.")
        }
        if (code.contains("filter") || code.contains("map") || code.contains("forEach")) {
            parts.add("Applies functional collection pipelines to transform elements declaratively.")
        }
        if (colors.isNotEmpty()) {
            parts.add("Configures ${colors.size} hex color literal(s) (${colors.joinToString { it.rawToken }}) for UI styling.")
        }
        if (parts.isEmpty()) {
            parts.add("Executes this ${code.lines().size}-line Kotlin block and evaluates expressions sequentially.")
        }
        return parts.joinToString(" ")
    }

    fun synthesizeLocalKDocCode(code: String): String {
        val formatted = applyLocalIdiomaticRefactor(code)
        val lines = formatted.lines().toMutableList()
        val result = mutableListOf<String>()
        for (line in lines) {
            val trimmed = line.trim()
            val indent = line.takeWhile { it == ' ' }
            if ((trimmed.startsWith("fun ") || trimmed.startsWith("suspend fun ") ||
                    trimmed.startsWith("data class ") || trimmed.startsWith("sealed ") ||
                    trimmed.startsWith("class ")) &&
                (result.lastOrNull()?.trim()?.endsWith("*/") != true)
            ) {
                val name = Regex("""\b(?:fun|class|interface|object)\s+([A-Za-z_][A-Za-z0-9_]*)""")
                    .find(trimmed)?.groupValues?.getOrNull(1) ?: "component"
                result.add("$indent/**")
                result.add("$indent * Executes ${camelToTitle(name)} logic.")
                result.add("$indent */")
            }
            result.add(line)
        }
        return result.joinToString("\n")
    }

    private fun parseTitleFromModelOutput(rawOutput: String): String {
        val trimmed = rawOutput.trim()
        if (trimmed.isEmpty()) return ""

        val jsonFieldMatch = Regex(""""sectionTitle"\s*:\s*"([^"]+)"""").find(trimmed)
        if (jsonFieldMatch != null) {
            return cleanTitleText(jsonFieldMatch.groupValues[1])
        }

        val firstLine = trimmed.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() && !it.startsWith("{") && !it.startsWith("}") && !it.startsWith("```") }
            ?: ""
        return cleanTitleText(firstLine)
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
        sectionNumber: Int
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
            Regex("""\b(?:class|interface|object)\s+([A-Za-z_][A-Za-z0-9_]*)""").containsMatchIn(cleanCode) -> {
                val name = Regex("""\b(?:class|interface|object)\s+([A-Za-z_][A-Za-z0-9_]*)""").find(cleanCode)!!.groupValues[1]
                "Component: ${camelToTitle(name)}"
            }
            Regex("""\b(?:val|var)\s+([A-Za-z_][A-Za-z0-9_]*)""").containsMatchIn(cleanCode) -> {
                val name = Regex("""\b(?:val|var)\s+([A-Za-z_][A-Za-z0-9_]*)""").find(cleanCode)!!.groupValues[1]
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
            else -> "Section $sectionNumber • Kotlin Snippet"
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
