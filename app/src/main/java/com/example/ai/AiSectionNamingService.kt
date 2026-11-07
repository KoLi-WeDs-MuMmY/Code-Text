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
import org.json.JSONObject

private const val TAG = "AiSectionNamingService"

sealed interface SectionNamingResult {
    data class Success(
        val sectionTitle: String,
        val contentHash: String,
        val remainingRpmQuota: Int
    ) : SectionNamingResult

    data class RateLimited(
        val retryAfterSeconds: Int,
        val fallbackTitle: String
    ) : SectionNamingResult

    data class Error(
        val message: String,
        val fallbackTitle: String
    ) : SectionNamingResult
}

/**
 * Generates concise, meaningful section titles from a Kotlin code block and its paired explanation
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

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = sectionSchema
            }
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
        val fallback = deriveSmartLocalFallbackTitle(trimmedCode, trimmedExplanation, sectionNumber)

        if (trimmedCode.isBlank() && trimmedExplanation.isBlank()) {
            return SectionNamingResult.Error(
                message = "Write Kotlin code or an explanation before naming this section.",
                fallbackTitle = fallback
            )
        }

        val contentHash = computeContentHash(trimmedCode, trimmedExplanation)

        // 1. Check content-hash cache so unchanged sections never consume RPM quota
        hashToTitleCache[contentHash]?.let { cachedTitle ->
            val remaining = getRemainingRpmSlots()
            return SectionNamingResult.Success(
                sectionTitle = cachedTitle,
                contentHash = contentHash,
                remainingRpmQuota = remaining
            )
        }

        // 2. Enforce Free-Tier RPM Sliding Window Rate Limiter
        val reservation = tryReserveRpmSlot()
        if (!reservation.allowed) {
            Log.w(TAG, "Free-tier RPM limit reached. Retry after ${reservation.retryAfterSeconds}s.")
            return SectionNamingResult.RateLimited(
                retryAfterSeconds = reservation.retryAfterSeconds,
                fallbackTitle = fallback
            )
        }

        // 3. Call Firebase AI Logic with structured JSON output and 60s timeout
        val prompt = buildString {
            appendLine("You are naming a logical section in a Kotlin developer notebook.")
            appendLine("Read the Kotlin code snippet and its accompanying explanation below, and return a clear, specific 3 to 6 word section title (no markdown, no quotes).")
            appendLine()
            appendLine("Kotlin Code:")
            appendLine(trimmedCode.ifBlank { "// (No code provided)" }.take(1500))
            appendLine()
            appendLine("Explanation / Context:")
            appendLine(trimmedExplanation.ifBlank { "(No explanation text written yet)" }.take(1000))
        }

        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text ?: throw IllegalStateException("Model returned an empty response.")
            val json = JSONObject(rawJson)
            val generatedTitle = json.optString("sectionTitle", "").trim()
                .removeSurrounding("\"")
                .take(65)

            if (generatedTitle.isNotBlank()) {
                hashToTitleCache[contentHash] = generatedTitle
                SectionNamingResult.Success(
                    sectionTitle = generatedTitle,
                    contentHash = contentHash,
                    remainingRpmQuota = reservation.remainingQuota
                )
            } else {
                SectionNamingResult.Error(
                    message = "AI returned a blank section title.",
                    fallbackTitle = fallback
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI section title generation timed out", e)
            SectionNamingResult.Error(
                message = "AI naming timed out after 60s.",
                fallbackTitle = fallback
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI section naming failed", e)
            SectionNamingResult.Error(
                message = e.localizedMessage ?: "AI naming failed.",
                fallbackTitle = fallback
            )
        }
    }

    /**
     * Extracts a deterministic fallback title from Kotlin declarations if offline or rate-limited.
     */
    fun deriveSmartLocalFallbackTitle(
        code: String,
        explanation: String,
        sectionNumber: Int
    ): String {
        val declMatch = Regex("\\b(?:fun|class|interface|object|val|var)\\s+([A-Za-z_][A-Za-z0-9_]*)")
            .find(code)
        if (declMatch != null) {
            val symbol = declMatch.groupValues[1]
            return "Section $sectionNumber • $symbol"
        }
        if (explanation.isNotBlank()) {
            val words = explanation.split(Regex("\\s+")).take(5).joinToString(" ")
            return words.take(40)
        }
        return "Section $sectionNumber • Kotlin Snippet"
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
        // Free-tier safe ceiling: 10 RPM keeps a generous safety buffer below the 15 RPM free-tier cap.
        const val FREE_TIER_SAFE_RPM_LIMIT = 10
        private const val WINDOW_MS = 60_000L
    }
}
