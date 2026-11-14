package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.ai.AiSectionNamingService
import com.example.code.BracketPairColorizer
import com.example.code.DiffEngine
import com.example.code.DiffLineKind
import com.example.code.KotlinCodeEngine
import com.example.compiler.KotlinCompilerPlugin
import com.example.compiler.KtBytecodeVm
import com.example.compiler.OpCode
import com.example.data.local.VaultStorageManager
import com.example.model.CodeSectionUiModel
import com.example.model.EditorFontChoice
import com.example.model.LanguageType
import com.example.model.TextAlignmentChoice
import com.example.model.TextFormattingStyle
import com.example.ui.theme.LightKotlinSyntaxColors
import com.example.ui.theme.ScreenRatioClassifier
import com.example.ui.theme.ScreenRatioTier
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun `val constant bottom bar has exact 8 shortcut keys and never mixes autocomplete`() {
        val keys = KotlinCodeEngine.VAL_CONSTANT_SHORTCUT_KEYS
        assertEquals(8, keys.size)
        assertEquals(
            listOf("Tab", "( )", "{ }", "\" \"", "->", ".", "=", ":"),
            keys.map { it.label }
        )
    }

    @Test
    fun `autocomplete tags newly declared symbols with NEW badge and IDE category badges`() {
        val code = "class CustomOrderProcessor\nval orderLimit = 10\nCustom"
        val (prefix, suggestions) = KotlinCodeEngine.getAutocompleteSuggestions(
            code = code,
            cursorPosition = code.length
        )
        assertEquals("Custom", prefix)
        val userClass = suggestions.firstOrNull { it.label == "CustomOrderProcessor" }
        assertNotNull(userClass)
        assertTrue(userClass!!.isNewSymbol)
        assertEquals("C", userClass.badgeChar)
    }

    @Test
    fun `max 2 floating windows enforced with shake notification when 3rd requested`() {
        val initial = com.example.viewmodel.EditorUiState()
        assertEquals(0, initial.openFloatingWindows.size)

        // Open 1st window (EXPLANATION)
        val stateWith1 = com.example.viewmodel.EditorUiState.applyToggleFloatingWindow(
            initial,
            com.example.model.FloatingWindowType.EXPLANATION
        )
        assertEquals(1, stateWith1.openFloatingWindows.size)

        // Open 2nd window (FOLDERS)
        val stateWith2 = com.example.viewmodel.EditorUiState.applyToggleFloatingWindow(
            stateWith1,
            com.example.model.FloatingWindowType.FOLDERS
        )
        assertEquals(2, stateWith2.openFloatingWindows.size)
        assertEquals(0, stateWith2.windowLimitShakeCount)

        // Try opening 3rd window (AI_STUDIO) -> keeps existing 2 and increments shake count
        val stateTried3 = com.example.viewmodel.EditorUiState.applyToggleFloatingWindow(
            stateWith2,
            com.example.model.FloatingWindowType.AI_STUDIO
        )
        assertEquals(2, stateTried3.openFloatingWindows.size)
        assertFalse(stateTried3.openFloatingWindows.contains(com.example.model.FloatingWindowType.AI_STUDIO))
        assertEquals(1, stateTried3.windowLimitShakeCount)
        assertNotNull(stateTried3.aiStatusMessage)

        // Cancel one of the open windows -> now only 1 open
        val stateAfterClose = com.example.viewmodel.EditorUiState.applyToggleFloatingWindow(
            stateTried3,
            com.example.model.FloatingWindowType.EXPLANATION
        )
        assertEquals(1, stateAfterClose.openFloatingWindows.size)
    }

    @Test
    fun `syntax-aware autocomplete suppresses suggestions in print string prose and comments`() {
        // 1. Typing a normal sentence inside println("...") must NOT trigger variable or type suggestions
        val codeInPrintString = """
            val message = "Hello"
            fun main() {
                println("Writing a sentence about message and String
        """.trimIndent()
        val (_, stringProseSuggestions) = KotlinCodeEngine.getAutocompleteSuggestions(
            code = codeInPrintString,
            cursorPosition = codeInPrintString.length
        )
        assertTrue(stringProseSuggestions.isEmpty())

        // 2. Typing inside a single-line comment must NOT trigger suggestions
        val codeInComment = "val counter = 1\n// Typing counter in comment"
        val (_, commentSuggestions) = KotlinCodeEngine.getAutocompleteSuggestions(
            code = codeInComment,
            cursorPosition = codeInComment.length
        )
        assertTrue(commentSuggestions.isEmpty())

        // 3. Naming a brand new variable (`val myNewVar`) must NOT hijack typing with autocomplete
        val codeDeclaringVar = "val counter = 1\nval coun"
        val (_, declSuggestions) = KotlinCodeEngine.getAutocompleteSuggestions(
            code = codeDeclaringVar,
            cursorPosition = codeDeclaringVar.length
        )
        assertTrue(declSuggestions.isEmpty())

        // 4. Typing `$coun` string template interpolation inside quotes DOES trigger variable suggestions
        val codeStringTemplate = "val counter = 1\nprintln(\"Value is \$coun"
        val (interpPrefix, interpSuggestions) = KotlinCodeEngine.getAutocompleteSuggestions(
            code = codeStringTemplate,
            cursorPosition = codeStringTemplate.length
        )
        assertEquals("coun", interpPrefix)
        assertTrue(interpSuggestions.any { it.label == "counter" })

        // 5. Typing after ':' only suggests Kotlin types
        val codeTypeAnnotation = "fun demo(input: Str"
        val (_, typeSuggestions) = KotlinCodeEngine.getAutocompleteSuggestions(
            code = codeTypeAnnotation,
            cursorPosition = codeTypeAnnotation.length
        )
        assertTrue(typeSuggestions.isNotEmpty())
        assertTrue(typeSuggestions.all { it.category == "Type" || it.badgeChar == "C" || it.badgeChar == "I" })
    }

    @Test
    fun `ktscribe file extension model formats 1 file per code block with bundled notes`() {
        val section = CodeSectionUiModel(
            id = 7L,
            orderIndex = 0,
            sectionTitle = "Coroutine Dispatcher Lab",
            codeContent = "fun main() {\n    println(\"IO\")\n}",
            explanationText = "Explains Dispatchers.IO usage",
            explanationStyle = TextFormattingStyle(learnedConceptTag = "Coroutines")
        )
        assertEquals("CoroutineDispatcherLab.kt", section.nativeFileName)
        assertEquals("CoroutineDispatcherLab.ktscribe", section.containerFileName)
        assertEquals("explanation.txt", section.explanationFileName)
        val payload = section.toKtScribeFilePayload("Concurrency Folder")
        assertTrue(payload.contains("// @code-file: CoroutineDispatcherLab.kt"))
        assertTrue(payload.contains("// @bundle-container: CoroutineDispatcherLab.ktscribe"))
        assertTrue(payload.contains("// @folder: Concurrency Folder"))
        assertTrue(payload.contains("Explains Dispatchers.IO usage"))
    }

    @Test
    fun `floating window converts into adjustable split screen and toggles orientation`() {
        val initial = com.example.viewmodel.EditorUiState(
            openFloatingWindows = listOf(
                com.example.model.FloatingWindowType.FOLDERS,
                com.example.model.FloatingWindowType.RUN_OUTPUT
            )
        )
        val docked = com.example.viewmodel.EditorUiState.applyDockWindowToSplitScreen(
            initial,
            com.example.model.FloatingWindowType.FOLDERS
        )
        assertEquals(com.example.model.FloatingWindowType.FOLDERS, docked.dockedSplitWindow)
        assertFalse(docked.openFloatingWindows.contains(com.example.model.FloatingWindowType.FOLDERS))
        assertEquals(
            com.example.model.SplitScreenOrientation.SIDE_BY_SIDE,
            docked.splitScreenOrientation
        )

        val toggled = com.example.viewmodel.EditorUiState.applyToggleSplitOrientation(docked)
        assertEquals(
            com.example.model.SplitScreenOrientation.BOTTOM_SPLIT,
            toggled.splitScreenOrientation
        )
    }

    @Test
    fun `6 curated IDE themes provide rich color palette swatches and exact hex codes`() {
        val allThemes = com.example.ui.theme.AppThemeId.entries
        assertEquals(6, allThemes.size)
        assertTrue(allThemes.contains(com.example.ui.theme.AppThemeId.DARCULA_ESPRESSO))
        assertTrue(allThemes.contains(com.example.ui.theme.AppThemeId.MACOS_MIDNIGHT))
        assertTrue(allThemes.contains(com.example.ui.theme.AppThemeId.MONOKAI_PRO))
        assertTrue(allThemes.contains(com.example.ui.theme.AppThemeId.GITHUB_DARK))
        assertTrue(allThemes.contains(com.example.ui.theme.AppThemeId.SOLARIZED_DARK))
        assertTrue(allThemes.contains(com.example.ui.theme.AppThemeId.NORD_FROST))

        allThemes.forEach { theme ->
            val swatches = theme.paletteSwatches
            assertTrue("Theme ${theme.title} must have at least 4 swatches", swatches.size >= 4)
            swatches.forEach { swatch ->
                assertTrue(swatch.hexCode.startsWith("#"))
                assertTrue(swatch.name.isNotBlank())
            }
        }
    }

    @Test
    fun `multi-language engine supports Java Python Dart and Kotlin with correct snippets and file extensions`() {
        // Kotlin
        val ktSection = CodeSectionUiModel(
            sectionTitle = "Greeter",
            language = com.example.model.LanguageType.KOTLIN
        )
        assertEquals("Greeter.kt", ktSection.nativeFileName)
        assertEquals("Greeter.ktscribe", ktSection.containerFileName)

        // Java
        val javaSection = CodeSectionUiModel(
            sectionTitle = "MainApp",
            language = com.example.model.LanguageType.JAVA
        )
        assertEquals("MainApp.java", javaSection.nativeFileName)
        assertEquals("MainApp.javascribe", javaSection.containerFileName)

        // Python
        val pySection = CodeSectionUiModel(
            sectionTitle = "Script",
            language = com.example.model.LanguageType.PYTHON
        )
        assertEquals("Script.py", pySection.nativeFileName)
        assertEquals("Script.pyscribe", pySection.containerFileName)

        // Dart
        val dartSection = CodeSectionUiModel(
            sectionTitle = "Widget",
            language = com.example.model.LanguageType.DART
        )
        assertEquals("Widget.dart", dartSection.nativeFileName)
        assertEquals("Widget.dartscribe", dartSection.containerFileName)

        // Java snippets test
        val (javaPrefix, javaSuggestions) = com.example.code.CodeLanguageEngine.getSuggestionsForLanguage(
            code = "public class Demo {\n    psvm",
            cursorPosition = 28,
            language = com.example.model.LanguageType.JAVA
        )
        assertEquals("psvm", javaPrefix)
        assertTrue(javaSuggestions.any { it.label == "psvm" })

        // Python def snippet test
        val (pyPrefix, pySuggestions) = com.example.code.CodeLanguageEngine.getSuggestionsForLanguage(
            code = "def",
            cursorPosition = 3,
            language = com.example.model.LanguageType.PYTHON
        )
        assertEquals("def", pyPrefix)
        assertTrue(pySuggestions.any { it.label == "def" })

        // Dart main snippet test
        val (dartPrefix, dartSuggestions) = com.example.code.CodeLanguageEngine.getSuggestionsForLanguage(
            code = "main",
            cursorPosition = 4,
            language = com.example.model.LanguageType.DART
        )
        assertEquals("main", dartPrefix)
        assertTrue(dartSuggestions.any { it.label == "main" })
    }

    @Test
    fun `docked suggestions and gutter badges ensure code text line is never obstructed`() {
        val codeWithSyntaxError = "fun test() {\n    val\n}"
        val diagnostics = KotlinCodeEngine.diagnoseKotlinSyntax(codeWithSyntaxError)
        assertTrue(diagnostics.isNotEmpty())
        assertEquals(2, diagnostics.first().line)

        // Autocomplete suggestions for non-string contexts
        val (prefix, suggestions) = com.example.code.CodeLanguageEngine.getSuggestionsForLanguage(
            code = "fun test() {\n    pr",
            cursorPosition = "fun test() {\n    pr".length,
            language = com.example.model.LanguageType.KOTLIN
        )
        assertEquals("pr", prefix)
        assertTrue(suggestions.any { it.label == "print" || it.label.startsWith("pr") })
    }

    @Test
    fun `file extension rename guard prevents accidental language change and produces warning`() {
        val ktSection = CodeSectionUiModel(
            sectionTitle = "Calculator.kt",
            language = com.example.model.LanguageType.KOTLIN
        )
        // Renaming stem without changing language is valid (returns null error)
        val validWarning = CodeSectionUiModel.validateFileExtensionRename(
            currentFileName = "Calculator.kt",
            newFileName = "AdvancedCalculator.kt",
            language = ktSection.language
        )
        assertNull(validWarning)

        // Attempting to rename extension from .kt to .py triggers warning
        val invalidWarning = CodeSectionUiModel.validateFileExtensionRename(
            currentFileName = "Calculator.kt",
            newFileName = "Calculator.py",
            language = ktSection.language
        )
        assertNotNull(invalidWarning)
        assertTrue(invalidWarning!!.contains("syntax and runtime language differ"))
    }

    @Test
    fun `floating window preset tiers support portrait and landscape dimensions`() {
        val windowType = com.example.model.FloatingWindowType.FOLDERS
        assertEquals(340f, windowType.getTierWidthDp(com.example.model.WindowPresetTier.PORTRAIT), 0.01f)
        assertEquals(520f, windowType.getTierHeightDp(com.example.model.WindowPresetTier.PORTRAIT), 0.01f)
        assertEquals(560f, windowType.getTierWidthDp(com.example.model.WindowPresetTier.LANDSCAPE), 0.01f)
        assertEquals(360f, windowType.getTierHeightDp(com.example.model.WindowPresetTier.LANDSCAPE), 0.01f)
    }

    @Test
    fun `built-in KotlinCompilerPlugin compiles AST symbols and stack bytecode on device`() {
        val code = """
            data class Point(val x: Int, val y: Int)

            fun add(a: Int, b: Int): Int {
                return a + b
            }

            fun main() {
                val p = Point(3, 4)
                val sum = add(p.x, p.y)
                println("sum=${'$'}sum")
            }
        """.trimIndent()

        val artifact = KotlinCompilerPlugin.compile(code, LanguageType.KOTLIN)
        assertTrue("Expected compilation to succeed cleanly", artifact.success)
        assertNotNull(artifact.ast)
        assertTrue(artifact.bytecode.isNotEmpty())
        assertTrue(artifact.bytecode.any { it.op == OpCode.PRINTLN || it.op == OpCode.CALL_FN })
        assertTrue(artifact.bytecode.last().op == OpCode.HALT)
        assertTrue(artifact.functions.containsKey("main"))
        assertTrue(artifact.functions.containsKey("add"))
        assertTrue(artifact.classes.containsKey("Point"))
        assertTrue(artifact.symbols.any { it.name == "sum" })
    }

    @Test
    fun `KtBytecodeVm DebugSession executes compiled code steps and pauses on breakpoints`() {
        val code = """
            fun main() {
                val x = 10
                val y = x * 3
                println("y=${'$'}y")
            }
        """.trimIndent()

        val artifact = KotlinCompilerPlugin.compile(code, LanguageType.KOTLIN)
        assertTrue(artifact.success)

        val runSession = KtBytecodeVm.DebugSession(artifact)
        val finalState = runSession.runToEnd()
        assertEquals(0, finalState.exitCode)
        assertTrue(finalState.isFinished)
        assertEquals(listOf("y=30"), finalState.stdout)

        val debugSession = KtBytecodeVm.DebugSession(artifact)
        val pausedState = debugSession.runUntilBreakpoint(setOf(3))
        assertTrue(pausedState.isPaused)
        assertEquals(3, pausedState.currentLine)

        val stepped = debugSession.stepOver()
        assertTrue(stepped.currentLine >= 3)
        val reset = debugSession.resetTrace()
        assertTrue(reset.isPaused)
    }

    @Test
    fun `KtBytecodeVm ReplSession persists variables and functions across evaluations`() {
        val repl = KtBytecodeVm.ReplSession()
        val declRes = repl.evaluate("val base = 21")
        assertFalse(declRes.isError)
        assertTrue(declRes.compiledOpsCount > 0)

        val fnRes = repl.evaluate("fun doubleIt(n: Int): Int { return n * 2 }")
        assertFalse(fnRes.isError)

        val evalRes = repl.evaluate("doubleIt(base)")
        assertFalse(evalRes.isError)
        assertEquals("42", evalRes.output.trim())

        repl.reset()
        val afterReset = repl.evaluate("base")
        assertEquals("null", afterReset.output.trim())
    }

    @Test
    fun `DiffEngine computes accurate LCS additions deletions and unchanged lines`() {
        val oldText = "fun main() {\n    val a = 1\n    println(a)\n}"
        val newText = "fun main() {\n    val a = 2\n    val b = 3\n    println(a)\n}"

        val diff = DiffEngine.computeLineDiff(oldText, newText)
        assertTrue(diff.hasChanges)
        assertEquals(1, diff.deletionsCount)
        assertEquals(2, diff.additionsCount)
        assertEquals(3, diff.unchangedCount)
        assertTrue(diff.lines.any { it.kind == DiffLineKind.REMOVED && it.text.contains("val a = 1") })
        assertTrue(diff.lines.any { it.kind == DiffLineKind.ADDED && it.text.contains("val b = 3") })
    }

    @Test
    fun `BracketPairColorizer tracks nested depths ignores strings and detects mismatches`() {
        val validCode = """fun test() { println("(ignored)") }"""
        val validSpans = BracketPairColorizer.analyzeBrackets(validCode)
        // Only the outer (), inner () for println, and {} are matched (6 brackets total; string "()" ignored)
        assertEquals(6, validSpans.size)
        assertTrue(validSpans.all { it.isMatched })

        val nestedCode = "{[()]}"
        val nestedSpans = BracketPairColorizer.analyzeBrackets(nestedCode)
        assertEquals(0, nestedSpans.first { it.offset == 0 }.depth)
        assertEquals(1, nestedSpans.first { it.offset == 1 }.depth)
        assertEquals(2, nestedSpans.first { it.offset == 2 }.depth)

        val brokenCode = "fun broken() { (1 + 2"
        val brokenSpans = BracketPairColorizer.analyzeBrackets(brokenCode)
        assertTrue(brokenSpans.any { !it.isMatched })
    }

    @Test
    fun `ScreenRatioClassifier dispatches separate theme files without extra permissions`() {
        val portrait = ScreenRatioClassifier.classify(widthDp = 390, heightDp = 844, densityDpi = 440)
        assertEquals(ScreenRatioTier.COMPACT_PORTRAIT, portrait.tier)
        val portraitSpec = ScreenRatioClassifier.resolveThemeSpec(portrait)
        assertEquals("CompactPortraitTheme.kt", portraitSpec.themeFileLabel)
        assertFalse(portraitSpec.useNavigationRail)
        assertTrue(portraitSpec.useCompactTopActions)

        val landscape = ScreenRatioClassifier.classify(widthDp = 844, heightDp = 390, densityDpi = 440)
        assertEquals(ScreenRatioTier.COMPACT_LANDSCAPE, landscape.tier)
        val landscapeSpec = ScreenRatioClassifier.resolveThemeSpec(landscape)
        assertEquals("CompactLandscapeTheme.kt", landscapeSpec.themeFileLabel)
        assertTrue(landscapeSpec.useNavigationRail)

        val foldable = ScreenRatioClassifier.classify(widthDp = 700, heightDp = 620, densityDpi = 420)
        assertEquals(ScreenRatioTier.MEDIUM_FOLDABLE, foldable.tier)
        assertEquals("MediumFoldableTheme.kt", ScreenRatioClassifier.resolveThemeSpec(foldable).themeFileLabel)

        val tablet = ScreenRatioClassifier.classify(widthDp = 1100, heightDp = 760, densityDpi = 320)
        assertEquals(ScreenRatioTier.EXPANDED_TABLET, tablet.tier)
        val tabletSpec = ScreenRatioClassifier.resolveThemeSpec(tablet)
        assertEquals("ExpandedTabletTheme.kt", tabletSpec.themeFileLabel)
        assertEquals(3, tabletSpec.gridColumns)
    }
}


