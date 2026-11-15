package com.example.learning

import com.example.code.CodeLanguageEngine
import com.example.compiler.KotlinCompilerPlugin
import com.example.compiler.KtBytecodeVm
import com.example.model.LanguageType

object LessonAutoGrader {

    fun grade(lesson: CodingLesson, studentCode: String): AutoGradeResult {
        val effectiveStdin = lesson.stdinInput
        var exitCode = 0
        var stdoutText = ""
        var variables: Map<String, String> = emptyMap()

        if (lesson.language == LanguageType.KOTLIN) {
            val artifact = KotlinCompilerPlugin.compile(studentCode, LanguageType.KOTLIN)
            val hasCompileErrors = artifact.diagnostics.any {
                it.severity == com.example.code.DiagnosticSeverity.ERROR
            }
            if (!artifact.success || hasCompileErrors) {
                val diagLines = artifact.diagnostics.joinToString("\n") { "Ln ${it.line}: ${it.message}" }
                val failedTests = lesson.assertions.map { assertion ->
                    LessonTestCaseResult(
                        label = assertion.label,
                        passed = false,
                        detail = "Blocked by compile error: ${diagLines.lineSequence().firstOrNull().orEmpty()}"
                    )
                }
                return AutoGradeResult(
                    lessonId = lesson.id,
                    passedCount = 0,
                    totalCount = failedTests.size,
                    testResults = failedTests,
                    earnedXp = 0,
                    summaryBanner = "Compile error: fix syntax before grading.",
                    stdoutPreview = diagLines
                )
            }

            val stdinLines = if (effectiveStdin.isBlank()) emptyList() else effectiveStdin.lines()
            val vmResult = KtBytecodeVm.DebugSession(artifact, stdinLines).runToEnd()
            exitCode = vmResult.exitCode
            stdoutText = vmResult.stdout.joinToString("\n")
            variables = vmResult.variables
            if (vmResult.stderr.isNotEmpty() && stdoutText.isBlank()) {
                stdoutText = vmResult.stderr.joinToString("\n")
            }
        } else {
            val debugResult = CodeLanguageEngine.executeAndDebug(
                code = studentCode,
                language = lesson.language,
                isSdkInstalled = true,
                stdinInput = effectiveStdin
            )
            exitCode = debugResult.exitCode
            stdoutText = debugResult.vmState?.stdout
                ?.joinToString("\n")
                ?.ifBlank { debugResult.output }
                ?: debugResult.output
            variables = debugResult.variables
        }

        val caseResults = lesson.assertions.map { assertion ->
            when (assertion) {
                is LessonTestAssertion.ExitCodeZero -> {
                    val ok = exitCode == 0
                    LessonTestCaseResult(
                        label = assertion.label,
                        passed = ok,
                        detail = if (ok) "Exit code 0" else "Exited with code $exitCode"
                    )
                }
                is LessonTestAssertion.ContainsStdout -> {
                    val ok = stdoutText.contains(assertion.expected)
                    LessonTestCaseResult(
                        label = assertion.label,
                        passed = ok,
                        detail = if (ok) {
                            "Matched \"${assertion.expected}\""
                        } else {
                            "Expected \"${assertion.expected}\", got \"${stdoutText.trim().ifBlank { "<empty>" }}\""
                        }
                    )
                }
                is LessonTestAssertion.VariableEquals -> {
                    val raw = variables[assertion.variableName]?.trim().orEmpty()
                    val normalizedActual = raw.trim('"')
                    val ok = normalizedActual == assertion.expectedValue
                    LessonTestCaseResult(
                        label = assertion.label,
                        passed = ok,
                        detail = if (ok) {
                            "${assertion.variableName} = $normalizedActual"
                        } else {
                            "Expected ${assertion.variableName}=${assertion.expectedValue}, got ${raw.ifBlank { "<undefined>" }}"
                        }
                    )
                }
                is LessonTestAssertion.NonEmptyOutput -> {
                    val ok = stdoutText.trim().isNotEmpty()
                    LessonTestCaseResult(
                        label = assertion.label,
                        passed = ok,
                        detail = if (ok) "${stdoutText.lines().size} output line(s)" else "No stdout produced"
                    )
                }
            }
        }

        val passed = caseResults.count { it.passed }
        val total = caseResults.size
        val allPassed = total > 0 && passed == total
        val earnedXp = if (allPassed) lesson.xpReward else 0
        val banner = if (allPassed) {
            "All $passed/$total checks passed! +${lesson.xpReward} XP"
        } else {
            "$passed/$total checks passed — review failed assertions below."
        }

        return AutoGradeResult(
            lessonId = lesson.id,
            passedCount = passed,
            totalCount = total,
            testResults = caseResults,
            earnedXp = earnedXp,
            summaryBanner = banner,
            stdoutPreview = stdoutText
        )
    }
}
