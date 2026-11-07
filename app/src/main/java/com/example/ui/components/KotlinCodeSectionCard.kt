package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.DiagnosticSeverity
import com.example.code.KotlinCodeEngine
import com.example.model.CodeSectionUiModel
import com.example.ui.theme.DarkKotlinSyntaxColors
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Authentic Apple macOS Terminal (cmd / Terminal.app) palette
private val MacTerminalChromeBg = Color(0xFF252830)
private val MacTerminalSubBarBg = Color(0xFF1C1F26)
private val MacTerminalCanvasBg = Color(0xFF121418)
private val MacTerminalGutterBg = Color(0xFF171A20)
private val MacTerminalConsoleBg = Color(0xFF0D0F12)
private val MacTerminalBorder = Color(0xFF343844)
private val MacTrafficRed = Color(0xFFFF5F56)
private val MacTrafficYellow = Color(0xFFFFBD2E)
private val MacTrafficGreen = Color(0xFF27C93F)
private val MacTerminalPromptGreen = Color(0xFF32D74B)
private val MacTerminalCyan = Color(0xFF5AC8FA)
private val MacTerminalMutedText = Color(0xFF8E95A5)
private val MacTerminalBrightText = Color(0xFFE6EDF3)

private data class IdeColorPreset(
    val name: String,
    val hexLiteral: String,
    val color: Color
)

private val IDE_COLOR_PRESETS = listOf(
    IdeColorPreset("Kotlin Violet", "0xFF7F52FF", Color(0xFF7F52FF)),
    IdeColorPreset("Apple Green", "0xFF32D74B", Color(0xFF32D74B)),
    IdeColorPreset("Coral Orange", "0xFFFF6B4A", Color(0xFFFF6B4A)),
    IdeColorPreset("Sky Cyan", "0xFF5AC8FA", Color(0xFF5AC8FA)),
    IdeColorPreset("Warm Amber", "0xFFFFBD2E", Color(0xFFFFBD2E)),
    IdeColorPreset("Crimson Red", "0xFFFF5F56", Color(0xFFFF5F56)),
    IdeColorPreset("Neon Rose", "0xFFFF2D55", Color(0xFFFF2D55)),
    IdeColorPreset("Pure White", "0xFFFFFFFF", Color(0xFFFFFFFF))
)

private data class QuickSymbolKey(
    val label: String,
    val insertText: String,
    val cursorOffsetBack: Int = 0
)

private val QUICK_SYMBOL_KEYS = listOf(
    QuickSymbolKey("Tab", "    ", 0),
    QuickSymbolKey("( )", "()", 1),
    QuickSymbolKey("{ }", "{}", 1),
    QuickSymbolKey("[ ]", "[]", 1),
    QuickSymbolKey("< >", "<>", 1),
    QuickSymbolKey("\" \"", "\"\"", 1),
    QuickSymbolKey("->", " -> ", 0),
    QuickSymbolKey("=", " = ", 0),
    QuickSymbolKey(":", ": ", 0),
    QuickSymbolKey(".", ".", 0),
    QuickSymbolKey("?", "?", 0),
    QuickSymbolKey("val", "val ", 0),
    QuickSymbolKey("fun", "fun ", 0),
    QuickSymbolKey("println", "println()", 1)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KotlinCodeSectionCard(
    section: CodeSectionUiModel,
    isCodeFocused: Boolean,
    isExplanationFocused: Boolean,
    isSingleSectionFocusMode: Boolean = false,
    requestAutoFocus: Boolean,
    onAutoFocusConsumed: () -> Unit,
    onCodeFocusChanged: (Boolean) -> Unit,
    onExplanationFocusChanged: (Boolean) -> Unit,
    onCodeChange: (String) -> Unit,
    onExplanationChange: (String) -> Unit,
    onSectionTitleChange: (String) -> Unit,
    onFormatCode: () -> Unit,
    onRequestAiNaming: () -> Unit,
    onRequestAiExplain: () -> Unit = {},
    onRequestAiRefactor: () -> Unit = {},
    onRequestAiKDoc: () -> Unit = {},
    onToggleFocusMode: () -> Unit = {},
    onToggleDivider: () -> Unit,
    onDeleteSection: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use Dark syntax colors inside the Apple Terminal code block so it always looks crisp like macOS Terminal
    val terminalSyntaxColors = DarkKotlinSyntaxColors
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current
    val codeFocusRequester = remember { FocusRequester() }
    val explanationFocusRequester = remember { FocusRequester() }
    val codeBringIntoViewRequester = remember { BringIntoViewRequester() }
    val explanationBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    var showColorPalettePicker by remember { mutableStateOf(false) }
    var showFindReplaceBar by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var showAiStudioMenu by remember { mutableStateOf(false) }
    var terminalConsoleOutput by remember { mutableStateOf<String?>(null) }
    var isCodeFolded by remember { mutableStateOf(false) }

    // Keep track of text layout results so we can bring the exact active cursor line into view
    // at a comfortable viewing angle above the on-screen keyboard.
    var codeLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var explanationLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    var codeFieldValue by remember(section.id) {
        mutableStateOf(
            TextFieldValue(
                text = section.codeContent,
                selection = TextRange(section.codeContent.length)
            )
        )
    }

    var explanationFieldValue by remember(section.id) {
        mutableStateOf(
            TextFieldValue(
                text = section.explanationText,
                selection = TextRange(section.explanationText.length)
            )
        )
    }

    LaunchedEffect(section.codeContent) {
        if (codeFieldValue.text != section.codeContent) {
            val safeCursor = codeFieldValue.selection.end.coerceIn(0, section.codeContent.length)
            codeFieldValue = TextFieldValue(
                text = section.codeContent,
                selection = TextRange(safeCursor)
            )
        }
    }

    LaunchedEffect(section.explanationText) {
        if (explanationFieldValue.text != section.explanationText) {
            val safeCursor = explanationFieldValue.selection.end.coerceIn(0, section.explanationText.length)
            explanationFieldValue = TextFieldValue(
                text = section.explanationText,
                selection = TextRange(safeCursor)
            )
        }
    }

    // Automatically unfold code block when the user clicks to focus code
    LaunchedEffect(isCodeFocused) {
        if (isCodeFocused && isCodeFolded) {
            isCodeFolded = false
        }
    }

    // Keep the exact cursor line at the optimal viewing angle above the soft keyboard
    fun bringActiveCodeLineIntoView(cursorOffset: Int) {
        coroutineScope.launch {
            try {
                val layout = codeLayoutResult
                if (layout != null && codeFieldValue.text.isNotEmpty()) {
                    val safeOffset = cursorOffset.coerceIn(0, codeFieldValue.text.length)
                    val cursorRect = layout.getCursorRect(safeOffset)
                    val verticalPadPx = with(density) { 56.dp.toPx() }
                    val targetRect = Rect(
                        left = 0f,
                        top = (cursorRect.top - verticalPadPx).coerceAtLeast(0f),
                        right = cursorRect.right + 20f,
                        bottom = cursorRect.bottom + verticalPadPx
                    )
                    codeBringIntoViewRequester.bringIntoView(targetRect)
                } else {
                    codeBringIntoViewRequester.bringIntoView()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun bringActiveExplanationLineIntoView(cursorOffset: Int) {
        coroutineScope.launch {
            try {
                val layout = explanationLayoutResult
                if (layout != null && explanationFieldValue.text.isNotEmpty()) {
                    val safeOffset = cursorOffset.coerceIn(0, explanationFieldValue.text.length)
                    val cursorRect = layout.getCursorRect(safeOffset)
                    val verticalPadPx = with(density) { 56.dp.toPx() }
                    val targetRect = Rect(
                        left = 0f,
                        top = (cursorRect.top - verticalPadPx).coerceAtLeast(0f),
                        right = cursorRect.right + 20f,
                        bottom = cursorRect.bottom + verticalPadPx
                    )
                    explanationBringIntoViewRequester.bringIntoView(targetRect)
                } else {
                    explanationBringIntoViewRequester.bringIntoView()
                }
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(requestAutoFocus) {
        if (requestAutoFocus) {
            try {
                isCodeFolded = false
                codeFocusRequester.requestFocus()
                delay(120)
                bringActiveCodeLineIntoView(codeFieldValue.selection.end)
            } catch (_: Exception) {
            }
            onAutoFocusConsumed()
        }
    }

    // Real-time IDE syntax diagnostics, detected colors, and cursor telemetry
    val diagnostics = remember(codeFieldValue.text) {
        KotlinCodeEngine.diagnoseKotlinSyntax(codeFieldValue.text)
    }
    val detectedColors = remember(codeFieldValue.text) {
        KotlinCodeEngine.detectColorCodes(codeFieldValue.text)
    }
    val cursorInfo = remember(codeFieldValue.text, codeFieldValue.selection.end) {
        KotlinCodeEngine.getCursorPositionInfo(codeFieldValue)
    }

    val syntaxVisualTransformation = remember(
        terminalSyntaxColors,
        codeFieldValue.selection.end,
        diagnostics,
        isCodeFocused
    ) {
        KotlinCodeEngine.KotlinSyntaxVisualTransformation(
            colors = terminalSyntaxColors,
            cursorOffset = if (isCodeFocused) codeFieldValue.selection.end else -1,
            diagnostics = diagnostics
        )
    }

    val (currentPrefix, suggestions) = remember(codeFieldValue.text, codeFieldValue.selection.end) {
        KotlinCodeEngine.getAutocompleteSuggestions(
            code = codeFieldValue.text,
            cursorPosition = codeFieldValue.selection.end
        )
    }

    val visibleLines = section.visibleLineSlots
    val lineHeightDp = 22.dp
    val minCodeEditorHeight = (visibleLines * 22 + 16).dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag("section_container_${section.orderIndex}")
    ) {
        // 1. Optional/Removable Top Section Divider
        if (section.showDivider && !isSingleSectionFocusMode) {
            SectionDividerBar(
                sectionNumber = section.orderIndex + 1,
                onRemoveDivider = onToggleDivider,
                testTag = "top_divider_section_${section.orderIndex}"
            )
        } else if (!section.showDivider && !isSingleSectionFocusMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onToggleDivider,
                    modifier = Modifier.testTag("restore_divider_section_${section.orderIndex}")
                ) {
                    Icon(
                        imageVector = Icons.Default.HorizontalRule,
                        contentDescription = "Restore Section Divider",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Show Section Divider",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // 2. Main Section Card featuring Apple macOS Terminal (`cmd`) Code Window + Paired Explanation
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isCodeFocused || isExplanationFocused) 6.dp else 3.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isCodeFocused || isExplanationFocused) 1.5.dp else 1.dp,
                    color = when {
                        diagnostics.any { it.severity == DiagnosticSeverity.ERROR } -> MacTrafficRed
                        isCodeFocused -> MacTerminalPromptGreen
                        isExplanationFocused -> MaterialTheme.colorScheme.primary
                        else -> MacTerminalBorder
                    },
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // =========================================================================
                // A. APPLE macOS TERMINAL (`cmd`) WINDOW TITLE BAR
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MacTerminalChromeBg)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Iconic macOS Traffic Lights (Red = Delete, Yellow = Fold Code, Green = Focus Mode)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        // Red Traffic Light: Delete Section
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(MacTrafficRed)
                                .clickable(onClick = onDeleteSection)
                                .testTag("delete_section_button_${section.orderIndex}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.65f)
                            )
                        }

                        // Yellow Traffic Light: Minimize / Fold Code Editor (great when writing explanation!)
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(MacTrafficYellow)
                                .clickable { isCodeFolded = !isCodeFolded }
                                .testTag("fold_code_button_${section.orderIndex}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "−",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.65f)
                            )
                        }

                        // Green Traffic Light: Toggle Single-Section Focus Mode
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(MacTrafficGreen)
                                .clickable(onClick = onToggleFocusMode)
                                .testTag("focus_section_button_${section.orderIndex}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.65f)
                            )
                        }
                    }

                    // Center: Apple Terminal Title (`⌘ zsh — section_X.kt — 80×N`)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MacTerminalMutedText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "⌘ kotlin — section_${section.orderIndex + 1}.kt — 80×$visibleLines",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MacTerminalBrightText.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Right: Live Syntax Diagnostic Badge + Focus Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val hasError = diagnostics.any { it.severity == DiagnosticSeverity.ERROR }
                        val statusColor = when {
                            hasError -> MacTrafficRed
                            diagnostics.isNotEmpty() -> MacTrafficYellow
                            else -> MacTerminalPromptGreen
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        hasError -> Icons.Default.ErrorOutline
                                        diagnostics.isNotEmpty() -> Icons.Default.WarningAmber
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (diagnostics.isEmpty()) "0 Err" else "${diagnostics.size} Issue",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = if (isSingleSectionFocusMode) {
                                MacTerminalPromptGreen.copy(alpha = 0.22f)
                            } else {
                                Color.White.copy(alpha = 0.08f)
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onToggleFocusMode)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInFull,
                                    contentDescription = "Toggle Single-Block Focus View",
                                    tint = if (isSingleSectionFocusMode) MacTerminalPromptGreen else MacTerminalMutedText,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isSingleSectionFocusMode) "Focused" else "Focus",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isSingleSectionFocusMode) MacTerminalPromptGreen else MacTerminalMutedText
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // B. SECTION TITLE & AI STUDIO + IDE TOOLBAR
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MacTerminalSubBarBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MacTerminalPromptGreen.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(
                            text = "§${section.orderIndex + 1}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = MacTerminalPromptGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Editable / AI-Generated Section Title
                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = section.sectionTitle,
                            onValueChange = onSectionTitleChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                color = MacTerminalBrightText,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(MacTerminalPromptGreen),
                            decorationBox = { innerTextField ->
                                if (section.sectionTitle.isBlank()) {
                                    Text(
                                        text = if (section.isNamingInProgress) {
                                            "✨ AI Studio processing section…"
                                        } else {
                                            "Untitled Section #${section.orderIndex + 1} (Tap ✨ AI Studio)"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MacTerminalMutedText,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("section_title_input_${section.orderIndex}")
                        )
                    }

                    // ▶ Run in Apple Terminal Simulator button
                    IconButton(
                        onClick = {
                            terminalConsoleOutput = KotlinCodeEngine.simulateKotlinExecution(codeFieldValue.text)
                        },
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("run_kotlin_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run Kotlin Snippet in Terminal",
                            tint = MacTerminalPromptGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // ✨ AI Studio Dropdown Menu (Auto-Name, Explain Code, Refactor & Fix, Add KDoc)
                    Box {
                        IconButton(
                            onClick = { showAiStudioMenu = true },
                            enabled = !section.isNamingInProgress,
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("ai_name_button_${section.orderIndex}")
                        ) {
                            if (section.isNamingInProgress) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = MacTerminalCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Open AI Studio Actions",
                                    tint = MacTerminalCyan,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showAiStudioMenu,
                            onDismissRequest = { showAiStudioMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("✨ AI Auto-Name Section", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Generate semantic section title from code & explanation",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    showAiStudioMenu = false
                                    onRequestAiNaming()
                                },
                                modifier = Modifier.testTag("ai_menu_autoname_${section.orderIndex}")
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("📖 AI Explain Code → Explanation Block", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Write a clear explanation below based on this Kotlin snippet",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    showAiStudioMenu = false
                                    onRequestAiExplain()
                                },
                                modifier = Modifier.testTag("ai_menu_explain_${section.orderIndex}")
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🛠 AI Refactor & Fix Syntax", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Fix syntax issues and upgrade to idiomatic Kotlin",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    showAiStudioMenu = false
                                    onRequestAiRefactor()
                                },
                                modifier = Modifier.testTag("ai_menu_refactor_${section.orderIndex}")
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("💬 AI Add KDoc & Inline Comments", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Insert KDoc headers and helpful code comments",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    showAiStudioMenu = false
                                    onRequestAiKDoc()
                                },
                                modifier = Modifier.testTag("ai_menu_kdoc_${section.orderIndex}")
                            )
                        }
                    }

                    // 🔍 Find & Replace in Code button
                    IconButton(
                        onClick = { showFindReplaceBar = !showFindReplaceBar },
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("find_replace_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Find and Replace in Code",
                            tint = if (showFindReplaceBar) MacTerminalCyan else MacTerminalMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // 🎨 Color Palette Picker button
                    IconButton(
                        onClick = { showColorPalettePicker = !showColorPalettePicker },
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("color_palette_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Insert or Edit Color Code",
                            tint = if (showColorPalettePicker) MacTrafficYellow else MacTerminalMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // { } Format Kotlin Code button
                    IconButton(
                        onClick = onFormatCode,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("format_kotlin_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Format Kotlin Code",
                            tint = MacTrafficYellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Copy Kotlin Code button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(section.codeContent))
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Kotlin Code",
                            tint = MacTerminalMutedText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // =========================================================================
                // C. INTERACTIVE IDE FIND & REPLACE BAR (When Toggled)
                // =========================================================================
                AnimatedVisibility(visible = showFindReplaceBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalChromeBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MacTerminalCanvasBg)
                                .border(1.dp, MacTerminalBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            BasicTextField(
                                value = findQuery,
                                onValueChange = { findQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.5.sp,
                                    color = MacTerminalBrightText
                                ),
                                cursorBrush = SolidColor(MacTerminalPromptGreen),
                                decorationBox = { inner ->
                                    if (findQuery.isEmpty()) {
                                        Text(
                                            text = "Find in code…",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.5.sp,
                                            color = MacTerminalMutedText
                                        )
                                    }
                                    inner()
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MacTerminalCanvasBg)
                                .border(1.dp, MacTerminalBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            BasicTextField(
                                value = replaceQuery,
                                onValueChange = { replaceQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.5.sp,
                                    color = MacTerminalBrightText
                                ),
                                cursorBrush = SolidColor(MacTerminalPromptGreen),
                                decorationBox = { inner ->
                                    if (replaceQuery.isEmpty()) {
                                        Text(
                                            text = "Replace with…",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.5.sp,
                                            color = MacTerminalMutedText
                                        )
                                    }
                                    inner()
                                }
                            )
                        }

                        Surface(
                            color = MacTerminalPromptGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    if (findQuery.isNotEmpty()) {
                                        val (updated, count) = KotlinCodeEngine.replaceInCode(
                                            code = codeFieldValue.text,
                                            findQuery = findQuery,
                                            replaceWith = replaceQuery,
                                            replaceAll = true
                                        )
                                        if (count > 0) {
                                            codeFieldValue = TextFieldValue(
                                                text = updated,
                                                selection = TextRange(updated.length)
                                            )
                                            onCodeChange(updated)
                                        }
                                    }
                                }
                        ) {
                            Text(
                                text = "Replace All",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = MacTerminalPromptGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // =========================================================================
                // D. INTERACTIVE IDE COLOR PALETTE PICKER STRIP (When Toggled)
                // =========================================================================
                AnimatedVisibility(visible = showColorPalettePicker) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalChromeBg)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Color Literal:",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = MacTerminalMutedText
                        )
                        IDE_COLOR_PRESETS.forEach { preset ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MacTerminalCanvasBg)
                                    .border(
                                        width = 1.dp,
                                        color = MacTerminalBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        val insertToken = "Color(${preset.hexLiteral})"
                                        val cursor = codeFieldValue.selection.end.coerceIn(0, codeFieldValue.text.length)
                                        val newText = codeFieldValue.text.substring(0, cursor) +
                                            insertToken +
                                            codeFieldValue.text.substring(cursor)
                                        val newCursor = cursor + insertToken.length
                                        codeFieldValue = TextFieldValue(
                                            text = newText,
                                            selection = TextRange(newCursor)
                                        )
                                        onCodeChange(newText)
                                        showColorPalettePicker = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(preset.color)
                                        .border(0.5.dp, Color.Gray, CircleShape)
                                )
                                Text(
                                    text = preset.name,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = MacTerminalBrightText
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // E. APPLE TERMINAL SHELL PROMPT BANNER (`ktscribe@macbook ~ %`)
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MacTerminalCanvasBg)
                        .clickable {
                            if (isCodeFolded) {
                                isCodeFolded = false
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ktscribe@macbook",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MacTerminalPromptGreen
                        )
                        Text(
                            text = " ~ % ",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MacTerminalCyan
                        )
                        Text(
                            text = "kotlinc section_${section.orderIndex + 1}.kt",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = MacTerminalMutedText
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isCodeFolded) {
                            Surface(
                                color = MacTrafficYellow.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable { isCodeFolded = false }
                            ) {
                                Text(
                                    text = "▸ Expand (${section.lineCount} lines)",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = MacTrafficYellow,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Ln ${cursorInfo.line}, Col ${cursorInfo.column}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                color = MacTerminalMutedText
                            )
                        }
                    }
                }

                HorizontalDivider(color = MacTerminalBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                // =========================================================================
                // F. AUTO-EXPANDING 4-LINE KOTLIN CODE BLOCK EDITOR (APPLE CMD CANVAS)
                // =========================================================================
                AnimatedVisibility(visible = !isCodeFolded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalCanvasBg)
                            .heightIn(min = minCodeEditorHeight)
                            .bringIntoViewRequester(codeBringIntoViewRequester)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Line Numbers Gutter with per-line error/warning dot indicator
                        Column(
                            modifier = Modifier
                                .width(44.dp)
                                .background(MacTerminalGutterBg.copy(alpha = 0.45f))
                                .padding(start = 6.dp, end = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (lineNum in 1..visibleLines) {
                                val lineDiag = diagnostics.firstOrNull { it.line == lineNum }
                                val isCurrentCursorLine = isCodeFocused && cursorInfo.line == lineNum
                                Row(
                                    modifier = Modifier.height(lineHeightDp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (lineDiag != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (lineDiag.severity == DiagnosticSeverity.ERROR) {
                                                        MacTrafficRed
                                                    } else {
                                                        MacTrafficYellow
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = lineNum.toString(),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 12.sp,
                                        lineHeight = 22.sp,
                                        fontWeight = if (isCurrentCursorLine) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            lineDiag != null -> MacTrafficRed
                                            isCurrentCursorLine -> MacTerminalPromptGreen
                                            else -> MacTerminalMutedText.copy(alpha = 0.65f)
                                        }
                                    )
                                }
                            }
                        }

                        // Vertical Gutter Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height((visibleLines * 22).dp)
                                .background(MacTerminalBorder)
                        )

                        // Kotlin Code Input with Live Syntax Highlighting, Auto-Closing Pairs & Smart Angle Tracking
                        BasicTextField(
                            value = codeFieldValue,
                            onValueChange = { incomingValue ->
                                val processed = KotlinCodeEngine.processIdeTextEdit(
                                    previous = codeFieldValue,
                                    incoming = incomingValue
                                )
                                codeFieldValue = processed
                                onCodeChange(processed.text)
                                bringActiveCodeLineIntoView(processed.selection.end)
                            },
                            onTextLayout = { layoutResult ->
                                codeLayoutResult = layoutResult
                            },
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.5.sp,
                                lineHeight = 22.sp,
                                color = MacTerminalBrightText
                            ),
                            visualTransformation = syntaxVisualTransformation,
                            cursorBrush = SolidColor(MacTerminalPromptGreen),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = (visibleLines * 22).dp)
                                        .padding(horizontal = 10.dp)
                                ) {
                                    if (codeFieldValue.text.isEmpty()) {
                                        Text(
                                            text = "// Apple Terminal Kotlin IDE (4 lines min, auto-expands)\nfun main() {\n    println(\"Hello from KtScribe!\")\n}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 13.sp,
                                            lineHeight = 22.sp,
                                            color = MacTerminalMutedText.copy(alpha = 0.55f)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(codeFocusRequester)
                                .onFocusChanged { focusState ->
                                    onCodeFocusChanged(focusState.isFocused)
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            delay(120)
                                            bringActiveCodeLineIntoView(codeFieldValue.selection.end)
                                        }
                                    }
                                }
                                .testTag("kotlin_code_input_${section.orderIndex}")
                        )
                    }
                }

                // =========================================================================
                // G. APPLE TERMINAL EXECUTION CONSOLE (`▶ Run` Output)
                // =========================================================================
                AnimatedVisibility(visible = terminalConsoleOutput != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalConsoleBg)
                            .border(0.5.dp, MacTerminalBorder)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("terminal_console_output_${section.orderIndex}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(MacTerminalPromptGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TERMINAL OUTPUT — zsh",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MacTerminalPromptGreen
                                )
                            }
                            IconButton(
                                onClick = { terminalConsoleOutput = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Terminal Output",
                                    tint = MacTerminalMutedText,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = terminalConsoleOutput.orEmpty(),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp,
                            color = MacTerminalBrightText
                        )
                    }
                }

                // =========================================================================
                // H. INLINE DETECTED COLOR SWATCH BAR
                // =========================================================================
                AnimatedVisibility(visible = detectedColors.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalSubBarBg)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("color_swatches_bar_${section.orderIndex}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Colors:",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            color = MacTerminalMutedText
                        )
                        detectedColors.forEachIndexed { idx, detected ->
                            val swatchColor = Color(detected.colorInt)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MacTerminalCanvasBg)
                                    .border(
                                        width = 1.dp,
                                        color = MacTerminalBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        val nextPreset = IDE_COLOR_PRESETS[(idx + 1) % IDE_COLOR_PRESETS.size]
                                        val replacement = if (detected.rawToken.startsWith("#")) {
                                            "#" + nextPreset.hexLiteral.removePrefix("0xFF")
                                        } else {
                                            nextPreset.hexLiteral
                                        }
                                        val updated = codeFieldValue.text.replaceRange(
                                            detected.startOffset.coerceIn(0, codeFieldValue.text.length),
                                            detected.endOffset.coerceIn(0, codeFieldValue.text.length),
                                            replacement
                                        )
                                        codeFieldValue = TextFieldValue(
                                            text = updated,
                                            selection = TextRange(detected.startOffset + replacement.length)
                                        )
                                        onCodeChange(updated)
                                    }
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(11.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(swatchColor)
                                        .border(0.5.dp, Color.Gray, RoundedCornerShape(3.dp))
                                )
                                Text(
                                    text = "${detected.rawToken} (L${detected.line})",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    color = MacTerminalBrightText
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // I. REAL-TIME SYNTAX DIAGNOSTICS & 1-TAP QUICK-FIX PANEL
                // =========================================================================
                AnimatedVisibility(visible = diagnostics.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF28161B))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("syntax_diagnostics_panel_${section.orderIndex}"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        diagnostics.take(3).forEach { diag ->
                            val isErr = diag.severity == DiagnosticSeverity.ERROR
                            val accent = if (isErr) MacTrafficRed else MacTrafficYellow
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isErr) {
                                            Icons.Default.ErrorOutline
                                        } else {
                                            Icons.Default.WarningAmber
                                        },
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ln ${diag.line}: ${diag.message}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = MacTerminalBrightText
                                    )
                                }

                                if (diag.quickFixLabel != null && diag.fixedCode != null) {
                                    Surface(
                                        color = MacTerminalPromptGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                val fixed = diag.fixedCode
                                                codeFieldValue = TextFieldValue(
                                                    text = fixed,
                                                    selection = TextRange(fixed.length)
                                                )
                                                onCodeChange(fixed)
                                            }
                                            .testTag("quick_fix_button_${section.orderIndex}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Build,
                                                contentDescription = null,
                                                tint = MacTerminalPromptGreen,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = diag.quickFixLabel,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.5.sp,
                                                color = MacTerminalPromptGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // J. IDE QUICK-ACTIONS, SYMBOL STRIP & AUTOCOMPLETE (When Code Focused)
                // =========================================================================
                AnimatedVisibility(visible = isCodeFocused && !isCodeFolded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalChromeBg)
                    ) {
                        // 1. IDE Line Operations + Quick Symbol Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // ⌘/ Toggle Line Comment chip
                            IdeActionChip(
                                label = "⌘/ Comment",
                                accentColor = MacTerminalCyan,
                                onClick = {
                                    val updated = KotlinCodeEngine.toggleLineCommentAtCursor(codeFieldValue)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                }
                            )

                            // ⌘D Duplicate Line chip
                            IdeActionChip(
                                label = "⌘D Dup Line",
                                accentColor = MacTerminalPromptGreen,
                                onClick = {
                                    val updated = KotlinCodeEngine.duplicateLineAtCursor(codeFieldValue)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                }
                            )

                            // ▲ Move Line Up
                            IdeActionChip(
                                label = "▲ Line",
                                accentColor = MacTrafficYellow,
                                onClick = {
                                    val updated = KotlinCodeEngine.moveLineVertically(codeFieldValue, moveUp = true)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                }
                            )

                            // ▼ Move Line Down
                            IdeActionChip(
                                label = "▼ Line",
                                accentColor = MacTrafficYellow,
                                onClick = {
                                    val updated = KotlinCodeEngine.moveLineVertically(codeFieldValue, moveUp = false)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                }
                            )

                            // Quick Symbol Keys
                            QUICK_SYMBOL_KEYS.forEach { sym ->
                                Surface(
                                    color = MacTerminalCanvasBg,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = 1.dp,
                                            color = MacTerminalBorder,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            val cur = codeFieldValue.selection.end.coerceIn(0, codeFieldValue.text.length)
                                            val updated = codeFieldValue.text.substring(0, cur) +
                                                sym.insertText +
                                                codeFieldValue.text.substring(cur)
                                            val nextCur = (cur + sym.insertText.length - sym.cursorOffsetBack)
                                                .coerceIn(0, updated.length)
                                            codeFieldValue = TextFieldValue(
                                                text = updated,
                                                selection = TextRange(nextCur)
                                            )
                                            onCodeChange(updated)
                                            bringActiveCodeLineIntoView(nextCur)
                                        }
                                ) {
                                    Text(
                                        text = sym.label,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MacTerminalBrightText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // 2. Contextual Kotlin Autocomplete Suggestions
                        if (suggestions.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                                    .testTag("autocomplete_bar_${section.orderIndex}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (currentPrefix.isNotBlank()) "Complete ($currentPrefix):" else "Snippets:",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    color = MacTerminalMutedText
                                )
                                suggestions.forEach { suggestion ->
                                    AssistChip(
                                        onClick = {
                                            val (newCode, newCursor) = KotlinCodeEngine.applySuggestion(
                                                code = codeFieldValue.text,
                                                cursorPosition = codeFieldValue.selection.end,
                                                suggestion = suggestion
                                            )
                                            codeFieldValue = TextFieldValue(
                                                text = newCode,
                                                selection = TextRange(newCursor)
                                            )
                                            onCodeChange(newCode)
                                            bringActiveCodeLineIntoView(newCursor)
                                        },
                                        label = {
                                            Text(
                                                text = suggestion.label,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 11.sp,
                                                color = MacTerminalBrightText
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MacTerminalCanvasBg
                                        ),
                                        modifier = Modifier.height(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MacTerminalBorder, thickness = 1.dp)

                // =========================================================================
                // K. PAIRED EXPLANATION RICH TEXT EDITOR (WITH ANGLE-CORRECT SCROLLING & AI)
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .bringIntoViewRequester(explanationBringIntoViewRequester)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "EXPLANATION • SECTION #${section.orderIndex + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 1-tap AI Explain button directly on the Explanation Block header
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(
                                        enabled = !section.isNamingInProgress,
                                        onClick = onRequestAiExplain
                                    )
                                    .testTag("ai_explain_button_${section.orderIndex}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Explain Code",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "AI Explain",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Fold/Unfold Code above button when writing explanation so the explanation sits at top eye-level
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isCodeFolded = !isCodeFolded }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCodeFolded) {
                                            Icons.Default.KeyboardArrowDown
                                        } else {
                                            Icons.Default.KeyboardArrowUp
                                        },
                                        contentDescription = "Toggle Code Visibility for Better Explanation Angle",
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (isCodeFolded) "Show Code" else "Fold Code",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Text(
                                text = "${section.explanationStyle.fontChoice.badge} • ${section.explanationStyle.fontSizeSp}sp",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val expStyle = section.explanationStyle
                    val textDecorations = buildList {
                        if (expStyle.isUnderline) add(TextDecoration.Underline)
                        if (expStyle.isStrikethrough) add(TextDecoration.LineThrough)
                    }
                    val combinedDecoration = if (textDecorations.isEmpty()) {
                        TextDecoration.None
                    } else {
                        TextDecoration.combine(textDecorations)
                    }

                    BasicTextField(
                        value = explanationFieldValue,
                        onValueChange = { updatedValue ->
                            explanationFieldValue = updatedValue
                            onExplanationChange(updatedValue.text)
                            bringActiveExplanationLineIntoView(updatedValue.selection.end)
                        },
                        onTextLayout = { layoutResult ->
                            explanationLayoutResult = layoutResult
                        },
                        textStyle = TextStyle(
                            fontFamily = expStyle.fontChoice.toComposeFontFamily(),
                            fontSize = expStyle.fontSizeSp.sp,
                            lineHeight = (expStyle.fontSizeSp * 1.45f).sp,
                            fontWeight = if (expStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (expStyle.isItalic) FontStyle.Italic else FontStyle.Normal,
                            textDecoration = combinedDecoration,
                            textAlign = expStyle.alignment.toComposeTextAlign(),
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                            ) {
                                if (explanationFieldValue.text.isEmpty()) {
                                    Text(
                                        text = "Explain how this Kotlin snippet works, or tap '✨ AI Explain' to auto-generate… (Bound to Section #${section.orderIndex + 1})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(explanationFocusRequester)
                            .onFocusChanged { focusState ->
                                onExplanationFocusChanged(focusState.isFocused)
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(120)
                                        bringActiveExplanationLineIntoView(explanationFieldValue.selection.end)
                                    }
                                }
                            }
                            .testTag("section_explanation_input_${section.orderIndex}")
                    )
                }
            }
        }

        // 3. Optional Bottom Divider separating this section from subsequent blocks
        if (section.showDivider && !isSingleSectionFocusMode) {
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun IdeActionChip(
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = accentColor.copy(alpha = 0.16f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SectionDividerBar(
    sectionNumber: Int,
    onRemoveDivider: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .clickable(onClick = onRemoveDivider)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Section #$sectionNumber Boundary",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = "Remove Divider",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
