package com.example

import com.example.ai.AiSectionNamingService
import com.example.code.KotlinCodeEngine
import com.example.model.CodeSectionUiModel
import com.example.model.EditorFontChoice
import com.example.model.TextAlignmentChoice
import com.example.model.TextFormattingStyle
import com.example.ui.theme.LightKotlinSyntaxColors
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
    fun `text formatting style serializes and deserializes cleanly`() {
        val style = TextFormattingStyle(
            fontChoice = EditorFontChoice.MERRIWEATHER,
            fontSizeSp = 20,
            isBold = true,
            isItalic = true,
            isUnderline = true,
            isStrikethrough = false,
            alignment = TextAlignmentChoice.CENTER
        )
        val restored = TextFormattingStyle.fromJsonString(style.toJsonString())
        assertEquals(style, restored)
    }

    @Test
    fun `ai naming service computes deterministic hashes and local fallback titles`() = runBlocking {
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
        assertEquals("Section 2 • fetchUserProfile", fallback)
    }
}
