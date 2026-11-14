package com.example.code

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
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

            BracketPairColorizer.applyToBuilder(
                builder = this,
                code = code,
                cursorOffset = cursorOffset,
                isPython = (language == LanguageType.PYTHON)
            )
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
        val diagnostics: List<KotlinDiagnostic>,
        val callStack: List<String> = emptyList(),
        val logcatEntries: List<String> = emptyList(),
        val toolchainInstalled: Boolean = true,
        val requiredSdkName: String = "",
        val compilationArtifact: com.example.compiler.CompilationArtifact? = null,
        val vmState: com.example.compiler.VmExecutionState? = null,
        val vmTrace: List<com.example.compiler.VmExecutionState> = emptyList()
    )

    /**
     * Detects when the caret is inside a function call's parentheses (e.g., `println(|)`)
     * and returns a CodeAssist-style parameter signature hint (`< 1/20 > println(message: Any?)`).
     */
    fun getParameterHintForCursor(
        code: String,
        cursorPosition: Int,
        language: LanguageType
    ): String? {
        if (code.isEmpty()) return null
        val safeCursor = cursorPosition.coerceIn(0, code.length)
        val lineStart = code.lastIndexOf('\n', (safeCursor - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val prefixOnLine = code.substring(lineStart, safeCursor)
        val openParenIdx = prefixOnLine.lastIndexOf('(')
        if (openParenIdx <= 0) return null
        // Check if the parenthesis was already closed before cursor on the same line
        val afterOpen = prefixOnLine.substring(openParenIdx + 1)
        if (afterOpen.count { it == ')' } > afterOpen.count { it == '(' }) return null

        val fnIdentMatch = Regex("""([A-Za-z_][A-Za-z0-9_!.]*)\s*$""").find(prefixOnLine.substring(0, openParenIdx))
            ?: return null
        val fnName = fnIdentMatch.groupValues[1]

        return when {
            fnName.endsWith("println") && language == LanguageType.KOTLIN ->
                "< 1/20 >\nprintln(message: Any?)"
            fnName.endsWith("print") && language == LanguageType.KOTLIN ->
                "< 1/14 >\nprint(message: Any?)"
            fnName.endsWith("println") && language == LanguageType.JAVA ->
                "< 1/10 >\nSystem.out.println(String x)"
            fnName == "print" && language == LanguageType.PYTHON ->
                "< 1/6 >\nprint(*objects, sep=' ', end='\\n')"
            fnName == "print" && language == LanguageType.DART ->
                "< 1/2 >\nvoid print(Object? object)"
            fnName.startsWith("println") && language == LanguageType.RUST ->
                "< 1/4 >\nprintln!(fmt: &str, ...args)"
            fnName == "filter" ->
                "< 1/4 >\nfilter(predicate: (T) -> Boolean): List<T>"
            fnName == "map" ->
                "< 1/5 >\nmap(transform: (T) -> R): List<R>"
            fnName == "listOf" ->
                "< 1/3 >\nlistOf(vararg elements: T): List<T>"
            fnName == "mutableStateOf" ->
                "< 1/2 >\nmutableStateOf(value: T, policy: SnapshotMutationPolicy<T>)"
            fnName == "range" && language == LanguageType.PYTHON ->
                "< 1/3 >\nrange(start: int, stop: int, step: int = 1)"
            fnName == "len" && language == LanguageType.PYTHON ->
                "< 1/1 >\nlen(s: Sized) -> int"
            else -> null
        }
    }

    /**
     * Syntactic and semantic validator for all supported programming languages.
     */
    fun getDiagnosticsForLanguage(code: String, language: LanguageType): List<KotlinDiagnostic> {
        val baseDiagnostics = if (language == LanguageType.KOTLIN) {
            KotlinCodeEngine.diagnoseKotlinSyntax(code).toMutableList()
        } else {
            diagnoseNonKotlinSyntax(code, language).toMutableList()
        }

        // Merge real Kotlin Compiler Plugin semantic diagnostics (val reassignment, arity, type mismatch, / by zero)
        if (language == LanguageType.KOTLIN && code.isNotBlank()) {
            val compilerDiags = com.example.compiler.KotlinCompilerPlugin
                .compile(code, LanguageType.KOTLIN)
                .diagnostics
            compilerDiags.forEach { cDiag ->
                val alreadyExists = baseDiagnostics.any { existing ->
                    existing.line == cDiag.line &&
                        (existing.severity == DiagnosticSeverity.ERROR || existing.message == cDiag.message)
                }
                if (!alreadyExists && !cDiag.message.startsWith("Syntax error near")) {
                    baseDiagnostics.add(cDiag)
                }
            }
        }

        // Append CodeAssist Auto-Import quick fixes for unimported stdlib/framework symbols
        val missingImports = getMissingImportsForLanguage(code, language)
        missingImports.forEach { fix ->
            val fixedWithImport = applyAutoImport(code, fix.importStatement, language)
            baseDiagnostics.add(
                KotlinDiagnostic(
                    line = fix.line,
                    startOffset = 0,
                    endOffset = fix.symbol.length,
                    message = "Unresolved import: '${fix.symbol}' (${fix.importStatement})",
                    severity = DiagnosticSeverity.WARNING,
                    quickFixLabel = "+ Import ${fix.symbol}",
                    fixedCode = fixedWithImport
                )
            )
        }
        return baseDiagnostics
    }

    private fun diagnoseNonKotlinSyntax(code: String, language: LanguageType): List<KotlinDiagnostic> {
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
                    if (!isBlockOpener && !isAnnotation && !isPackageOrImport && !trimmed.endsWith(";") && !isHeader) {
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
        }

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
     * Executes the code with real syntax validation, variable tracking, call stack frames,
     * Logcat telemetry, and SDK Toolchain verification.
     */
    fun executeAndDebug(
        code: String,
        language: LanguageType,
        isSdkInstalled: Boolean = true,
        stdinInput: String = ""
    ): DebugSessionInfo {
        val trimmed = code.trim()
        val diagnostics = getDiagnosticsForLanguage(code, language)
        val errors = diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }

        val sdkTitle = when (language) {
            LanguageType.KOTLIN -> "Kotlin Compiler & K2 Debugger (kotlinc 2.1)"
            LanguageType.JAVA -> "OpenJDK 21 LTS & JDB Debugger"
            LanguageType.PYTHON -> "Python 3.12 Runtime & PDB"
            LanguageType.CPP -> "Clang/LLVM 18 & LLDB Debugger"
            LanguageType.DART -> "Dart SDK & VM Observatory"
            LanguageType.RUST -> "Rust Cargo & Rust-GDB Toolchain"
        }

        val stdinLines = stdinInput.lines().filter { it.isNotEmpty() }.toMutableList()
        var stdinCursor = 0
        fun nextStdinValue(defaultVal: String = "KtScribeUser"): String {
            return if (stdinCursor < stdinLines.size) {
                stdinLines[stdinCursor++]
            } else {
                defaultVal
            }
        }

        // Extract variables from assignments across languages
        val variables = mutableMapOf<String, String>()
        val varRegex = Regex("""(?:val|var|int|String|double|float|boolean|let|mut|const|final)?\s*([a-zA-Z_][a-zA-Z0-9_]*)\s*[:=]\s*([^;\n]+)""")
        varRegex.findAll(code).forEach { match ->
            val name = match.groupValues[1].trim()
            val rawRhs = match.groupValues[2].trim()
            if (name !in setOf("if", "for", "while", "class", "def", "fun", "return", "public", "private", "print", "println", "package", "import")) {
                val resolvedVal = when {
                    rawRhs.contains("readln") || rawRhs.contains("readLine") ||
                        rawRhs.contains("nextLine") || rawRhs.contains("next(") ||
                        rawRhs.startsWith("input(") || rawRhs.contains("readLineSync") -> {
                        "\"${nextStdinValue()}\""
                    }
                    else -> rawRhs.take(40)
                }
                variables[name] = resolvedVal
            }
        }

        // Also check C++ `cin >> varName`
        Regex("""(?:std::)?cin\s*>>\s*([A-Za-z_][A-Za-z0-9_]*)""").findAll(code).forEach { match ->
            val vName = match.groupValues[1]
            variables[vName] = nextStdinValue("42")
        }

        // Build call stack frames
        val callStack = mutableListOf<String>()
        code.lines().forEachIndexed { idx, line ->
            val fnMatch = Regex("""\b(?:fun|def|void|int|fn)\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(""").find(line)
            if (fnMatch != null) {
                callStack.add("${fnMatch.groupValues[1]}() at ${language.defaultFileName}:${idx + 1}")
            }
        }
        if (callStack.isEmpty()) {
            callStack.add("main() at ${language.defaultFileName}:1")
        }

        if (!isSdkInstalled) {
            val missingOutput = buildString {
                appendLine("$ ${language.runCommandPrefix} ${language.defaultFileName}")
                appendLine("Error: Toolchain binary '${language.runCommandPrefix}' is not installed in local SDK environment.")
                appendLine("Required Package: $sdkTitle")
                appendLine("→ Tap 'Install SDK Plugin' above or open Settings > SDK & Plugin Manager to download.")
                appendLine("\nProcess finished with exit code 127 (command not found)")
            }
            val logcat = listOf(
                "E/ToolchainManager: Missing runtime binary '${language.runCommandPrefix}' for ${language.displayName}",
                "W/DebuggerBridge: Cannot attach debugger session without $sdkTitle",
                "I/PackageManager: Ready to download ${com.example.model.ToolchainPluginItem.sdkIdForLanguage(language)} from KtScribe SDK Registry"
            )
            return DebugSessionInfo(
                output = missingOutput,
                hasErrors = true,
                exitCode = 127,
                variables = variables,
                diagnostics = diagnostics,
                callStack = callStack,
                logcatEntries = logcat,
                toolchainInstalled = false,
                requiredSdkName = sdkTitle
            )
        }

        val compilationArtifact = com.example.compiler.KotlinCompilerPlugin.compile(code, language)

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
                        appendLine("$ kotlinc $fileName  [Built-in Plugin]")
                        appendLine("$fileName:${firstErr.line}:1: ${firstErr.message}")
                        appendLine("\nProcess finished with exit code 1 (compilation failed)")
                    }
                }
            }
            val logcat = buildList {
                add("I/BuildTask: Starting ${language.displayName} toolchain ($sdkTitle)")
                errors.forEach { err ->
                    add("E/Compiler: ${language.defaultFileName}:${err.line} -> ${err.message}")
                }
                add("W/DebugRunner: Build halted with ${errors.size} error(s) (exit code 1)")
            }
            return DebugSessionInfo(
                output = output,
                hasErrors = true,
                exitCode = 1,
                variables = variables,
                diagnostics = diagnostics,
                callStack = callStack,
                logcatEntries = logcat,
                toolchainInstalled = true,
                requiredSdkName = sdkTitle,
                compilationArtifact = compilationArtifact
            )
        }

        // For Kotlin, execute via our real 100% on-device Compiler Plugin + KtVM Stack Bytecode VM
        if (language == LanguageType.KOTLIN && compilationArtifact.success) {
            val vmSession = com.example.compiler.KtBytecodeVm.DebugSession(compilationArtifact, stdinLines)
            val vmState = vmSession.runToEnd()
            val vmTrace = vmSession.getTrace()

            val mergedVariables = LinkedHashMap<String, String>(variables)
            vmState.variables.forEach { (k, v) -> mergedVariables[k] = v }

            val mergedCallStack = if (vmState.callStack.isNotEmpty()) vmState.callStack else callStack
            val hasRuntimeErr = vmState.exitCode != 0 || vmState.stderr.isNotEmpty()
            val output = buildString {
                appendLine("[Kotlin Built-in Compiler Plugin + KtVM Bytecode VM • Zero Server]")
                appendLine("Compiled ${compilationArtifact.astSummaryLines.size} AST node(s) -> ${compilationArtifact.bytecode.size} bytecode op(s) in ${compilationArtifact.compileTimeMs}ms")
                if (stdinLines.isNotEmpty()) {
                    appendLine("[stdin] ${stdinLines.joinToString(" | ")}")
                }
                appendLine("$ kotlinc ${language.defaultFileName} && ktvm MainKt")
                if (hasRuntimeErr) {
                    vmState.stderr.forEach { appendLine("Runtime Error: $it") }
                    vmState.stdout.forEach { appendLine(it) }
                    append("\nProcess finished with exit code ${vmState.exitCode.coerceAtLeast(1)} (${vmState.stepsExecuted} VM steps)")
                } else {
                    if (vmState.stdout.isEmpty()) {
                        appendLine("(Executed ${vmState.stepsExecuted} VM step(s) cleanly with no stdout)")
                    } else {
                        vmState.stdout.forEach { appendLine(it) }
                    }
                    append("\nProcess finished with exit code 0 (${vmState.stepsExecuted} VM steps)")
                }
            }

            val logcat = buildList {
                add("I/KtCompilerPlugin: Compiled ${compilationArtifact.bytecode.size} bytecode ops, ${compilationArtifact.symbols.size} symbols (${compilationArtifact.compileTimeMs}ms)")
                add("I/KtVM: Executed ${vmState.stepsExecuted} step(s), ${vmTrace.size} debug trace snapshot(s)")
                if (stdinLines.isNotEmpty()) {
                    add("D/StdinPipe: Consumed ${stdinLines.size} line(s) from standard input")
                }
                add("D/Debugger: Captured ${mergedVariables.size} live runtime variable(s)")
                diagnostics.filter { it.severity == DiagnosticSeverity.WARNING }.forEach { warn ->
                    add("W/Compiler: Ln ${warn.line}: ${warn.message}")
                }
                if (hasRuntimeErr) {
                    vmState.stderr.forEach { add("E/KtVM: $it") }
                } else {
                    add("I/Process: Process finished with exit code 0")
                }
            }

            return DebugSessionInfo(
                output = output,
                hasErrors = hasRuntimeErr,
                exitCode = if (hasRuntimeErr) vmState.exitCode.coerceAtLeast(1) else 0,
                variables = mergedVariables,
                diagnostics = diagnostics,
                callStack = mergedCallStack,
                logcatEntries = logcat,
                toolchainInstalled = true,
                requiredSdkName = sdkTitle,
                compilationArtifact = compilationArtifact,
                vmState = vmState,
                vmTrace = vmTrace
            )
        }

        val interpreterResult = OnDeviceInterpreter.execute(
            code = code,
            language = language,
            stdinLines = stdinLines
        )
        val mergedVariables = LinkedHashMap<String, String>(variables)
        interpreterResult.variables.forEach { (k, v) -> mergedVariables[k] = v }

        val mergedCallStack = if (interpreterResult.callStack.isNotEmpty()) {
            interpreterResult.callStack
        } else {
            callStack
        }

        val cmdHeader = when (language) {
            LanguageType.KOTLIN -> "$ kotlinc ${language.defaultFileName} && kotlin MainKt"
            LanguageType.JAVA -> "$ javac ${language.defaultFileName} && java Main"
            LanguageType.PYTHON -> "$ python3 ${language.defaultFileName}"
            LanguageType.DART -> "$ dart run ${language.defaultFileName}"
            LanguageType.CPP -> "$ g++ ${language.defaultFileName} -o main && ./main"
            LanguageType.RUST -> "$ rustc ${language.defaultFileName} && ./main"
        }

        val output = buildString {
            appendLine("[On-Device AST & Bytecode Engine • ${compilationArtifact.bytecode.size} IR ops • Zero Server]")
            if (stdinLines.isNotEmpty()) {
                appendLine("[stdin] ${stdinLines.joinToString(" | ")}")
            }
            appendLine(cmdHeader)
            if (interpreterResult.runtimeError != null) {
                appendLine("Runtime Error: ${interpreterResult.runtimeError}")
                append("\nProcess finished with exit code 1 (${interpreterResult.stepsExecuted} steps)")
            } else {
                if (interpreterResult.stdoutLines.isEmpty()) {
                    appendLine("(Executed ${interpreterResult.stepsExecuted} step(s) cleanly with no stdout)")
                } else {
                    interpreterResult.stdoutLines.forEach { appendLine(it) }
                }
                append("\nProcess finished with exit code 0 (${interpreterResult.stepsExecuted} steps)")
            }
        }

        val logcat = buildList {
            add("I/OnDeviceRuntime: Active $sdkTitle (${interpreterResult.stepsExecuted} AST steps, ${compilationArtifact.bytecode.size} IR ops)")
            if (stdinLines.isNotEmpty()) {
                add("D/StdinPipe: Consumed ${stdinLines.size} line(s) from standard input")
            }
            add("D/Debugger: Captured ${mergedVariables.size} live runtime variable(s)")
            diagnostics.filter { it.severity == DiagnosticSeverity.WARNING }.forEach { warn ->
                add("W/Linter: Ln ${warn.line}: ${warn.message}")
            }
            if (interpreterResult.runtimeError != null) {
                add("E/Runtime: ${interpreterResult.runtimeError}")
            } else {
                add("I/Process: Process finished with exit code 0")
            }
        }

        return DebugSessionInfo(
            output = output,
            hasErrors = interpreterResult.runtimeError != null,
            exitCode = if (interpreterResult.runtimeError != null) 1 else 0,
            variables = mergedVariables,
            diagnostics = diagnostics,
            callStack = mergedCallStack,
            logcatEntries = logcat,
            toolchainInstalled = true,
            requiredSdkName = sdkTitle,
            compilationArtifact = compilationArtifact
        )
    }

    /**
     * Generates a genuine copy-and-run command for users who want bare-metal Linux binaries
     * in Termux (`com.termux`) locally on Android without paying for a remote compiler server.
     */
    fun buildTermuxNativeCommand(
        fileName: String,
        code: String,
        language: LanguageType
    ): String {
        val cleanName = fileName.ifBlank { language.defaultFileName }
        val runStep = when (language) {
            LanguageType.KOTLIN -> "kotlinc \"$cleanName\" -include-runtime -d out.jar && java -jar out.jar"
            LanguageType.JAVA -> "javac \"$cleanName\" && java ${cleanName.removeSuffix(".java")}"
            LanguageType.PYTHON -> "python3 \"$cleanName\""
            LanguageType.CPP -> "g++ \"$cleanName\" -std=c++20 -o main && ./main"
            LanguageType.DART -> "dart run \"$cleanName\""
            LanguageType.RUST -> "rustc \"$cleanName\" -o main && ./main"
        }
        return "cat << 'KTSCRIBE_EOF' > \"$cleanName\"\n$code\nKTSCRIBE_EOF\n$runStep"
    }

    /**
     * Simulates or runs terminal script execution for the given language on-device.
     */
    fun simulateExecutionForLanguage(code: String, language: LanguageType, stdinInput: String = ""): String {
        return executeAndDebug(code, language, isSdkInstalled = true, stdinInput = stdinInput).output
    }

    // =========================================================================
    // CODEASSIST AUTO-IMPORT QUICK FIXES & OPTIMIZE IMPORTS ENGINE
    // =========================================================================

    data class AutoImportQuickFix(
        val symbol: String,
        val importStatement: String,
        val line: Int
    )

    private val KOTLIN_AUTO_IMPORTS = mapOf(
        "File" to "import java.io.File",
        "UUID" to "import java.util.UUID",
        "Scanner" to "import java.util.Scanner",
        "LocalDate" to "import java.time.LocalDate",
        "LocalDateTime" to "import java.time.LocalDateTime",
        "CoroutineScope" to "import kotlinx.coroutines.CoroutineScope",
        "Dispatchers" to "import kotlinx.coroutines.Dispatchers",
        "Flow" to "import kotlinx.coroutines.flow.Flow",
        "MutableStateFlow" to "import kotlinx.coroutines.flow.MutableStateFlow",
        "delay" to "import kotlinx.coroutines.delay",
        "launch" to "import kotlinx.coroutines.launch",
        "sqrt" to "import kotlin.math.sqrt",
        "sin" to "import kotlin.math.sin",
        "cos" to "import kotlin.math.cos",
        "abs" to "import kotlin.math.abs"
    )

    private val JAVA_AUTO_IMPORTS = mapOf(
        "Scanner" to "import java.util.Scanner;",
        "List" to "import java.util.List;",
        "ArrayList" to "import java.util.ArrayList;",
        "Map" to "import java.util.Map;",
        "HashMap" to "import java.util.HashMap;",
        "Set" to "import java.util.Set;",
        "HashSet" to "import java.util.HashSet;",
        "File" to "import java.io.File;",
        "UUID" to "import java.util.UUID;",
        "LocalDate" to "import java.time.LocalDate;",
        "Optional" to "import java.util.Optional;"
    )

    private val PYTHON_AUTO_IMPORTS = mapOf(
        "math" to "import math",
        "json" to "import json",
        "os" to "import os",
        "sys" to "import sys",
        "re" to "import re",
        "random" to "import random",
        "datetime" to "import datetime",
        "Path" to "from pathlib import Path"
    )

    private val CPP_AUTO_IMPORTS = mapOf(
        "cout" to "#include <iostream>",
        "cin" to "#include <iostream>",
        "vector" to "#include <vector>",
        "string" to "#include <string>",
        "map" to "#include <map>",
        "sort" to "#include <algorithm>",
        "sqrt" to "#include <cmath>"
    )

    private val DART_AUTO_IMPORTS = mapOf(
        "jsonDecode" to "import 'dart:convert';",
        "jsonEncode" to "import 'dart:convert';",
        "Timer" to "import 'dart:async';",
        "sqrt" to "import 'dart:math';",
        "Random" to "import 'dart:math';",
        "File" to "import 'dart:io';"
    )

    private val RUST_AUTO_IMPORTS = mapOf(
        "HashMap" to "use std::collections::HashMap;",
        "HashSet" to "use std::collections::HashSet;",
        "VecDeque" to "use std::collections::VecDeque;",
        "File" to "use std::fs::File;",
        "Arc" to "use std::sync::Arc;",
        "Mutex" to "use std::sync::Mutex;"
    )

    fun getMissingImportsForLanguage(code: String, language: LanguageType): List<AutoImportQuickFix> {
        val importTable = when (language) {
            LanguageType.KOTLIN -> KOTLIN_AUTO_IMPORTS
            LanguageType.JAVA -> JAVA_AUTO_IMPORTS
            LanguageType.PYTHON -> PYTHON_AUTO_IMPORTS
            LanguageType.CPP -> CPP_AUTO_IMPORTS
            LanguageType.DART -> DART_AUTO_IMPORTS
            LanguageType.RUST -> RUST_AUTO_IMPORTS
        }
        val lines = code.lines()
        val results = mutableListOf<AutoImportQuickFix>()
        val seenSymbols = mutableSetOf<String>()

        lines.forEachIndexed { idx, rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.startsWith("import ") || trimmed.startsWith("from ") ||
                trimmed.startsWith("#include") || trimmed.startsWith("use ") ||
                trimmed.startsWith("//") || trimmed.startsWith("#")
            ) {
                return@forEachIndexed
            }
            for ((symbol, statement) in importTable) {
                if (symbol in seenSymbols) continue
                if (code.contains(statement)) continue
                val regex = Regex("""\b${Regex.escape(symbol)}\b""")
                if (regex.containsMatchIn(rawLine)) {
                    seenSymbols.add(symbol)
                    results.add(AutoImportQuickFix(symbol = symbol, importStatement = statement, line = idx + 1))
                }
            }
        }
        return results
    }

    fun applyAutoImport(code: String, importStatement: String, language: LanguageType): String {
        if (code.contains(importStatement)) return code
        val lines = code.lines().toMutableList()
        val packageIdx = lines.indexOfFirst { it.trim().startsWith("package ") }
        val firstImportIdx = lines.indexOfFirst {
            val t = it.trim()
            t.startsWith("import ") || t.startsWith("from ") || t.startsWith("#include") || t.startsWith("use ")
        }
        when {
            firstImportIdx >= 0 -> lines.add(firstImportIdx, importStatement)
            packageIdx >= 0 -> {
                lines.add(packageIdx + 1, "")
                lines.add(packageIdx + 2, importStatement)
            }
            else -> {
                lines.add(0, importStatement)
                if (lines.getOrNull(1)?.isNotBlank() == true) {
                    lines.add(1, "")
                }
            }
        }
        return lines.joinToString("\n")
    }

    /**
     * CodeAssist Optimize Imports:
     * Deduplicates, sorts alphabetically, and removes unused import/include/use statements.
     */
    fun optimizeImportsForLanguage(code: String, language: LanguageType): String {
        val lines = code.lines()
        val importPrefixPredicate: (String) -> Boolean = { trimmed ->
            when (language) {
                LanguageType.KOTLIN, LanguageType.JAVA, LanguageType.DART -> trimmed.startsWith("import ")
                LanguageType.PYTHON -> trimmed.startsWith("import ") || trimmed.startsWith("from ")
                LanguageType.CPP -> trimmed.startsWith("#include")
                LanguageType.RUST -> trimmed.startsWith("use ")
            }
        }

        val packageLines = mutableListOf<String>()
        val importLines = mutableListOf<String>()
        val bodyLines = mutableListOf<String>()
        var inHeader = true

        for (line in lines) {
            val trimmed = line.trim()
            if (inHeader && trimmed.startsWith("package ")) {
                packageLines.add(trimmed)
            } else if (importPrefixPredicate(trimmed)) {
                importLines.add(trimmed)
            } else if (inHeader && trimmed.isEmpty() && (packageLines.isNotEmpty() || importLines.isNotEmpty())) {
                // Skip extra blank lines between package and imports
            } else {
                if (trimmed.isNotEmpty()) inHeader = false
                bodyLines.add(line)
            }
        }

        val bodyText = bodyLines.joinToString("\n")
        // Filter out unused imports (keep wildcard imports and imports whose terminal identifier appears in bodyText)
        val usedAndSortedImports = importLines
            .distinct()
            .filter { imp ->
                if (imp.contains("*")) return@filter true
                val symbolMatch = Regex("""([A-Za-z0-9_]+)\s*[;>'"\s]*$""").find(imp)
                val importedSymbol = symbolMatch?.groupValues?.get(1) ?: return@filter true
                if (importedSymbol == "iostream") {
                    bodyText.contains("cout") || bodyText.contains("cin") || bodyText.contains("endl")
                } else {
                    Regex("""\b${Regex.escape(importedSymbol)}\b""").containsMatchIn(bodyText)
                }
            }
            .sorted()

        return buildString {
            if (packageLines.isNotEmpty()) {
                appendLine(packageLines.joinToString("\n"))
                appendLine()
            }
            if (usedAndSortedImports.isNotEmpty()) {
                appendLine(usedAndSortedImports.joinToString("\n"))
                appendLine()
            }
            append(bodyLines.dropWhile { it.isBlank() }.joinToString("\n"))
        }
    }

    // =========================================================================
    // CODEASSIST CODE FOLDING & GO-TO-DEFINITION ENGINE
    // =========================================================================

    data class FoldableCodeRegion(
        val startLine: Int,
        val endLine: Int,
        val headerPreview: String
    ) {
        val lineSpan: Int get() = (endLine - startLine + 1).coerceAtLeast(1)
        val summaryLabel: String get() = headerPreview
    }

    fun detectFoldableRegions(code: String, language: LanguageType): List<FoldableCodeRegion> {
        val lines = code.lines()
        val regions = mutableListOf<FoldableCodeRegion>()

        if (language == LanguageType.PYTHON) {
            for (i in lines.indices) {
                val raw = lines[i]
                val trimmed = raw.trim()
                if ((trimmed.startsWith("def ") || trimmed.startsWith("class ") || trimmed.startsWith("if ") || trimmed.startsWith("for ")) &&
                    trimmed.endsWith(":")
                ) {
                    val baseIndent = raw.takeWhile { it == ' ' || it == '\t' }.length
                    var endIdx = i
                    for (j in (i + 1) until lines.size) {
                        val nextLine = lines[j]
                        if (nextLine.isBlank()) continue
                        val nextIndent = nextLine.takeWhile { it == ' ' || it == '\t' }.length
                        if (nextIndent > baseIndent) {
                            endIdx = j
                        } else {
                            break
                        }
                    }
                    if (endIdx > i) {
                        regions.add(
                            FoldableCodeRegion(
                                startLine = i + 1,
                                endLine = endIdx + 1,
                                headerPreview = trimmed.take(42)
                            )
                        )
                    }
                }
            }
            return regions
        }

        // Brace-delimited languages (Kotlin, Java, C++, Dart, Rust)
        val openStack = ArrayDeque<Pair<Int, String>>()
        lines.forEachIndexed { idx, raw ->
            val trimmed = raw.trim()
            if (trimmed.startsWith("//")) return@forEachIndexed
            val opens = trimmed.count { it == '{' }
            val closes = trimmed.count { it == '}' }
            repeat(opens) {
                openStack.addLast((idx + 1) to trimmed.removeSuffix("{").trim())
            }
            repeat(closes) {
                if (openStack.isNotEmpty()) {
                    val (startLine, preview) = openStack.removeLast()
                    val endLine = idx + 1
                    if (endLine > startLine && preview.isNotBlank()) {
                        regions.add(
                            FoldableCodeRegion(
                                startLine = startLine,
                                endLine = endLine,
                                headerPreview = preview.take(44)
                            )
                        )
                    }
                }
            }
        }
        return regions.sortedBy { it.startLine }
    }

    data class CodeSymbolDefinition(
        val name: String,
        val kind: String,
        val signature: String,
        val line: Int,
        val sectionId: Long = 0L,
        val fileName: String = ""
    ) {
        val kindBadge: Char
            get() = when (kind) {
                "Function" -> 'f'
                "Class/Type" -> 'C'
                "Interface/Enum" -> 'I'
                else -> 'v'
            }
    }

    fun extractSymbolsForSection(
        code: String,
        language: LanguageType,
        sectionId: Long = 0L,
        fileName: String = ""
    ): List<CodeSymbolDefinition> {
        val symbols = mutableListOf<CodeSymbolDefinition>()
        val declRegex = Regex(
            """^\s*(?:public\s+|private\s+|protected\s+|internal\s+|suspend\s+|inline\s+|data\s+|sealed\s+|open\s+|abstract\s+|static\s+|final\s+)*(fun|def|class|interface|object|struct|enum|impl|void|int|val|var|let)\s+([A-Za-z_][A-Za-z0-9_]*)"""
        )
        code.lines().forEachIndexed { idx, rawLine ->
            val match = declRegex.find(rawLine) ?: return@forEachIndexed
            val keyword = match.groupValues[1]
            val name = match.groupValues[2]
            if (name in setOf("if", "for", "while", "switch", "when", "return")) return@forEachIndexed
            val kind = when (keyword) {
                "fun", "def", "void", "int" -> "Function"
                "class", "struct", "object", "impl" -> "Class/Type"
                "interface", "enum" -> "Interface/Enum"
                else -> "Property"
            }
            symbols.add(
                CodeSymbolDefinition(
                    name = name,
                    kind = kind,
                    signature = rawLine.trim().removeSuffix("{").trim().take(54),
                    line = idx + 1,
                    sectionId = sectionId,
                    fileName = fileName.ifBlank { language.defaultFileName }
                )
            )
        }
        return symbols
    }

    fun findDefinitionAtCursor(
        code: String,
        cursorPosition: Int,
        allProjectSymbols: List<CodeSymbolDefinition>
    ): CodeSymbolDefinition? {
        if (code.isEmpty()) return null
        val safeCursor = cursorPosition.coerceIn(0, code.length)
        var start = safeCursor
        while (start > 0 && (code[start - 1].isLetterOrDigit() || code[start - 1] == '_')) {
            start--
        }
        var end = safeCursor
        while (end < code.length && (code[end].isLetterOrDigit() || code[end] == '_')) {
            end++
        }
        if (start >= end) return null
        val token = code.substring(start, end)
        return allProjectSymbols.firstOrNull { it.name == token }
    }

    // =========================================================================
    // CODEASSIST DEBUGGER EXPRESSION WATCH EVALUATOR
    // =========================================================================

    fun evaluateWatchExpression(
        expression: String,
        variables: Map<String, String>
    ): String {
        val expr = expression.trim()
        if (expr.isEmpty()) return "<empty>"

        // Direct variable match
        variables[expr]?.let { return it }

        // Property/method calls on variables (`name.length`, `name.uppercase()`, `name.lowercase()`, `items.size`)
        if (expr.contains(".")) {
            val targetVar = expr.substringBefore(".").trim()
            val member = expr.substringAfter(".").trim()
            val rawVal = variables[targetVar]?.removeSurrounding("\"")?.removeSurrounding("'")
            if (rawVal != null) {
                return when (member) {
                    "length", "length()", "size", "count()" -> {
                        if (rawVal.startsWith("listOf(") || rawVal.startsWith("[")) {
                            rawVal.split(",").filter { it.isNotBlank() }.size.toString()
                        } else {
                            rawVal.length.toString()
                        }
                    }
                    "uppercase()", "toUpperCase()", "upper()" -> "\"${rawVal.uppercase()}\""
                    "lowercase()", "toLowerCase()", "lower()" -> "\"${rawVal.lowercase()}\""
                    "isEmpty()", "isBlank()" -> rawVal.isBlank().toString()
                    else -> "$rawVal.$member"
                }
            }
        }

        // Substitute known variable values into arithmetic or comparison expressions
        var substituted = expr
        for ((k, v) in variables) {
            substituted = substituted.replace(Regex("""\b${Regex.escape(k)}\b"""), v.removeSurrounding("\""))
        }

        // Binary numeric operations (`+`, `-`, `*`, `/`, `%`, `>`, `<`, `==`)
        val binaryMatch = Regex("""^\s*(-?\d+(?:\.\d+)?)\s*(\+|\-|\*|/|%|==|!=|>=|<=|>|<)\s*(-?\d+(?:\.\d+)?)\s*$""")
            .find(substituted)
        if (binaryMatch != null) {
            val left = binaryMatch.groupValues[1].toDoubleOrNull()
            val op = binaryMatch.groupValues[2]
            val right = binaryMatch.groupValues[3].toDoubleOrNull()
            if (left != null && right != null) {
                val isIntegerMath = !binaryMatch.groupValues[1].contains(".") && !binaryMatch.groupValues[3].contains(".")
                return when (op) {
                    "+" -> if (isIntegerMath) (left.toLong() + right.toLong()).toString() else (left + right).toString()
                    "-" -> if (isIntegerMath) (left.toLong() - right.toLong()).toString() else (left - right).toString()
                    "*" -> if (isIntegerMath) (left.toLong() * right.toLong()).toString() else (left * right).toString()
                    "/" -> if (right == 0.0) "ArithmeticException: / by zero" else if (isIntegerMath) (left.toLong() / right.toLong()).toString() else (left / right).toString()
                    "%" -> if (right == 0.0) "NaN" else (left.toLong() % right.toLong()).toString()
                    "==" -> (left == right).toString()
                    "!=" -> (left != right).toString()
                    ">" -> (left > right).toString()
                    "<" -> (left < right).toString()
                    ">=" -> (left >= right).toString()
                    "<=" -> (left <= right).toString()
                    else -> substituted
                }
            }
        }

        return if (substituted != expr) substituted else "unresolved: $expr"
    }

    // =========================================================================
    // MULTI-LANGUAGE CODE FORMATTER, LINE COMMENTS & LIVE TEMPLATES
    // =========================================================================

    /**
     * Formats source code according to the target language's indentation rules.
     * Prevents applying brace-based Kotlin formatting to Python indentation blocks.
     */
    fun formatCodeForLanguage(code: String, language: LanguageType): String {
        if (code.isBlank()) return code
        if (language == LanguageType.KOTLIN) {
            return KotlinCodeEngine.formatKotlinCode(code)
        }
        if (language == LanguageType.PYTHON) {
            val rawLines = code.replace("\t", "    ").lines()
            val formatted = mutableListOf<String>()
            var indentLevel = 0
            for (raw in rawLines) {
                val trimmed = raw.trim()
                if (trimmed.isEmpty()) {
                    formatted.add("")
                    continue
                }
                if (trimmed.startsWith("elif ") || trimmed.startsWith("else:") ||
                    trimmed.startsWith("except") || trimmed.startsWith("finally:")
                ) {
                    indentLevel = (indentLevel - 1).coerceAtLeast(0)
                }
                val existingSpaces = raw.takeWhile { it == ' ' }.length
                // Respect user dedent if they explicitly unindented to top-level
                if (existingSpaces == 0 && !trimmed.startsWith("elif ") &&
                    !trimmed.startsWith("else:") && !trimmed.startsWith("except") &&
                    !trimmed.startsWith("finally:") && formatted.lastOrNull()?.isBlank() == true
                ) {
                    indentLevel = 0
                }
                val indent = "    ".repeat(indentLevel)
                formatted.add("$indent$trimmed")
                when {
                    trimmed.endsWith(":") -> indentLevel++
                    trimmed == "return" || trimmed.startsWith("return ") ||
                        trimmed == "pass" || trimmed == "break" || trimmed == "continue" ||
                        trimmed.startsWith("raise ") -> {
                        indentLevel = (indentLevel - 1).coerceAtLeast(0)
                    }
                }
            }
            return formatted.joinToString("\n").trimEnd()
        }

        // Brace-delimited formatting for Java, C++, Dart, and Rust
        val indentUnit = if (language == LanguageType.DART) "  " else "    "
        var indentLevel = 0
        val out = mutableListOf<String>()
        for (rawLine in code.lines()) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) {
                out.add("")
                continue
            }
            val leadingCloses = trimmed.takeWhile { it == '}' || it == ')' || it == ']' }.length
            val effectiveIndent = (indentLevel - leadingCloses).coerceAtLeast(0)
            out.add(indentUnit.repeat(effectiveIndent) + trimmed)
            val netBraces = trimmed.count { it == '{' } - trimmed.count { it == '}' }
            indentLevel = (indentLevel + netBraces).coerceAtLeast(0)
        }
        return out.joinToString("\n").trimEnd()
    }

    /**
     * Toggles line comment at the current cursor line using `# ` for Python and `// ` for C-style languages.
     */
    fun toggleLineCommentForLanguage(value: TextFieldValue, language: LanguageType): TextFieldValue {
        if (language != LanguageType.PYTHON) {
            return KotlinCodeEngine.toggleLineCommentAtCursor(value)
        }
        val text = value.text
        val cursor = value.selection.start.coerceIn(0, text.length)
        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it < 0) text.length else it }
        val line = text.substring(lineStart, lineEnd)
        val leadingSpaces = line.takeWhile { it == ' ' || it == '\t' }
        val content = line.substring(leadingSpaces.length)

        val newLine: String
        val cursorDelta: Int
        if (content.startsWith("# ")) {
            newLine = leadingSpaces + content.removePrefix("# ")
            cursorDelta = -2
        } else if (content.startsWith("#")) {
            newLine = leadingSpaces + content.removePrefix("#")
            cursorDelta = -1
        } else {
            newLine = "$leadingSpaces# $content"
            cursorDelta = 2
        }
        val updated = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
        val newCursor = (cursor + cursorDelta).coerceIn(lineStart, lineStart + newLine.length)
        return TextFieldValue(text = updated, selection = TextRange(newCursor))
    }

    /**
     * Language-specific live templates for the editor's `⌘/` Live Templates bar.
     */
    fun getLiveTemplatesForLanguage(language: LanguageType): List<KotlinLiveTemplate> = when (language) {
        LanguageType.KOTLIN -> KotlinCodeEngine.LIVE_TEMPLATES
        LanguageType.JAVA -> listOf(
            KotlinLiveTemplate("java_psvm", "psvm", "main()", "public static void main(String[] args) {\n    |\n}"),
            KotlinLiveTemplate("java_sout", "sout", "println", "System.out.println(\"|\");"),
            KotlinLiveTemplate("java_fori", "fori", "for loop", "for (int i = 0; i < n; i++) {\n    |\n}"),
            KotlinLiveTemplate("java_tryc", "tryc", "try/catch", "try {\n    |\n} catch (Exception e) {\n    e.printStackTrace();\n}"),
            KotlinLiveTemplate("java_cls", "cls", "class", "public class | {\n    \n}")
        )
        LanguageType.PYTHON -> listOf(
            KotlinLiveTemplate("py_def", "def", "def fn", "def |(args):\n    pass"),
            KotlinLiveTemplate("py_main", "main", "__main__", "if __name__ == '__main__':\n    |"),
            KotlinLiveTemplate("py_for", "for", "for in", "for item in |:\n    print(item)"),
            KotlinLiveTemplate("py_comp", "comp", "list comp", "[x for x in |]"),
            KotlinLiveTemplate("py_try", "try", "try/except", "try:\n    |\nexcept Exception as e:\n    print(e)")
        )
        LanguageType.CPP -> listOf(
            KotlinLiveTemplate("cpp_main", "main", "int main", "#include <iostream>\n\nint main() {\n    |\n    return 0;\n}"),
            KotlinLiveTemplate("cpp_cout", "cout", "std::cout", "std::cout << \"|\" << std::endl;"),
            KotlinLiveTemplate("cpp_for", "for", "for loop", "for (int i = 0; i < n; ++i) {\n    |\n}"),
            KotlinLiveTemplate("cpp_vec", "vec", "vector", "std::vector<int> items = {|};"),
            KotlinLiveTemplate("cpp_cls", "cls", "class", "class | {\npublic:\n    \n};")
        )
        LanguageType.DART -> listOf(
            KotlinLiveTemplate("dart_main", "main", "void main", "void main() {\n  |\n}"),
            KotlinLiveTemplate("dart_pr", "pr", "print()", "print('|');"),
            KotlinLiveTemplate("dart_async", "async", "Future", "Future<void> |() async {\n  \n}"),
            KotlinLiveTemplate("dart_for", "for", "for in", "for (final item in |) {\n  print(item);\n}"),
            KotlinLiveTemplate("dart_cls", "cls", "class", "class | {\n  const ();\n}")
        )
        LanguageType.RUST -> listOf(
            KotlinLiveTemplate("rs_main", "main", "fn main", "fn main() {\n    println!(\"|\");\n}"),
            KotlinLiveTemplate("rs_fn", "fn", "fn def", "fn |() {\n    \n}"),
            KotlinLiveTemplate("rs_match", "match", "match", "match | {\n    _ => (),\n}"),
            KotlinLiveTemplate("rs_struct", "struct", "struct", "#[derive(Debug, Clone)]\nstruct | {\n    \n}"),
            KotlinLiveTemplate("rs_for", "for", "for in", "for item in | {\n    println!(\"{:?}\", item);\n}")
        )
    }
}
