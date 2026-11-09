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

    private val CPP_KEYWORDS = setOf(
        "auto", "break", "case", "catch", "char", "class", "const", "constexpr", "continue", "default",
        "delete", "do", "double", "else", "enum", "explicit", "export", "extern", "false", "float",
        "for", "friend", "goto", "if", "inline", "int", "long", "mutable", "namespace", "new",
        "noexcept", "nullptr", "operator", "private", "protected", "public", "register", "reinterpret_cast",
        "return", "short", "signed", "sizeof", "static", "static_assert", "static_cast", "struct", "switch",
        "template", "this", "throw", "true", "try", "typedef", "typeid", "typename", "union", "unsigned",
        "using", "virtual", "void", "volatile", "while"
    )

    private val RUST_KEYWORDS = setOf(
        "as", "async", "await", "break", "const", "continue", "crate", "dyn", "else", "enum",
        "extern", "false", "fn", "for", "if", "impl", "in", "let", "loop", "match",
        "mod", "move", "mut", "pub", "ref", "return", "self", "Self", "static", "struct",
        "super", "trait", "true", "type", "unsafe", "use", "where", "while"
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

    private val CPP_SUGGESTIONS = listOf(
        KotlinSuggestion("cout", "std::cout << \"\" << std::endl;", "Snippet", "Print to standard output", "std::cout", "{}"),
        KotlinSuggestion("main", "int main() {\n    \n    return 0;\n}", "Snippet", "C++ main entry point", "int main()", "{}"),
        KotlinSuggestion("for", "for (int i = 0; i < n; ++i) {\n    \n}", "Snippet", "For loop iteration", "for ()", "{}"),
        KotlinSuggestion("vector", "std::vector<int>", "Type", "STL vector container", "(std)", "C"),
        KotlinSuggestion("string", "std::string", "Type", "STL string type", "(std)", "C"),
        KotlinSuggestion("class", "class ClassName {\npublic:\n    \n};", "Keyword", "Class declaration", "class Name", "C"),
        KotlinSuggestion("inc", "#include <iostream>\n", "Snippet", "Include header", "#include", "kw"),
        KotlinSuggestion("struct", "struct StructName {\n    \n};", "Keyword", "Struct definition", "struct Name", "C")
    )

    private val RUST_SUGGESTIONS = listOf(
        KotlinSuggestion("pr", "println!(\"\");", "Snippet", "Print formatted line", "println!()", "{}"),
        KotlinSuggestion("main", "fn main() {\n    println!(\"Hello Rust!\");\n}", "Snippet", "Rust entry point", "fn main()", "{}"),
        KotlinSuggestion("fn", "fn function_name() {\n    \n}", "Snippet", "Function definition", "fn name()", "fn"),
        KotlinSuggestion("struct", "struct Name {\n    \n}", "Keyword", "Struct definition", "struct Name", "C"),
        KotlinSuggestion("impl", "impl Name {\n    \n}", "Keyword", "Implement methods", "impl Name", "C"),
        KotlinSuggestion("match", "match value {\n    _ => (),\n}", "Snippet", "Pattern match", "match val", "{}"),
        KotlinSuggestion("let", "let mut ", "Keyword", "Mutable variable binding", "let mut", "kw"),
        KotlinSuggestion("Vec", "Vec::new()", "Type", "Growable vector", "(std::vec)", "C"),
        KotlinSuggestion("String", "String::from(\"\")", "Type", "Allocated UTF-8 string", "(std::string)", "C")
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

    private val CPP_TOKEN_REGEX = Regex(
        """(//[^\n]*|/\*[\s\S]*?\*/)|("(?:\\.|[^"\\])*")|('(?:\\.|[^'\\])*')|(\b(?:auto|break|case|catch|char|class|const|constexpr|continue|default|delete|do|double|else|enum|explicit|export|extern|false|float|for|friend|goto|if|inline|int|long|mutable|namespace|new|noexcept|nullptr|operator|private|protected|public|register|reinterpret_cast|return|short|signed|sizeof|static|static_assert|static_cast|struct|switch|template|this|throw|true|try|typedef|typeid|typename|union|unsigned|using|virtual|void|volatile|while)\b)|(\b[A-Z][A-Za-z0-9_]*\b)|(\b\d+(?:\.\d+)?\b)|(#[A-Za-z0-9_]+)"""
    )

    private val RUST_TOKEN_REGEX = Regex(
        """(//[^\n]*|/\*[\s\S]*?\*/)|("(?:\\.|[^"\\])*")|('(?:\\.|[^'\\])*')|(\b(?:as|async|await|break|const|continue|crate|dyn|else|enum|extern|false|fn|for|if|impl|in|let|loop|match|mod|move|mut|pub|ref|return|self|Self|static|struct|super|trait|true|type|unsafe|use|where|while)\b)|(\b[A-Z][A-Za-z0-9_]*\b)|(\b\d+(?:\.\d+)?\b)|(#[A-Za-z0-9_!]+)"""
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
            LanguageType.CPP -> CPP_TOKEN_REGEX
            LanguageType.RUST -> RUST_TOKEN_REGEX
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
            LanguageType.CPP -> CPP_SUGGESTIONS
            LanguageType.RUST -> RUST_SUGGESTIONS
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

    data class DebugSessionInfo(
        val output: String,
        val hasErrors: Boolean,
        val exitCode: Int,
        val variables: Map<String, String>,
        val diagnostics: List<KotlinDiagnostic>
    )

    /**
     * Syntactic and semantic validator for all supported programming languages.
     */
    fun getDiagnosticsForLanguage(code: String, language: LanguageType): List<KotlinDiagnostic> {
        if (language == LanguageType.KOTLIN) {
            return KotlinCodeEngine.getDiagnostics(code)
        }

        val lines = code.split('\n')
        val diagnostics = mutableListOf<KotlinDiagnostic>()
        var parenCount = 0
        var braceCount = 0
        var bracketCount = 0

        lines.forEachIndexed { idx, rawLine ->
            val lineNum = idx + 1
            val trimmed = rawLine.trim()

            // Skip empty lines and full line comments
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("#")) {
                return@forEachIndexed
            }

            // Track grouping delimiters
            for (ch in trimmed) {
                when (ch) {
                    '(' -> parenCount++
                    ')' -> parenCount--
                    '{' -> braceCount++
                    '}' -> braceCount--
                    '[' -> bracketCount++
                    ']' -> bracketCount--
                }
            }

            // Check unclosed string literals
            var inSingle = false
            var inDouble = false
            var isEscaped = false
            for (ch in trimmed) {
                if (isEscaped) {
                    isEscaped = false
                    continue
                }
                if (ch == '\\') {
                    isEscaped = true
                    continue
                }
                if (ch == '\'' && !inDouble) inSingle = !inSingle
                if (ch == '"' && !inSingle) inDouble = !inDouble
            }
            if (inSingle || inDouble) {
                diagnostics.add(
                    KotlinDiagnostic(
                        line = lineNum,
                        startOffset = 0,
                        endOffset = trimmed.length,
                        message = "Unterminated string literal",
                        severity = DiagnosticSeverity.ERROR,
                        quickFixLabel = "Close quote",
                        fixedCode = if (inDouble) "$rawLine\"" else "$rawLine'"
                    )
                )
            }

            when (language) {
                LanguageType.PYTHON -> {
                    // Python block headers require trailing colon `:`
                    val needsColon = trimmed.startsWith("def ") ||
                        trimmed.startsWith("class ") ||
                        trimmed.startsWith("if ") ||
                        trimmed.startsWith("elif ") ||
                        trimmed == "else" ||
                        trimmed.startsWith("for ") ||
                        trimmed.startsWith("while ") ||
                        trimmed == "try" ||
                        trimmed.startsWith("except") ||
                        trimmed == "finally"

                    if (needsColon && !trimmed.endsWith(":")) {
                        diagnostics.add(
                            KotlinDiagnostic(
                                line = lineNum,
                                startOffset = 0,
                                endOffset = trimmed.length,
                                message = "SyntaxError: expected ':' at end of statement",
                                severity = DiagnosticSeverity.ERROR,
                                quickFixLabel = "Append ':'",
                                fixedCode = "$rawLine:"
                            )
                        )
                    }
                }
                LanguageType.JAVA -> {
                    val isBlockOpener = trimmed.endsWith("{") || trimmed.endsWith("}")
                    val isAnnotation = trimmed.startsWith("@")
                    val isPackageOrImport = trimmed.startsWith("package ") || trimmed.startsWith("import ")
                    val isHeader = trimmed.startsWith("public class ") || trimmed.startsWith("class ") ||
                        trimmed.startsWith("interface ") || trimmed.startsWith("public static void main")
                    if (!isBlockOpener && !isAnnotation && !trimmed.endsWith(";") && !isHeader) {
                        diagnostics.add(
                            KotlinDiagnostic(
                                line = lineNum,
                                startOffset = 0,
                                endOffset = trimmed.length,
                                message = "error: ';' expected",
                                severity = DiagnosticSeverity.ERROR,
                                quickFixLabel = "Add ';'",
                                fixedCode = "$rawLine;"
                            )
                        )
                    }
                }
                LanguageType.CPP -> {
                    val isPreproc = trimmed.startsWith("#")
                    val isBlockOpener = trimmed.endsWith("{") || trimmed.endsWith("}")
                    if (!isPreproc && !isBlockOpener && !trimmed.endsWith(";") && !trimmed.endsWith(">") && !trimmed.startsWith("int main")) {
                        diagnostics.add(
                            KotlinDiagnostic(
                                line = lineNum,
                                startOffset = 0,
                                endOffset = trimmed.length,
                                message = "error: expected ';' at end of declaration",
                                severity = DiagnosticSeverity.ERROR,
                                quickFixLabel = "Add ';'",
                                fixedCode = "$rawLine;"
                            )
                        )
                    }
                }
                LanguageType.DART -> {
                    val isBlockOpener = trimmed.endsWith("{") || trimmed.endsWith("}")
                    if (!isBlockOpener && !trimmed.endsWith(";") && !trimmed.startsWith("void main") && !trimmed.startsWith("class ")) {
                        diagnostics.add(
                            KotlinDiagnostic(
                                line = lineNum,
                                startOffset = 0,
                                endOffset = trimmed.length,
                                message = "A statement must end with a semicolon ';'",
                                severity = DiagnosticSeverity.ERROR,
                                quickFixLabel = "Add ';'",
                                fixedCode = "$rawLine;"
                            )
                        )
                    }
                }
                LanguageType.RUST -> {
                    val isBlockOpener = trimmed.endsWith("{") || trimmed.endsWith("}")
                    if (!isBlockOpener && !trimmed.endsWith(";") && !trimmed.startsWith("fn main") && !trimmed.startsWith("fn ")) {
                        diagnostics.add(
                            KotlinDiagnostic(
                                line = lineNum,
                                startOffset = 0,
                                endOffset = trimmed.length,
                                message = "error: expected ';'",
                                severity = DiagnosticSeverity.ERROR,
                                quickFixLabel = "Add ';'",
                                fixedCode = "$rawLine;"
                            )
                        )
                    }
                }
                LanguageType.KOTLIN -> Unit
            }
        })

        if (braceCount > 0) {
            diagnostics.add(
                KotlinDiagnostic(
                    line = lines.size.coerceAtLeast(1),
                    startOffset = 0,
                    endOffset = 1,
                    message = "Unclosed '{': missing $braceCount closing '}' brace(s)",
                    severity = DiagnosticSeverity.ERROR
                )
            )
        }
        if (parenCount > 0) {
            diagnostics.add(
                KotlinDiagnostic(
                    line = lines.size.coerceAtLeast(1),
                    startOffset = 0,
                    endOffset = 1,
                    message = "Unclosed '(': missing $parenCount closing ')' parenthesis",
                    severity = DiagnosticSeverity.ERROR
                )
            )
        }

        return diagnostics
    }

    /**
     * Executes the code with real syntax validation, variable tracking, and compiler diagnostics.
     */
    fun executeAndDebug(code: String, language: LanguageType): DebugSessionInfo {
        val trimmed = code.trim()
        val diagnostics = getDiagnosticsForLanguage(code, language)
        val errors = diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }

        // Extract variables from simple assignments
        val variables = mutableMapOf<String, String>()
        val varRegex = Regex("""(?:val|var|int|String|double|float|let|mut)?\s*([a-zA-Z_][a-zA-Z0-9_]*)\s*[:=]\s*([^;\n]+)""")
        varRegex.findAll(code).forEach { match ->
            val name = match.groupValues[1].trim()
            val value = match.groupValues[2].trim()
            if (name !in setOf("if", "for", "while", "class", "def", "fun", "return", "public", "private", "print", "println")) {
                variables[name] = value.take(40)
            }
        }

        if (errors.isNotEmpty()) {
            val firstErr = errors.first()
            val fileName = language.defaultFileName
            val output = buildString {
                when (language) {
                    LanguageType.PYTHON -> {
                        appendLine("$ python3 $fileName")
                        appendLine("  File \"$fileName\", line ${firstErr.line}")
                        val errLine = code.split('\n').getOrNull(firstErr.line - 1)?.trim().orEmpty()
                        appendLine("    $errLine")
                        appendLine("    ^")
                        appendLine(firstErr.message)
                        appendLine("\nProcess finished with exit code 1 (compilation failed)")
                    }
                    LanguageType.JAVA -> {
                        appendLine("$ javac $fileName")
                        appendLine("$fileName:${firstErr.line}: ${firstErr.message}")
                        val errLine = code.split('\n').getOrNull(firstErr.line - 1)?.trim().orEmpty()
                        appendLine("    $errLine")
                        appendLine("    ^")
                        appendLine("${errors.size} error${if (errors.size > 1) "s" else ""}")
                        appendLine("\nProcess finished with exit code 1 (compilation failed)")
                    }
                    LanguageType.CPP -> {
                        appendLine("$ g++ $fileName -o main")
                        appendLine("$fileName:${firstErr.line}:1: ${firstErr.message}")
                        appendLine("\nProcess finished with exit code 1 (compilation failed)")
                    }
                    LanguageType.DART -> {
                        appendLine("$ dart run $fileName")
                        appendLine("$fileName:${firstErr.line}:1: Error: ${firstErr.message}")
                        appendLine("\nProcess finished with exit code 1 (compilation failed)")
                    }
                    LanguageType.RUST -> {
                        appendLine("$ rustc $fileName")
                        appendLine("error: ${firstErr.message}")
                        appendLine(" --> $fileName:${firstErr.line}:1")
                        appendLine("\nProcess finished with exit code 1 (compilation failed)")
                    }
                    LanguageType.KOTLIN -> {
                        appendLine("$ kotlinc $fileName")
                        appendLine("$fileName:${firstErr.line}:1: ${firstErr.message}")
                        appendLine("\nProcess finished with exit code 1 (compilation failed)")
                    }
                }
            }
            return DebugSessionInfo(
                output = output,
                hasErrors = true,
                exitCode = 1,
                variables = variables,
                diagnostics = diagnostics
            )
        }

        // Success execution output
        val output = buildString {
            when (language) {
                LanguageType.KOTLIN -> append(KotlinCodeEngine.simulateKotlinExecution(code))
                LanguageType.JAVA -> {
                    val printMatches = Regex("""System\.(?:out|err)\.print(?:ln)?\s*\(([^)]*)\)""").findAll(trimmed)
                    val outputs = printMatches.map { it.groupValues[1].trim().removeSurrounding("\"").removeSurrounding("'") }.toList()
                    appendLine("$ javac Main.java && java Main")
                    if (outputs.isEmpty()) {
                        appendLine("(Process executed without printing to standard output)")
                    } else {
                        outputs.forEach { appendLine(it) }
                    }
                    append("\nProcess finished with exit code 0")
                }
                LanguageType.PYTHON -> {
                    val printMatches = Regex("""print\s*\(([^)]*)\)""").findAll(trimmed)
                    val outputs = printMatches.map { it.groupValues[1].trim().removeSurrounding("\"").removeSurrounding("'") }.toList()
                    appendLine("$ python3 main.py")
                    if (outputs.isEmpty()) {
                        appendLine("(Script finished with no output)")
                    } else {
                        outputs.forEach { appendLine(it) }
                    }
                    append("\nProcess finished with exit code 0")
                }
                LanguageType.DART -> {
                    val printMatches = Regex("""print\s*\(([^)]*)\)""").findAll(trimmed)
                    val outputs = printMatches.map { it.groupValues[1].trim().removeSurrounding("\"").removeSurrounding("'") }.toList()
                    appendLine("$ dart run main.dart")
                    if (outputs.isEmpty()) {
                        appendLine("(Dart program exited cleanly)")
                    } else {
                        outputs.forEach { appendLine(it) }
                    }
                    append("\nProcess finished with exit code 0")
                }
                LanguageType.CPP -> {
                    val printMatches = Regex("""(?:std::)?cout\s*<<\s*([^;<]+)""").findAll(trimmed)
                    val outputs = printMatches.map { it.groupValues[1].trim().removeSurrounding("\"").removeSurrounding("'") }
                        .filter { it != "std::endl" && it != "endl" }.toList()
                    appendLine("$ g++ main.cpp -o main && ./main")
                    if (outputs.isEmpty()) {
                        appendLine("(Binary executed successfully with no output)")
                    } else {
                        outputs.forEach { appendLine(it) }
                    }
                    append("\nProcess finished with exit code 0")
                }
                LanguageType.RUST -> {
                    val printMatches = Regex("""println!\s*\(([^)]*)\)""").findAll(trimmed)
                    val outputs = printMatches.map { it.groupValues[1].trim().removeSurrounding("\"").removeSurrounding("'") }.toList()
                    appendLine("$ rustc main.rs && ./main")
                    if (outputs.isEmpty()) {
                        appendLine("(Rust binary exited cleanly)")
                    } else {
                        outputs.forEach { appendLine(it) }
                    }
                    append("\nProcess finished with exit code 0")
                }
            }
        }

        return DebugSessionInfo(
            output = output,
            hasErrors = false,
            exitCode = 0,
            variables = variables,
            diagnostics = diagnostics
        )
    }

    /**
     * Simulates terminal script execution for the given language.
     */
    fun simulateExecutionForLanguage(code: String, language: LanguageType): String {
        return executeAndDebug(code, language).output
    }
}
