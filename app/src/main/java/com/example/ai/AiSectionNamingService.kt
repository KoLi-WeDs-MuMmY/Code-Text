package com.example.ai

import android.util.Log
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

/**
 * Generates concise, descriptive section titles from a Kotlin code block and its paired explanation
 * using Firebase AI Logic (`gemini-3.8-flash`), strictly enforcing a free-tier RPM sliding window
 * limiter (max 10 requests per 60 seconds) and SHA-256 content-hash deduplication.
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

        // 1. Check content-hash cache so unchanged sections never consume RPM quota
        hashToTitleCache[contentHash]?.let { cachedTitle ->
            val remaining = getRemainingRpmSlots()
            return SectionNamingResult.Success(
                sectionTitle = cachedTitle,
                contentHash = contentHash,
                remainingRpmQuota = remaining,
                isCloudAi = true
            )
        }

        // 2. Enforce Free-Tier RPM Sliding Window Rate Limiter
        val reservation = tryReserveRpmSlot()
        if (!reservation.allowed) {
            Log.w(TAG, "Free-tier RPM limit reached. Retry after ${reservation.retryAfterSeconds}s.")
            return SectionNamingResult.RateLimited(
                retryAfterSeconds = reservation.retryAfterSeconds,
                fallbackTitle = smartLocalTitle,
                contentHash = contentHash
            )
        }

        // 3. Call Firebase AI Logic with structured JSON output and fallback model support
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
            val generatedTitle = withTimeout(45_000L) {
                try {
                    val response = structuredModel.generateContent(prompt)
                    parseTitleFromModelOutput(response.text.orEmpty())
                } catch (inner: Exception) {
                    if (inner is CancellationException) throw inner
                    Log.w(TAG, "Structured model call failed, trying flash-lite text model", inner)
                    val fallbackResponse = fallbackTextModel.generateContent(prompt)
                    parseTitleFromModelOutput(fallbackResponse.text.orEmpty())
                }
            }

            if (generatedTitle.isNotBlank()) {
                hashToTitleCache[contentHash] = generatedTitle
                SectionNamingResult.Success(
                    sectionTitle = generatedTitle,
                    contentHash = contentHash,
                    remainingRpmQuota = reservation.remainingQuota,
                    isCloudAi = true
                )
            } else {
                hashToTitleCache[contentHash] = smartLocalTitle
                SectionNamingResult.Success(
                    sectionTitle = smartLocalTitle,
                    contentHash = contentHash,
                    remainingRpmQuota = reservation.remainingQuota,
                    isCloudAi = false
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI section title generation timed out", e)
            SectionNamingResult.Error(
                message = "Cloud AI timed out; applied smart Kotlin analyzer title.",
                fallbackTitle = smartLocalTitle,
                contentHash = contentHash
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI section naming failed, applying smart semantic analyzer title", e)
            SectionNamingResult.Error(
                message = "Applied smart analyzer title: \"${smartLocalTitle}\"",
                fallbackTitle = smartLocalTitle,
                contentHash = contentHash
            )
        }
    }

    private fun parseTitleFromModelOutput(rawOutput: String): String {
        val trimmed = rawOutput.trim()
        if (trimmed.isEmpty()) return ""

        // Extract "sectionTitle": "..." without depending on Android JSONObject stubs
        val jsonFieldMatch = Regex(""""sectionTitle"\s*:\s*"([^"]+)"""").find(trimmed)
        if (jsonFieldMatch != null) {
            return cleanTitleText(jsonFieldMatch.groupValues[1])
        }

        // Or clean plain-text response
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

    /**
     * Synthesizes a descriptive, human-readable section title from Kotlin AST patterns
     * AND the user's explanation prose when offline or rate-limited.
     */
    fun deriveSmartLocalFallbackTitle(
        code: String,
        explanation: String,
        sectionNumber: Int
    ): String {
        val cleanCode = code.trim()
        val cleanExp = explanation.trim()

        // 1. Extract primary Kotlin construct
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

        // 2. Extract summary phrase from explanation if written
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
