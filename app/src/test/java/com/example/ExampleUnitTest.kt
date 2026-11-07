package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.ai.AiSectionNamingService
import com.example.code.KotlinCodeEngine
import com.example.data.local.VaultStorageManager
import com.example.model.CodeSectionUiModel
import com.example.model.EditorFontChoice
import com.example.model.TextAlignmentChoice
import com.example.model.TextFormattingStyle
import com.example.ui.theme.LightKotlinSyntaxColors
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `code block starts at 4 lines minimum and expands automatically`() {
        val emptyBlock = CodeSectionUiModel(codeContent = "val x = 1")
        assertEquals(1, emptyBlock.lineCount)
        assertEquals(4, emptyBlock.visibleLineSlots)

        val sixLineBlock = CodeSectionUiModel(
            codeContent = "line1\nline2\nline3\nline4\nline5\nline6"
        )
        assertEquals(6, sixLineBlock.lineCount)
        assertEquals(6, sixLineBlock.visibleLineSlots)
    }

    @Test
    fun `ide sout postfix abbreviation suggests and expands to println`() {
        val code = "fun main() {\n    sout"
        val (prefix, suggestions) = KotlinCodeEngine.getAutocompleteSuggestions(
            code = code,
            cursorPosition = code.length
        )
        assertEquals("sout", prefix)
        assertTrue(suggestions.any { it.insertText == "println(\"\")" })

        // Applying the sout suggestion replaces 'sout' with 'println("")' and places cursor inside quotes
        val soutSuggestion = suggestions.first { it.insertText == "println(\"\")" }
        val (expandedCode, cursor) = KotlinCodeEngine.applySuggestion(code, code.length, soutSuggestion)
        assertEquals("fun main() {\n    println(\"\")", expandedCode)
        assertEquals("fun main() {\n    println(\"".length, cursor)

        // Pressing Space right after 'sout' also auto-expands to println("")
        val beforeSpace = TextFieldValue("    sout", TextRange(8))
        val afterSpace = TextFieldValue("    sout ", TextRange(9))
        val autoExpanded = KotlinCodeEngine.processIdeTextEdit(beforeSpace, afterSpace)
        assertEquals("    println(\"\")", autoExpanded.text)
    }

    @Test
    fun `hidden vault manager obfuscates and decodes vault payload cleanly`() {
        val sampleJson = """{"folders":[{"title":"Coroutines Lab","sections":2}]}"""
        val encoded = VaultStorageManager.encodeVaultPayload(sampleJson)
        assertTrue(encoded.startsWith("KTSCRIBE_VAULT_V2::"))
        assertFalse(encoded.contains("Coroutines Lab")) // Not readable in plain text

        val decoded = VaultStorageManager.decodeVaultPayload(encoded)
        assertEquals(sampleJson, decoded)
    }

    @Test
    fun `ide auto-completes parenthesis braces brackets and quotes`() {
        val before = TextFieldValue("fun demo", TextRange(8))
        val typedOpenParen = TextFieldValue("fun demo(", TextRange(9))

        val processed = KotlinCodeEngine.processIdeTextEdit(before, typedOpenParen)
        assertEquals("fun demo()", processed.text)
        assertEquals(9, processed.selection.end) // Cursor placed inside ()

        // Typing closing ')' right before existing ')' skips over instead of duplicating
        val typedCloseParen = TextFieldValue("fun demo())", TextRange(10))
        val skipped = KotlinCodeEngine.processIdeTextEdit(processed, typedCloseParen)
        assertEquals("fun demo()", skipped.text)
        assertEquals(10, skipped.selection.end)
    }

    @Test
    fun `ide line operations toggle comments duplicate and move lines`() {
        val initial = TextFieldValue("val a = 1\nval b = 2", TextRange(3))
        val commented = KotlinCodeEngine.toggleLineCommentAtCursor(initial)
        assertTrue(commented.text.startsWith("// val a = 1"))

        val uncommented = KotlinCodeEngine.toggleLineCommentAtCursor(commented)
        assertTrue(uncommented.text.startsWith("val a = 1"))

        val duplicated = KotlinCodeEngine.duplicateLineAtCursor(initial)
        assertEquals(3, duplicated.text.split('\n').size)

        val movedDown = KotlinCodeEngine.moveLineVertically(initial, moveUp = false)
        assertTrue(movedDown.text.startsWith("val b = 2\nval a = 1"))
    }

    @Test
    fun `ide metrics analyzer and surround-with and var-to-val refactoring work`() {
        val code = "var count = 1\nval title = \"KtScribe\"\nfun compute(x: Int) {\n    if (x > 0) println(x)\n}"
        val metrics = KotlinCodeEngine.analyzeCodeMetrics(code)
        assertEquals(1, metrics.functionCount)
        assertEquals(1, metrics.valCount)
        assertEquals(1, metrics.varCount)
        assertEquals(50, metrics.immutabilityPercent)

        val (immutableCode, convertedCount) = KotlinCodeEngine.convertMutableVarsToVal(code)
        assertEquals(1, convertedCount)
        assertTrue(immutableCode.startsWith("val count = 1"))

        val fieldVal = TextFieldValue("println(\" risky \")", TextRange(4))
        val wrappedTry = KotlinCodeEngine.surroundSelectionOrLine(fieldVal, "try_catch")
        assertTrue(wrappedTry.text.contains("try {"))
        assertTrue(wrappedTry.text.contains("catch (e: Exception)"))
    }

    @Test
    fun `apple terminal simulator evaluates println output and syntax diagnostics`() {
        val code = """
            val user = "KtScribe"
            println("Hello, ${'$'}user!")
        """.trimIndent()
        val output = KotlinCodeEngine.simulateKotlinExecution(code)
        assertTrue(output.contains("Hello, KtScribe!"))
        assertTrue(output.contains("Process finished with exit code 0"))
    }

    @Test
    fun `ide detects hex color codes in kotlin snippets`() {
        val code = "val primary = Color(0xFF7F52FF)\nval hexStr = \"#FF6B4A\""
        val colors = KotlinCodeEngine.detectColorCodes(code)
        assertEquals(2, colors.size)
        assertEquals("0xFF7F52FF", colors[0].rawToken)
        assertEquals("#FF6B4A", colors[1].rawToken)
    }

    @Test
    fun `ide syntax checker flags errors typos and unexpanded sout and provides quick fixes`() {
        val badCode = "function greet(name: Sting) {\n    sout\n    println(\"Hello\n}"
        val diagnostics = KotlinCodeEngine.diagnoseKotlinSyntax(badCode)
        assertTrue(diagnostics.isNotEmpty())
        assertTrue(diagnostics.any { it.message.contains("'fun'") })
        assertTrue(diagnostics.any { it.message.contains("String") })
        assertTrue(diagnostics.any { it.message.contains("println(\"\")") })
        assertTrue(diagnostics.any { it.message.contains("Unclosed string literal") })
        assertNotNull(diagnostics.first().fixedCode)
    }

    @Test
    fun `kotlin formatter indents nested blocks and normalizes keyword spacing`() {
        val unformatted = """
            fun calculate(items: List<Int>){
            if(items.isNotEmpty()){
            println(items.size)
            }
            }
        """.trimIndent()

        val formatted = KotlinCodeEngine.formatKotlinCode(unformatted)
        val expected = """
            fun calculate(items: List<Int>) {
                if (items.isNotEmpty()) {
                    println(items.size)
                }
            }
        """.trimIndent()

        assertEquals(expected, formatted)
    }

    @Test
    fun `kotlin syntax highlighting preserves exact character length`() {
        val sample = "// Comment\nfun greet(name: String) = println(\"Hello \$name\")"
        val highlighted = KotlinCodeEngine.highlightKotlinCode(sample, LightKotlinSyntaxColors)
        assertEquals(sample, highlighted.text)
        assertTrue(highlighted.spanStyles.isNotEmpty())
    }

    @Test
    fun `kotlin autocomplete returns relevant suggestions and user symbols`() {
        val code = "val customCounter = 42\ncust"
        val (prefix, suggestions) = KotlinCodeEngine.getAutocompleteSuggestions(
            code = code,
            cursorPosition = code.length
        )
        assertEquals("cust", prefix)
        assertTrue(suggestions.any { it.label == "customCounter" })
    }

    @Test
    fun `text formatting style serializes and deserializes with learnedConceptTag`() {
        val style = TextFormattingStyle(
            fontChoice = EditorFontChoice.MERRIWEATHER,
            fontSizeSp = 20,
            isBold = true,
            isItalic = true,
            isUnderline = true,
            isStrikethrough = false,
            alignment = TextAlignmentChoice.CENTER,
            learnedConceptTag = "Sealed StateFlow"
        )
        val restored = TextFormattingStyle.fromJsonString(style.toJsonString())
        assertEquals(style, restored)
        assertEquals("Sealed StateFlow", restored.learnedConceptTag)
    }

    @Test
    fun `ai studio service computes titles thorough explanations concept tags refactors and kdoc`() = runBlocking {
        val service = AiSectionNamingService(maxRequestsPerMinute = 10)
        val hash1 = service.computeContentHash("fun test() {}", "Explanation")
        val hash2 = service.computeContentHash("fun test() {}", "Explanation")
        assertEquals(hash1, hash2)
        assertEquals(10, service.getRemainingRpmSlots())

        val fallback = service.deriveSmartLocalFallbackTitle(
            code = "suspend fun fetchUserProfile() {}",
            explanation = "Loads user profile from disk",
            sectionNumber = 2
        )
        assertTrue(fallback.contains("Fetch User Profile"))

        val conceptTag = service.deriveLearnedConceptTag(
            code = "sealed interface UiState {\n    data object Loading : UiState\n}",
            explanation = "",
            sectionNumber = 1
        )
        assertEquals("Sealed Hierarchy", conceptTag)

        val localExplain = service.synthesizeLocalCodeExplanation(
            code = "suspend fun syncNotes(items: List<String>) {\n    println(items.size)\n}",
            existingExplanation = ""
        )
        assertTrue(localExplain.contains("Concept Learned"))
        assertTrue(localExplain.contains("Execution & Data Flow"))
        assertTrue(localExplain.contains("Idiomatic Takeaways"))
        assertTrue(localExplain.contains("syncNotes"))

        val localRefactored = service.applyLocalIdiomaticRefactor(
            "function hello() {\nconsole.log(\"hi\");\n}"
        )
        assertTrue(localRefactored.contains("fun hello()"))
        assertTrue(localRefactored.contains("println(\"hi\")"))

        val kdocAdded = service.synthesizeLocalKDocCode(
            code = "fun computeTotal(price: Int): Int {\n    return price * 2\n}"
        )
        assertTrue(kdocAdded.contains("/**"))
    }
}
