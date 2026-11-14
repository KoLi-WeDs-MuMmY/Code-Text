package com.example.code

import com.example.model.LanguageType
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * 100% On-Device Multi-Language AST Statement & Expression Interpreter.
 *
 * Genuinely executes variables, arithmetic, booleans, conditionals (`if`/`else if`/`elif`/`else`),
 * `for` & `while` loops, user-defined functions with `return` & recursion, lists/arrays,
 * string interpolation, and interactive `stdin` reads across Kotlin, Java, Python, C++, Dart, and Rust
 * on Android without requiring external native ELF binary execution or a remote paid server.
 */
object OnDeviceInterpreter {

    private const val MAX_EXECUTION_STEPS = 4500

    data class ExecutionResult(
        val stdoutLines: List<String>,
        val variables: Map<String, String>,
        val callStack: List<String>,
        val runtimeError: String? = null,
        val stepsExecuted: Int = 0
    )

    private sealed class ExecFlow {
        data object Next : ExecFlow()
        data object Break : ExecFlow()
        data object Continue : ExecFlow()
        data class Return(val value: Any?) : ExecFlow()
    }

    private data class FunctionDef(
        val name: String,
        val params: List<String>,
        val body: List<Stmt>,
        val definedLine: Int
    )

    private sealed class Stmt(val lineNumber: Int) {
        class PrintStmt(line: Int, val rawArgs: List<String>, val formatMacro: Boolean = false) : Stmt(line)
        class VarDecl(line: Int, val name: String, val expr: String?) : Stmt(line)
        class AssignStmt(line: Int, val target: String, val op: String, val expr: String) : Stmt(line)
        class ReturnStmt(line: Int, val expr: String?) : Stmt(line)
        class BreakStmt(line: Int) : Stmt(line)
        class ContinueStmt(line: Int) : Stmt(line)
        class ExprStmt(line: Int, val expr: String) : Stmt(line)
        class IfStmt(
            line: Int,
            val branches: List<Pair<String, List<Stmt>>>,
            val elseBranch: List<Stmt>
        ) : Stmt(line)

        class WhileStmt(line: Int, val condition: String, val body: List<Stmt>) : Stmt(line)
        class ForInStmt(line: Int, val varName: String, val iterableExpr: String, val body: List<Stmt>) : Stmt(line)
        class CForStmt(
            line: Int,
            val initStmt: Stmt?,
            val condition: String,
            val updateStmt: Stmt?,
            val body: List<Stmt>
        ) : Stmt(line)
    }

    fun execute(
        code: String,
        language: LanguageType,
        stdinLines: List<String> = emptyList()
    ): ExecutionResult {
        val rawLines = code.replace("\r\n", "\n").split('\n')
        val functions = linkedMapOf<String, FunctionDef>()
        val topLevelStmts = mutableListOf<Stmt>()
        val stdout = mutableListOf<String>()
        val capturedGlobals = linkedMapOf<String, Any?>()
        val callStackLog = mutableListOf<String>()

        try {
            if (language == LanguageType.PYTHON) {
                parsePythonBlock(
                    lines = rawLines,
                    startIdx = 0,
                    minIndent = 0,
                    functions = functions,
                    outStmts = topLevelStmts
                )
            } else {
                parseBraceProgram(
                    lines = rawLines,
                    language = language,
                    functions = functions,
                    outTopLevel = topLevelStmts
                )
            }
        } catch (e: Exception) {
            return ExecutionResult(
                stdoutLines = emptyList(),
                variables = emptyMap(),
                callStack = listOf("parse() at ${language.defaultFileName}:1"),
                runtimeError = "Syntax/Parse error: ${e.message ?: "unexpected structure"}"
            )
        }

        val ctx = RuntimeContext(
            language = language,
            functions = functions,
            stdinQueue = ArrayDeque(stdinLines),
            stdout = stdout,
            capturedVars = capturedGlobals,
            callStackLog = callStackLog
        )

        val rootScope = linkedMapOf<String, Any?>()
        try {
            val flow = ctx.executeBlock(topLevelStmts, rootScope)
            if (flow is ExecFlow.Return) {
                return buildResult(ctx, rootScope, null)
            }
            val entryFn = when {
                "main" in functions && !hasExplicitMainCall(topLevelStmts) -> functions["main"]
                else -> null
            }
            if (entryFn != null) {
                ctx.invokeFunction(entryFn, emptyList())
            }
        } catch (e: RuntimeException) {
            return buildResult(ctx, rootScope, e.message ?: "Runtime error")
        }

        return buildResult(ctx, rootScope, null)
    }

    private fun hasExplicitMainCall(stmts: List<Stmt>): Boolean {
        return stmts.any { stmt ->
            when (stmt) {
                is Stmt.ExprStmt -> stmt.expr.trim().startsWith("main(")
                is Stmt.IfStmt -> stmt.branches.any { hasExplicitMainCall(it.second) } || hasExplicitMainCall(stmt.elseBranch)
                else -> false
            }
        }
    }

    private fun buildResult(
        ctx: RuntimeContext,
        rootScope: Map<String, Any?>,
        error: String?
    ): ExecutionResult {
        val formattedVars = linkedMapOf<String, String>()
        rootScope.forEach { (k, v) -> formattedVars[k] = formatValueForInspector(v) }
        ctx.capturedVars.forEach { (k, v) -> formattedVars[k] = formatValueForInspector(v) }
        val stack = if (ctx.callStackLog.isEmpty()) {
            listOf("main() at ${ctx.language.defaultFileName}:1")
        } else {
            ctx.callStackLog.distinct()
        }
        return ExecutionResult(
            stdoutLines = ctx.stdout.toList(),
            variables = formattedVars,
            callStack = stack,
            runtimeError = error,
            stepsExecuted = ctx.steps
        )
    }

    private fun formatValueForInspector(value: Any?): String = when (value) {
        null -> "null"
        is String -> "\"$value\""
        is Double -> if (value == floor(value) && abs(value) < 1e12) value.toLong().toString() else value.toString()
        is List<*> -> "[${value.joinToString(", ") { formatValueForInspector(it) }}]"
        else -> value.toString()
    }

    private fun formatValueForPrint(value: Any?, language: LanguageType): String = when (value) {
        null -> if (language == LanguageType.PYTHON) "None" else "null"
        is Boolean -> if (language == LanguageType.PYTHON) (if (value) "True" else "False") else value.toString()
        is Double -> {
            if (value == floor(value) && abs(value) < 1e12) {
                value.toLong().toString()
            } else {
                val rounded = ((value * 1_000_000.0).roundToLong() / 1_000_000.0)
                rounded.toString()
            }
        }
        is List<*> -> "[${value.joinToString(", ") { formatValueForPrint(it, language) }}]"
        else -> value.toString()
    }

    // =========================================================================
    // PYTHON INDENTATION PARSER
    // =========================================================================

    private fun parsePythonBlock(
        lines: List<String>,
        startIdx: Int,
        minIndent: Int,
        functions: MutableMap<String, FunctionDef>,
        outStmts: MutableList<Stmt>
    ): Int {
        var i = startIdx
        while (i < lines.size) {
            val raw = lines[i]
            val trimmed = stripInlineComment(raw, '#').trim()
            if (trimmed.isEmpty() || trimmed.startsWith("import ") || trimmed.startsWith("from ")) {
                i++
                continue
            }
            val indent = raw.takeWhile { it == ' ' || it == '\t' }.sumOf { if (it == '\t') 4 else 1 }
            if (indent < minIndent) {
                return i
            }
            val lineNum = i + 1

            // Function definition `def name(a, b):`
            val defMatch = Regex("""^def\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(([^)]*)\)\s*:\s*$""").find(trimmed)
            if (defMatch != null) {
                val fnName = defMatch.groupValues[1]
                val params = defMatch.groupValues[2].split(",")
                    .mapNotNull { p -> p.trim().substringBefore("=").substringBefore(":").trim().takeIf { it.isNotEmpty() } }
                val body = mutableListOf<Stmt>()
                val nextIdx = parsePythonBlock(lines, i + 1, indent + 1, functions, body)
                functions[fnName] = FunctionDef(fnName, params, body, lineNum)
                i = nextIdx
                continue
            }

            // Skip class wrapper headers cleanly while parsing body if top-level
            if (trimmed.startsWith("class ") && trimmed.endsWith(":")) {
                val classBody = mutableListOf<Stmt>()
                i = parsePythonBlock(lines, i + 1, indent + 1, functions, classBody)
                outStmts.addAll(classBody)
                continue
            }

            // `if cond:` (+ `elif` + `else`)
            if (trimmed.startsWith("if ") && trimmed.endsWith(":")) {
                val branches = mutableListOf<Pair<String, List<Stmt>>>()
                var elseBody = emptyList<Stmt>()
                val firstCond = trimmed.removePrefix("if ").removeSuffix(":").trim()
                val firstBody = mutableListOf<Stmt>()
                i = parsePythonBlock(lines, i + 1, indent + 1, functions, firstBody)
                branches.add(firstCond to firstBody)

                while (i < lines.size) {
                    val nextRaw = lines[i]
                    val nextTrimmed = stripInlineComment(nextRaw, '#').trim()
                    if (nextTrimmed.isEmpty()) {
                        i++
                        continue
                    }
                    val nextIndent = nextRaw.takeWhile { it == ' ' || it == '\t' }.sumOf { if (it == '\t') 4 else 1 }
                    if (nextIndent != indent) break
                    if (nextTrimmed.startsWith("elif ") && nextTrimmed.endsWith(":")) {
                        val cond = nextTrimmed.removePrefix("elif ").removeSuffix(":").trim()
                        val b = mutableListOf<Stmt>()
                        i = parsePythonBlock(lines, i + 1, indent + 1, functions, b)
                        branches.add(cond to b)
                    } else if (nextTrimmed.matches(Regex("""^else\s*:\s*$"""))) {
                        val b = mutableListOf<Stmt>()
                        i = parsePythonBlock(lines, i + 1, indent + 1, functions, b)
                        elseBody = b
                        break
                    } else {
                        break
                    }
                }
                outStmts.add(Stmt.IfStmt(lineNum, branches, elseBody))
                continue
            }

            if (trimmed.startsWith("while ") && trimmed.endsWith(":")) {
                val cond = trimmed.removePrefix("while ").removeSuffix(":").trim()
                val body = mutableListOf<Stmt>()
                i = parsePythonBlock(lines, i + 1, indent + 1, functions, body)
                outStmts.add(Stmt.WhileStmt(lineNum, cond, body))
                continue
            }

            val forMatch = Regex("""^for\s+([A-Za-z_][A-Za-z0-9_]*)\s+in\s+(.+):$""").find(trimmed)
            if (forMatch != null) {
                val varName = forMatch.groupValues[1]
                val iterExpr = forMatch.groupValues[2].trim()
                val body = mutableListOf<Stmt>()
                i = parsePythonBlock(lines, i + 1, indent + 1, functions, body)
                outStmts.add(Stmt.ForInStmt(lineNum, varName, iterExpr, body))
                continue
            }

            parseSimpleStatement(trimmed, lineNum, LanguageType.PYTHON)?.let { outStmts.add(it) }
            i++
        }
        return i
    }

    // =========================================================================
    // BRACE LANGUAGE PARSER (Kotlin, Java, C++, Dart, Rust)
    // =========================================================================

    private data class CodeLine(val lineNumber: Int, val text: String)

    private fun parseBraceProgram(
        lines: List<String>,
        language: LanguageType,
        functions: MutableMap<String, FunctionDef>,
        outTopLevel: MutableList<Stmt>
    ) {
        val cleaned = mutableListOf<CodeLine>()
        var inBlockComment = false
        lines.forEachIndexed { idx, raw ->
            var s = raw
            if (inBlockComment) {
                val endIdx = s.indexOf("*/")
                if (endIdx >= 0) {
                    s = s.substring(endIdx + 2)
                    inBlockComment = false
                } else {
                    return@forEachIndexed
                }
            }
            while (true) {
                val startBlock = s.indexOf("/*")
                if (startBlock < 0) break
                val endBlock = s.indexOf("*/", startBlock + 2)
                if (endBlock >= 0) {
                    s = s.removeRange(startBlock, endBlock + 2)
                } else {
                    s = s.substring(0, startBlock)
                    inBlockComment = true
                    break
                }
            }
            val stripped = stripInlineComment(s, '/').trim()
            if (stripped.isNotEmpty() &&
                !stripped.startsWith("package ") &&
                !stripped.startsWith("import ") &&
                !stripped.startsWith("#include") &&
                !stripped.startsWith("using namespace") &&
                !stripped.startsWith("use ")
            ) {
                // Split multiple brace or semicolon statements cleanly while respecting strings
                val pieces = splitBraceLineTokens(stripped)
                pieces.forEach { piece ->
                    if (piece.isNotBlank()) cleaned.add(CodeLine(idx + 1, piece.trim()))
                }
            }
        }
        parseBraceBlock(cleaned, 0, language, functions, outTopLevel, stopAtCloseBrace = false)
    }

    private fun splitBraceLineTokens(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inDouble = false
        var inSingle = false
        var parenDepth = 0
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"' && !inSingle && (i == 0 || line[i - 1] != '\\')) inDouble = !inDouble
            else if (c == '\'' && !inDouble && (i == 0 || line[i - 1] != '\\')) inSingle = !inSingle
            else if (!inDouble && !inSingle) {
                if (c == '(') parenDepth++
                else if (c == ')' && parenDepth > 0) parenDepth--
                if (c == '{') {
                    val isInterpolationOrMacro = (i > 0 && line[i - 1] == '$') || sb.toString().contains("{}")
                    if (!isInterpolationOrMacro) {
                        sb.append(c)
                        result.add(sb.toString().trim())
                        sb.clear()
                        i++
                        continue
                    }
                } else if (c == '}') {
                    if (sb.isNotEmpty()) {
                        result.add(sb.toString().trim())
                        sb.clear()
                    }
                    result.add("}")
                    i++
                    continue
                } else if (c == ';' && parenDepth == 0) {
                    if (sb.isNotEmpty()) {
                        result.add(sb.toString().trim())
                        sb.clear()
                    }
                    i++
                    continue
                }
            }
            sb.append(c)
            i++
        }
        if (sb.isNotEmpty()) result.add(sb.toString().trim())
        return result
    }

    private fun parseBraceBlock(
        lines: List<CodeLine>,
        startIdx: Int,
        language: LanguageType,
        functions: MutableMap<String, FunctionDef>,
        outStmts: MutableList<Stmt>,
        stopAtCloseBrace: Boolean
    ): Int {
        var i = startIdx
        while (i < lines.size) {
            val item = lines[i]
            val text = item.text.trim()
            if (text == "}") {
                return if (stopAtCloseBrace) i + 1 else i + 1
            }

            // Class / struct / object wrapper: unwrap body cleanly
            val classHeader = Regex("""^(?:public\s+|private\s+|data\s+|sealed\s+)?(?:class|object|struct)\s+[A-Za-z_][A-Za-z0-9_]*.*\{$""")
            if (classHeader.matches(text)) {
                val innerStmts = mutableListOf<Stmt>()
                i = parseBraceBlock(lines, i + 1, language, functions, innerStmts, stopAtCloseBrace = true)
                outStmts.addAll(innerStmts)
                continue
            }

            // Function definition detection
            val fnHeader = matchFunctionHeader(text, language)
            if (fnHeader != null) {
                val (fnName, params, singleExprBody) = fnHeader
                if (singleExprBody != null) {
                    functions[fnName] = FunctionDef(
                        name = fnName,
                        params = params,
                        body = listOf(Stmt.ReturnStmt(item.lineNumber, singleExprBody)),
                        definedLine = item.lineNumber
                    )
                    i++
                    continue
                }
                val bodyStart = if (text.endsWith("{")) i + 1 else {
                    if (i + 1 < lines.size && lines[i + 1].text == "{") i + 2 else i + 1
                }
                val body = mutableListOf<Stmt>()
                i = parseBraceBlock(lines, bodyStart, language, functions, body, stopAtCloseBrace = true)
                functions[fnName] = FunctionDef(fnName, params, body, item.lineNumber)
                continue
            }

            // If / else if / else
            if (text.startsWith("if ") || text.startsWith("if(")) {
                val branches = mutableListOf<Pair<String, List<Stmt>>>()
                var elseBody = emptyList<Stmt>()
                val cond = extractHeaderCondition(text, "if")
                val body = mutableListOf<Stmt>()
                i = parseBraceBlock(lines, i + 1, language, functions, body, stopAtCloseBrace = true)
                branches.add(cond to body)

                while (i < lines.size) {
                    var nextText = lines[i].text.trim()
                    if (nextText.startsWith("}")) nextText = nextText.removePrefix("}").trim()
                    if (nextText.startsWith("else if")) {
                        val eCond = extractHeaderCondition(nextText, "else if")
                        val eBody = mutableListOf<Stmt>()
                        i = parseBraceBlock(lines, i + 1, language, functions, eBody, stopAtCloseBrace = true)
                        branches.add(eCond to eBody)
                    } else if (nextText == "else {" || nextText == "else") {
                        val bStart = if (nextText == "else" && lines.getOrNull(i + 1)?.text == "{") i + 2 else i + 1
                        val eBody = mutableListOf<Stmt>()
                        i = parseBraceBlock(lines, bStart, language, functions, eBody, stopAtCloseBrace = true)
                        elseBody = eBody
                        break
                    } else {
                        break
                    }
                }
                outStmts.add(Stmt.IfStmt(item.lineNumber, branches, elseBody))
                continue
            }

            // While loop
            if (text.startsWith("while ") || text.startsWith("while(")) {
                val cond = extractHeaderCondition(text, "while")
                val body = mutableListOf<Stmt>()
                i = parseBraceBlock(lines, i + 1, language, functions, body, stopAtCloseBrace = true)
                outStmts.add(Stmt.WhileStmt(item.lineNumber, cond, body))
                continue
            }

            // For loop (Kotlin/Rust `for x in ...`, or Java/C++/Dart `for (init; cond; step)`)
            if (text.startsWith("for ") || text.startsWith("for(")) {
                val headerInner = if (text.startsWith("for(") || text.startsWith("for (")) {
                    extractHeaderCondition(text, "for")
                } else {
                    text.removePrefix("for ").removeSuffix("{").trim()
                }
                val body = mutableListOf<Stmt>()
                i = parseBraceBlock(lines, i + 1, language, functions, body, stopAtCloseBrace = true)

                val cParts = splitTopLevelArgs(headerInner, ';')
                if (cParts.size == 3) {
                    val initS = parseSimpleStatement(cParts[0].trim(), item.lineNumber, language)
                    val updS = parseSimpleStatement(cParts[2].trim(), item.lineNumber, language)
                    outStmts.add(Stmt.CForStmt(item.lineNumber, initS, cParts[1].trim(), updS, body))
                } else {
                    val inMatch = Regex("""^(?:val\s+|var\s+|final\s+|auto\s+)?([A-Za-z_][A-Za-z0-9_]*)\s+(?:in|:)\s+(.+)$""").find(headerInner)
                    if (inMatch != null) {
                        outStmts.add(Stmt.ForInStmt(item.lineNumber, inMatch.groupValues[1], inMatch.groupValues[2].trim(), body))
                    }
                }
                continue
            }

            if (text == "{") {
                val inner = mutableListOf<Stmt>()
                i = parseBraceBlock(lines, i + 1, language, functions, inner, stopAtCloseBrace = true)
                outStmts.addAll(inner)
                continue
            }

            parseSimpleStatement(text.removeSuffix(";").trim(), item.lineNumber, language)?.let {
                outStmts.add(it)
            }
            i++
        }
        return i
    }

    private fun extractHeaderCondition(text: String, keyword: String): String {
        val afterKw = text.removePrefix(keyword).trim()
        if (afterKw.startsWith("(")) {
            var depth = 0
            for (idx in afterKw.indices) {
                when (afterKw[idx]) {
                    '(' -> depth++
                    ')' -> {
                        depth--
                        if (depth == 0) return afterKw.substring(1, idx).trim()
                    }
                }
            }
        }
        return afterKw.removeSuffix("{").trim()
    }

    private fun matchFunctionHeader(
        text: String,
        language: LanguageType
    ): Triple<String, List<String>, String?>? {
        val clean = text.trim()
        // Kotlin single-expression function `fun add(a: Int, b: Int): Int = a + b`
        if (language == LanguageType.KOTLIN) {
            val exprFn = Regex("""^(?:suspend\s+|private\s+|public\s+)?fun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(([^)]*)\)(?:\s*:\s*[^=]+)?\s*=\s*(.+)$""").find(clean)
            if (exprFn != null && !clean.endsWith("{")) {
                val params = exprFn.groupValues[2].split(",").mapNotNull { it.trim().substringBefore(":").trim().takeIf { p -> p.isNotEmpty() } }
                return Triple(exprFn.groupValues[1], params, exprFn.groupValues[3].trim())
            }
        }
        if (!clean.endsWith("{")) return null
        val match = when (language) {
            LanguageType.KOTLIN -> Regex("""^(?:suspend\s+|private\s+|public\s+)?fun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(([^)]*)\).*\{$""").find(clean)
            LanguageType.RUST -> Regex("""^(?:pub\s+)?fn\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(([^)]*)\).*\{$""").find(clean)
            LanguageType.JAVA, LanguageType.CPP, LanguageType.DART ->
                Regex("""^(?:public\s+|private\s+|static\s+|final\s+|Future<[^>]+>\s+)*(?:void|int|long|double|float|boolean|bool|String|std::string|auto|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(([^)]*)\).*\{$""").find(clean)
            LanguageType.PYTHON -> null
        } ?: return null

        val fnName = match.groupValues[1]
        if (fnName in setOf("if", "for", "while", "when", "switch", "catch")) return null
        val params = match.groupValues[2].split(",").mapNotNull { rawParam ->
            val p = rawParam.trim()
            if (p.isEmpty()) null
            else if (language == LanguageType.KOTLIN || language == LanguageType.RUST) {
                p.substringBefore(":").removePrefix("mut ").trim().takeIf { it.isNotEmpty() }
            } else {
                p.substringBefore("=").trim().split(Regex("""\s+""")).lastOrNull()?.removePrefix("&")?.removePrefix("*")
            }
        }
        return Triple(fnName, params, null)
    }

    private fun parseSimpleStatement(
        raw: String,
        lineNum: Int,
        language: LanguageType
    ): Stmt? {
        val text = raw.trim().removeSuffix(";").trim()
        if (text.isEmpty() || text == "{" || text == "}") return null
        if (text == "break") return Stmt.BreakStmt(lineNum)
        if (text == "continue") return Stmt.ContinueStmt(lineNum)

        // Skip Rust/C++/Java boilerplate statements that return cleanly
        if (text == "return 0" && language == LanguageType.CPP) {
            return Stmt.ReturnStmt(lineNum, "0")
        }
        if (text.startsWith("return ") || text == "return") {
            return Stmt.ReturnStmt(lineNum, text.removePrefix("return").trim().takeIf { it.isNotEmpty() })
        }

        // Print statements across all 6 languages
        val printlnMatch = Regex("""^(?:System\.(?:out|err)\.)?(println|print)\s*\((.*)\)$""").find(text)
        if (printlnMatch != null) {
            val rawInner = printlnMatch.groupValues[2].trim()
            return Stmt.PrintStmt(lineNum, splitTopLevelArgs(rawInner, ','), formatMacro = false)
        }
        val rustPrintMatch = Regex("""^println!\s*\((.*)\)$""").find(text)
        if (rustPrintMatch != null) {
            return Stmt.PrintStmt(lineNum, splitTopLevelArgs(rustPrintMatch.groupValues[1], ','), formatMacro = true)
        }
        val coutMatch = Regex("""^(?:std::)?cout\s*<<\s*(.+)$""").find(text)
        if (coutMatch != null) {
            val parts = coutMatch.groupValues[1].split("<<")
                .map { it.trim() }
                .filter { it != "std::endl" && it != "endl" && it != "\"\\n\"" && it != "'\\n'" }
            return Stmt.PrintStmt(lineNum, parts, formatMacro = false)
        }
        val cinMatch = Regex("""^(?:std::)?cin\s*>>\s*([A-Za-z_][A-Za-z0-9_]*)$""").find(text)
        if (cinMatch != null) {
            return Stmt.AssignStmt(lineNum, cinMatch.groupValues[1], "=", "readln()")
        }

        // Increment / decrement shorthand `i++`, `++i`, `i--`, `--i`
        val incDec = Regex("""^(?:(\+\+|--)\s*([A-Za-z_][A-Za-z0-9_]*)|([A-Za-z_][A-Za-z0-9_]*)\s*(\+\+|--))$""").find(text)
        if (incDec != null) {
            val varName = incDec.groupValues[2].ifEmpty { incDec.groupValues[3] }
            val op = if (incDec.groupValues[1] == "--" || incDec.groupValues[4] == "--") "-=" else "+="
            return Stmt.AssignStmt(lineNum, varName, op, "1")
        }

        // Variable declarations (val/var/let/typed Java/C++/Dart)
        val kotlinRustDecl = Regex("""^(?:val|var|let\s+mut|let|final|const)\s+([A-Za-z_][A-Za-z0-9_]*)(?:\s*:\s*[^=]+)?(?:\s*=\s*(.+))?$""").find(text)
        if (kotlinRustDecl != null) {
            val name = kotlinRustDecl.groupValues[1]
            val expr = kotlinRustDecl.groupValues[2].trim().takeIf { it.isNotEmpty() }
            return Stmt.VarDecl(lineNum, name, expr)
        }
        val cTypedDecl = Regex("""^(?:int|long|double|float|boolean|bool|String|std::string|auto|var|final\s+(?:int|double|String|bool))\s+([A-Za-z_][A-Za-z0-9_]*)(?:\[\])?(?:\s*=\s*(.+))?$""").find(text)
        if (cTypedDecl != null && !text.startsWith("Scanner ")) {
            val name = cTypedDecl.groupValues[1]
            val expr = cTypedDecl.groupValues[2].trim().takeIf { it.isNotEmpty() }
            return Stmt.VarDecl(lineNum, name, expr)
        }
        if (text.startsWith("Scanner ")) return null

        // Compound or regular assignment (`x += 2`, `x = y + 1`, `arr[0] = 5`)
        val assignIdx = findTopLevelAssignment(text)
        if (assignIdx != null) {
            val (idx, op) = assignIdx
            val target = text.substring(0, idx).trim()
            val expr = text.substring(idx + op.length).trim()
            if (target.isNotEmpty() && expr.isNotEmpty()) {
                return Stmt.AssignStmt(lineNum, target, op, expr)
            }
        }

        return Stmt.ExprStmt(lineNum, text)
    }

    // =========================================================================
    // RUNTIME CONTEXT & EXPRESSION EVALUATOR
    // =========================================================================

    private class RuntimeContext(
        val language: LanguageType,
        val functions: Map<String, FunctionDef>,
        val stdinQueue: ArrayDeque<String>,
        val stdout: MutableList<String>,
        val capturedVars: MutableMap<String, Any?>,
        val callStackLog: MutableList<String>
    ) {
        var steps = 0
        private var recursionDepth = 0

        private fun tickStep() {
            steps++
            if (steps > MAX_EXECUTION_STEPS) {
                throw IllegalStateException("Execution halted after $MAX_EXECUTION_STEPS steps (loop guard triggered)")
            }
        }

        fun executeBlock(stmts: List<Stmt>, scope: MutableMap<String, Any?>): ExecFlow {
            for (stmt in stmts) {
                tickStep()
                when (stmt) {
                    is Stmt.BreakStmt -> return ExecFlow.Break
                    is Stmt.ContinueStmt -> return ExecFlow.Continue
                    is Stmt.ReturnStmt -> {
                        val rv = stmt.expr?.let { evalExpr(it, scope) }
                        return ExecFlow.Return(rv)
                    }
                    is Stmt.VarDecl -> {
                        val v = stmt.expr?.let { evalExpr(it, scope) } ?: 0
                        scope[stmt.name] = v
                        capturedVars[stmt.name] = v
                    }
                    is Stmt.AssignStmt -> {
                        val rhs = evalExpr(stmt.expr, scope)
                        applyAssignment(stmt.target, stmt.op, rhs, scope)
                    }
                    is Stmt.PrintStmt -> {
                        if (stmt.formatMacro && stmt.rawArgs.size > 1 && stmt.rawArgs[0].contains("{}")) {
                            var fmt = evalExpr(stmt.rawArgs[0], scope).toString()
                            for (arg in stmt.rawArgs.drop(1)) {
                                val evaluated = evalExpr(arg, scope)
                                fmt = fmt.replaceFirst("{}", formatValueForPrint(evaluated, language))
                            }
                            stdout.add(fmt)
                        } else if (stmt.rawArgs.isEmpty()) {
                            stdout.add("")
                        } else {
                            val sep = if (language == LanguageType.PYTHON) " " else ""
                            val rendered = stmt.rawArgs.joinToString(sep) { arg ->
                                formatValueForPrint(evalExpr(arg, scope), language)
                            }
                            stdout.add(rendered)
                        }
                    }
                    is Stmt.ExprStmt -> {
                        evalExpr(stmt.expr, scope)
                    }
                    is Stmt.IfStmt -> {
                        var executed = false
                        for ((cond, body) in stmt.branches) {
                            if (toBoolean(evalExpr(cond, scope))) {
                                val child = scope.toMutableMap()
                                val flow = executeBlock(body, child)
                                propagateMutations(scope, child)
                                if (flow != ExecFlow.Next) return flow
                                executed = true
                                break
                            }
                        }
                        if (!executed && stmt.elseBranch.isNotEmpty()) {
                            val child = scope.toMutableMap()
                            val flow = executeBlock(stmt.elseBranch, child)
                            propagateMutations(scope, child)
                            if (flow != ExecFlow.Next) return flow
                        }
                    }
                    is Stmt.WhileStmt -> {
                        while (toBoolean(evalExpr(stmt.condition, scope))) {
                            tickStep()
                            val child = scope.toMutableMap()
                            val flow = executeBlock(stmt.body, child)
                            propagateMutations(scope, child)
                            when (flow) {
                                ExecFlow.Break -> break
                                ExecFlow.Continue -> continue
                                is ExecFlow.Return -> return flow
                                ExecFlow.Next -> Unit
                            }
                        }
                    }
                    is Stmt.ForInStmt -> {
                        val iterable = evalIterable(stmt.iterableExpr, scope)
                        for (item in iterable) {
                            tickStep()
                            val child = scope.toMutableMap()
                            child[stmt.varName] = item
                            capturedVars[stmt.varName] = item
                            val flow = executeBlock(stmt.body, child)
                            propagateMutations(scope, child, excludeKey = stmt.varName)
                            when (flow) {
                                ExecFlow.Break -> break
                                ExecFlow.Continue -> continue
                                is ExecFlow.Return -> return flow
                                ExecFlow.Next -> Unit
                            }
                        }
                    }
                    is Stmt.CForStmt -> {
                        val loopScope = scope.toMutableMap()
                        if (stmt.initStmt != null) {
                            executeBlock(listOf(stmt.initStmt), loopScope)
                        }
                        while (stmt.condition.isBlank() || toBoolean(evalExpr(stmt.condition, loopScope))) {
                            tickStep()
                            val flow = executeBlock(stmt.body, loopScope)
                            when (flow) {
                                ExecFlow.Break -> break
                                is ExecFlow.Return -> {
                                    propagateMutations(scope, loopScope)
                                    return flow
                                }
                                ExecFlow.Continue, ExecFlow.Next -> Unit
                            }
                            if (stmt.updateStmt != null) {
                                executeBlock(listOf(stmt.updateStmt), loopScope)
                            }
                        }
                        propagateMutations(scope, loopScope)
                    }
                }
            }
            return ExecFlow.Next
        }

        private fun propagateMutations(
            parent: MutableMap<String, Any?>,
            child: Map<String, Any?>,
            excludeKey: String? = null
        ) {
            child.forEach { (k, v) ->
                if (k != excludeKey && k in parent) {
                    parent[k] = v
                    capturedVars[k] = v
                }
            }
        }

        private fun applyAssignment(
            target: String,
            op: String,
            rhs: Any?,
            scope: MutableMap<String, Any?>
        ) {
            val cleanTarget = target.trim()
            if (cleanTarget.contains("[") && cleanTarget.endsWith("]")) {
                val arrName = cleanTarget.substringBefore("[").trim()
                val idxExpr = cleanTarget.substringAfter("[").substringBeforeLast("]")
                val idx = (evalExpr(idxExpr, scope) as? Number)?.toInt() ?: 0
                val list = scope[arrName] as? MutableList<Any?> ?: return
                if (idx in list.indices) {
                    val current = list[idx]
                    val nextVal = if (op == "=") rhs else applyCompoundOp(current, op, rhs)
                    list[idx] = nextVal
                    capturedVars[arrName] = list.toList()
                }
                return
            }
            val current = scope[cleanTarget]
            val nextVal = if (op == "=") rhs else applyCompoundOp(current, op, rhs)
            scope[cleanTarget] = nextVal
            capturedVars[cleanTarget] = nextVal
        }

        private fun applyCompoundOp(left: Any?, op: String, right: Any?): Any? {
            if (op == "+=" && (left is String || right is String)) {
                return "${formatValueForPrint(left, language)}${formatValueForPrint(right, language)}"
            }
            val l = (left as? Number)?.toDouble() ?: 0.0
            val r = (right as? Number)?.toDouble() ?: 0.0
            val res = when (op) {
                "+=" -> l + r
                "-=" -> l - r
                "*=" -> l * r
                "/=" -> if (r == 0.0) throw ArithmeticException("Division by zero") else l / r
                "%=" -> l % r
                else -> r
            }
            return simplifyNumber(res, left is Int && right is Int)
        }

        fun invokeFunction(fn: FunctionDef, args: List<Any?>): Any? {
            recursionDepth++
            if (recursionDepth > 64) {
                throw IllegalStateException("Stack overflow in ${fn.name}() (recursion depth > 64)")
            }
            callStackLog.add("${fn.name}() at ${language.defaultFileName}:${fn.definedLine}")
            val fnScope = linkedMapOf<String, Any?>()
            fn.params.forEachIndexed { idx, paramName ->
                fnScope[paramName] = args.getOrNull(idx)
                capturedVars[paramName] = fnScope[paramName]
            }
            return try {
                val flow = executeBlock(fn.body, fnScope)
                if (flow is ExecFlow.Return) flow.value else null
            } finally {
                recursionDepth--
            }
        }

        private fun evalIterable(rawExpr: String, scope: MutableMap<String, Any?>): List<Any?> {
            val expr = rawExpr.trim()
            // Python `range(...)`
            val rangeMatch = Regex("""^range\s*\((.+)\)$""").find(expr)
            if (rangeMatch != null) {
                val args = splitTopLevelArgs(rangeMatch.groupValues[1], ',')
                    .map { (evalExpr(it, scope) as? Number)?.toInt() ?: 0 }
                val start = if (args.size == 1) 0 else args[0]
                val stop = if (args.size == 1) args[0] else args[1]
                val step = args.getOrElse(2) { 1 }.takeIf { it != 0 } ?: 1
                val list = mutableListOf<Int>()
                var cur = start
                while ((step > 0 && cur < stop) || (step < 0 && cur > stop)) {
                    list.add(cur)
                    cur += step
                    if (list.size > 2000) break
                }
                return list
            }
            // Kotlin/Rust ranges `a..b`, `a..=b`, `a until b`, `a downTo b`
            val untilMatch = Regex("""^(.+?)\s+until\s+(.+)$""").find(expr)
            if (untilMatch != null) {
                val s = (evalExpr(untilMatch.groupValues[1], scope) as? Number)?.toInt() ?: 0
                val e = (evalExpr(untilMatch.groupValues[2], scope) as? Number)?.toInt() ?: 0
                return (s until e).toList()
            }
            val downMatch = Regex("""^(.+?)\s+downTo\s+(.+)$""").find(expr)
            if (downMatch != null) {
                val s = (evalExpr(downMatch.groupValues[1], scope) as? Number)?.toInt() ?: 0
                val e = (evalExpr(downMatch.groupValues[2], scope) as? Number)?.toInt() ?: 0
                return (s downTo e).toList()
            }
            val incRange = Regex("""^(.+?)\.\.(=?)(.+)$""").find(expr)
            if (incRange != null) {
                val s = (evalExpr(incRange.groupValues[1], scope) as? Number)?.toInt() ?: 0
                val isIncFlag = incRange.groupValues[2] == "=" || language == LanguageType.KOTLIN
                val e = (evalExpr(incRange.groupValues[3], scope) as? Number)?.toInt() ?: 0
                return if (isIncFlag) (s..e).toList() else (s until e).toList()
            }
            return when (val v = evalExpr(expr, scope)) {
                is List<*> -> v
                is String -> v.map { it.toString() }
                else -> emptyList()
            }
        }

        fun evalExpr(rawExpr: String, scope: MutableMap<String, Any?>): Any? {
            val expr = rawExpr.trim()
            if (expr.isEmpty()) return ""

            // Strip outer matching parentheses cleanly
            if (expr.startsWith("(") && expr.endsWith(")") && matchingParenIndex(expr, 0) == expr.lastIndex) {
                return evalExpr(expr.substring(1, expr.length - 1), scope)
            }

            // Logical OR
            splitBinaryTopLevel(expr, listOf("||", " or "))?.let { (l, op, r) ->
                return toBoolean(evalExpr(l, scope)) || toBoolean(evalExpr(r, scope))
            }
            // Logical AND
            splitBinaryTopLevel(expr, listOf("&&", " and "))?.let { (l, op, r) ->
                return toBoolean(evalExpr(l, scope)) && toBoolean(evalExpr(r, scope))
            }
            // Equality
            splitBinaryTopLevel(expr, listOf("==", "!="))?.let { (l, op, r) ->
                val lv = evalExpr(l, scope)
                val rv = evalExpr(r, scope)
                val eq = compareValues(lv, rv) == 0 && valuesEqual(lv, rv)
                return if (op == "==") eq else !eq
            }
            // Comparison
            splitBinaryTopLevel(expr, listOf("<=", ">=", "<", ">"))?.let { (l, op, r) ->
                val cmp = compareValues(evalExpr(l, scope), evalExpr(r, scope))
                return when (op) {
                    "<" -> cmp < 0
                    "<=" -> cmp <= 0
                    ">" -> cmp > 0
                    ">=" -> cmp >= 0
                    else -> false
                }
            }
            // Addition / Subtraction
            splitBinaryRightmost(expr, listOf('+', '-'))?.let { (l, op, r) ->
                val lv = evalExpr(l, scope)
                val rv = evalExpr(r, scope)
                if (op == '+' && (lv is String || rv is String)) {
                    return "${formatValueForPrint(lv, language)}${formatValueForPrint(rv, language)}"
                }
                val ld = toDouble(lv)
                val rd = toDouble(rv)
                val res = if (op == '+') ld + rd else ld - rd
                return simplifyNumber(res, lv is Int && rv is Int)
            }
            // Multiplication / Division / Modulo
            splitBinaryRightmost(expr, listOf('*', '/', '%'))?.let { (l, op, r) ->
                val lv = evalExpr(l, scope)
                val rv = evalExpr(r, scope)
                if (op == '*' && lv is String && rv is Number) {
                    return lv.repeat(rv.toInt().coerceIn(0, 500))
                }
                val ld = toDouble(lv)
                val rd = toDouble(rv)
                return when (op) {
                    '*' -> simplifyNumber(ld * rd, lv is Int && rv is Int)
                    '/' -> {
                        if (rd == 0.0) throw ArithmeticException("Division by zero")
                        if (lv is Int && rv is Int) (lv.toInt() / rv.toInt()) else ld / rd
                    }
                    '%' -> {
                        if (rd == 0.0) throw ArithmeticException("Modulo by zero")
                        simplifyNumber(ld % rd, lv is Int && rv is Int)
                    }
                    else -> 0
                }
            }

            // Unary NOT / minus
            if (expr.startsWith("!") && !expr.startsWith("!=")) {
                return !toBoolean(evalExpr(expr.substring(1), scope))
            }
            if (expr.startsWith("not ")) {
                return !toBoolean(evalExpr(expr.removePrefix("not "), scope))
            }
            if (expr.startsWith("-")) {
                val inner = evalExpr(expr.substring(1), scope)
                return if (inner is Int) -inner else -toDouble(inner)
            }

            // Literals
            if (expr == "true" || expr == "True") return true
            if (expr == "false" || expr == "False") return false
            if (expr == "null" || expr == "None" || expr == "nil") return null
            expr.toIntOrNull()?.let { return it }
            expr.toDoubleOrNull()?.let { return it }

            // String literals & interpolation (`"..."`, `'...'`, `f"..."`)
            if ((expr.startsWith("f\"") && expr.endsWith("\"")) ||
                (expr.startsWith("f'") && expr.endsWith("'")) ||
                (expr.startsWith("\"") && expr.endsWith("\"")) ||
                (expr.startsWith("'") && expr.endsWith("'"))
            ) {
                val isFString = expr.startsWith("f\"") || expr.startsWith("f'")
                val inner = expr.removePrefix("f").let { it.substring(1, it.length - 1) }
                return interpolateString(inner, isFString, scope)
            }

            // List literals `[...]` / `vec![...]` / `listOf(...)` / `mutableListOf(...)`
            if (expr.startsWith("[") && expr.endsWith("]")) {
                val inside = expr.substring(1, expr.length - 1).trim()
                if (inside.isEmpty()) return mutableListOf<Any?>()
                return splitTopLevelArgs(inside, ',').map { evalExpr(it, scope) }.toMutableList()
            }
            val listFnMatch = Regex("""^(?:listOf|mutableListOf|arrayOf|vec!)\s*[(\[](.*)[)\]]$""").find(expr)
            if (listFnMatch != null) {
                val inside = listFnMatch.groupValues[1].trim()
                if (inside.isEmpty()) return mutableListOf<Any?>()
                return splitTopLevelArgs(inside, ',').map { evalExpr(it, scope) }.toMutableList()
            }

            // Stdin input calls (`readln()`, `readLine()`, `input(...)`, `scanner.nextLine()`, `stdin.readLineSync()`)
            if (Regex("""^(?:readln|readLine|input|stdin\.readLineSync|[A-Za-z_][A-Za-z0-9_]*\.next(?:Line|Int|Double))\s*\([^)]*\)$""").matches(expr)) {
                val nextLine = stdinQueue.removeFirstOrNull() ?: "42"
                return if (expr.contains("nextInt")) nextLine.trim().toIntOrNull() ?: 0
                else if (expr.contains("nextDouble")) nextLine.trim().toDoubleOrNull() ?: 0.0
                else nextLine
            }

            // Member access / method calls like `s.uppercase()`, `items.size`, `vec.len()`
            val dotIdx = findLastTopLevelChar(expr, '.')
            if (dotIdx > 0) {
                val receiverExpr = expr.substring(0, dotIdx).trim()
                val member = expr.substring(dotIdx + 1).trim()
                if (!receiverExpr.all { it.isDigit() }) {
                    val receiver = evalExpr(receiverExpr, scope)
                    return evalMember(receiver, member, scope)
                }
            }

            // Index access `arr[i]`
            if (expr.endsWith("]") && expr.contains("[")) {
                val bracketStart = expr.lastIndexOf('[')
                val targetExpr = expr.substring(0, bracketStart).trim()
                val indexExpr = expr.substring(bracketStart + 1, expr.length - 1).trim()
                val target = evalExpr(targetExpr, scope)
                val idx = (evalExpr(indexExpr, scope) as? Number)?.toInt() ?: 0
                return when (target) {
                    is List<*> -> target.getOrNull(idx)
                    is String -> target.getOrNull(idx)?.toString() ?: ""
                    else -> null
                }
            }

            // Function call `fnName(arg1, arg2)`
            val callMatch = Regex("""^([A-Za-z_][A-Za-z0-9_]*)\s*\((.*)\)$""").find(expr)
            if (callMatch != null && expr.indexOf('(') > 0 && matchingParenIndex(expr, expr.indexOf('(')) == expr.lastIndex) {
                val fnName = callMatch.groupValues[1]
                val rawArgs = splitTopLevelArgs(callMatch.groupValues[2], ',')
                val evalArgs = rawArgs.map { evalExpr(it, scope) }
                functions[fnName]?.let { return invokeFunction(it, evalArgs) }
                return evalBuiltinFunction(fnName, evalArgs)
            }

            // Variable lookup
            if (expr in scope) return scope[expr]
            if (expr in capturedVars) return capturedVars[expr]

            return expr
        }

        private fun evalMember(receiver: Any?, member: String, scope: MutableMap<String, Any?>): Any? {
            return when {
                member == "length" || member == "size" -> when (receiver) {
                    is String -> receiver.length
                    is List<*> -> receiver.size
                    else -> 0
                }
                member == "len()" || member == "size()" -> when (receiver) {
                    is String -> receiver.length
                    is List<*> -> receiver.size
                    else -> 0
                }
                member == "uppercase()" || member == "toUpperCase()" || member == "upper()" ->
                    receiver?.toString()?.uppercase() ?: ""
                member == "lowercase()" || member == "toLowerCase()" || member == "lower()" ->
                    receiver?.toString()?.lowercase() ?: ""
                member == "trim()" || member == "strip()" ->
                    receiver?.toString()?.trim() ?: ""
                member == "toInt()" || member == "toIntOrNull()" ->
                    receiver?.toString()?.trim()?.toIntOrNull() ?: 0
                member == "toDouble()" ->
                    receiver?.toString()?.trim()?.toDoubleOrNull() ?: 0.0
                member == "toString()" ->
                    formatValueForPrint(receiver, language)
                member.startsWith("add(") || member.startsWith("append(") || member.startsWith("push(") -> {
                    val argRaw = member.substringAfter("(").substringBeforeLast(")")
                    val arg = evalExpr(argRaw, scope)
                    if (receiver is MutableList<*>) {
                        @Suppress("UNCHECKED_CAST")
                        (receiver as MutableList<Any?>).add(arg)
                    }
                    null
                }
                else -> receiver
            }
        }

        private fun evalBuiltinFunction(name: String, args: List<Any?>): Any? = when (name) {
            "len" -> when (val a = args.firstOrNull()) {
                is String -> a.length
                is List<*> -> a.size
                else -> 0
            }
            "abs" -> {
                val n = args.firstOrNull()
                if (n is Int) abs(n) else abs(toDouble(n))
            }
            "min" -> simplifyNumber(min(toDouble(args.getOrNull(0)), toDouble(args.getOrNull(1))), args.all { it is Int })
            "max" -> simplifyNumber(max(toDouble(args.getOrNull(0)), toDouble(args.getOrNull(1))), args.all { it is Int })
            "sqrt" -> sqrt(toDouble(args.firstOrNull()))
            "pow" -> toDouble(args.getOrNull(0)).pow(toDouble(args.getOrNull(1)))
            "int" -> (args.firstOrNull() as? Number)?.toInt() ?: args.firstOrNull()?.toString()?.trim()?.toIntOrNull() ?: 0
            "float" -> (args.firstOrNull() as? Number)?.toDouble() ?: args.firstOrNull()?.toString()?.trim()?.toDoubleOrNull() ?: 0.0
            "str" -> formatValueForPrint(args.firstOrNull(), language)
            else -> "${name}(${args.joinToString(", ") { formatValueForPrint(it, language) }})"
        }

        private fun interpolateString(
            template: String,
            isPythonFString: Boolean,
            scope: MutableMap<String, Any?>
        ): String {
            val sb = StringBuilder()
            var i = 0
            while (i < template.length) {
                val c = template[i]
                if (c == '$' && i + 1 < template.length) {
                    if (template[i + 1] == '{') {
                        val end = template.indexOf('}', i + 2)
                        if (end > i + 2) {
                            val sub = template.substring(i + 2, end)
                            sb.append(formatValueForPrint(evalExpr(sub, scope), language))
                            i = end + 1
                            continue
                        }
                    } else if (template[i + 1].isLetter() || template[i + 1] == '_') {
                        var end = i + 1
                        while (end < template.length && (template[end].isLetterOrDigit() || template[end] == '_')) end++
                        val varName = template.substring(i + 1, end)
                        val valStr = if (varName in scope) formatValueForPrint(scope[varName], language) else "$$varName"
                        sb.append(valStr)
                        i = end
                        continue
                    }
                } else if (isPythonFString && c == '{') {
                    val end = template.indexOf('}', i + 1)
                    if (end > i + 1) {
                        val sub = template.substring(i + 1, end)
                        sb.append(formatValueForPrint(evalExpr(sub, scope), language))
                        i = end + 1
                        continue
                    }
                }
                sb.append(c)
                i++
            }
            return sb.toString()
        }
    }

    // =========================================================================
    // LOW-LEVEL TOKEN & PRECEDENCE HELPERS
    // =========================================================================

    private fun toBoolean(v: Any?): Boolean = when (v) {
        null -> false
        is Boolean -> v
        is Number -> v.toDouble() != 0.0
        is String -> v.isNotEmpty()
        is List<*> -> v.isNotEmpty()
        else -> true
    }

    private fun toDouble(v: Any?): Double = when (v) {
        is Number -> v.toDouble()
        is String -> v.trim().toDoubleOrNull() ?: 0.0
        is Boolean -> if (v) 1.0 else 0.0
        else -> 0.0
    }

    private fun simplifyNumber(d: Double, preferInt: Boolean): Any {
        return if (preferInt || (d == floor(d) && abs(d) < Int.MAX_VALUE.toDouble())) {
            d.toInt()
        } else {
            d
        }
    }

    private fun valuesEqual(a: Any?, b: Any?): Boolean = when {
        a is Number && b is Number -> a.toDouble() == b.toDouble()
        else -> a == b
    }

    private fun compareValues(a: Any?, b: Any?): Int = when {
        a is Number && b is Number -> a.toDouble().compareTo(b.toDouble())
        a is String && b is String -> a.compareTo(b)
        else -> (a?.toString() ?: "").compareTo(b?.toString() ?: "")
    }

    private fun stripInlineComment(line: String, commentChar: Char): String {
        var inDouble = false
        var inSingle = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"' && !inSingle && (i == 0 || line[i - 1] != '\\')) inDouble = !inDouble
            else if (c == '\'' && !inDouble && (i == 0 || line[i - 1] != '\\')) inSingle = !inSingle
            else if (!inDouble && !inSingle) {
                if (commentChar == '#' && c == '#') return line.substring(0, i)
                if (commentChar == '/' && c == '/' && i + 1 < line.length && line[i + 1] == '/') {
                    return line.substring(0, i)
                }
            }
            i++
        }
        return line
    }

    private fun splitTopLevelArgs(input: String, delimiter: Char): List<String> {
        if (input.isBlank()) return emptyList()
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var paren = 0
        var bracket = 0
        var brace = 0
        var inDouble = false
        var inSingle = false
        for (i in input.indices) {
            val c = input[i]
            if (c == '"' && !inSingle && (i == 0 || input[i - 1] != '\\')) inDouble = !inDouble
            else if (c == '\'' && !inDouble && (i == 0 || input[i - 1] != '\\')) inSingle = !inSingle
            else if (!inDouble && !inSingle) {
                when (c) {
                    '(' -> paren++
                    ')' -> if (paren > 0) paren--
                    '[' -> bracket++
                    ']' -> if (bracket > 0) bracket--
                    '{' -> brace++
                    '}' -> if (brace > 0) brace--
                    delimiter -> if (paren == 0 && bracket == 0 && brace == 0) {
                        result.add(sb.toString().trim())
                        sb.clear()
                        continue
                    }
                }
            }
            sb.append(c)
        }
        result.add(sb.toString().trim())
        return result
    }

    private fun findTopLevelAssignment(text: String): Pair<Int, String>? {
        val ops = listOf("+=", "-=", "*=", "/=", "%=", "=")
        var paren = 0
        var bracket = 0
        var inDouble = false
        var inSingle = false
        for (i in text.indices) {
            val c = text[i]
            if (c == '"' && !inSingle && (i == 0 || text[i - 1] != '\\')) inDouble = !inDouble
            else if (c == '\'' && !inDouble && (i == 0 || text[i - 1] != '\\')) inSingle = !inSingle
            else if (!inDouble && !inSingle) {
                when (c) {
                    '(' -> paren++
                    ')' -> if (paren > 0) paren--
                    '[' -> bracket++
                    ']' -> if (bracket > 0) bracket--
                    else -> if (paren == 0 && bracket == 0) {
                        for (op in ops) {
                            if (text.startsWith(op, i)) {
                                if (op == "=") {
                                    val prev = text.getOrNull(i - 1)
                                    val next = text.getOrNull(i + 1)
                                    if (prev in listOf('=', '!', '<', '>') || next == '=') continue
                                }
                                return i to op
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun matchingParenIndex(s: String, openIdx: Int): Int {
        var depth = 0
        var inDouble = false
        var inSingle = false
        for (i in openIdx until s.length) {
            val c = s[i]
            if (c == '"' && !inSingle && (i == 0 || s[i - 1] != '\\')) inDouble = !inDouble
            else if (c == '\'' && !inDouble && (i == 0 || s[i - 1] != '\\')) inSingle = !inSingle
            else if (!inDouble && !inSingle) {
                if (c == '(') depth++
                else if (c == ')') {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    private fun splitBinaryTopLevel(expr: String, ops: List<String>): Triple<String, String, String>? {
        var paren = 0
        var bracket = 0
        var inDouble = false
        var inSingle = false
        for (i in expr.indices) {
            val c = expr[i]
            if (c == '"' && !inSingle && (i == 0 || expr[i - 1] != '\\')) inDouble = !inDouble
            else if (c == '\'' && !inDouble && (i == 0 || expr[i - 1] != '\\')) inSingle = !inSingle
            else if (!inDouble && !inSingle) {
                when (c) {
                    '(' -> paren++
                    ')' -> if (paren > 0) paren--
                    '[' -> bracket++
                    ']' -> if (bracket > 0) bracket--
                    else -> if (paren == 0 && bracket == 0) {
                        for (op in ops) {
                            if (expr.startsWith(op, i)) {
                                val left = expr.substring(0, i).trim()
                                val right = expr.substring(i + op.length).trim()
                                if (left.isNotEmpty() && right.isNotEmpty()) {
                                    return Triple(left, op, right)
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun splitBinaryRightmost(expr: String, ops: List<Char>): Triple<String, Char, String>? {
        var paren = 0
        var bracket = 0
        var inDouble = false
        var inSingle = false
        for (i in expr.lastIndex downTo 1) {
            val c = expr[i]
            if (c == '"' && !inSingle && expr[i - 1] != '\\') inDouble = !inDouble
            else if (c == '\'' && !inDouble && expr[i - 1] != '\\') inSingle = !inSingle
            else if (!inDouble && !inSingle) {
                when (c) {
                    ')' -> paren++
                    '(' -> if (paren > 0) paren--
                    ']' -> bracket++
                    '[' -> if (bracket > 0) bracket--
                    else -> if (paren == 0 && bracket == 0 && c in ops) {
                        val left = expr.substring(0, i).trim()
                        val right = expr.substring(i + 1).trim()
                        if (left.isNotEmpty() && right.isNotEmpty() &&
                            !left.endsWith("(") && !left.endsWith(",") &&
                            !left.endsWith("+") && !left.endsWith("-") &&
                            !left.endsWith("*") && !left.endsWith("/") && !left.endsWith("=")
                        ) {
                            return Triple(left, c, right)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun findLastTopLevelChar(expr: String, target: Char): Int {
        var paren = 0
        var bracket = 0
        var inDouble = false
        var inSingle = false
        for (i in expr.lastIndex downTo 0) {
            val c = expr[i]
            if (c == '"' && !inSingle && (i == 0 || expr[i - 1] != '\\')) inDouble = !inDouble
            else if (c == '\'' && !inDouble && (i == 0 || expr[i - 1] != '\\')) inSingle = !inSingle
            else if (!inDouble && !inSingle) {
                when (c) {
                    ')' -> paren++
                    '(' -> if (paren > 0) paren--
                    ']' -> bracket++
                    '[' -> if (bracket > 0) bracket--
                    target -> if (paren == 0 && bracket == 0) return i
                }
            }
        }
        return -1
    }
}
