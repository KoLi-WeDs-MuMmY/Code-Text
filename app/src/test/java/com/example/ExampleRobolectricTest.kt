package com.example

import com.example.code.KotlinCodeEngine
import com.example.code.KotlinSuggestion
import com.example.model.CodeSectionUiModel
import com.example.model.DocumentDetailUiModel
import com.example.model.TextFormattingStyle
import org.junit.Assert.assertEquals
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
}
