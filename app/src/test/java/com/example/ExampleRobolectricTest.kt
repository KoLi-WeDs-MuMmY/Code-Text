package com.example

import com.example.code.CodeLanguageEngine
import com.example.code.KotlinCodeEngine
import com.example.code.KotlinSuggestion
import com.example.model.CodeSectionUiModel
import com.example.model.DocumentDetailUiModel
import com.example.model.LanguageType
import com.example.model.TextFormattingStyle
import com.example.model.ToolchainPluginItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `apply autocomplete suggestion inserts template and positions cursor`() {
        val initialCode = "fun demo() {\n    pri\n}"
        val cursorAtPri = initialCode.indexOf("pri") + 3
        val suggestion = KotlinSuggestion(
            label = "println",
            insertText = "println(\"\")",
            category = "Stdlib",
            description = "Print line"
        )

        val (updatedCode, newCursor) = KotlinCodeEngine.applySuggestion(
            code = initialCode,
            cursorPosition = cursorAtPri,
            suggestion = suggestion
        )

        assertTrue(updatedCode.contains("println(\"\")"))
        // Cursor should be placed right inside the quotes of println("")
        assertEquals('"', updatedCode[newCursor])
    }

    @Test
    fun `document detail computes accurate word and character counts across sections`() {
        val doc = DocumentDetailUiModel(
            id = 1L,
            title = "Kotlin Architecture Note",
            introText = "Introductory paragraph with five words",
            introStyle = TextFormattingStyle(),
            sections = listOf(
                CodeSectionUiModel(
                    id = 10L,
                    documentId = 1L,
                    orderIndex = 0,
                    codeContent = "val count = 1",
                    explanationText = "Explains the immutable value"
                )
            ),
            updatedAt = 1000L
        )

        // 5 intro words + 4 code tokens + 4 explanation words = 13 total words
        assertEquals(13, doc.totalWordCount)
        assertTrue(doc.totalCharacterCount > 0)
    }

    @Test
    fun `sdk and plugin toolchain manager verifies language sdk installation and debug session`() {
        val catalog = ToolchainPluginItem.defaultCatalog()
        assertTrue(catalog.any { it.id == "sdk_kotlin" && it.isInstalled })
        assertTrue(catalog.any { it.id == "sdk_python" })

        val pythonCode = "def main():\n    count = 42\n    print(\"Result:\", count)\n"
        val uninstalledResult = CodeLanguageEngine.executeAndDebug(
            code = pythonCode,
            language = LanguageType.PYTHON,
            isSdkInstalled = false
        )
        assertFalse(uninstalledResult.toolchainInstalled)
        assertTrue(uninstalledResult.output.contains("not installed"))

        val installedResult = CodeLanguageEngine.executeAndDebug(
            code = pythonCode,
            language = LanguageType.PYTHON,
            isSdkInstalled = true
        )
        assertTrue(installedResult.toolchainInstalled)
        assertEquals("42", installedResult.variables["count"])
        assertTrue(installedResult.callStack.isNotEmpty())
        assertTrue(installedResult.logcatEntries.isNotEmpty())
    }

    @Test
    fun `parameter hint engine detects active function call inside parentheses`() {
        val code = "fun main() {\n    println(\"Hello\")\n}"
        val cursorInsidePrintln = code.indexOf("\"Hello\"")
        val hint = CodeLanguageEngine.getParameterHintForCursor(
            code = code,
            cursorPosition = cursorInsidePrintln,
            language = LanguageType.KOTLIN
        )
        assertNotNull(hint)
        assertTrue(hint!!.contains("println"))
    }

    @Test
    fun `codeassist auto import and optimize imports clean up unused and duplicate imports`() {
        val codeWithMissingImport = "fun main() {\n    val now = UUID.randomUUID()\n}"
        val missing = CodeLanguageEngine.getMissingImportsForLanguage(codeWithMissingImport, LanguageType.KOTLIN)
        assertTrue(missing.any { it.symbol == "UUID" })

        val codeWithImported = CodeLanguageEngine.applyAutoImport(
            code = codeWithMissingImport,
            importStatement = "import java.util.UUID",
            language = LanguageType.KOTLIN
        )
        assertTrue(codeWithImported.contains("import java.util.UUID"))

        val unoptimized = "import java.util.Date\nimport java.util.UUID\nimport java.util.UUID\n\nfun main() {\n    val id = UUID.randomUUID()\n}"
        val optimized = CodeLanguageEngine.optimizeImportsForLanguage(unoptimized, LanguageType.KOTLIN)
        assertTrue(optimized.contains("import java.util.UUID"))
        assertFalse(optimized.contains("import java.util.Date"))
    }

    @Test
    fun `codeassist foldable regions symbol indexer and watch expression evaluator work accurately`() {
        val code = "class Counter {\n    fun increment(step: Int): Int {\n        return step + 1\n    }\n}\n"
        val regions = CodeLanguageEngine.detectFoldableRegions(code, LanguageType.KOTLIN)
        assertTrue(regions.isNotEmpty())

        val symbols = CodeLanguageEngine.extractSymbolsForSection(
            code = code,
            language = LanguageType.KOTLIN,
            sectionId = 100L,
            fileName = "Counter.kt"
        )
        assertTrue(symbols.any { it.name == "Counter" && it.kindBadge == 'C' })
        assertTrue(symbols.any { it.name == "increment" && it.kindBadge == 'f' })

        val cursorOnIncrement = code.indexOf("increment") + 2
        val foundDef = CodeLanguageEngine.findDefinitionAtCursor(code, cursorOnIncrement, symbols)
        assertNotNull(foundDef)
        assertEquals("increment", foundDef!!.name)

        val vars = mapOf("count" to "10", "label" to "\"KtScribe\"")
        assertEquals("25", CodeLanguageEngine.evaluateWatchExpression("count + 15", vars))
        assertEquals("true", CodeLanguageEngine.evaluateWatchExpression("count > 5", vars))
        assertEquals("8", CodeLanguageEngine.evaluateWatchExpression("label.length", vars))
    }

    @Test
    fun `multi language formatter comment toggle and interactive stdin execution work offline`() {
        // 1. Python colon-block indentation formatter
        val unformattedPy = "def compute(x):\nif x > 0:\nreturn x * 2"
        val formattedPy = CodeLanguageEngine.formatCodeForLanguage(unformattedPy, LanguageType.PYTHON)
        val pyLines = formattedPy.lines()
        assertEquals("def compute(x):", pyLines[0])
        assertEquals("    if x > 0:", pyLines[1])
        assertEquals("        return x * 2", pyLines[2])

        // 2. Language-aware line comment toggle (# for Python, // for Java/Kotlin)
        val pyValue = androidx.compose.ui.text.input.TextFieldValue(
            text = "print(\"Hi\")",
            selection = androidx.compose.ui.text.TextRange(0, 5)
        )
        val commentedPy = CodeLanguageEngine.toggleLineCommentForLanguage(pyValue, LanguageType.PYTHON)
        assertTrue(commentedPy.text.startsWith("# "))
        val uncommentedPy = CodeLanguageEngine.toggleLineCommentForLanguage(commentedPy, LanguageType.PYTHON)
        assertEquals("print(\"Hi\")", uncommentedPy.text)

        // 3. Interactive stdin pipe + variable interpolation in Python & Kotlin
        val pyInteractive = "name = input(\"Enter name:\")\nprint(f\"Welcome {name}\")"
        val pyDebug = CodeLanguageEngine.executeAndDebug(
            code = pyInteractive,
            language = LanguageType.PYTHON,
            isSdkInstalled = true,
            stdinInput = "Ada Lovelace"
        )
        assertTrue(pyDebug.output.contains("Ada Lovelace"))
        assertEquals("\"Ada Lovelace\"", pyDebug.variables["name"])

        // 4. Multi-language smart concept tag inference
        val rustSection = CodeSectionUiModel(
            id = 5L,
            documentId = 1L,
            orderIndex = 0,
            codeContent = "fn calculate() {\n    println!(\"Rust\");\n}",
            explanationText = "",
            language = LanguageType.RUST
        )
        assertTrue(rustSection.effectiveLearnedConceptTag.contains("Rust"))
    }

    @Test
    fun `zero server offline ast engine generates multi language docs explanations and refactors`() = kotlinx.coroutines.runBlocking {
        val aiService = com.example.ai.AiSectionNamingService(preferOfflineLocalEngine = true)

        val pyCode = "def greet(user):\n    print(\"Hello\", user)"
        val pyDocs = aiService.addKDocAndCommentsToCode(pyCode, LanguageType.PYTHON).outputText
        assertTrue(pyDocs.contains("\"\"\""))

        val rustCode = "fn main() {\n    println!(\"Hi\");\n}"
        val rustDocs = aiService.addKDocAndCommentsToCode(rustCode, LanguageType.RUST).outputText
        assertTrue(rustDocs.contains("///"))

        val explanation = aiService.generateCodeExplanation(pyCode, "greet.py", 0, LanguageType.PYTHON).outputText
        assertTrue(explanation.contains("Concept Learned"))
        assertTrue(explanation.contains("Python"))
    }
}
