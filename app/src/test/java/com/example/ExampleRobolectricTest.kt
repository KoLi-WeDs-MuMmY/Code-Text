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
}
