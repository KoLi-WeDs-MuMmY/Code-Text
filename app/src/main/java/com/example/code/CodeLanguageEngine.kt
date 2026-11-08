package com.example.code

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import com.example.model.LanguageType
import com.example.ui.theme.KotlinSyntaxColors

object CodeLanguageEngine {

    private val JAVA_KEYWORDS = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
        "record", "sealed", "permits", "var"
    )

    private val PYTHON_KEYWORDS = setOf(
        "False", "None", "True", "and", "as", "assert", "async", "await", "break", "class", "continue",
        "def", "del", "elif", "else", "except", "finally", "for", "from", "global", "if", "import",
        "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try", "while", "with", "yield"
    )

    private val DART_KEYWORDS = setOf(
        "abstract", "as", "assert", "async", "await", "break", "case", "catch", "class", "const",
        "continue", "covariant", "default", "deferred", "do", "dynamic", "else", "enum", "export", "extends",
        "extension", "external", "factory", "false", "final", "finally", "for", "Function", "get", "hide",
        "if", "implements", "import", "in", "interface", "is", "late", "library", "mixin", "new",
        "null", "on", "operator", "part", "required", "rethrow", "return", "set", "show", "static",
        "super", "switch", "sync", "this", "throw", "true", "try", "typedef", "var", "void", "while", "with", "yield"
    )

    private val JAVA_SUGGESTIONS = listOf(
        KotlinSuggestion("psvm", "public static void main(String[] args) {\n    \n}", "Snippet", "Main method declaration", "psvm", "{}"),
        KotlinSuggestion("sout", "System.out.println(\"\");", "Snippet", "Print to standard output", "System.out.println()", "{}"),
        KotlinSuggestion("serr", "System.err.println(\"\");", "Snippet", "Print to error output", "System.err.println()", "{}"),
        KotlinSuggestion("public", "public ", "Keyword", "Access modifier public", "", "kw"),
        KotlinSuggestion("class", "class Name {\n    \n}", "Keyword", "Class declaration", "class Name { }", "C"),
        KotlinSuggestion("interface", "interface Name {\n    \n}", "Keyword", "Interface declaration", "interface Name { }", "I"),
        KotlinSuggestion("String", "String", "Type", "Java String class", "(java.lang)", "C"),
        KotlinSuggestion("List", "List<String>", "Type", "Java List interface", "(java.util)", "I"),
        KotlinSuggestion("ArrayList", "new ArrayList<>()", "Type", "Resizble array implementation", "(java.util)", "C"),
        KotlinSuggestion("Map", "Map<String, Object>", "Type", "Key-value map interface", "(java.util)", "I"),
        KotlinSuggestion("System", "System.out.println()", "Class", "System utilities", "(java.lang)", "C")
    )

    private val PYTHON_SUGGESTIONS = listOf(
        KotlinSuggestion("pr", "print(\"\")", "Snippet", "Print to stdout", "print()", "{}"),
        KotlinSuggestion("def", "def function_name():\n    pass", "Snippet", "Define a function", "def name():", "fn"),
        KotlinSuggestion("class", "class ClassName:\n    def __init__(self):\n        pass", "Snippet", "Define a class", "class Name:", "C"),
        KotlinSuggestion("main", "if __name__ == '__main__':\n    main()", "Snippet", "Main guard block", "if __name__ == '__main__':", "{}"),
        KotlinSuggestion("import", "import ", "Keyword", "Import module", "", "kw"),
        KotlinSuggestion("from", "from module import ", "Keyword", "From module import", "", "kw"),
        KotlinSuggestion("return", "return ", "Keyword", "Return statement", "", "kw"),
        KotlinSuggestion("len", "len(items)", "Function", "Length of collection", "len()", "fn"),
        KotlinSuggestion("range", "range(0, 10)", "Function", "Numeric range sequence", "range()", "fn"),
        KotlinSuggestion("enumerate", "enumerate(items)", "Function", "Indexed iteration", "enumerate()", "fn"),
        KotlinSuggestion("isinstance", "isinstance(obj, type)", "Function", "Type check", "isinstance()", "fn")
    )

    private val DART_SUGGESTIONS = listOf(
        KotlinSuggestion("pr", "print('');", "Snippet", "Print to stdout", "print()", "{}"),
        KotlinSuggestion("main", "void main() {\n  print('Hello Dart!');\n}", "Snippet", "Dart entry point", "void main() { }", "{}"),
        KotlinSuggestion("stless", "class MyWidget extends StatelessWidget {\n  const MyWidget({super.key});\n\n  @override\n  Widget build(BuildContext context) {\n    return Container();\n  }\n}", "Snippet", "StatelessWidget skeleton", "StatelessWidget", "C"),
        KotlinSuggestion("stful", "class MyWidget extends StatefulWidget {\n  const MyWidget({super.key});\n\n  @override\n  State<MyWidget> createState() => _MyWidgetState();\n}", "Snippet", "StatefulWidget skeleton", "StatefulWidget", "C"),
        KotlinSuggestion("class", "class ClassName {\n  \n}", "Keyword", "Class declaration", "class Name { }", "C"),
        KotlinSuggestion("Future", "Future<void>", "Type", "Asynchronous computation", "(dart:async)", "C"),
        KotlinSuggestion("Stream", "Stream<int>", "Type", "Asynchronous sequence", "(dart:async)", "C"),
        KotlinSuggestion("Widget", "Widget", "Type", "Flutter Widget base", "(flutter)", "C"),
        KotlinSuggestion("BuildContext", "BuildContext context", "Type", "Flutter BuildContext", "(flutter)", "C")
    )

    private val JAVA_TOKEN_REGEX = Regex(
        """(//[^\n]*|/\*[\s\S]*?\*/)|("(?:\\.|[^"\\])*")|('(?:\\.|[^'\\])*')|(\b(?:abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while|record|sealed|permits|var)\b)|(\b[A-Z][A-Za-z0-9_]*\b)|(\b\d+(?:\.\d+)?(?:[fFLdD])?\b)|(@[A-Za-z0-9_]+)"""
    )

    private val PYTHON_TOKEN_REGEX = Regex(
        """(#[^\n]*)|(""" + "\"\"\"" + """[\s\S]*?""" + "\"\"\"" + """|'''[\s\S]*?'''|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*')|(\b(?:False|None|True|and|as|assert|async|await|break|class|continue|def|del|elif|else|except|finally|for|from|global|if|import|in|is|lambda|nonlocal|not|or|pass|raise|return|try|while|with|yield)\b)|(\b[A-Z][A-Za-z0-9_]*\b)|(\b\d+(?:\.\d+)?\b)|(@[A-Za-z0-9_]+)"""
    )

    private val DART_TOKEN_REGEX = Regex(
        """(//[^\n]*|/\*[\s\S]*?\*/)|("(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*')|(\b(?:abstract|as|assert|async|await|break|case|catch|class|const|continue|covariant|default|deferred|do|dynamic|else|enum|export|extends|extension|external|factory|false|final|finally|for|Function|get|hide|if|implements|import|in|interface|is|late|library|mixin|new|null|on|operator|part|required|rethrow|return|set|show|static|super|switch|sync|this|throw|true|try|typedef|var|void|while|with|yield)\b)|(\b[A-Z][A-Za-z0-9_]*\b)|(\b\d+(?:\.\d+)?\b)|(@[A-Za-z0-9_]+)"""
    )

    /**
     * Highlights code syntax according to the active language type.
     */
    fun highlightCodeForLanguage(
        code: String,
        language: LanguageType,
        colors: KotlinSyntaxColors,
        cursorOffset: Int = -1
    ): AnnotatedString {
        if (language == LanguageType.KOTLIN) {
            return KotlinCodeEngine.highlightKotlinCode(code, colors, cursorOffset)
        }

        val tokenRegex = when (language) {
            LanguageType.JAVA -> JAVA_TOKEN_REGEX
            LanguageType.PYTHON -> PYTHON_TOKEN_REGEX
            LanguageType.DART -> DART_TOKEN_REGEX
            LanguageType.KOTLIN -> JAVA_TOKEN_REGEX
        }

        return buildAnnotatedString {
            append(code)
            addStyle(SpanStyle(color = colors.plainText), 0, code.length)

            for (match in tokenRegex.findAll(code)) {
                val start = match.range.first
                val end = match.range.last + 1

                when {
                    match.groups[1] != null -> {
                        addStyle(SpanStyle(color = colors.comment, fontStyle = FontStyle.Italic), start, end)
                    }
                    match.groups[2] != null -> {
                        addStyle(SpanStyle(color = colors.stringLiteral), start, end)
                    }
                    match.groups[3] != null -> {
                        addStyle(SpanStyle(color = colors.keyword), start, end)
                    }
                    match.groups[4] != null -> {
                        addStyle(SpanStyle(color = colors.typeName), start, end)
                    }
                    match.groups[5] != null -> {
                        addStyle(SpanStyle(color = colors.numberLiteral), start, end)
                    }
                    match.groups.size > 6 && match.groups[6] != null -> {
                        addStyle(SpanStyle(color = colors.annotation), start, end)
                    }
                }
            }
        }
    }

    /**
     * Provides autocomplete suggestions tailored to the specific language.
     */
    fun getSuggestionsForLanguage(
        code: String,
        cursorPosition: Int,
        language: LanguageType,
        maxResults: Int = 8
    ): Pair<String, List<KotlinSuggestion>> {
        if (language == LanguageType.KOTLIN) {
            return KotlinCodeEngine.getAutocompleteSuggestions(code, cursorPosition, maxResults)
        }

        val safeCursor = cursorPosition.coerceIn(0, code.length)
        var prefixStart = safeCursor
        while (prefixStart > 0 && (code[prefixStart - 1].isLetterOrDigit() || code[prefixStart - 1] == '_')) {
            prefixStart--
        }
        val prefix = code.substring(prefixStart, safeCursor)

        val pool = when (language) {
            LanguageType.JAVA -> JAVA_SUGGESTIONS
            LanguageType.PYTHON -> PYTHON_SUGGESTIONS
            LanguageType.DART -> DART_SUGGESTIONS
            LanguageType.KOTLIN -> emptyList()
        }

        if (prefix.isBlank()) {
            return "" to pool.take(maxResults)
        }

        val matches = pool.filter {
            it.label.startsWith(prefix, ignoreCase = true) || it.insertText.startsWith(prefix, ignoreCase = true)
        }.take(maxResults)

        return prefix to matches
    }

    /**
     * Applies a suggestion for the given language.
     */
    fun applySuggestionForLanguage(
        code: String,
        cursorPosition: Int,
        suggestion: KotlinSuggestion
    ): Pair<String, Int> {
        return KotlinCodeEngine.applySuggestion(code, cursorPosition, suggestion)
    }

    /**
     * Simulates terminal script execution for the given language.
     */
    fun simulateExecutionForLanguage(code: String, language: LanguageType): String {
        val trimmed = code.trim()
        if (trimmed.isBlank()) {
            return "$ ${language.runCommandPrefix} (no code to execute)"
        }

        return when (language) {
            LanguageType.KOTLIN -> KotlinCodeEngine.simulateKotlinExecution(code)
            LanguageType.JAVA -> {
                val printMatches = Regex("""System\.(?:out|err)\.print(?:ln)?\s*\(([^)]*)\)""").findAll(trimmed)
                val outputs = printMatches.map { it.groupValues[1].trim().removeSurrounding("\"").removeSurrounding("'") }.toList()
                buildString {
                    appendLine("$ javac Main.java && java Main")
                    if (outputs.isEmpty()) {
                        appendLine("(Process executed without printing to standard output)")
                    } else {
                        outputs.forEach { appendLine(it) }
                    }
                    append("\nProcess finished with exit code 0")
                }
            }
            LanguageType.PYTHON -> {
                val printMatches = Regex("""print\s*\(([^)]*)\)""").findAll(trimmed)
                val outputs = printMatches.map { it.groupValues[1].trim().removeSurrounding("\"").removeSurrounding("'") }.toList()
                buildString {
                    appendLine("$ python3 main.py")
                    if (outputs.isEmpty()) {
                        appendLine("(Script finished with no output)")
                    } else {
                        outputs.forEach { appendLine(it) }
                    }
                    append("\nProcess finished with exit code 0")
                }
            }
            LanguageType.DART -> {
                val printMatches = Regex("""print\s*\(([^)]*)\)""").findAll(trimmed)
                val outputs = printMatches.map { it.groupValues[1].trim().removeSurrounding("\"").removeSurrounding("'") }.toList()
                buildString {
                    appendLine("$ dart run main.dart")
                    if (outputs.isEmpty()) {
                        appendLine("(Dart program exited cleanly)")
                    } else {
                        outputs.forEach { appendLine(it) }
                    }
                    append("\nProcess finished with exit code 0")
                }
            }
        }
    }
}
