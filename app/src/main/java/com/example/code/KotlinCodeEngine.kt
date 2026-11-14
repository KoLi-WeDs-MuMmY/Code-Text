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
    val description: String,
    val tailSignature: String = "",
    val badgeChar: String = "K",
    val isNewSymbol: Boolean = false
)

enum class DiagnosticSeverity {
    ERROR,
    WARNING
}

/**
 * Lexical syntax context at the active cursor position:
 * Prevents false-positive autocomplete popups when writing sentences inside `println("...")`,
 * comments (`// ...`, `/* ... */`), or when naming a brand-new variable/function (`val myNewVar`).
 */
enum class KotlinLexicalContext {
    DEFAULT_CODE,
    STRING_PROSE,
    STRING_TEMPLATE_INTERPOLATION,
    COMMENT,
    CHAR_LITERAL,
    DECLARATION_IDENTIFIER,
    MEMBER_ACCESS_AFTER_DOT,
    TYPE_ANNOTATION_AFTER_COLON
}

data class KotlinDiagnostic(
    val line: Int, // 1-indexed line number
    val startOffset: Int,
    val endOffset: Int,
    val message: String,
    val severity: DiagnosticSeverity,
    val quickFixLabel: String? = null,
    val fixedCode: String? = null,
    val inlinePillLabel: String = message
)

data class KotlinStructureSymbol(
    val name: String,
    val kindBadge: String, // "m", "C", "I", "v"
    val line: Int,
    val signature: String
)

data class ConstantShortcutKey(
    val label: String,
    val insertText: String,
    val cursorOffsetBack: Int = 0
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

data class KotlinCodeMetrics(
    val functionCount: Int,
    val typeCount: Int,
    val valCount: Int,
    val varCount: Int,
    val cyclomaticComplexity: Int,
    val complexityLabel: String,
    val immutabilityPercent: Int
)

data class KotlinLiveTemplate(
    val id: String,
    val badge: String,
    val title: String,
    val snippet: String
)

object KotlinCodeEngine {

    /**
     * Constant (`val`) bottom shortcut strip confirmed by user:
     * n(snippets) = val (strictly 8 constant typing keys; autocomplete suggestions never appear here).
     */
    val VAL_CONSTANT_SHORTCUT_KEYS: List<ConstantShortcutKey> = listOf(
        ConstantShortcutKey("Tab", "    ", 0),
        ConstantShortcutKey("( )", "()", 1),
        ConstantShortcutKey("{ }", "{}", 1),
        ConstantShortcutKey("\" \"", "\"\"", 1),
        ConstantShortcutKey("->", " -> ", 0),
        ConstantShortcutKey(".", ".", 0),
        ConstantShortcutKey("=", " = ", 0),
        ConstantShortcutKey(":", ": ", 0)
    )

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

    private val POSTFIX_SUGGESTIONS = listOf(
        KotlinSuggestion(
            label = "sout",
            insertText = "println(\"\")",
            category = "Postfix",
            description = "Print to standard output",
            tailSignature = "println()",
            badgeChar = "{}"
        ),
        KotlinSuggestion(
            label = "soutv",
            insertText = "println(\"value = \${value}\")",
            category = "Postfix",
            description = "Prints a value to System.out",
            tailSignature = "println(\"value = \${value}\")",
            badgeChar = "{}"
        ),
        KotlinSuggestion(
            label = "serr",
            insertText = "System.err.println(\"\")",
            category = "Postfix",
            description = "Print to standard error",
            tailSignature = "System.err.println()",
            badgeChar = "{}"
        ),
        KotlinSuggestion(
            label = "main",
            insertText = "fun main() {\n    \n}",
            category = "Postfix",
            description = "Kotlin main entry point function",
            tailSignature = "fun main(args: Array<String>)",
            badgeChar = "{}"
        ),
        KotlinSuggestion(
            label = "psvm",
            insertText = "fun main() {\n    \n}",
            category = "Postfix",
            description = "Public static void main equivalent",
            tailSignature = "fun main()",
            badgeChar = "{}"
        ),
        KotlinSuggestion(
            label = "fori",
            insertText = "for (i in 0 until n) {\n    \n}",
            category = "Postfix",
            description = "Create indexed loop over range",
            tailSignature = "for (i in 0 until n)",
            badgeChar = "{}"
        ),
        KotlinSuggestion(
            label = "iter",
            insertText = "for (item in items) {\n    \n}",
            category = "Postfix",
            description = "Iterate elements of iterable",
            tailSignature = "for (item in items)",
            badgeChar = "{}"
        ),
        KotlinSuggestion(
            label = "ifn",
            insertText = "if (value == null) {\n    \n}",
            category = "Postfix",
            description = "Inserts 'if null' statement",
            tailSignature = "if (x == null)",
            badgeChar = "{}"
        ),
        KotlinSuggestion(
            label = "inn",
            insertText = "if (value != null) {\n    \n}",
            category = "Postfix",
            description = "Inserts 'if not null' statement",
            tailSignature = "if (x != null)",
            badgeChar = "{}"
        ),
        KotlinSuggestion(
            label = "tryc",
            insertText = "try {\n    \n} catch (e: Exception) {\n    println(e.message)\n}",
            category = "Postfix",
            description = "Surround with try / catch block",
            tailSignature = "try { } catch (e: Exception)",
            badgeChar = "{}"
        ),
        KotlinSuggestion(
            label = "todo",
            insertText = "// TODO: ",
            category = "Postfix",
            description = "Insert TODO comment marker",
            tailSignature = "// TODO",
            badgeChar = "{}"
        )
    )

    private val BUILTIN_SUGGESTIONS = listOf(
        KotlinSuggestion("fun", "fun ", "Keyword", "Declare a Kotlin function", "", "K"),
        KotlinSuggestion("fun interface", "fun interface ", "Keyword", "Declare a SAM functional interface", "", "K"),
        KotlinSuggestion("fun", "fun name() {\n    \n}", "Template", "New function declaration block", "fun name() { }", "{}"),
        KotlinSuggestion("FunctionalInterface", "@FunctionalInterface", "Annotation", "Indicates an interface is a SAM functional interface", "(java.lang)", "@"),
        KotlinSuggestion("Function", "Function<Any>", "Interface", "Represents a function value", "<out R> (kotlin)", "C"),
        KotlinSuggestion("Function0", "Function0<Unit>", "Interface", "A function that takes 0 arguments", "<out R> (kotlin.jvm.functions)", "I"),
        KotlinSuggestion("Function1", "Function1<Any, Unit>", "Interface", "A function that takes 1 argument", "<in P1, out R> (kotlin.jvm.functions)", "I"),
        KotlinSuggestion("Function2", "Function2<Any, Any, Unit>", "Interface", "A function that takes 2 arguments", "<in P1, in P2, out R> (kotlin.jvm.functions)", "I"),
        KotlinSuggestion("SourceDebugExtension", "@SourceDebugExtension", "Annotation", "Kotlin compiler debug metadata annotation", "(kotlin.jvm.internal)", "@"),
        KotlinSuggestion("SocketTimeoutException", "SocketTimeoutException()", "Class", "Signals that a timeout has occurred on a socket read or accept", "(java.net)", "C"),
        KotlinSuggestion("SocketOptions", "SocketOptions", "Interface", "Interface of methods to get/set socket options", "(java.net)", "I"),
        KotlinSuggestion("val", "val ", "Keyword", "Declare an immutable read-only property", "", "K"),
        KotlinSuggestion("var", "var ", "Keyword", "Declare a mutable property", "", "K"),
        KotlinSuggestion("data class", "data class Item(\n    val id: Int,\n    val name: String\n)", "Template", "Immutable data holder class with equals/hashCode/copy", "data class Name(...)", "{}"),
        KotlinSuggestion("sealed interface", "sealed interface UiState {\n    data object Loading : UiState\n}", "Template", "Restricted hierarchy for exhaustive when-expressions", "sealed interface Name", "{}"),
        KotlinSuggestion("suspend fun", "suspend fun ", "Keyword", "Declare a coroutine suspending function", "kotlinx.coroutines", "K"),
        KotlinSuggestion("when", "when (value) {\n    else -> {}\n}", "Keyword", "Exhaustive multi-branch pattern matching expression", "when (expr) { ... }", "K"),
        KotlinSuggestion("if", "if (condition) {\n    \n}", "Keyword", "Conditional expression", "if (cond) { ... }", "K"),
        KotlinSuggestion("forEach", "forEach { item ->\n    \n}", "Function", "Performs the given action on each element", "inline fun Iterable<T>.forEach", "m"),
        KotlinSuggestion("map", "map { it }", "Function", "Returns a list containing the results of applying transform", "inline fun Iterable<T>.map", "m"),
        KotlinSuggestion("filter", "filter { it }", "Function", "Returns a list containing only elements matching predicate", "inline fun Iterable<T>.filter", "m"),
        KotlinSuggestion("println", "println(\"\")", "Function", "Prints the given message and newline to standard output", "(kotlin.io)", "m"),
        KotlinSuggestion("listOf", "listOf()", "Function", "Returns a new read-only list of given elements", "(kotlin.collections)", "m"),
        KotlinSuggestion("mutableListOf", "mutableListOf()", "Function", "Returns a new MutableList with the given elements", "(kotlin.collections)", "m"),
        KotlinSuggestion("mapOf", "mapOf()", "Function", "Returns a new read-only map with the specified contents", "(kotlin.collections)", "m"),
        KotlinSuggestion("coroutineScope", "coroutineScope {\n    \n}", "Function", "Creates a CoroutineScope and calls the specified suspend block", "(kotlinx.coroutines)", "m"),
        KotlinSuggestion("withContext", "withContext(Dispatchers.IO) {\n    \n}", "Function", "Calls the specified suspending block with a given coroutine context", "(kotlinx.coroutines)", "m"),
        KotlinSuggestion("class", "class ", "Keyword", "Declare a Kotlin class", "", "K"),
        KotlinSuggestion("interface", "interface ", "Keyword", "Declare a Kotlin interface", "", "K"),
        KotlinSuggestion("object", "object ", "Keyword", "Declare a singleton object", "", "K"),
        KotlinSuggestion("override fun", "override fun ", "Keyword", "Override a superclass or interface member function", "", "K"),
        KotlinSuggestion("private val", "private val ", "Keyword", "Declare a private immutable property", "", "K"),
        KotlinSuggestion("return", "return ", "Keyword", "Return from the nearest enclosing function", "", "K"),
        KotlinSuggestion("String", "String", "Class", "The String class represents character strings", "(kotlin)", "C"),
        KotlinSuggestion("Int", "Int", "Class", "Represents a 32-bit signed integer", "(kotlin)", "C"),
        KotlinSuggestion("Boolean", "Boolean", "Class", "Represents a value which is either true or false", "(kotlin)", "C"),
        KotlinSuggestion("List", "List<String>", "Interface", "A generic ordered collection of elements", "<out E> (kotlin.collections)", "I"),
        KotlinSuggestion("Flow", "Flow<String>", "Interface", "An asynchronous data stream that sequentially emits values", "<out T> (kotlinx.coroutines.flow)", "I"),
        KotlinSuggestion("StateFlow", "StateFlow<String>", "Interface", "A SharedFlow that represents a read-only state with a single value", "<out T> (kotlinx.coroutines.flow)", "I")
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

                // A0. Live IDE Postfix Abbreviation Expansion when Space is pressed after sout, psvm, fori, ifn, inn
                if (insertedChar == ' ') {
                    val beforeSpace = oldText.substring(0, insertedIndex)
                    val lineStart = beforeSpace.lastIndexOf('\n') + 1
                    val lineBeforeWord = beforeSpace.substring(lineStart)
                    val trimmedLine = lineBeforeWord.trim()
                    val leadingIndent = lineBeforeWord.takeWhile { it == ' ' || it == '\t' }
                    val abbrevExpansion: Pair<String, Int>? = when (trimmedLine) {
                        "sout" -> "println(\"\")" to 9
                        "serr" -> "System.err.println(\"\")" to 20
                        "psvm" -> "fun main() {\n$leadingIndent    \n$leadingIndent}" to (14 + leadingIndent.length + 4)
                        "fori" -> "for (i in 0 until n) {\n$leadingIndent    \n$leadingIndent}" to (24 + leadingIndent.length + 4)
                        "ifn" -> "if (value == null) {\n$leadingIndent    \n$leadingIndent}" to (22 + leadingIndent.length + 4)
                        "inn" -> "if (value != null) {\n$leadingIndent    \n$leadingIndent}" to (22 + leadingIndent.length + 4)
                        else -> null
                    }
                    if (abbrevExpansion != null) {
                        val (replacement, cursorDelta) = abbrevExpansion
                        val wordStart = insertedIndex - trimmedLine.length
                        val expandedText = oldText.substring(0, wordStart) + replacement + oldText.substring(insertedIndex)
                        return TextFieldValue(
                            text = expandedText,
                            selection = TextRange((wordStart + cursorDelta).coerceIn(0, expandedText.length))
                        )
                    }
                }

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
                    val existingIndentAfter = newText.substring(newCursor).takeWhile { it == ' ' || it == '\t' }
                    val neededIndent = if (existingIndentAfter.length >= totalIndent.length) "" else totalIndent.substring(existingIndentAfter.length)
                    if (neededIndent.isNotEmpty()) {
                        val indentedText = newText.substring(0, newCursor) + neededIndent + newText.substring(newCursor)
                        return TextFieldValue(
                            text = indentedText,
                            selection = TextRange(newCursor + neededIndent.length)
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

    private val DECL_SYMBOL_REGEX = Regex("""\b(val|var|fun|class|interface|object)\s+([A-Za-z_][A-Za-z0-9_]*)""")
    private val NEW_DECL_PREFIX_REGEX = Regex("""\b(?:val|var|fun|class|interface|object|typealias)\s+[A-Za-z_][A-Za-z0-9_]*$""")
    private val TYPE_CONTEXT_PREFIX_REGEX = Regex("""(?::|\bas\??|\bis)\s*[A-Za-z_][A-Za-z0-9_]*$""")

    private val diagnosticsCache = object : LinkedHashMap<String, List<KotlinDiagnostic>>(24, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<KotlinDiagnostic>>?): Boolean = size > 20
    }

    private val structureCache = object : LinkedHashMap<String, List<KotlinStructureSymbol>>(24, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<KotlinStructureSymbol>>?): Boolean = size > 20
    }

    /**
     * Real-time Kotlin Syntax Checker & Auto-Corrector (with O(1) LRU memoization).
     */
    fun diagnoseKotlinSyntax(code: String): List<KotlinDiagnostic> {
        if (code.isBlank()) return emptyList()
        synchronized(diagnosticsCache) {
            diagnosticsCache[code]?.let { return it }
        }
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
                            severity = DiagnosticSeverity.ERROR,
                            inlinePillLabel = "Syntax error  2"
                        )
                    )
                }

                if (trimmed == "sout" || trimmed == "serr" || trimmed == "soutv") {
                    val replacement = when (trimmed) {
                        "sout" -> "println(\"\")"
                        "soutv" -> "println(\"value = \${value}\")"
                        else -> "System.err.println(\"\")"
                    }
                    val leading = rawLine.takeWhile { it == ' ' || it == '\t' }
                    val fixed = code.replaceRange(lineStartOffset, lineEndOffset, leading + replacement)
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = lineStartOffset,
                            endOffset = lineEndOffset,
                            message = "Unexpanded IDE shorthand '$trimmed' on line $lineNum (use '$replacement')",
                            severity = DiagnosticSeverity.ERROR,
                            quickFixLabel = "Expand to $replacement",
                            fixedCode = fixed,
                            inlinePillLabel = "Unresolved reference: $trimmed"
                        )
                    )
                }

                val typoKeywordMatch = Regex("""^\s*(funn|vall|varr)\b""").find(rawLine)
                if (typoKeywordMatch != null) {
                    val badKw = typoKeywordMatch.groupValues[1]
                    val rightKw = when (badKw) {
                        "funn" -> "fun"
                        "vall" -> "val"
                        else -> "var"
                    }
                    val kwIdx = rawLine.indexOf(badKw)
                    val badStart = lineStartOffset + kwIdx
                    val badEnd = badStart + badKw.length
                    val fixed = code.replaceRange(badStart, badEnd, rightKw)
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = badStart,
                            endOffset = badEnd,
                            message = "Unknown keyword typo '$badKw' on line $lineNum; did you mean '$rightKw'?",
                            severity = DiagnosticSeverity.ERROR,
                            quickFixLabel = "Fix to '$rightKw'",
                            fixedCode = fixed
                        )
                    )
                }

                val typoTypeMatch = Regex("""\b(Sting|Integar|Booleen)\b""").find(rawLine)
                if (typoTypeMatch != null) {
                    val badType = typoTypeMatch.groupValues[1]
                    val rightType = when (badType) {
                        "Sting" -> "String"
                        "Integar" -> "Int"
                        else -> "Boolean"
                    }
                    val badStart = lineStartOffset + typoTypeMatch.range.first
                    val badEnd = badStart + badType.length
                    val fixed = code.replaceRange(badStart, badEnd, rightType)
                    diagnostics.add(
                        KotlinDiagnostic(
                            line = lineNum,
                            startOffset = badStart,
                            endOffset = badEnd,
                            message = "Unresolved type '$badType' on line $lineNum; did you mean '$rightType'?",
                            severity = DiagnosticSeverity.ERROR,
                            quickFixLabel = "Change to '$rightType'",
                            fixedCode = fixed
                        )
                    )
                }
            }

            lineStartOffset = lineEndOffset + 1
        }

        synchronized(diagnosticsCache) {
            diagnosticsCache[code] = diagnostics
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

            BracketPairColorizer.applyToBuilder(
                builder = this,
                code = code,
                cursorOffset = cursorOffset,
                isPython = false
            )

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

    private val MEMBER_EXTENSION_SUGGESTIONS = listOf(
        KotlinSuggestion("forEach", "forEach { item ->\n    \n}", "Function", "Performs the given action on each element", "inline fun Iterable<T>.forEach", "m"),
        KotlinSuggestion("map", "map { it }", "Function", "Returns a list containing the results of applying transform", "inline fun Iterable<T>.map", "m"),
        KotlinSuggestion("filter", "filter { it }", "Function", "Returns a list containing only elements matching predicate", "inline fun Iterable<T>.filter", "m"),
        KotlinSuggestion("firstOrNull", "firstOrNull()", "Function", "Returns the first element, or null if empty", "fun Iterable<T>.firstOrNull(): T?", "m"),
        KotlinSuggestion("isNotEmpty", "isNotEmpty()", "Function", "Returns true if this char sequence or collection is not empty", "fun isNotEmpty(): Boolean", "m"),
        KotlinSuggestion("isEmpty", "isEmpty()", "Function", "Returns true if this char sequence or collection is empty", "fun isEmpty(): Boolean", "m"),
        KotlinSuggestion("length", "length", "Property", "Returns the length of this character sequence", "val String.length: Int", "v"),
        KotlinSuggestion("size", "size", "Property", "Returns the number of elements in the collection", "val Collection<T>.size: Int", "v"),
        KotlinSuggestion("trim", "trim()", "Function", "Removes leading and trailing whitespace", "fun String.trim(): String", "m"),
        KotlinSuggestion("let", "let { \n    \n}", "Scope", "Calls the specified function block with this value as its argument", "inline fun <T, R> T.let", "m"),
        KotlinSuggestion("apply", "apply {\n    \n}", "Scope", "Calls the specified function block with this value as its receiver", "inline fun <T> T.apply: T", "m"),
        KotlinSuggestion("also", "also { \n    \n}", "Scope", "Calls the specified function block with this value as its argument and returns this", "inline fun <T> T.also: T", "m"),
        KotlinSuggestion("toString", "toString()", "Function", "Returns a string representation of the object", "fun Any.toString(): String", "m")
    )

    /**
     * Scans the Kotlin source from index 0 up to `cursorPosition` to determine the exact lexical syntax context.
     * Ensures that typing normal text inside `println("some sentence here")` or inside comments NEVER pops up variable suggestions!
     */
    fun analyzeLexicalContext(code: String, cursorPosition: Int): KotlinLexicalContext {
        val safeCursor = cursorPosition.coerceIn(0, code.length)
        var i = 0
        var inLineComment = false
        var inBlockComment = false
        var inSingleLineString = false
        var inRawTripleString = false
        var inCharLiteral = false
        var templateBraceDepth = 0

        while (i < safeCursor) {
            val c = code[i]

            if (inLineComment) {
                if (c == '\n') inLineComment = false
                i++
                continue
            }
            if (inBlockComment) {
                if (c == '*' && i + 1 < safeCursor && code[i + 1] == '/') {
                    inBlockComment = false
                    i += 2
                } else {
                    i++
                }
                continue
            }
            if (inCharLiteral) {
                if (c == '\\') {
                    i += 2
                    continue
                }
                if (c == '\'' || c == '\n') {
                    inCharLiteral = false
                }
                i++
                continue
            }
            if (inRawTripleString) {
                if (templateBraceDepth > 0) {
                    if (c == '{') templateBraceDepth++
                    else if (c == '}') templateBraceDepth--
                    i++
                    continue
                }
                if (c == '$' && i + 1 < safeCursor && code[i + 1] == '{') {
                    templateBraceDepth = 1
                    i += 2
                    continue
                }
                if (c == '"' && i + 2 < safeCursor && code[i + 1] == '"' && code[i + 2] == '"') {
                    inRawTripleString = false
                    i += 3
                    continue
                }
                i++
                continue
            }
            if (inSingleLineString) {
                if (templateBraceDepth > 0) {
                    if (c == '{') templateBraceDepth++
                    else if (c == '}') templateBraceDepth--
                    i++
                    continue
                }
                if (c == '\\') {
                    i += 2
                    continue
                }
                if (c == '$' && i + 1 < safeCursor && code[i + 1] == '{') {
                    templateBraceDepth = 1
                    i += 2
                    continue
                }
                if (c == '"' || c == '\n') {
                    inSingleLineString = false
                }
                i++
                continue
            }

            // Outside comments and strings:
            if (c == '/' && i + 1 < safeCursor && code[i + 1] == '/') {
                inLineComment = true
                i += 2
                continue
            }
            if (c == '/' && i + 1 < safeCursor && code[i + 1] == '*') {
                inBlockComment = true
                i += 2
                continue
            }
            if (c == '\'') {
                inCharLiteral = true
                i++
                continue
            }
            if (c == '"') {
                if (i + 2 < safeCursor && code[i + 1] == '"' && code[i + 2] == '"') {
                    inRawTripleString = true
                    i += 3
                } else {
                    inSingleLineString = true
                    i++
                }
                continue
            }
            i++
        }

        if (inLineComment || inBlockComment) return KotlinLexicalContext.COMMENT
        if (inCharLiteral) return KotlinLexicalContext.CHAR_LITERAL

        if (inSingleLineString || inRawTripleString) {
            if (templateBraceDepth > 0) {
                return KotlinLexicalContext.STRING_TEMPLATE_INTERPOLATION
            }
            // Check if cursor is immediately preceded by `$identifier` or `$` inside the string
            var scanBack = safeCursor
            while (scanBack > 0 && (code[scanBack - 1].isLetterOrDigit() || code[scanBack - 1] == '_')) {
                scanBack--
            }
            if (scanBack > 0 && code[scanBack - 1] == '$' && (scanBack < 2 || code[scanBack - 2] != '\\')) {
                return KotlinLexicalContext.STRING_TEMPLATE_INTERPOLATION
            }
            return KotlinLexicalContext.STRING_PROSE
        }

        // In DEFAULT_CODE: check if we are naming a brand-new identifier, after a dot `.`, or after a type colon `:`
        val lineStart = code.lastIndexOf('\n', (safeCursor - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val linePrefix = code.substring(lineStart, safeCursor)

        if (NEW_DECL_PREFIX_REGEX.containsMatchIn(linePrefix)) {
            return KotlinLexicalContext.DECLARATION_IDENTIFIER
        }

        var tokenStart = safeCursor
        while (tokenStart > 0 && (code[tokenStart - 1].isLetterOrDigit() || code[tokenStart - 1] == '_' || code[tokenStart - 1] == '@')) {
            tokenStart--
        }
        if (tokenStart > 0 && code[tokenStart - 1] == '.') {
            return KotlinLexicalContext.MEMBER_ACCESS_AFTER_DOT
        }
        if (TYPE_CONTEXT_PREFIX_REGEX.containsMatchIn(linePrefix)) {
            return KotlinLexicalContext.TYPE_ANNOTATION_AFTER_COLON
        }

        return KotlinLexicalContext.DEFAULT_CODE
    }

    fun getAutocompleteSuggestions(
        code: String,
        cursorPosition: Int,
        maxResults: Int = 8
    ): Pair<String, List<KotlinSuggestion>> {
        val safeCursor = cursorPosition.coerceIn(0, code.length)
        val lexicalContext = analyzeLexicalContext(code, safeCursor)

        // Never pop up suggestions when writing normal prose inside a string (e.g. inside println("...")),
        // inside comments, inside char literals, or when naming a brand-new declaration!
        when (lexicalContext) {
            KotlinLexicalContext.STRING_PROSE,
            KotlinLexicalContext.COMMENT,
            KotlinLexicalContext.CHAR_LITERAL,
            KotlinLexicalContext.DECLARATION_IDENTIFIER -> return "" to emptyList()
            else -> {}
        }

        var prefixStart = safeCursor
        while (prefixStart > 0 && (code[prefixStart - 1].isLetterOrDigit() || code[prefixStart - 1] == '_' || code[prefixStart - 1] == '@')) {
            prefixStart--
        }
        val prefix = code.substring(prefixStart, safeCursor)

        val userDeclaredSymbols = DECL_SYMBOL_REGEX.findAll(code)
            .mapNotNull { match ->
                val kind = match.groupValues[1]
                val symbol = match.groupValues[2]
                if (symbol.isBlank() || symbol == prefix) null
                else {
                    val badge = when (kind) {
                        "fun" -> "m"
                        "class", "object" -> "C"
                        "interface" -> "I"
                        else -> "v"
                    }
                    KotlinSuggestion(
                        label = symbol,
                        insertText = if (kind == "fun") "$symbol()" else symbol,
                        category = "Local Symbol",
                        description = "Newly declared $kind '$symbol' in current .ktscribe file",
                        tailSignature = "($kind in file)",
                        badgeChar = badge,
                        isNewSymbol = true
                    )
                }
            }
            .distinctBy { it.label }
            .toList()

        // Choose syntax-specific candidate pool based on lexicalContext
        val combinedPool = when (lexicalContext) {
            KotlinLexicalContext.MEMBER_ACCESS_AFTER_DOT -> {
                MEMBER_EXTENSION_SUGGESTIONS + userDeclaredSymbols.filter { it.badgeChar == "m" || it.badgeChar == "v" }
            }
            KotlinLexicalContext.TYPE_ANNOTATION_AFTER_COLON -> {
                userDeclaredSymbols.filter { it.badgeChar == "C" || it.badgeChar == "I" } +
                    BUILTIN_SUGGESTIONS.filter { it.badgeChar == "C" || it.badgeChar == "I" }
            }
            KotlinLexicalContext.STRING_TEMPLATE_INTERPOLATION -> {
                userDeclaredSymbols + BUILTIN_SUGGESTIONS.filter { it.badgeChar == "m" || it.badgeChar == "v" }
            }
            else -> {
                POSTFIX_SUGGESTIONS + userDeclaredSymbols + BUILTIN_SUGGESTIONS
            }
        }

        if (prefix.isBlank()) {
            return when (lexicalContext) {
                KotlinLexicalContext.MEMBER_ACCESS_AFTER_DOT -> {
                    "" to MEMBER_EXTENSION_SUGGESTIONS.take(maxResults)
                }
                KotlinLexicalContext.TYPE_ANNOTATION_AFTER_COLON -> {
                    "" to combinedPool.take(maxResults)
                }
                else -> {
                    "" to (POSTFIX_SUGGESTIONS.take(3) + userDeclaredSymbols.take(2) + BUILTIN_SUGGESTIONS.take(maxResults - 3))
                }
            }
        }

        val prefixFirst = prefix.take(2)
        val exactStarts = combinedPool.filter { suggestion ->
            (suggestion.label.startsWith(prefix, ignoreCase = true) ||
                suggestion.insertText.startsWith(prefix, ignoreCase = true)) &&
                suggestion.insertText != prefix
        }
        val secondaryMatches = if (prefixFirst.isNotEmpty()) {
            combinedPool.filter { suggestion ->
                suggestion !in exactStarts &&
                    (suggestion.label.startsWith(prefixFirst, ignoreCase = true) ||
                        suggestion.label.contains(prefix, ignoreCase = true)) &&
                    suggestion.insertText != prefix
            }
        } else {
            emptyList()
        }

        val matches = (exactStarts + secondaryMatches)
            .distinctBy { "${it.label}_${it.tailSignature}" }
            .take(maxResults)

        return prefix to matches
    }

    /**
     * Extracts structure outline symbols (functions, classes, interfaces, properties) with 1-indexed line numbers
     * for the Left Rail Structure & Find tool (with O(1) LRU memoization).
     */
    fun extractStructureSymbols(code: String): List<KotlinStructureSymbol> {
        if (code.isBlank()) return emptyList()
        synchronized(structureCache) {
            structureCache[code]?.let { return it }
        }
        val results = mutableListOf<KotlinStructureSymbol>()
        code.lines().forEachIndexed { idx, line ->
            val match = DECL_SYMBOL_REGEX.find(line)
            if (match != null) {
                val kind = match.groupValues[1]
                val name = match.groupValues[2]
                val badge = when (kind) {
                    "fun" -> "m"
                    "class", "object" -> "C"
                    "interface" -> "I"
                    else -> "v"
                }
                results.add(
                    KotlinStructureSymbol(
                        name = if (kind == "fun") "$name()" else name,
                        kindBadge = badge,
                        line = idx + 1,
                        signature = line.trim().removeSuffix("{").trim()
                    )
                )
            }
        }
        synchronized(structureCache) {
            structureCache[code] = results
        }
        return results
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

    val LIVE_TEMPLATES: List<KotlinLiveTemplate> = listOf(
        KotlinLiveTemplate(
            id = "main_fn",
            badge = "fun main",
            title = "Entry Point",
            snippet = "fun main() {\n    val message = \"Hello, Kotlin!\"\n    println(message)\n}"
        ),
        KotlinLiveTemplate(
            id = "data_class",
            badge = "data class",
            title = "Immutable Model",
            snippet = "data class UserProfile(\n    val id: Long,\n    val name: String,\n    val isActive: Boolean = true\n)"
        ),
        KotlinLiveTemplate(
            id = "sealed_ui",
            badge = "sealed UI",
            title = "Sealed State",
            snippet = "sealed interface ScreenState {\n    data object Loading : ScreenState\n    data class Success(val items: List<String>) : ScreenState\n    data class Error(val reason: String) : ScreenState\n}"
        ),
        KotlinLiveTemplate(
            id = "state_flow",
            badge = "StateFlow",
            title = "Reactive StateHolder",
            snippet = "private val _uiState = MutableStateFlow(\"Ready\")\nval uiState: StateFlow<String> = _uiState.asStateFlow()"
        ),
        KotlinLiveTemplate(
            id = "suspend_try",
            badge = "suspend",
            title = "Safe Coroutine Call",
            snippet = "suspend fun fetchItems(): Result<List<String>> = runCatching {\n    listOf(\"Alpha\", \"Beta\", \"Gamma\")\n}"
        ),
        KotlinLiveTemplate(
            id = "collection_pipe",
            badge = "pipeline",
            title = "Filter & Map",
            snippet = "val processed = rawItems\n    .filter { it.isNotBlank() }\n    .map { it.trim().uppercase() }\n    .distinct()"
        ),
        KotlinLiveTemplate(
            id = "when_expr",
            badge = "when {}",
            title = "Exhaustive When",
            snippet = "val statusLabel = when (code) {\n    200 -> \"OK\"\n    404 -> \"Not Found\"\n    else -> \"Unknown (\$code)\"\n}"
        )
    )

    /**
     * Computes live IDE code quality & complexity metrics for the current Kotlin block.
     */
    fun analyzeCodeMetrics(code: String): KotlinCodeMetrics {
        if (code.isBlank()) {
            return KotlinCodeMetrics(
                functionCount = 0,
                typeCount = 0,
                valCount = 0,
                varCount = 0,
                cyclomaticComplexity = 1,
                complexityLabel = "O(1) Clean",
                immutabilityPercent = 100
            )
        }
        val fnCount = Regex("""\bfun\s+[A-Za-z_][A-Za-z0-9_]*""").findAll(code).count()
        val typeCount = Regex("""\b(?:class|interface|object)\s+[A-Za-z_][A-Za-z0-9_]*""").findAll(code).count()
        val valCount = Regex("""\bval\s+[A-Za-z_][A-Za-z0-9_]*""").findAll(code).count()
        val varCount = Regex("""\bvar\s+[A-Za-z_][A-Za-z0-9_]*""").findAll(code).count()
        val branchCount = Regex("""\b(?:if|else\s+if|when|for|while|catch)\b|&&|\|\||\?:""").findAll(code).count()
        val complexity = 1 + branchCount
        val complexityLabel = when {
            complexity <= 2 -> "Low ($complexity)"
            complexity <= 5 -> "Moderate ($complexity)"
            else -> "High ($complexity)"
        }
        val totalProps = valCount + varCount
        val immutability = if (totalProps == 0) 100 else ((valCount * 100f) / totalProps).toInt()
        return KotlinCodeMetrics(
            functionCount = fnCount,
            typeCount = typeCount,
            valCount = valCount,
            varCount = varCount,
            cyclomaticComplexity = complexity,
            complexityLabel = complexityLabel,
            immutabilityPercent = immutability
        )
    }

    /**
     * IDE Refactoring: Surrounds the current selection (or current line if collapsed) with a Kotlin construct.
     */
    fun surroundSelectionOrLine(
        fieldValue: TextFieldValue,
        wrapperType: String
    ): TextFieldValue {
        val text = fieldValue.text
        val start: Int
        val end: Int
        if (!fieldValue.selection.collapsed) {
            start = fieldValue.selection.min.coerceIn(0, text.length)
            end = fieldValue.selection.max.coerceIn(start, text.length)
        } else {
            val cur = fieldValue.selection.end.coerceIn(0, text.length)
            start = text.lastIndexOf('\n', (cur - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
            end = text.indexOf('\n', cur).let { if (it < 0) text.length else it }
        }
        val targetBlock = text.substring(start, end)
        val baseIndent = targetBlock.takeWhile { it == ' ' || it == '\t' }
        val indentedBody = targetBlock.lines().joinToString("\n") { line ->
            if (line.isBlank()) line else "    $line"
        }

        val wrapped = when (wrapperType) {
            "try_catch" -> buildString {
                append(baseIndent).appendLine("try {")
                appendLine(indentedBody)
                append(baseIndent).appendLine("} catch (e: Exception) {")
                append(baseIndent).appendLine("    println(\"Error: \${e.message}\")")
                append(baseIndent).append("}")
            }
            "if_block" -> buildString {
                append(baseIndent).appendLine("if (isValid) {")
                appendLine(indentedBody)
                append(baseIndent).append("}")
            }
            "coroutine" -> buildString {
                append(baseIndent).appendLine("coroutineScope {")
                appendLine(indentedBody)
                append(baseIndent).append("}")
            }
            else -> buildString {
                append(baseIndent).appendLine("{")
                appendLine(indentedBody)
                append(baseIndent).append("}")
            }
        }

        val updated = text.replaceRange(start, end, wrapped)
        return TextFieldValue(
            text = updated,
            selection = TextRange((start + wrapped.length).coerceIn(0, updated.length))
        )
    }

    /**
     * IDE Immutability Refactoring: Converts mutable `var` declarations into immutable `val`.
     */
    fun convertMutableVarsToVal(code: String): Pair<String, Int> {
        val regex = Regex("""\bvar(\s+[A-Za-z_][A-Za-z0-9_]*)""")
        val count = regex.findAll(code).count()
        if (count == 0) return code to 0
        val updated = regex.replace(code, "val$1")
        return updated to count
    }
}
