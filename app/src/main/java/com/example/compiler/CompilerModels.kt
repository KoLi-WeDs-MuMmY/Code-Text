package com.example.compiler

import com.example.code.KotlinDiagnostic
import com.example.model.LanguageType

enum class TokenType {
    KEYWORD,
    IDENTIFIER,
    INT_LITERAL,
    DOUBLE_LITERAL,
    STRING_LITERAL,
    CHAR_LITERAL,
    BOOLEAN_LITERAL,
    NULL_LITERAL,
    OPERATOR,
    PUNCTUATION,
    NEWLINE,
    EOF
}

data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int,
    val column: Int,
    val startOffset: Int,
    val endOffset: Int
)

sealed interface AstNode {
    val line: Int
}

data class ProgramAst(
    override val line: Int = 1,
    val packageName: String? = null,
    val imports: List<String> = emptyList(),
    val declarations: List<DeclAst> = emptyList(),
    val topLevelStatements: List<StmtAst> = emptyList()
) : AstNode

sealed interface DeclAst : AstNode

data class ParamAst(
    val name: String,
    val typeName: String? = null,
    val defaultExpr: ExprAst? = null
)

data class FunDeclAst(
    override val line: Int,
    val name: String,
    val params: List<ParamAst>,
    val returnType: String?,
    val body: List<StmtAst>
) : DeclAst

data class PropertyDeclAst(
    override val line: Int,
    val isMutable: Boolean,
    val name: String,
    val explicitType: String?,
    val initializer: ExprAst?
) : DeclAst, StmtAst

data class ClassDeclAst(
    override val line: Int,
    val name: String,
    val isData: Boolean = false,
    val isEnum: Boolean = false,
    val isInterface: Boolean = false,
    val isObject: Boolean = false,
    val constructorParams: List<ParamAst> = emptyList(),
    val enumEntries: List<String> = emptyList(),
    val members: List<DeclAst> = emptyList()
) : DeclAst

sealed interface StmtAst : AstNode

data class BlockStmtAst(
    override val line: Int,
    val statements: List<StmtAst>
) : StmtAst

data class ExprStmtAst(
    override val line: Int,
    val expr: ExprAst
) : StmtAst

data class AssignStmtAst(
    override val line: Int,
    val target: ExprAst,
    val op: String,
    val value: ExprAst
) : StmtAst

data class ReturnStmtAst(
    override val line: Int,
    val value: ExprAst?
) : StmtAst

data class BreakStmtAst(override val line: Int) : StmtAst
data class ContinueStmtAst(override val line: Int) : StmtAst

data class ThrowStmtAst(
    override val line: Int,
    val expr: ExprAst
) : StmtAst

data class IfStmtAst(
    override val line: Int,
    val condition: ExprAst,
    val thenBranch: List<StmtAst>,
    val elseBranch: List<StmtAst>
) : StmtAst

data class WhenBranchAst(
    val conditions: List<ExprAst>, // empty == else
    val body: List<StmtAst>
)

data class WhenStmtAst(
    override val line: Int,
    val subject: ExprAst?,
    val branches: List<WhenBranchAst>
) : StmtAst

data class WhileStmtAst(
    override val line: Int,
    val condition: ExprAst,
    val body: List<StmtAst>,
    val isDoWhile: Boolean = false
) : StmtAst

data class ForInStmtAst(
    override val line: Int,
    val varName: String,
    val iterable: ExprAst,
    val body: List<StmtAst>
) : StmtAst

data class TryCatchStmtAst(
    override val line: Int,
    val tryBlock: List<StmtAst>,
    val catchVar: String?,
    val catchBlock: List<StmtAst>,
    val finallyBlock: List<StmtAst>
) : StmtAst

sealed interface ExprAst : AstNode

data class LiteralExprAst(
    override val line: Int,
    val value: Any?
) : ExprAst

data class StringTemplateExprAst(
    override val line: Int,
    val parts: List<TemplatePart>
) : ExprAst

sealed interface TemplatePart {
    data class Literal(val text: String) : TemplatePart
    data class Expression(val expr: ExprAst) : TemplatePart
}

data class IdentifierExprAst(
    override val line: Int,
    val name: String
) : ExprAst

data class UnaryExprAst(
    override val line: Int,
    val op: String,
    val operand: ExprAst,
    val isPostfix: Boolean = false
) : ExprAst

data class BinaryExprAst(
    override val line: Int,
    val left: ExprAst,
    val op: String,
    val right: ExprAst
) : ExprAst

data class RangeExprAst(
    override val line: Int,
    val start: ExprAst,
    val end: ExprAst,
    val isUntil: Boolean = false,
    val isDownTo: Boolean = false,
    val step: ExprAst? = null
) : ExprAst

data class CallExprAst(
    override val line: Int,
    val callee: ExprAst,
    val args: List<ExprAst>,
    val trailingLambda: LambdaExprAst? = null
) : ExprAst

data class MemberExprAst(
    override val line: Int,
    val receiver: ExprAst,
    val memberName: String,
    val isSafeCall: Boolean = false
) : ExprAst

data class IndexExprAst(
    override val line: Int,
    val target: ExprAst,
    val index: ExprAst
) : ExprAst

data class LambdaExprAst(
    override val line: Int,
    val params: List<String>,
    val body: List<StmtAst>
) : ExprAst

data class ListLiteralExprAst(
    override val line: Int,
    val elements: List<ExprAst>,
    val isMutable: Boolean = false
) : ExprAst

data class MapLiteralExprAst(
    override val line: Int,
    val entries: List<Pair<ExprAst, ExprAst>>
) : ExprAst

data class IfExprAst(
    override val line: Int,
    val condition: ExprAst,
    val thenExpr: ExprAst,
    val elseExpr: ExprAst
) : ExprAst

data class SymbolEntry(
    val name: String,
    val kind: String,
    val inferredType: String,
    val line: Int,
    val scope: String,
    val isMutable: Boolean = false
)

enum class OpCode {
    PUSH_CONST,
    PUSH_NULL,
    LOAD_VAR,
    STORE_VAR,
    DECL_VAR,
    ADD,
    SUB,
    MUL,
    DIV,
    MOD,
    NEG,
    NOT,
    CMP_EQ,
    CMP_NEQ,
    CMP_LT,
    CMP_LE,
    CMP_GT,
    CMP_GE,
    AND,
    OR,
    ELVIS,
    RANGE,
    RANGE_UNTIL,
    RANGE_DOWNTO,
    RANGE_STEP,
    BUILD_STRING,
    NEW_LIST,
    NEW_MAP,
    INDEX_GET,
    INDEX_SET,
    GET_PROP,
    SET_PROP,
    CALL_FN,
    CALL_MEMBER,
    MAKE_LAMBDA,
    JMP,
    JMP_IF_FALSE,
    JMP_IF_TRUE,
    ITER_INIT,
    ITER_HAS_NEXT,
    ITER_NEXT,
    TRY_BEGIN,
    TRY_END,
    THROW,
    PRINT,
    PRINTLN,
    READ_STDIN,
    POP,
    DUP,
    RET,
    HALT
}

data class BytecodeInstruction(
    val pc: Int,
    val op: OpCode,
    val operand: String = "",
    val line: Int = 1,
    val comment: String = ""
)

data class CompiledFunctionInfo(
    val name: String,
    val params: List<String>,
    val entryPc: Int,
    val returnType: String,
    val definedLine: Int
)

data class CompiledClassInfo(
    val name: String,
    val isData: Boolean,
    val isEnum: Boolean,
    val isObject: Boolean,
    val fields: List<String>,
    val enumEntries: List<String>,
    val methods: Map<String, CompiledFunctionInfo>
)

data class CompilationArtifact(
    val language: LanguageType,
    val success: Boolean,
    val ast: ProgramAst?,
    val astSummaryLines: List<String>,
    val symbols: List<SymbolEntry>,
    val diagnostics: List<KotlinDiagnostic>,
    val constantPool: List<String>,
    val bytecode: List<BytecodeInstruction>,
    val functions: Map<String, CompiledFunctionInfo>,
    val classes: Map<String, CompiledClassInfo>,
    val entryPc: Int,
    val compileTimeMs: Long,
    val pluginName: String = "KtScribe Built-in Kotlin Compiler Plugin v2.2"
)

data class VmFrameSnapshot(
    val functionName: String,
    val line: Int,
    val locals: Map<String, String>
)

data class VmExecutionState(
    val stdout: List<String> = emptyList(),
    val stderr: List<String> = emptyList(),
    val variables: Map<String, String> = emptyMap(),
    val callStack: List<String> = emptyList(),
    val frameSnapshots: List<VmFrameSnapshot> = emptyList(),
    val operandStackPreview: List<String> = emptyList(),
    val heapPreview: Map<String, String> = emptyMap(),
    val currentPc: Int = 0,
    val currentLine: Int = 1,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val stepsExecuted: Int = 0,
    val exitCode: Int = 0
)

data class ReplHistoryEntry(
    val id: Long,
    val input: String,
    val output: String,
    val isError: Boolean,
    val compiledOpsCount: Int,
    val inferredType: String = "Unit"
)
