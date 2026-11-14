package com.example.code

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Depth-aware Bracket Pair Colorizer:
 * Colors nested `()`, `{}`, and `[]` pairs by nesting depth while skipping strings and comments,
 * highlights the matching bracket pair adjacent to the active cursor, and marks unmatched brackets.
 */
object BracketPairColorizer {

    private val RAINBOW_BRACKET_COLORS = listOf(
        Color(0xFFFFD866), // Depth 0: Warm Gold
        Color(0xFF78DCE8), // Depth 1: Ice Cyan
        Color(0xFFAB9DF2), // Depth 2: Soft Violet
        Color(0xFFA9DC76), // Depth 3: Mint Sage
        Color(0xFFFF9B5E), // Depth 4: Warm Coral
        Color(0xFF6897BB)  // Depth 5: Steel Blue
    )

    private val UNMATCHED_BRACKET_COLOR = Color(0xFFFF5555)
    private val ACTIVE_MATCH_BG = Color(0xFF4F4028)

    data class BracketSpan(
        val offset: Int,
        val depth: Int,
        val isMatched: Boolean,
        val partnerOffset: Int?
    )

    fun analyzeBrackets(code: String, isPython: Boolean = false): List<BracketSpan> {
        if (code.isEmpty()) return emptyList()
        val stack = ArrayDeque<Pair<Char, Int>>() // char to offset
        val spans = mutableMapOf<Int, BracketSpan>()

        var i = 0
        var inLineComment = false
        var inBlockComment = false
        var inDoubleString = false
        var inSingleString = false

        while (i < code.length) {
            val c = code[i]
            val next = code.getOrNull(i + 1)

            if (inLineComment) {
                if (c == '\n') inLineComment = false
                i++
                continue
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false
                    i += 2
                    continue
                }
                i++
                continue
            }
            if (inDoubleString) {
                if (c == '\\') {
                    i += 2
                    continue
                }
                if (c == '"') inDoubleString = false
                i++
                continue
            }
            if (inSingleString) {
                if (c == '\\') {
                    i += 2
                    continue
                }
                if (c == '\'') inSingleString = false
                i++
                continue
            }

            if (isPython && c == '#') {
                inLineComment = true
                i++
                continue
            }
            if (!isPython && c == '/' && next == '/') {
                inLineComment = true
                i += 2
                continue
            }
            if (!isPython && c == '/' && next == '*') {
                inBlockComment = true
                i += 2
                continue
            }
            if (c == '"') {
                inDoubleString = true
                i++
                continue
            }
            if (c == '\'') {
                inSingleString = true
                i++
                continue
            }

            when (c) {
                '(', '{', '[' -> {
                    stack.addLast(c to i)
                }
                ')', '}', ']' -> {
                    val expectedOpen = when (c) {
                        ')' -> '('
                        '}' -> '{'
                        else -> '['
                    }
                    if (stack.isNotEmpty() && stack.last().first == expectedOpen) {
                        val (_, openOffset) = stack.removeLast()
                        val depth = stack.size
                        spans[openOffset] = BracketSpan(openOffset, depth, isMatched = true, partnerOffset = i)
                        spans[i] = BracketSpan(i, depth, isMatched = true, partnerOffset = openOffset)
                    } else {
                        spans[i] = BracketSpan(i, 0, isMatched = false, partnerOffset = null)
                    }
                }
            }
            i++
        }

        while (stack.isNotEmpty()) {
            val (_, openOffset) = stack.removeLast()
            spans[openOffset] = BracketSpan(openOffset, 0, isMatched = false, partnerOffset = null)
        }

        return spans.values.sortedBy { it.offset }
    }

    fun applyToBuilder(
        builder: AnnotatedString.Builder,
        code: String,
        cursorOffset: Int,
        isPython: Boolean = false
    ) {
        val spans = analyzeBrackets(code, isPython)
        if (spans.isEmpty()) return
        val byOffset = spans.associateBy { it.offset }

        val activeOffsets = mutableSetOf<Int>()
        if (cursorOffset in 0..code.length) {
            val candidateA = byOffset[cursorOffset]
            val candidateB = byOffset[cursorOffset - 1]
            val active = candidateA ?: candidateB
            if (active != null && active.isMatched) {
                activeOffsets.add(active.offset)
                active.partnerOffset?.let { activeOffsets.add(it) }
            }
        }

        for (span in spans) {
            val color = if (!span.isMatched) {
                UNMATCHED_BRACKET_COLOR
            } else {
                RAINBOW_BRACKET_COLORS[span.depth % RAINBOW_BRACKET_COLORS.size]
            }
            val isActive = span.offset in activeOffsets
            builder.addStyle(
                style = SpanStyle(
                    color = color,
                    fontWeight = if (isActive || !span.isMatched) FontWeight.ExtraBold else FontWeight.SemiBold,
                    background = if (isActive) ACTIVE_MATCH_BG else Color.Unspecified
                ),
                start = span.offset,
                end = span.offset + 1
            )
        }
    }
}
