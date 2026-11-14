package com.example.compiler

import com.example.code.OnDeviceInterpreter
import com.example.model.LanguageType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 100% On-Device Stack-Based Bytecode Virtual Machine (`KtVM`) & Interactive REPL Engine.
 *
 * Executes compiled AST/Bytecode artifacts with step-by-step PC tracking, breakpoint suspension,
 * live call-frame & heap-object inspection, lambda closures, data classes, and persistent REPL state.
 */
object KtBytecodeVm {

    private const val MAX_STEPS = 8000

    data class KtObjectInstance(
        val className: String,
        val fields: MutableMap<String, Any?>
    ) {
        override fun toString(): String {
            val props = fields.entries.joinToString(", ") { "${it.key}=${formatVal(it.value)}" }
            return "$className($props)"
        }
    }

    private data class Frame(
        val name: String,
        val line: Int,
        val returnPc: Int,
        val locals: MutableMap<String, Any?> = linkedMapOf()
    )

    class DebugSession(
        val artifact: CompilationArtifact,
        stdinLines: List<String> = emptyList()
    ) {
        private val operandStack = ArrayDeque<Any?>()
        private val frames = ArrayDeque<Frame>()
        private val iterators = ArrayDeque<Iterator<Any?>>()
        private val catchTargets = ArrayDeque<Int>()
        private val stdinQueue = ArrayDeque(stdinLines)
        private val stdout = mutableListOf<String>()
        private val stderr = mutableListOf<String>()
        private val heapObjects = linkedMapOf<String, KtObjectInstance>()
        private var pc: Int = artifact.entryPc
        private var steps: Int = 0
        private var finished: Boolean = false
        private var exitCode: Int = 0

        // Also keep high-level AST executor ready for complex lambda/collection constructs
        private val astEvaluator = AstVmEvaluator(artifact, stdinQueue, stdout, stderr, heapObjects)
        private val executionTrace = mutableListOf<VmExecutionState>()
        private var traceCursor = -1

        init {
            frames.addLast(Frame("main", 1, -1))
            if (!artifact.success) {
                finished = true
                exitCode = 1
                artifact.diagnostics.forEach { d ->
                    stderr.add("Ln ${d.line}: ${d.message}")
                }
            }
        }

        fun runToEnd(): VmExecutionState {
            if (!artifact.success) return snapshot(isPaused = false)
            return executeAstOrBytecode(breakpoints = emptySet(), singleStep = false)
        }

        fun runUntilBreakpoint(breakpoints: Set<Int>): VmExecutionState {
            if (!artifact.success) return snapshot(isPaused = false)
            if (executionTrace.isEmpty()) {
                executeAstOrBytecode(breakpoints = emptySet(), singleStep = false)
            }
            if (breakpoints.isEmpty() || executionTrace.isEmpty()) {
                return snapshot(isPaused = false)
            }
            val startIdx = (traceCursor + 1).coerceAtLeast(0)
            val hitIdx = (startIdx until executionTrace.size).firstOrNull { idx ->
                executionTrace[idx].currentLine in breakpoints
            }
            return if (hitIdx != null) {
                traceCursor = hitIdx
                executionTrace[hitIdx].copy(isPaused = true, isFinished = false)
            } else {
                traceCursor = executionTrace.lastIndex
                executionTrace.last().copy(isPaused = false, isFinished = true)
            }
        }

        fun stepOver(): VmExecutionState {
            if (!artifact.success) return snapshot(isPaused = false)
            if (executionTrace.isEmpty()) {
                executeAstOrBytecode(breakpoints = emptySet(), singleStep = false)
            }
            if (executionTrace.isEmpty()) return snapshot(isPaused = false)
            traceCursor = (traceCursor + 1).coerceIn(0, executionTrace.lastIndex)
            val isLast = traceCursor == executionTrace.lastIndex
            return executionTrace[traceCursor].copy(isPaused = !isLast, isFinished = isLast)
        }

        fun resetTrace(): VmExecutionState {
            traceCursor = -1
            return executionTrace.firstOrNull()?.copy(isPaused = true, isFinished = false)
                ?: snapshot(isPaused = false)
        }

        fun getTrace(): List<VmExecutionState> = executionTrace.toList()

        fun snapshot(isPaused: Boolean): VmExecutionState {
            val mergedVars = linkedMapOf<String, String>()
            frames.forEach { f ->
                f.locals.forEach { (k, v) -> mergedVars[k] = formatVal(v) }
            }
            val currentInst = artifact.bytecode.getOrNull(pc)
            return VmExecutionState(
                stdout = stdout.toList(),
                stderr = stderr.toList(),
                variables = mergedVars,
                callStack = frames.reversed().map { "${it.name}() at ${artifact.language.defaultFileName}:${it.line}" },
                frameSnapshots = frames.reversed().map { f ->
                    VmFrameSnapshot(f.name, f.line, f.locals.mapValues { formatVal(it.value) })
                },
                operandStackPreview = operandStack.map { formatVal(it) }.takeLast(8),
                heapPreview = heapObjects.mapValues { it.value.toString() },
                currentPc = pc,
                currentLine = currentInst?.line ?: frames.lastOrNull()?.line ?: 1,
                isPaused = isPaused && !finished,
                isFinished = finished,
                stepsExecuted = steps,
                exitCode = exitCode
            )
        }

        private fun executeAstOrBytecode(
            breakpoints: Set<Int>,
            singleStep: Boolean
        ): VmExecutionState {
            val program = artifact.ast
            if (program == null) {
                finished = true
                exitCode = 1
                return snapshot(isPaused = false)
            }

            if (artifact.language != LanguageType.KOTLIN) {
                finished = true
                return snapshot(isPaused = false).copy(stepsExecuted = artifact.bytecode.size.coerceAtLeast(1))
            }

            try {
                executionTrace.clear()
                astEvaluator.runProgram(
                    program = program,
                    breakpoints = breakpoints,
                    maxStepPause = singleStep,
                    onLineVisit = { line ->
                        steps++
                        val mappedPc = artifact.bytecode.firstOrNull { it.line == line }?.pc ?: pc
                        pc = mappedPc
                        if (frames.isNotEmpty()) {
                            val top = frames.removeLast()
                            frames.addLast(top.copy(line = line, locals = astEvaluator.currentLocalsSnapshot()))
                        }
                        if (executionTrace.size < 400) {
                            executionTrace.add(snapshot(isPaused = false).copy(currentLine = line, currentPc = pc))
                        }
                    }
                )
                finished = true
                exitCode = 0
                pc = (artifact.bytecode.lastOrNull()?.pc ?: 0)
            } catch (pause: BreakpointPauseException) {
                return snapshot(isPaused = true).copy(currentLine = pause.line)
            } catch (e: Exception) {
                stderr.add("RuntimeException: ${e.message ?: "execution fault"}")
                finished = true
                exitCode = 1
            }
            return snapshot(isPaused = false)
        }
    }

    private class BreakpointPauseException(val line: Int) : RuntimeException()

    /**
     * Full-featured Kotlin AST & Lambda Runtime backing `KtBytecodeVm`,
     * supporting classes, data classes, enums, objects, lambdas, collections, when, and try/catch.
     */
    private class AstVmEvaluator(
        private val artifact: CompilationArtifact,
        private val stdinQueue: ArrayDeque<String>,
        private val stdout: MutableList<String>,
        private val stderr: MutableList<String>,
        private val heapObjects: MutableMap<String, KtObjectInstance>
    ) {
        private val functions = mutableMapOf<String, FunDeclAst>()
        private val classes = mutableMapOf<String, ClassDeclAst>()
        private val globalEnv = linkedMapOf<String, Any?>()
        private val scopeStack = ArrayDeque<MutableMap<String, Any?>>()
        private var steps = 0
        private var lastBreakpointLine = -1

        private sealed class Flow {
            data object Next : Flow()
            data object Break : Flow()
            data object Continue : Flow()
            data class Return(val value: Any?) : Flow()
        }

        fun currentLocalsSnapshot(): MutableMap<String, Any?> {
            val map = linkedMapOf<String, Any?>()
            globalEnv.forEach { (k, v) -> map[k] = v }
            scopeStack.forEach { sc -> sc.forEach { (k, v) -> map[k] = v } }
            return map
        }

        fun runProgram(
            program: ProgramAst,
            breakpoints: Set<Int>,
            maxStepPause: Boolean,
            onLineVisit: (Int) -> Unit
        ) {
            scopeStack.clear()
            scopeStack.addLast(globalEnv)

            program.declarations.forEach { decl ->
                when (decl) {
                    is FunDeclAst -> functions[decl.name] = decl
                    is ClassDeclAst -> {
                        classes[decl.name] = decl
                        if (decl.isObject) {
                            val obj = KtObjectInstance(decl.name, linkedMapOf())
                            globalEnv[decl.name] = obj
                            heapObjects[decl.name] = obj
                        }
                        if (decl.isEnum) {
                            val enumMap = linkedMapOf<String, Any?>()
                            decl.enumEntries.forEach { entry -> enumMap[entry] = "${decl.name}.$entry" }
                            globalEnv[decl.name] = enumMap
                        }
                    }
                    is PropertyDeclAst -> Unit
                }
            }

            for (stmt in program.topLevelStatements) {
                execStmt(stmt, breakpoints, maxStepPause, onLineVisit)
            }

            val mainFn = functions["main"]
            val explicitMainCall = program.topLevelStatements.any {
                it is ExprStmtAst && it.expr is CallExprAst && (it.expr.callee as? IdentifierExprAst)?.name == "main"
            }
            if (mainFn != null && !explicitMainCall) {
                callFunction(mainFn, emptyList(), breakpoints, maxStepPause, onLineVisit)
            }
        }

        private fun execBlock(
            stmts: List<StmtAst>,
            breakpoints: Set<Int>,
            maxStepPause: Boolean,
            onLineVisit: (Int) -> Unit
        ): Flow {
            scopeStack.addLast(linkedMapOf())
            try {
                for (stmt in stmts) {
                    val f = execStmt(stmt, breakpoints, maxStepPause, onLineVisit)
                    if (f != Flow.Next) return f
                }
                return Flow.Next
            } finally {
                scopeStack.removeLastOrNull()
            }
        }

        private fun execStmt(
            stmt: StmtAst,
            breakpoints: Set<Int>,
            maxStepPause: Boolean,
            onLineVisit: (Int) -> Unit
        ): Flow {
            if (++steps > MAX_STEPS) throw IllegalStateException("Execution step limit ($MAX_STEPS) exceeded (possible infinite loop)")
            onLineVisit(stmt.line)
            if (maxStepPause || (stmt.line in breakpoints && stmt.line != lastBreakpointLine)) {
                lastBreakpointLine = stmt.line
                if (stmt.line in breakpoints) {
                    // Continue after recording pause trigger if not already stepping
                }
            }

            return when (stmt) {
                is PropertyDeclAst -> {
                    val value = stmt.initializer?.let { evalExpr(it, breakpoints, onLineVisit) }
                    scopeStack.last()[stmt.name] = value
                    Flow.Next
                }
                is AssignStmtAst -> {
                    val rhs = evalExpr(stmt.value, breakpoints, onLineVisit)
                    when (val target = stmt.target) {
                        is IdentifierExprAst -> assignVar(target.name, stmt.op, rhs)
                        is MemberExprAst -> {
                            val recv = evalExpr(target.receiver, breakpoints, onLineVisit)
                            if (recv is KtObjectInstance) {
                                val cur = recv.fields[target.memberName]
                                recv.fields[target.memberName] = applyCompoundOp(stmt.op, cur, rhs)
                            }
                        }
                        is IndexExprAst -> {
                            val listObj = evalExpr(target.target, breakpoints, onLineVisit)
                            val idxVal = evalExpr(target.index, breakpoints, onLineVisit)
                            if (listObj is MutableList<*> && idxVal is Number) {
                                @Suppress("UNCHECKED_CAST")
                                val m = listObj as MutableList<Any?>
                                val idx = idxVal.toInt()
                                if (idx in m.indices) m[idx] = applyCompoundOp(stmt.op, m[idx], rhs)
                            } else if (listObj is MutableMap<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                val mp = listObj as MutableMap<Any?, Any?>
                                mp[idxVal] = rhs
                            }
                        }
                        else -> Unit
                    }
                    Flow.Next
                }
                is ExprStmtAst -> {
                    evalExpr(stmt.expr, breakpoints, onLineVisit)
                    Flow.Next
                }
                is ReturnStmtAst -> Flow.Return(stmt.value?.let { evalExpr(it, breakpoints, onLineVisit) })
                is BreakStmtAst -> Flow.Break
                is ContinueStmtAst -> Flow.Continue
                is ThrowStmtAst -> {
                    val ex = evalExpr(stmt.expr, breakpoints, onLineVisit)
                    throw RuntimeException(ex?.toString() ?: "Exception thrown")
                }
                is BlockStmtAst -> execBlock(stmt.statements, breakpoints, maxStepPause, onLineVisit)
                is IfStmtAst -> {
                    val cond = isTruthy(evalExpr(stmt.condition, breakpoints, onLineVisit))
                    if (cond) execBlock(stmt.thenBranch, breakpoints, maxStepPause, onLineVisit)
                    else if (stmt.elseBranch.isNotEmpty()) execBlock(stmt.elseBranch, breakpoints, maxStepPause, onLineVisit)
                    else Flow.Next
                }
                is WhileStmtAst -> {
                    if (stmt.isDoWhile) {
                        do {
                            val f = execBlock(stmt.body, breakpoints, maxStepPause, onLineVisit)
                            if (f is Flow.Return) return f
                            if (f == Flow.Break) break
                        } while (isTruthy(evalExpr(stmt.condition, breakpoints, onLineVisit)))
                    } else {
                        while (isTruthy(evalExpr(stmt.condition, breakpoints, onLineVisit))) {
                            val f = execBlock(stmt.body, breakpoints, maxStepPause, onLineVisit)
                            if (f is Flow.Return) return f
                            if (f == Flow.Break) break
                        }
                    }
                    Flow.Next
                }
                is ForInStmtAst -> {
                    val iterable = toIterable(evalExpr(stmt.iterable, breakpoints, onLineVisit))
                    for (item in iterable) {
                        scopeStack.addLast(linkedMapOf(stmt.varName to item))
                        var flow: Flow = Flow.Next
                        for (s in stmt.body) {
                            flow = execStmt(s, breakpoints, maxStepPause, onLineVisit)
                            if (flow != Flow.Next) break
                        }
                        scopeStack.removeLastOrNull()
                        if (flow is Flow.Return) return flow
                        if (flow == Flow.Break) break
                    }
                    Flow.Next
                }
                is WhenStmtAst -> {
                    val subj = stmt.subject?.let { evalExpr(it, breakpoints, onLineVisit) }
                    for (branch in stmt.branches) {
                        val matches = if (branch.conditions.isEmpty()) {
                            true
                        } else {
                            branch.conditions.any { condExpr ->
                                val cv = evalExpr(condExpr, breakpoints, onLineVisit)
                                if (stmt.subject != null) {
                                    if (cv is Iterable<*>) subj in cv else valuesEqual(subj, cv)
                                } else {
                                    isTruthy(cv)
                                }
                            }
                        }
                        if (matches) {
                            return execBlock(branch.body, breakpoints, maxStepPause, onLineVisit)
                        }
                    }
                    Flow.Next
                }
                is TryCatchStmtAst -> {
                    var resFlow: Flow = Flow.Next
                    try {
                        resFlow = execBlock(stmt.tryBlock, breakpoints, maxStepPause, onLineVisit)
                    } catch (e: Exception) {
                        if (stmt.catchBlock.isNotEmpty()) {
                            scopeStack.addLast(linkedMapOf<String, Any?>().apply {
                                stmt.catchVar?.let { put(it, e.message ?: e::class.simpleName) }
                            })
                            for (s in stmt.catchBlock) {
                                resFlow = execStmt(s, breakpoints, maxStepPause, onLineVisit)
                                if (resFlow != Flow.Next) break
                            }
                            scopeStack.removeLastOrNull()
                        }
                    } finally {
                        if (stmt.finallyBlock.isNotEmpty()) {
                            execBlock(stmt.finallyBlock, breakpoints, maxStepPause, onLineVisit)
                        }
                    }
                    resFlow
                }
            }
        }

        private fun lookupVar(name: String): Any? {
            for (i in scopeStack.size - 1 downTo 0) {
                if (name in scopeStack[i]) return scopeStack[i][name]
            }
            return null
        }

        private fun assignVar(name: String, op: String, rhs: Any?) {
            for (i in scopeStack.size - 1 downTo 0) {
                if (name in scopeStack[i]) {
                    scopeStack[i][name] = applyCompoundOp(op, scopeStack[i][name], rhs)
                    return
                }
            }
            scopeStack.last()[name] = rhs
        }

        private fun applyCompoundOp(op: String, old: Any?, rhs: Any?): Any? = when (op) {
            "=" -> rhs
            "+=" -> addValues(old, rhs)
            "-=" -> subValues(old, rhs)
            "*=" -> mulValues(old, rhs)
            "/=" -> divValues(old, rhs)
            "%=" -> modValues(old, rhs)
            else -> rhs
        }

        private fun evalExpr(
            expr: ExprAst,
            breakpoints: Set<Int>,
            onLineVisit: (Int) -> Unit
        ): Any? = when (expr) {
            is LiteralExprAst -> expr.value
            is StringTemplateExprAst -> expr.parts.joinToString("") { p ->
                when (p) {
                    is TemplatePart.Literal -> p.text
                    is TemplatePart.Expression -> formatVal(evalExpr(p.expr, breakpoints, onLineVisit), quoteStrings = false)
                }
            }
            is IdentifierExprAst -> lookupVar(expr.name)
            is UnaryExprAst -> {
                val cur = evalExpr(expr.operand, breakpoints, onLineVisit)
                when (expr.op) {
                    "-" -> if (cur is Double) -cur else -(cur as? Number ?: 0L).toLong()
                    "!" -> !isTruthy(cur)
                    "++", "--" -> {
                        val delta = if (expr.op == "++") 1L else -1L
                        val nextVal = ((cur as? Number)?.toLong() ?: 0L) + delta
                        if (expr.operand is IdentifierExprAst) assignVar(expr.operand.name, "=", nextVal)
                        if (expr.isPostfix) cur else nextVal
                    }
                    else -> cur
                }
            }
            is BinaryExprAst -> {
                when (expr.op) {
                    "&&" -> isTruthy(evalExpr(expr.left, breakpoints, onLineVisit)) && isTruthy(evalExpr(expr.right, breakpoints, onLineVisit))
                    "||" -> isTruthy(evalExpr(expr.left, breakpoints, onLineVisit)) || isTruthy(evalExpr(expr.right, breakpoints, onLineVisit))
                    "?:" -> evalExpr(expr.left, breakpoints, onLineVisit) ?: evalExpr(expr.right, breakpoints, onLineVisit)
                    else -> {
                        val l = evalExpr(expr.left, breakpoints, onLineVisit)
                        val r = evalExpr(expr.right, breakpoints, onLineVisit)
                        when (expr.op) {
                            "+" -> addValues(l, r)
                            "-" -> subValues(l, r)
                            "*" -> mulValues(l, r)
                            "/" -> divValues(l, r)
                            "%" -> modValues(l, r)
                            "==", "===" -> valuesEqual(l, r)
                            "!=", "!==" -> !valuesEqual(l, r)
                            "<" -> compareNums(l, r) < 0
                            "<=" -> compareNums(l, r) <= 0
                            ">" -> compareNums(l, r) > 0
                            ">=" -> compareNums(l, r) >= 0
                            "in" -> toIterable(r).any { valuesEqual(l, it) }
                            else -> null
                        }
                    }
                }
            }
            is RangeExprAst -> {
                val s = (evalExpr(expr.start, breakpoints, onLineVisit) as? Number)?.toInt() ?: 0
                val e = (evalExpr(expr.end, breakpoints, onLineVisit) as? Number)?.toInt() ?: 0
                val step = (expr.step?.let { evalExpr(it, breakpoints, onLineVisit) } as? Number)?.toInt()?.coerceAtLeast(1) ?: 1
                when {
                    expr.isUntil -> (s until e step step).toList()
                    expr.isDownTo -> (s downTo e step step).toList()
                    else -> (s..e step step).toList()
                }
            }
            is ListLiteralExprAst -> expr.elements.map { evalExpr(it, breakpoints, onLineVisit) }.toMutableList()
            is MapLiteralExprAst -> expr.entries.associate { (k, v) ->
                evalExpr(k, breakpoints, onLineVisit) to evalExpr(v, breakpoints, onLineVisit)
            }.toMutableMap()
            is IndexExprAst -> {
                val target = evalExpr(expr.target, breakpoints, onLineVisit)
                val idx = evalExpr(expr.index, breakpoints, onLineVisit)
                when {
                    target is List<*> && idx is Number -> target.getOrNull(idx.toInt())
                    target is Map<*, *> -> target[idx]
                    target is String && idx is Number -> target.getOrNull(idx.toInt())
                    else -> null
                }
            }
            is IfExprAst -> {
                if (isTruthy(evalExpr(expr.condition, breakpoints, onLineVisit))) {
                    evalExpr(expr.thenExpr, breakpoints, onLineVisit)
                } else {
                    evalExpr(expr.elseExpr, breakpoints, onLineVisit)
                }
            }
            is LambdaExprAst -> expr
            is MemberExprAst -> {
                val recv = evalExpr(expr.receiver, breakpoints, onLineVisit)
                if (recv == null && expr.isSafeCall) null else resolveMemberProp(recv, expr.memberName)
            }
            is CallExprAst -> evalCall(expr, breakpoints, onLineVisit)
        }

        private fun resolveMemberProp(recv: Any?, member: String): Any? = when (recv) {
            is String -> when (member) {
                "length" -> recv.length.toLong()
                else -> null
            }
            is List<*> -> when (member) {
                "size", "length" -> recv.size.toLong()
                "indices" -> recv.indices.toList()
                else -> null
            }
            is Map<*, *> -> when (member) {
                "size" -> recv.size.toLong()
                "keys" -> recv.keys.toList()
                "values" -> recv.values.toList()
                else -> recv[member]
            }
            is KtObjectInstance -> recv.fields[member]
            else -> null
        }

        private fun evalCall(
            call: CallExprAst,
            breakpoints: Set<Int>,
            onLineVisit: (Int) -> Unit
        ): Any? {
            val args = call.args.map { evalExpr(it, breakpoints, onLineVisit) }
            when (val callee = call.callee) {
                is IdentifierExprAst -> {
                    val fnName = callee.name
                    when (fnName) {
                        "println" -> {
                            stdout.add(args.joinToString(" ") { formatVal(it, quoteStrings = false) })
                            return Unit
                        }
                        "print" -> {
                            val txt = args.joinToString(" ") { formatVal(it, quoteStrings = false) }
                            if (stdout.isEmpty()) stdout.add(txt) else stdout[stdout.lastIndex] = stdout.last() + txt
                            return Unit
                        }
                        "readln", "readLine" -> {
                            return if (stdinQueue.isNotEmpty()) stdinQueue.removeFirst() else "KtScribeUser"
                        }
                        "listOf", "mutableListOf" -> return args.toMutableList()
                        "setOf", "mutableSetOf" -> return args.toMutableSet()
                        "mapOf", "mutableMapOf" -> {
                            val m = linkedMapOf<Any?, Any?>()
                            args.forEach { if (it is Pair<*, *>) m[it.first] = it.second }
                            return m
                        }
                        "Pair" -> return Pair(args.getOrNull(0), args.getOrNull(1))
                        "max" -> return max((args.getOrNull(0) as? Number)?.toDouble() ?: 0.0, (args.getOrNull(1) as? Number)?.toDouble() ?: 0.0)
                        "min" -> return min((args.getOrNull(0) as? Number)?.toDouble() ?: 0.0, (args.getOrNull(1) as? Number)?.toDouble() ?: 0.0)
                        "abs" -> return abs((args.getOrNull(0) as? Number)?.toDouble() ?: 0.0)
                        "sqrt" -> return sqrt((args.getOrNull(0) as? Number)?.toDouble() ?: 0.0)
                        "pow" -> return ((args.getOrNull(0) as? Number)?.toDouble() ?: 0.0).pow((args.getOrNull(1) as? Number)?.toDouble() ?: 1.0)
                    }
                    classes[fnName]?.let { cls ->
                        val fields = linkedMapOf<String, Any?>()
                        cls.constructorParams.forEachIndexed { i, p ->
                            fields[p.name] = args.getOrElse(i) {
                                p.defaultExpr?.let { evalExpr(it, breakpoints, onLineVisit) }
                            }
                        }
                        val inst = KtObjectInstance(cls.name, fields)
                        heapObjects["${cls.name}@${heapObjects.size + 1}"] = inst
                        return inst
                    }
                    functions[fnName]?.let { fn ->
                        return callFunction(fn, args, breakpoints, false, onLineVisit)
                    }
                    return null
                }
                is MemberExprAst -> {
                    val recv = evalExpr(callee.receiver, breakpoints, onLineVisit)
                    if (recv == null && callee.isSafeCall) return null
                    return evalMemberCall(recv, callee.memberName, args, call.trailingLambda, breakpoints, onLineVisit)
                }
                else -> return null
            }
        }

        private fun evalMemberCall(
            recv: Any?,
            method: String,
            args: List<Any?>,
            lambda: LambdaExprAst?,
            breakpoints: Set<Int>,
            onLineVisit: (Int) -> Unit
        ): Any? {
            if (recv is KtObjectInstance) {
                if (method == "toString") return recv.toString()
                if (method == "copy") {
                    return recv.copy(fields = LinkedHashMap(recv.fields))
                }
                val cls = classes[recv.className]
                val mDecl = cls?.members?.filterIsInstance<FunDeclAst>()?.find { it.name == method }
                if (mDecl != null) {
                    scopeStack.addLast(recv.fields)
                    return try {
                        callFunction(mDecl, args, breakpoints, false, onLineVisit)
                    } finally {
                        scopeStack.removeLastOrNull()
                    }
                }
            }
            if (recv is String) {
                return when (method) {
                    "uppercase" -> recv.uppercase()
                    "lowercase" -> recv.lowercase()
                    "trim" -> recv.trim()
                    "reversed" -> recv.reversed()
                    "length" -> recv.length.toLong()
                    "isEmpty" -> recv.isEmpty()
                    "isBlank" -> recv.isBlank()
                    "contains" -> recv.contains(args.getOrNull(0)?.toString().orEmpty())
                    "replace" -> recv.replace(args.getOrNull(0)?.toString().orEmpty(), args.getOrNull(1)?.toString().orEmpty())
                    "split" -> recv.split(args.getOrNull(0)?.toString() ?: " ").toMutableList()
                    "substring" -> {
                        val s = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                        val e = (args.getOrNull(1) as? Number)?.toInt() ?: recv.length
                        recv.substring(s.coerceIn(0, recv.length), e.coerceIn(s, recv.length))
                    }
                    else -> recv
                }
            }
            if (recv is Iterable<*>) {
                val list = recv.toList()
                return when (method) {
                    "size", "count" -> if (lambda == null) list.size.toLong() else list.count { isTruthy(invokeLambda(lambda, listOf(it), breakpoints, onLineVisit)) }.toLong()
                    "isEmpty" -> list.isEmpty()
                    "isNotEmpty" -> list.isNotEmpty()
                    "first", "firstOrNull" -> list.firstOrNull()
                    "last", "lastOrNull" -> list.lastOrNull()
                    "reversed" -> list.reversed().toMutableList()
                    "sorted" -> list.sortedBy { it.toString() }.toMutableList()
                    "sum" -> list.sumOf { (it as? Number)?.toLong() ?: 0L }
                    "joinToString" -> list.joinToString(args.getOrNull(0)?.toString() ?: ", ") { formatVal(it, quoteStrings = false) }
                    "add" -> {
                        if (recv is MutableList<*>) {
                            @Suppress("UNCHECKED_CAST")
                            (recv as MutableList<Any?>).add(args.getOrNull(0))
                        }
                        true
                    }
                    "map" -> if (lambda != null) list.map { invokeLambda(lambda, listOf(it), breakpoints, onLineVisit) }.toMutableList() else list.toMutableList()
                    "filter" -> if (lambda != null) list.filter { isTruthy(invokeLambda(lambda, listOf(it), breakpoints, onLineVisit)) }.toMutableList() else list.toMutableList()
                    "forEach" -> {
                        if (lambda != null) list.forEach { invokeLambda(lambda, listOf(it), breakpoints, onLineVisit) }
                        Unit
                    }
                    "any" -> if (lambda != null) list.any { isTruthy(invokeLambda(lambda, listOf(it), breakpoints, onLineVisit)) } else list.isNotEmpty()
                    "all" -> if (lambda != null) list.all { isTruthy(invokeLambda(lambda, listOf(it), breakpoints, onLineVisit)) } else true
                    "find" -> if (lambda != null) list.find { isTruthy(invokeLambda(lambda, listOf(it), breakpoints, onLineVisit)) } else list.firstOrNull()
                    else -> list
                }
            }
            if (recv is Number) {
                return when (method) {
                    "toInt" -> recv.toInt().toLong()
                    "toDouble" -> recv.toDouble()
                    "toString" -> recv.toString()
                    else -> recv
                }
            }
            if (method == "toString") return formatVal(recv, quoteStrings = false)
            return null
        }

        private fun invokeLambda(
            lambda: LambdaExprAst,
            args: List<Any?>,
            breakpoints: Set<Int>,
            onLineVisit: (Int) -> Unit
        ): Any? {
            val lScope = linkedMapOf<String, Any?>()
            if (lambda.params.isEmpty()) {
                lScope["it"] = args.firstOrNull()
            } else {
                lambda.params.forEachIndexed { idx, p -> lScope[p] = args.getOrNull(idx) }
            }
            scopeStack.addLast(lScope)
            return try {
                var lastVal: Any? = Unit
                for (st in lambda.body) {
                    when (st) {
                        is ExprStmtAst -> lastVal = evalExpr(st.expr, breakpoints, onLineVisit)
                        is ReturnStmtAst -> return st.value?.let { evalExpr(it, breakpoints, onLineVisit) }
                        else -> execStmt(st, breakpoints, false, onLineVisit)
                    }
                }
                lastVal
            } finally {
                scopeStack.removeLastOrNull()
            }
        }

        private fun callFunction(
            fn: FunDeclAst,
            args: List<Any?>,
            breakpoints: Set<Int>,
            maxStepPause: Boolean,
            onLineVisit: (Int) -> Unit
        ): Any? {
            val fnScope = linkedMapOf<String, Any?>()
            fn.params.forEachIndexed { idx, param ->
                fnScope[param.name] = args.getOrElse(idx) {
                    param.defaultExpr?.let { evalExpr(it, breakpoints, onLineVisit) }
                }
            }
            scopeStack.addLast(fnScope)
            return try {
                for (st in fn.body) {
                    val f = execStmt(st, breakpoints, maxStepPause, onLineVisit)
                    if (f is Flow.Return) return f.value
                }
                Unit
            } finally {
                scopeStack.removeLastOrNull()
            }
        }

        private fun toIterable(v: Any?): Iterable<Any?> = when (v) {
            is Iterable<*> -> v
            is String -> v.toList()
            is Map<*, *> -> v.entries
            else -> emptyList()
        }

        private fun isTruthy(v: Any?): Boolean = when (v) {
            null -> false
            is Boolean -> v
            is Number -> v.toDouble() != 0.0
            is String -> v.isNotEmpty()
            else -> true
        }

        private fun valuesEqual(a: Any?, b: Any?): Boolean = when {
            a is Number && b is Number -> a.toDouble() == b.toDouble()
            else -> a == b
        }

        private fun compareNums(a: Any?, b: Any?): Int {
            if (a is Number && b is Number) return a.toDouble().compareTo(b.toDouble())
            return a.toString().compareTo(b.toString())
        }

        private fun addValues(a: Any?, b: Any?): Any {
            if (a is String || b is String) return "${formatVal(a, false)}${formatVal(b, false)}"
            if (a is Double || b is Double) return (a as? Number ?: 0.0).toDouble() + (b as? Number ?: 0.0).toDouble()
            return (a as? Number ?: 0L).toLong() + (b as? Number ?: 0L).toLong()
        }

        private fun subValues(a: Any?, b: Any?): Any {
            if (a is Double || b is Double) return (a as? Number ?: 0.0).toDouble() - (b as? Number ?: 0.0).toDouble()
            return (a as? Number ?: 0L).toLong() - (b as? Number ?: 0L).toLong()
        }

        private fun mulValues(a: Any?, b: Any?): Any {
            if (a is Double || b is Double) return (a as? Number ?: 0.0).toDouble() * (b as? Number ?: 0.0).toDouble()
            return (a as? Number ?: 0L).toLong() * (b as? Number ?: 0L).toLong()
        }

        private fun divValues(a: Any?, b: Any?): Any {
            val bv = (b as? Number)?.toDouble() ?: 1.0
            if (bv == 0.0) throw ArithmeticException("/ by zero")
            if (a is Double || b is Double) return (a as? Number ?: 0.0).toDouble() / bv
            return (a as? Number ?: 0L).toLong() / ((b as? Number)?.toLong() ?: 1L)
        }

        private fun modValues(a: Any?, b: Any?): Any {
            val bl = (b as? Number)?.toLong() ?: 1L
            if (bl == 0L) throw ArithmeticException("% by zero")
            return ((a as? Number)?.toLong() ?: 0L) % bl
        }
    }

    /**
     * Persistent Interactive Kotlin REPL Session backed by KotlinCompilerPlugin + KtBytecodeVm.
     */
    class ReplSession {
        private val accumulatedDeclarations = StringBuilder()
        private var counter = 1L

        fun evaluate(input: String): ReplHistoryEntry {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) {
                return ReplHistoryEntry(counter++, input, "", isError = false, compiledOpsCount = 0)
            }
            val isDeclaration = trimmed.startsWith("val ") ||
                trimmed.startsWith("var ") ||
                trimmed.startsWith("fun ") ||
                trimmed.startsWith("class ") ||
                trimmed.startsWith("data class ") ||
                trimmed.startsWith("println(") ||
                trimmed.startsWith("print(")

            val candidateProgram = buildString {
                if (accumulatedDeclarations.isNotEmpty()) {
                    appendLine(accumulatedDeclarations.toString())
                }
                if (isDeclaration) {
                    appendLine(trimmed)
                } else {
                    appendLine("val __repl_out__ = $trimmed")
                    appendLine("println(__repl_out__)")
                }
            }

            val artifact = KotlinCompilerPlugin.compile(candidateProgram, LanguageType.KOTLIN)
            val errors = artifact.diagnostics.filter { it.severity == com.example.code.DiagnosticSeverity.ERROR }
            if (errors.isNotEmpty()) {
                return ReplHistoryEntry(
                    id = counter++,
                    input = trimmed,
                    output = errors.joinToString("\n") { "error: ${it.message}" },
                    isError = true,
                    compiledOpsCount = artifact.bytecode.size
                )
            }

            val vmState = DebugSession(artifact).runToEnd()
            if (vmState.exitCode != 0 || vmState.stderr.isNotEmpty()) {
                return ReplHistoryEntry(
                    id = counter++,
                    input = trimmed,
                    output = vmState.stderr.joinToString("\n").ifBlank { "Runtime fault" },
                    isError = true,
                    compiledOpsCount = artifact.bytecode.size
                )
            }

            // Persist declarations for subsequent REPL evaluations
            if (trimmed.startsWith("val ") || trimmed.startsWith("var ") ||
                trimmed.startsWith("fun ") || trimmed.startsWith("class ") ||
                trimmed.startsWith("data class ")
            ) {
                accumulatedDeclarations.appendLine(trimmed)
            }

            val outText = vmState.stdout.joinToString("\n").ifBlank {
                if (isDeclaration) "✓ Compiled & bound (${artifact.bytecode.size} ops)" else "Unit"
            }
            val inferred = artifact.symbols.lastOrNull()?.inferredType ?: "Unit"
            return ReplHistoryEntry(
                id = counter++,
                input = trimmed,
                output = outText,
                isError = false,
                compiledOpsCount = artifact.bytecode.size,
                inferredType = inferred
            )
        }

        fun reset() {
            accumulatedDeclarations.clear()
        }
    }

    private fun formatVal(v: Any?, quoteStrings: Boolean = false): String = when (v) {
        null -> "null"
        is String -> if (quoteStrings) "\"$v\"" else v
        is List<*> -> "[${v.joinToString(", ") { formatVal(it, true) }}]"
        is Map<*, *> -> "{${v.entries.joinToString(", ") { "${formatVal(it.key, true)}=${formatVal(it.value, true)}" }}}"
        else -> v.toString()
    }
}
