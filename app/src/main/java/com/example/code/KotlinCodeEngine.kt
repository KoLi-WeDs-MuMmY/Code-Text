package com.example.code

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.theme.KotlinSyntaxColors

data class KotlinSuggestion(
    val label: String,
    val insertText: String,
    val category: String,
    val description: String
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
        "Dispatchers", "Result", "Pair", "Triple", "Throwable", "Exception"
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
        """(//[^\n]*)|(/\*[\s\S]*?\*/)|("(?:\\.|[^"\\])*")|(@[A-Za-z_][A-Za-z0-9_]*)|\b(\d+(?:\.\d+)?[fFL]?)\b|\b([A-Za-z_][A-Za-z0-9_]*)\b"""
    )

    /**
     * Highlights Kotlin syntax without altering character length so cursor offsets stay 1:1.
     */
    fun highlightKotlinCode(code: String, colors: KotlinSyntaxColors): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            addStyle(SpanStyle(color = colors.plainText), 0, code.length)

            for (match in TOKEN_REGEX.findAll(code)) {
                val range = match.range
                val start = range.first
                val end = range.last + 1

                when {
                    // Single-line or multi-line comment
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
                    // String literal
                    match.groups[3] != null -> {
                        addStyle(
                            SpanStyle(color = colors.stringLiteral),
                            start,
                            end
                        )
                    }
                    // Annotation (@...)
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
                    // Numeric literal
                    match.groups[5] != null -> {
                        addStyle(
                            SpanStyle(color = colors.numberLiteral),
                            start,
                            end
                        )
                    }
                    // Identifier or Keyword
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
        }
    }

    /**
     * VisualTransformation wrapper for BasicTextField so live typing has full Kotlin syntax highlighting.
     */
    class KotlinSyntaxVisualTransformation(
        private val colors: KotlinSyntaxColors
    ) : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            return TransformedText(
                text = highlightKotlinCode(text.text, colors),
                offsetMapping = OffsetMapping.Identity
            )
        }
    }

    /**
     * Extracts current prefix at cursor and provides context-aware Kotlin autocomplete suggestions.
     */
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

        // Discover user-declared symbols inside the current code block
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
            // Return top quick-insert Kotlin building blocks when cursor is at whitespace
            return "" to BUILTIN_SUGGESTIONS.take(maxResults)
        }

        val matches = combinedPool.filter { suggestion ->
            suggestion.label.startsWith(prefix, ignoreCase = true) &&
                suggestion.label != prefix
        }.distinctBy { it.label }
            .take(maxResults)

        return prefix to matches
    }

    /**
     * Applies an autocomplete suggestion at the current cursor offset and returns (newCode, newCursorOffset).
     */
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

        // Place cursor sensibly inside parentheses or braces if template contains them
        val cursorOffsetInInsert = when {
            inserted.contains("(\"\")") -> inserted.indexOf("(\"\")") + 2
            inserted.contains("()") && !inserted.contains("{") -> inserted.indexOf("()") + 1
            inserted.contains("<>") -> inserted.indexOf("<>") + 1
            inserted.contains("{\n    \n}") -> inserted.indexOf("{\n    ") + 6
            else -> inserted.length
        }

        return newCode to (prefixStart + cursorOffsetInInsert).coerceIn(0, newCode.length)
    }

    /**
     * Language-specific Kotlin code formatter:
     * - Normalizes line endings
     * - Cleans keyword spacing (`if(`, `for(`, `when(`, `while(`, `catch(` -> `if (`, etc.)
     * - Ensures single space before `{`
     * - Auto-indents blocks using 4 spaces per brace/paren depth
     * - Collapses 3+ consecutive blank lines
     */
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

            // Normalize common Kotlin keyword spacing outside comments/strings
            if (!trimmed.startsWith("//") && !trimmed.startsWith("/*") && !trimmed.startsWith("*")) {
                trimmed = trimmed
                    .replace(Regex("\\b(if|for|while|when|catch)\\("), "$1 (")
                    .replace(Regex("\\)\\{"), ") {")
                    .replace(Regex("([^\\s])\\{$"), "$1 {")
                    .replace(Regex("\\}\\s*else\\s*\\{"), "} else {")
                    .replace(Regex("\\}\\s*catch"), "} catch")
                    .replace(Regex("\\}\\s*finally\\s*\\{"), "} finally {")
            }

            // Decrease indent before printing line if line starts with closing brace/paren
            val leadingCloses = trimmed.takeWhile { it == '}' || it == ')' }.length
            if (leadingCloses > 0) {
                indentLevel = (indentLevel - leadingCloses).coerceAtLeast(0)
            }

            val indentPrefix = "    ".repeat(indentLevel)
            formattedLines.add(indentPrefix + trimmed)

            // Calculate net brace change ignoring strings and line comments
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
