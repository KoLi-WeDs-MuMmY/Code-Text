package com.example.code

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.theme.KotlinSyntaxColors

data class KotlinSuggestion(
    val label: String,
    val insertText: String,
    val category: String,
    val description: String
)

enum class DiagnosticSeverity {
    ERROR,
    WARNING
}

data class KotlinDiagnostic(
    val line: Int, // 1-indexed line number
    val startOffset: Int,
    val endOffset: Int,
    val message: String,
    val severity: DiagnosticSeverity,
    val quickFixLabel: String? = null,
    val fixedCode: String? = null
)

data class DetectedColorCode(
    val rawToken: String,
    val colorInt: Long,
    val startOffset: Int,
    val endOffset: Int,
    val line: Int
)

data class CursorPositionInfo(
    val line: Int,
    val column: Int,
    val selectedChars: Int,
    val totalLines: Int
)

object KotlinCodeEngine {

    val KOTLIN_KEYWORDS = setOf(
        "package", "import", "class", "interface", "object", "fun", "val", "var",
        "typealias", "constructor", "by", "companion", "init", "this", "super",
        "where", "if", "else", "when", "try", "catch", "finally", "for", "do",
        "while", "throw", "return", "continue", "break", "as", "is", "in",
        "out", "reified", "true", "false", "null", "abstract", "annotation",
        "crossinline", "data", "enum", "expect", "actual", "external", "final",
        "infix", "inline", "inner", "internal", "lateinit", "noinline", "open",
        "operator", "override", "private", "protected", "public", "sealed",
        "suspend", "tailrec", "vararg", "it", "lazy"
    )

    val KOTLIN_STD_TYPES = setOf(
        "String", "Int", "Long", "Double", "Float", "Boolean", "Char", "Byte",
        "Short", "Unit", "Nothing", "Any", "List", "MutableList", "Map",
        "MutableMap", "Set", "MutableSet", "Array", "Sequence", "Flow",
        "StateFlow", "MutableStateFlow", "SharedFlow", "CoroutineScope",
        "Dispatchers", "Result", "Pair", "Triple", "Throwable", "Exception",
        "Color", "Modifier", "Composable"
    )

    private val GENERIC_TYPE_PREFIXES = setOf(
        "List", "MutableList", "Map", "MutableMap", "Set", "MutableSet",
        "Array", "Sequence", "Flow", "StateFlow", "MutableStateFlow",
        "SharedFlow", "Result", "Pair", "Triple"
    )

    private val BUILTIN_SUGGESTIONS = listOf(
        KotlinSuggestion("fun", "fun ", "Keyword", "Declare a function"),
        KotlinSuggestion("fun main()", "fun main() {\n    \n}", "Template", "Main entry point"),
        KotlinSuggestion("val", "val ", "Keyword", "Immutable reference"),
        KotlinSuggestion("var", "var ", "Keyword", "Mutable reference"),
        KotlinSuggestion("data class", "data class Item(\n    val id: Int,\n    val name: String\n)", "Template", "Kotlin data class"),
        KotlinSuggestion("sealed interface", "sealed interface UiState {\n    data object Loading : UiState\n}", "Template", "Sealed state hierarchy"),
        KotlinSuggestion("suspend fun", "suspend fun ", "Coroutine", "Suspendable function"),
        KotlinSuggestion("when", "when (value) {\n    else -> {}\n}", "Control", "Pattern matching expression"),
        KotlinSuggestion("if / else", "if (condition) {\n    \n} else {\n    \n}", "Control", "Conditional branch"),
        KotlinSuggestion("forEach", "forEach { item ->\n    \n}", "Stdlib", "Iterate over collection"),
        KotlinSuggestion("map", "map { it }", "Stdlib", "Transform collection elements"),
        KotlinSuggestion("filter", "filter { it }", "Stdlib", "Filter collection elements"),
        KotlinSuggestion("println", "println(\"\")", "Stdlib", "Print line to standard output"),
        KotlinSuggestion("listOf", "listOf()", "Stdlib", "Immutable list builder"),
        KotlinSuggestion("mutableListOf", "mutableListOf()", "Stdlib", "Mutable list builder"),
        KotlinSuggestion("mapOf", "mapOf()", "Stdlib", "Immutable map builder"),
        KotlinSuggestion("Color(0xFF...)", "Color(0xFF7F52FF)", "Color", "ARGB hex color literal"),
        KotlinSuggestion("coroutineScope", "coroutineScope {\n    \n}", "Coroutine", "Scoped coroutine block"),
        KotlinSuggestion("withContext", "withContext(Dispatchers.IO) {\n    \n}", "Coroutine", "Switch coroutine dispatcher"),
        KotlinSuggestion("try / catch", "try {\n    \n} catch (e: Exception) {\n    \n}", "Control", "Exception handling block"),
        KotlinSuggestion("class", "class ", "Keyword", "Declare a class"),
        KotlinSuggestion("interface", "interface ", "Keyword", "Declare an interface"),
        KotlinSuggestion("object", "object ", "Keyword", "Singleton object declaration"),
        KotlinSuggestion("override fun", "override fun ", "Keyword", "Override member function"),
        KotlinSuggestion("private val", "private val ", "Keyword", "Private immutable property"),
        KotlinSuggestion("return", "return ", "Keyword", "Return from function"),
        KotlinSuggestion("null", "null", "Keyword", "Null reference"),
        KotlinSuggestion("true", "true", "Keyword", "Boolean true"),
        KotlinSuggestion("false", "false", "Keyword", "Boolean false"),
        KotlinSuggestion("String", "String", "Type", "Kotlin String type"),
        KotlinSuggestion("Int", "Int", "Type", "32-bit signed integer"),
        KotlinSuggestion("Boolean", "Boolean", "Type", "Boolean type"),
        KotlinSuggestion("Flow", "Flow<>", "Type", "Asynchronous data stream"),
        KotlinSuggestion("StateFlow", "StateFlow<>", "Type", "Observable state holder")
    )

    private val IDENTIFIER_REGEX = Regex("\\b(?:val|var|fun|class|interface|object)\\s+([A-Za-z_][A-Za-z0-9_]*)")
    private val TOKEN_REGEX = Regex(
        """(//[^\n]*)|(/\*[\s\S]*?\*/)|("(?:\\.|[^"\\])*")|(@[A-Za-z_][A-Za-z0-9_]*)|\b(0x[0-9A-Fa-f]{6,8}|\d+(?:\.\d+)?[fFL]?)\b|\b([A-Za-z_][A-Za-z0-9_]*)\b"""
    )

    private val HEX_COLOR_REGEX = Regex("""\b0x([0-9A-Fa-f]{8}|[0-9A-Fa-f]{6})\b|#([0-9A-Fa-f]{8}|[0-9A-Fa-f]{6})\b""")

    private val AUTO_CLOSE_PAIRS = mapOf(
        '(' to ')',
        '{' to '}',
        '[' to ']',
        '"' to '"',
        '\'' to '\''
    )
    private val CLOSING_CHARS = setOf(')', '}', ']', '>', '"', '\'')

    /**
     * Computes 1-indexed Line and Column for the Apple Terminal status bar.
     */
    fun getCursorPositionInfo(fieldValue: TextFieldValue): CursorPositionInfo {
        val text = fieldValue.text
        val safeCursor = fieldValue.selection.end.coerceIn(0, text.length)
        val beforeCursor = text.substring(0, safeCursor)
        val line = beforeCursor.count { it == '\n' } + 1
        val lastNewline = beforeCursor.lastIndexOf('\n')
        val column = if (lastNewline < 0) safeCursor + 1 else (safeCursor - lastNewline)
        val selectedChars = kotlin.math.abs(fieldValue.selection.end - fieldValue.selection.start)
        val totalLines = text.count { it == '\n' } + 1
        return CursorPositionInfo(
            line = line,
            column = column,
            selectedChars = selectedChars,
            totalLines = totalLines
        )
    }

    /**
     * Handles IDE smart typing:
     * 1. Auto-closing parenthesis `()`, braces `{}`, brackets `[]`, quotes `""`/`''`, and generic `<>`.
     * 2. Skipping over closing characters if already present at the cursor.
     * 3. Deleting both characters when pressing Backspace inside an empty pair `(|)`, `{|}`, `[|]`, `"|"`, `<|>`.
     * 4. Smart auto-indentation when pressing Enter (including expanding `{|}` into an indented block).
     */
    fun processIdeTextEdit(
        previous: TextFieldValue,
        incoming: TextFieldValue
    ): TextFieldValue {
        val oldText = previous.text
        val newText = incoming.text
        val newCursor = incoming.selection.end

        if (newText.length == oldText.length + 1 && previous.selection.collapsed && incoming.selection.collapsed) {
            val insertedIndex = (newCursor - 1).coerceIn(0, newText.lastIndex)
            val insertedChar = newText[insertedIndex]

            val prefixMatches = newText.startsWith(oldText.substring(0, insertedIndex))
            val suffixMatches = newText.substring(insertedIndex + 1) == oldText.substring(insertedIndex)

            if (prefixMatches && suffixMatches) {
                val charAfterCursorInOld = oldText.getOrNull(insertedIndex)

                // A. Skip-over existing closing bracket/quote
                if (insertedChar in CLOSING_CHARS && charAfterCursorInOld == insertedChar) {
                    if (insertedChar != '"' && insertedChar != '\'') {
                        return TextFieldValue(
                            text = oldText,
                            selection = TextRange(insertedIndex + 1)
                        )
                    } else {
                        val lineQuoteCount = oldText.count { it == insertedChar }
                        if (lineQuoteCount % 2 == 0) {
                            return TextFieldValue(
                                text = oldText,
                                selection = TextRange(insertedIndex + 1)
                            )
                        }
                    }
                }

                // B. Auto-close generic `<` -> `<>` when typed immediately after a Kotlin generic type name
                if (insertedChar == '<') {
                    val beforeAngle = oldText.substring(0, insertedIndex)
                    val wordBefore = beforeAngle.takeLastWhile { it.isLetterOrDigit() || it == '_' }
                    if (wordBefore in GENERIC_TYPE_PREFIXES || wordBefore.firstOrNull()?.isUpperCase() == true) {
                        val withCloseAngle = newText.substring(0, newCursor) + ">" + newText.substring(newCursor)
                        return TextFieldValue(
                            text = withCloseAngle,
                            selection = TextRange(newCursor)
                        )
                    }
                }

                // C. Auto-close opening bracket or quote
                val matchingClose = AUTO_CLOSE_PAIRS[insertedChar]
                if (matchingClose != null) {
                    val shouldAutoClose = when (insertedChar) {
                        '"', '\'' -> {
                            val prevChar = oldText.getOrNull(insertedIndex - 1)
                            prevChar != '\\' && !(charAfterCursorInOld?.isLetterOrDigit() ?: false)
                        }
                        else -> !(charAfterCursorInOld?.isLetterOrDigit() ?: false)
                    }
                    if (shouldAutoClose) {
                        val withClosing = newText.substring(0, newCursor) + matchingClose + newText.substring(newCursor)
                        return TextFieldValue(
                            text = withClosing,
                            selection = TextRange(newCursor)
                        )
                    }
                }

                // D. Smart Enter Auto-Indentation
                if (insertedChar == '\n') {
                    val textBeforeNewline = oldText.substring(0, insertedIndex)
                    val currentLineStart = textBeforeNewline.lastIndexOf('\n') + 1
                    val currentLine = textBeforeNewline.substring(currentLineStart)
                    val baseIndent = currentLine.takeWhile { it == ' ' || it == '\t' }.replace("\t", "    ")
                    val prevNonSpaceChar = currentLine.trimEnd().lastOrNull()

                    if ((prevNonSpaceChar == '{' && charAfterCursorInOld == '}') ||
                        (prevNonSpaceChar == '(' && charAfterCursorInOld == ')')
                    ) {
                        val innerIndent = "$baseIndent    "
                        val expanded = buildString {
                            append(oldText.substring(0, insertedIndex))
                            append("\n")
                            append(innerIndent)
                            append("\n")
                            append(baseIndent)
                            append(oldText.substring(insertedIndex))
                        }
                        val targetCursor = insertedIndex + 1 + innerIndent.length
                        return TextFieldValue(
                            text = expanded,
                            selection = TextRange(targetCursor)
                        )
                    }

                    val extraIndent = if (prevNonSpaceChar == '{' || prevNonSpaceChar == '(' || currentLine.trimEnd().endsWith("->")) {
                        "    "
                    } else {
                        ""
                    }
                    val totalIndent = baseIndent + extraIndent
                    if (totalIndent.isNotEmpty()) {
                        val indentedText = newText.substring(0, newCursor) + totalIndent + newText.substring(newCursor)
                        return TextFieldValue(
                            text = indentedText,
                            selection = TextRange(newCursor + totalIndent.length)
                        )
                    }
                }
            }
        }

        // 2. Backspace deletion inside an empty pair `(|)`, `{|}`, `[|]`, `"|"`, `<|>`
        if (newText.length == oldText.length - 1 && previous.selection.collapsed && incoming.selection.collapsed) {
            val deletedIndex = newCursor
            if (deletedIndex in 0 until oldText.length - 1) {
                val deletedChar = oldText[deletedIndex]
                val followingChar = oldText[deletedIndex + 1]
                val expectedPartner = AUTO_CLOSE_PAIRS[deletedChar] ?: if (deletedChar == '<') '>' else null
                if (expectedPartner == followingChar && newText.getOrNull(deletedIndex) == followingChar) {
                    val withoutBoth = newText.removeRange(deletedIndex, deletedIndex + 1)
                    return TextFieldValue(
                        text = withoutBoth,
                        selection = TextRange(deletedIndex)
                    )
                }
            }
        }

        return incoming
    }

    /**
     * IDE Action: Toggle `//` line comment on the current cursor line.
     */
    fun toggleLineCommentAtCursor(fieldValue: TextFieldValue): TextFieldValue {
        val text = fieldValue.text
        val cursor = fieldValue.selection.end.coerceIn(0, text.length)
        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it < 0) text.length else it }
        val line = text.substring(lineStart, lineEnd)
        val leadingSpaces = line.takeWhile { it == ' ' || it == '\t' }
        val trimmed = line.substring(leadingSpaces.length)

        return if (trimmed.startsWith("//")) {
            val uncommented = leadingSpaces + trimmed.removePrefix("// ").removePrefix("//")
            val delta = line.length - uncommented.length
            val newText = text.replaceRange(lineStart, lineEnd, uncommented)
            TextFieldValue(newText, TextRange((cursor - delta).coerceAtLeast(lineStart)))
        } else {
            val commented = "$leadingSpaces// $trimmed"
            val newText = text.replaceRange(lineStart, lineEnd, commented)
            TextFieldValue(newText, TextRange((cursor + 3).coerceAtMost(newText.length)))
        }
    }

    /**
     * IDE Action: Duplicate the current line below the cursor.
     */
    fun duplicateLineAtCursor(fieldValue: TextFieldValue): TextFieldValue {
        val text = fieldValue.text
        val cursor = fieldValue.selection.end.coerceIn(0, text.length)
        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it < 0) text.length else it }
        val line = text.substring(lineStart, lineEnd)
        val newText = text.substring(0, lineEnd) + "\n" + line + text.substring(lineEnd)
        val offsetInLine = cursor - lineStart
        val newCursor = (lineEnd + 1 + offsetInLine).coerceIn(0, newText.length)
        return TextFieldValue(newText, TextRange(newCursor))
    }

    /**
     * IDE Action: Move current line up or down by 1 line.
     */
    fun moveLineVertically(fieldValue: TextFieldValue, moveUp: Boolean): TextFieldValue {
        val lines = fieldValue.text.split('\n').toMutableList()
        if (lines.size <= 1) return fieldValue
        val info = getCursorPositionInfo(fieldValue)
        val idx = (info.line - 1).coerceIn(0, lines.lastIndex)
        val targetIdx = if (moveUp) idx - 1 else idx + 1
        if (targetIdx !in lines.indices) return fieldValue

        val temp = lines[idx]
        lines[idx] = lines[targetIdx]
        lines[targetIdx] = temp

        val newText = lines.joinToString("\n")
        val newLineStart = lines.take(targetIdx).sumOf { it.length + 1 }
        val newCursor = (newLineStart + (info.column - 1).coerceAtMost(lines[targetIdx].length))
            .coerceIn(0, newText.length)
        return TextFieldValue(newText, TextRange(newCursor))
    }

    /**
     * IDE Find & Replace inside a code block.
     */
    fun replaceInCode(
        code: String,
        findQuery: String,
        replaceWith: String,
        replaceAll: Boolean = true
    ): Pair<String, Int> {
        if (findQuery.isEmpty()) return code to 0
        val matches = Regex(Regex.escape(findQuery)).findAll(code).toList()
        if (matches.isEmpty()) return code to 0
        return if (replaceAll) {
            code.replace(findQuery, replaceWith) to matches.size
        } else {
            val first = matches.first().range
            code.replaceRange(first, replaceWith) to 1
        }
    }

    /**
     * Simulates running the Kotlin snippet inside the Apple Terminal console,
     * evaluating variable bindings, string interpolations, loops, and `println(...)` calls.
     */
    fun simulateKotlinExecution(code: String): String {
        val trimmed = code.trim()
        if (trimmed.isBlank()) {
            return "$ kotlinc -script section.kts\n(No Kotlin code to execute)"
        }

        val diagnostics = diagnoseKotlinSyntax(trimmed)
        val errors = diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }
        if (errors.isNotEmpty()) {
            return buildString {
                appendLine("$ kotlinc section.kt -include-runtime -d section.jar")
                errors.forEach { err ->
                    appendLine("section.kt:${err.line}: error: ${err.message}")
                }
                append("COMPILATION FAILED (${errors.size} error${if (errors.size > 1) "s" else ""})")
            }
        }

        // Extract simple val/var bindings for string interpolation in println
        val env = mutableMapOf<String, String>()
        val valRegex = Regex("""\b(?:val|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*(?::\s*[A-Za-z0-9_<>?]+)?\s*=\s*([^\n]+)""")
        for (match in valRegex.findAll(trimmed)) {
            val name = match.groupValues[1]
            val rawExpr = match.groupValues[2].trim().removeSuffix(";")
            val evaluated = evaluateSimpleExpression(rawExpr, env)
            env[name] = evaluated
        }

        // Collect all println(...) outputs
        val outputs = mutableListOf<String>()
        val printlnRegex = Regex("""\bprint(?:ln)?\s*\(([^)]*)\)""")
        for (match in printlnRegex.findAll(trimmed)) {
            val arg = match.groupValues[1].trim()
            outputs.add(evaluateSimpleExpression(arg, env))
        }

        return buildString {
            appendLine("$ kotlinc section.kt && kotlin SectionKt")
            if (outputs.isNotEmpty()) {
                outputs.forEach { out -> appendLine(out) }
            } else if (env.isNotEmpty()) {
                env.entries.forEach { (k, v) ->
                    appendLine(">> $k = $v")
                }
            } else {
                val declarations = IDENTIFIER_REGEX.findAll(trimmed).map { it.groupValues[1] }.toList()
                if (declarations.isNotEmpty()) {
                    appendLine("Compiled successfully: ${declarations.joinToString(", ")}")
                } else {
                    appendLine("Compiled & executed cleanly (exit code 0)")
                }
            }
            append("Process finished with exit code 0")
        }
    }

    private fun evaluateSimpleExpression(expr: String, env: Map<String, String>): String {
        val clean = expr.trim()
        if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length >= 2) {
            var inner = clean.substring(1, clean.length - 1)
            // Replace ${var} and $var
            env.forEach { (k, v) ->
                inner = inner.replace("\${$k}", v).replace("\$$k", v)
            }
            return inner
        }
        if (clean in env) {
            return env.getValue(clean)
        }
        // Simple integer addition/multiplication
        val mathMatch = Regex("""^(-?\d+)\s*([+\-*])\s*(-?\d+)$""").find(clean)
        if (mathMatch != null) {
            val a = mathMatch.groupValues[1].toLongOrNull() ?: 0L
            val op = mathMatch.groupValues[2]
            val b = mathMatch.groupValues[3].toLongOrNull() ?: 0L
            return when (op) {
                "+" -> (a + b).toString()
                "-" -> (a - b).toString()
                "*" -> (a * b).toString()
                else -> clean
            }
        }
        return clean
    }

    /**
     * Detects all hex color codes (`0xFF7F52FF`, `#7F52FF`, etc.) inside the Kotlin code block.
     */
    fun detectColorCodes(code: String): List<DetectedColorCode> {
        if (code.isBlank()) return emptyList()
        val results = mutableListOf<DetectedColorCode>()
        for (match in HEX_COLOR_REGEX.findAll(code)) {
            val rawHex = match.groups[1]?.value ?: match.groups[2]?.value ?: continue
            val normalizedHex = if (rawHex.length == 6) "FF$rawHex" else rawHex
            val parsedLong = normalizedHex.toLongOrNull(16) ?: continue
            val lineNum = code.substring(0, match.range.first).count { it == '\n' } + 1
            results.add(
                DetectedColorCode(
                    rawToken = match.value,
                    colorInt = parsedLong,
                    startOffset = match.range.first,
                    endOffset = match.range.last + 1,
                    line = lineNum
                )
            )
        }
        return results
    }

    /**
     * Finds the matching bracket offset `( <-> )`, `{ <-> }`, `[ <-> ]` near the cursor.
     */
    fun findMatchingBracketOffsets(code: String, cursorOffset: Int): Pair<Int, Int>? {
        if (code.isEmpty()) return null
        val safeCursor = cursorOffset.coerceIn(0, code.length)
        val candidateIndices = listOf(safeCursor - 1, safeCursor).filter { it in code.indices }
        val openToClose = mapOf('(' to ')', '{' to '}', '[' to ']')
        val closeToOpen = mapOf(')' to '(', '}' to '{', ']' to '[')

        for (idx in candidateIndices) {
            val ch = code[idx]
            if (ch in openToClose) {
                val targetClose = openToClose.getValue(ch)
                var depth = 0
                for (i in idx until code.length) {
                    if (code[i] == ch) depth++
                    else if (code[i] == targetClose) {
                        depth--
                        if (depth == 0) return idx to i
                    }
                }
            } else if (ch in closeToOpen) {
                val targetOpen = closeToOpen.getValue(ch)
                var depth = 0
                for (i in idx downTo 0) {
                    if (code[i] == ch) depth++
                    else if (code[i] == targetOpen) {
                        depth--
                        if (depth == 0) return i to idx
                    }
                }
            }
        }
        return null
    }

    /**
     * Real-time Kotlin Syntax Checker & Auto-Corrector.
     */
    fun diagnoseKotlinSyntax(code: String): List<KotlinDiagnostic> {
        if (code.isBlank()) return emptyList()
        val diagnostics = mutableListOf<KotlinDiagnostic>()

        data class BracketFrame(val char: Char, val offset: Int, val line: Int)
        val stack = ArrayDeque<BracketFrame>()
        val matchingOpen = mapOf(')' to '(', '}' to '{', ']' to '[')
        val matchingClose = mapOf('(' to ')', '{' to '}', '[' to ']')

        var i = 0
        var currentLine = 1
        var inBlockComment = false

        while (i < code.length) {
            val c = code[i]
            if (c == '\n') {
                currentLine++
                i++
                continue
            }
            if (inBlockComment) {
                if (c == '*' && i + 1 < code.length && code[i + 1] == '/') {
                    inBlockComment = false
                    i += 2
                } else {
                    i++
                }
                continue
            }
            if (c == '/' && i + 1 < code.length && code[i + 1] == '/') {
                while (i < code.length && code[i] != '\n') i++
                continue
            }
            if (c == '/' && i + 1 < code.length && code[i + 1] == '*') {
                inBlockComment = true
                i += 2
                continue
            }
            if (c == '"') {
                if (i + 2 < code.length && code[i + 1] == '"' && code[i + 2] == '"') {
                    val startOffset = i
                    val startLine = currentLine
                    i += 3
                    var closed = false
                    while (i + 2 < code.length) {
                        if (code[i] == '\n') currentLine++
                        if (code[i] == '"' && code[i + 1] == '"' && code[i + 2] == '"') {
                            closed = true
                            i += 3
                            break
                        }
                        i++
                    }
                    if (!closed) {
                        diagnostics.add(
                            KotlinDiagnostic(
                                line = startLine,
                                startOffset = startOffset,
                                endOffset = code.length,
                                message = "Unclosed raw string literal (missing \"\"\")",
                                severity = DiagnosticSeverity.ERROR,
                                quickFixLabel = "Append \"\"\"",
                                fixedCode = "$code\"\"\""
                            )
                        )
                        break
                    }
                    continue
                } else {
                    val stringStart = i
                    i++
                    var closed = false
                    while (i < code.length && code[i] != '\n') {
                        if (code[i] == '\\') {
                            i += 2
                            continue
                        }
                        if (code[i] == '"') {
                            closed = true
                            i++
                            break
                        }
                        i++
                    }
                    if (!closed) {
                        val fixed = code.substring(0, i) + "\"" + code.substring(i)
                        diagnostics.add(
                            KotlinDiagnostic(
                                line = currentLine,
                                startOffset = stringStart,
                                endOffset = i,
                                message = "Unclosed string literal on line $currentLine (missing closing \")",
                                severity = DiagnosticSeverity.ERROR,
                                quickFixLabel = "Close quote \"",
                                fixedCode = fixed
                            )
                        )
                    }
                    continue
                }
            }

            if (c in matchingClose) {
                stack.addLast(BracketFrame(c, i, currentLine))
            } else if (c in matchingOpen) {
                val expectedOpen = matchingOpen.getValue(c)
                if (stack.isEmpty() || stack.last().char != expectedOpen) {
                    val fixed = code.removeRange(i, i + 1)
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = currentLine,
                            startOffset = i,
                            endOffset = i + 1,
                            message = "Unexpected closing '$c' on line $currentLine with no matching '$expectedOpen'",
                            severity = DiagnosticSeverity.ERROR,
                            quickFixLabel = "Remove extra '$c'",
                            fixedCode = fixed
                        )
                    )
                } else {
                    stack.removeLast()
                }
            }
            i++
        }

        while (stack.isNotEmpty()) {
            val unclosed = stack.removeLast()
            val neededClose = matchingClose.getValue(unclosed.char)
            val appendSuffix = if (neededClose == '}') "\n}" else neededClose.toString()
            diagnostics.add(
                KotlinDiagnostic(
                    line = unclosed.line,
                    startOffset = unclosed.offset,
                    endOffset = (unclosed.offset + 1).coerceAtMost(code.length),
                    message = "Unclosed '${unclosed.char}' opened on line ${unclosed.line} (missing '$neededClose')",
                    severity = DiagnosticSeverity.ERROR,
                    quickFixLabel = "Insert '$neededClose'",
                    fixedCode = code + appendSuffix
                )
            )
        }

        val lines = code.split('\n')
        var lineStartOffset = 0
        lines.forEachIndexed { idx, rawLine ->
            val lineNum = idx + 1
            val trimmed = rawLine.trim()
            val lineEndOffset = lineStartOffset + rawLine.length

            if (!trimmed.startsWith("//") && !trimmed.startsWith("/*") && !trimmed.startsWith("*")) {
                val wrongFunMatch = Regex("""\b(function|func|def)\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(""").find(rawLine)
                if (wrongFunMatch != null) {
                    val badKeyword = wrongFunMatch.groupValues[1]
                    val badStart = lineStartOffset + wrongFunMatch.range.first
                    val badEnd = badStart + badKeyword.length
                    val fixed = code.replaceRange(badStart, badEnd, "fun")
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = badStart,
                            endOffset = badEnd,
                            message = "Kotlin uses 'fun' instead of '$badKeyword' to declare functions",
                            severity = DiagnosticSeverity.ERROR,
                            quickFixLabel = "Change '$badKeyword' to 'fun'",
                            fixedCode = fixed
                        )
                    )
                }

                val wrongValMatch = Regex("""^\s*(let|const)\s+([A-Za-z_][A-Za-z0-9_]*)\s*=""").find(rawLine)
                if (wrongValMatch != null) {
                    val badKeyword = wrongValMatch.groupValues[1]
                    val keywordIndexInLine = rawLine.indexOf(badKeyword)
                    val badStart = lineStartOffset + keywordIndexInLine
                    val badEnd = badStart + badKeyword.length
                    val fixed = code.replaceRange(badStart, badEnd, "val")
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = badStart,
                            endOffset = badEnd,
                            message = "Use 'val' or 'var' instead of '$badKeyword' in Kotlin",
                            severity = DiagnosticSeverity.ERROR,
                            quickFixLabel = "Replace with 'val'",
                            fixedCode = fixed
                        )
                    )
                }

                val switchMatch = Regex("""\bswitch\s*\(""").find(rawLine)
                if (switchMatch != null) {
                    val badStart = lineStartOffset + switchMatch.range.first
                    val badEnd = badStart + 6
                    val fixed = code.replaceRange(badStart, badEnd, "when")
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = badStart,
                            endOffset = badEnd,
                            message = "Kotlin uses 'when (...)' instead of 'switch (...)'",
                            severity = DiagnosticSeverity.ERROR,
                            quickFixLabel = "Change to 'when'",
                            fixedCode = fixed
                        )
                    )
                }

                val printMatch = Regex("""\b(System\.out\.println|console\.log)\s*\(""").find(rawLine)
                if (printMatch != null) {
                    val badCall = printMatch.groupValues[1]
                    val badStart = lineStartOffset + printMatch.range.first
                    val badEnd = badStart + badCall.length
                    val fixed = code.replaceRange(badStart, badEnd, "println")
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = badStart,
                            endOffset = badEnd,
                            message = "Prefer Kotlin's top-level 'println(...)' over '$badCall'",
                            severity = DiagnosticSeverity.WARNING,
                            quickFixLabel = "Use 'println'",
                            fixedCode = fixed
                        )
                    )
                }

                val newKeywordMatch = Regex("""\bnew\s+([A-Z][A-Za-z0-9_]*)\s*\(""").find(rawLine)
                if (newKeywordMatch != null) {
                    val badStart = lineStartOffset + newKeywordMatch.range.first
                    val classStart = lineStartOffset + newKeywordMatch.groups[1]!!.range.first
                    val fixed = code.removeRange(badStart, classStart)
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = badStart,
                            endOffset = badStart + 3,
                            message = "Kotlin does not use the 'new' keyword to instantiate classes",
                            severity = DiagnosticSeverity.ERROR,
                            quickFixLabel = "Remove 'new'",
                            fixedCode = fixed
                        )
                    )
                }

                val ifAssignMatch = Regex("""\bif\s*\(\s*[A-Za-z_][A-Za-z0-9_.]*\s*(=)\s*[^=]""").find(rawLine)
                if (ifAssignMatch != null) {
                    val eqRange = ifAssignMatch.groups[1]!!.range
                    val eqOffset = lineStartOffset + eqRange.first
                    val fixed = code.replaceRange(eqOffset, eqOffset + 1, "==")
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = eqOffset,
                            endOffset = eqOffset + 1,
                            message = "Assignment '=' used inside 'if' condition; Kotlin requires '=='",
                            severity = DiagnosticSeverity.ERROR,
                            quickFixLabel = "Change '=' to '=='",
                            fixedCode = fixed
                        )
                    )
                }

                if (trimmed.endsWith(";") && !trimmed.contains("for (") && !trimmed.contains("enum ")) {
                    val semiIndex = lineStartOffset + rawLine.lastIndexOf(';')
                    val fixed = code.removeRange(semiIndex, semiIndex + 1)
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = semiIndex,
                            endOffset = semiIndex + 1,
                            message = "Redundant trailing semicolon ';' in Kotlin",
                            severity = DiagnosticSeverity.WARNING,
                            quickFixLabel = "Remove ';'",
                            fixedCode = fixed
                        )
                    )
                }

                if (trimmed == "val" || trimmed == "var" || trimmed == "fun") {
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = lineStartOffset,
                            endOffset = lineEndOffset,
                            message = "Incomplete '$trimmed' declaration on line $lineNum (missing identifier)",
                            severity = DiagnosticSeverity.ERROR
                        )
                    )
                }
            }

            lineStartOffset = lineEndOffset + 1
        }

        return diagnostics
    }

    /**
     * Highlights Kotlin syntax, hex color literals, matching brackets, and syntax error squiggles.
     */
    fun highlightKotlinCode(
        code: String,
        colors: KotlinSyntaxColors,
        cursorOffset: Int = -1,
        diagnostics: List<KotlinDiagnostic> = emptyList()
    ): AnnotatedString {
        val colorLiterals = detectColorCodes(code)
        val matchedBrackets = if (cursorOffset >= 0) {
            findMatchingBracketOffsets(code, cursorOffset)
        } else {
            null
        }

        return buildAnnotatedString {
            append(code)
            addStyle(SpanStyle(color = colors.plainText), 0, code.length)

            for (match in TOKEN_REGEX.findAll(code)) {
                val range = match.range
                val start = range.first
                val end = range.last + 1

                when {
                    match.groups[1] != null || match.groups[2] != null -> {
                        addStyle(
                            SpanStyle(
                                color = colors.comment,
                                fontStyle = FontStyle.Italic
                            ),
                            start,
                            end
                        )
                    }
                    match.groups[3] != null -> {
                        addStyle(
                            SpanStyle(color = colors.stringLiteral),
                            start,
                            end
                        )
                    }
                    match.groups[4] != null -> {
                        addStyle(
                            SpanStyle(
                                color = colors.annotation,
                                fontWeight = FontWeight.Medium
                            ),
                            start,
                            end
                        )
                    }
                    match.groups[5] != null -> {
                        addStyle(
                            SpanStyle(color = colors.numberLiteral),
                            start,
                            end
                        )
                    }
                    match.groups[6] != null -> {
                        val word = match.value
                        val nextChar = code.getOrNull(end)
                        when {
                            word in KOTLIN_KEYWORDS -> {
                                addStyle(
                                    SpanStyle(
                                        color = colors.keyword,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    start,
                                    end
                                )
                            }
                            word in KOTLIN_STD_TYPES || (word.firstOrNull()?.isUpperCase() == true) -> {
                                addStyle(
                                    SpanStyle(
                                        color = colors.typeName,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    start,
                                    end
                                )
                            }
                            nextChar == '(' -> {
                                addStyle(
                                    SpanStyle(color = colors.functionName),
                                    start,
                                    end
                                )
                            }
                        }
                    }
                }
            }

            for (colorCode in colorLiterals) {
                val s = colorCode.startOffset.coerceIn(0, code.length)
                val e = colorCode.endOffset.coerceIn(s, code.length)
                if (s < e) {
                    val swatchColor = Color(colorCode.colorInt)
                    addStyle(
                        SpanStyle(
                            background = swatchColor.copy(alpha = 0.28f),
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        s,
                        e
                    )
                }
            }

            if (matchedBrackets != null) {
                val (openIdx, closeIdx) = matchedBrackets
                val bracketHighlight = SpanStyle(
                    background = colors.keyword.copy(alpha = 0.32f),
                    color = colors.keyword,
                    fontWeight = FontWeight.ExtraBold
                )
                if (openIdx in code.indices) addStyle(bracketHighlight, openIdx, openIdx + 1)
                if (closeIdx in code.indices) addStyle(bracketHighlight, closeIdx, closeIdx + 1)
            }

            for (diag in diagnostics) {
                val s = diag.startOffset.coerceIn(0, code.length)
                val e = diag.endOffset.coerceIn(s, code.length)
                if (s < e) {
                    val diagColor = if (diag.severity == DiagnosticSeverity.ERROR) {
                        Color(0xFFFF5F56)
                    } else {
                        Color(0xFFFFBD2E)
                    }
                    addStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            background = diagColor.copy(alpha = 0.22f)
                        ),
                        s,
                        e
                    )
                }
            }
        }
    }

    class KotlinSyntaxVisualTransformation(
        private val colors: KotlinSyntaxColors,
        private val cursorOffset: Int = -1,
        private val diagnostics: List<KotlinDiagnostic> = emptyList()
    ) : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            return TransformedText(
                text = highlightKotlinCode(
                    code = text.text,
                    colors = colors,
                    cursorOffset = cursorOffset,
                    diagnostics = diagnostics
                ),
                offsetMapping = OffsetMapping.Identity
            )
        }
    }

    fun getAutocompleteSuggestions(
        code: String,
        cursorPosition: Int,
        maxResults: Int = 6
    ): Pair<String, List<KotlinSuggestion>> {
        val safeCursor = cursorPosition.coerceIn(0, code.length)
        var prefixStart = safeCursor
        while (prefixStart > 0 && (code[prefixStart - 1].isLetterOrDigit() || code[prefixStart - 1] == '_' || code[prefixStart - 1] == '@')) {
            prefixStart--
        }
        val prefix = code.substring(prefixStart, safeCursor)

        val userDeclaredSymbols = IDENTIFIER_REGEX.findAll(code)
            .map { it.groupValues[1] }
            .filter { it.isNotBlank() && it != prefix }
            .distinct()
            .map { symbol ->
                KotlinSuggestion(
                    label = symbol,
                    insertText = symbol,
                    category = "Symbol",
                    description = "Identifier in block"
                )
            }
            .toList()

        val combinedPool = userDeclaredSymbols + BUILTIN_SUGGESTIONS

        if (prefix.isBlank()) {
            return "" to BUILTIN_SUGGESTIONS.take(maxResults)
        }

        val matches = combinedPool.filter { suggestion ->
            suggestion.label.startsWith(prefix, ignoreCase = true) &&
                suggestion.label != prefix
        }.distinctBy { it.label }
            .take(maxResults)

        return prefix to matches
    }

    fun applySuggestion(
        code: String,
        cursorPosition: Int,
        suggestion: KotlinSuggestion
    ): Pair<String, Int> {
        val safeCursor = cursorPosition.coerceIn(0, code.length)
        var prefixStart = safeCursor
        while (prefixStart > 0 && (code[prefixStart - 1].isLetterOrDigit() || code[prefixStart - 1] == '_' || code[prefixStart - 1] == '@')) {
            prefixStart--
        }
        val before = code.substring(0, prefixStart)
        val after = code.substring(safeCursor)
        val inserted = suggestion.insertText
        val newCode = before + inserted + after

        val cursorOffsetInInsert = when {
            inserted.contains("(\"\")") -> inserted.indexOf("(\"\")") + 2
            inserted.contains("()") && !inserted.contains("{") -> inserted.indexOf("()") + 1
            inserted.contains("<>") -> inserted.indexOf("<>") + 1
            inserted.contains("{\n    \n}") -> inserted.indexOf("{\n    ") + 6
            else -> inserted.length
        }

        return newCode to (prefixStart + cursorOffsetInInsert).coerceIn(0, newCode.length)
    }

    fun formatKotlinCode(rawCode: String): String {
        if (rawCode.isBlank()) return rawCode

        val rawLines = rawCode.replace("\r\n", "\n").split('\n')
        val formattedLines = mutableListOf<String>()
        var indentLevel = 0
        var consecutiveBlankLines = 0

        for (rawLine in rawLines) {
            var trimmed = rawLine.trim()

            if (trimmed.isEmpty()) {
                consecutiveBlankLines++
                if (consecutiveBlankLines <= 1) {
                    formattedLines.add("")
                }
                continue
            }
            consecutiveBlankLines = 0

            if (!trimmed.startsWith("//") && !trimmed.startsWith("/*") && !trimmed.startsWith("*")) {
                trimmed = trimmed
                    .replace(Regex("^function\\s+"), "fun ")
                    .replace(Regex("^func\\s+"), "fun ")
                    .replace(Regex("^def\\s+"), "fun ")
                    .replace(Regex("^(let|const)\\s+"), "val ")
                    .replace(Regex("\\b(if|for|while|when|catch)\\("), "$1 (")
                    .replace(Regex("\\)\\{"), ") {")
                    .replace(Regex("([^\\s])\\{$"), "$1 {")
                    .replace(Regex("\\}\\s*else\\s*\\{"), "} else {")
                    .replace(Regex("\\}\\s*catch"), "} catch")
                    .replace(Regex("\\}\\s*finally\\s*\\{"), "} finally {")
                    .removeSuffix(";")
            }

            val leadingCloses = trimmed.takeWhile { it == '}' || it == ')' }.length
            if (leadingCloses > 0) {
                indentLevel = (indentLevel - leadingCloses).coerceAtLeast(0)
            }

            val indentPrefix = "    ".repeat(indentLevel)
            formattedLines.add(indentPrefix + trimmed)

            val codeWithoutStringsAndComments = stripStringsAndLineComments(trimmed)
            val opens = codeWithoutStringsAndComments.count { it == '{' || it == '(' }
            val closes = codeWithoutStringsAndComments.count { it == '}' || it == ')' }
            val netChange = (opens - closes) + leadingCloses
            indentLevel = (indentLevel + netChange).coerceAtLeast(0)
        }

        return formattedLines.joinToString("\n").trimEnd()
    }

    private fun stripStringsAndLineComments(line: String): String {
        val sb = StringBuilder()
        var inString = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (!inString && c == '/' && i + 1 < line.length && line[i + 1] == '/') {
                break
            }
            if (c == '"' && (i == 0 || line[i - 1] != '\\')) {
                inString = !inString
            } else if (!inString) {
                sb.append(c)
            }
            i++
        }
        return sb.toString()
    }
}
