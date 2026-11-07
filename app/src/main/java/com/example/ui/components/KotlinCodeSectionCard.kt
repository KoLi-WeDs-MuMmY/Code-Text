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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
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
import androidx.compose.runtime.mutableIntStateOf
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

// Authentic Apple macOS Terminal (cmd / Terminal.app) & Executive Studio palette
private val MacTerminalChromeBg = Color(0xFF1E222B)
private val MacTerminalSubBarBg = Color(0xFF161A22)
private val MacTerminalCanvasBg = Color(0xFF0D1117)
private val MacTerminalGutterBg = Color(0xFF121720)
private val MacTerminalActiveLineBg = Color(0xFF192231)
private val MacTerminalConsoleBg = Color(0xFF090C10)
private val MacTerminalBorder = Color(0xFF2B3240)
private val MacTrafficRed = Color(0xFFFF5F56)
private val MacTrafficYellow = Color(0xFFFFBD2E)
private val MacTrafficGreen = Color(0xFF27C93F)
private val MacTerminalPromptGreen = Color(0xFF10B981)
private val MacTerminalCyan = Color(0xFF38BDF8)
private val MacTerminalViolet = Color(0xFFA78BFA)
private val MacTerminalMutedText = Color(0xFF8B949E)
private val MacTerminalBrightText = Color(0xFFE6EDF3)

private data class IdeColorPreset(
    val name: String,
    val hexLiteral: String,
    val color: Color
)

private val IDE_COLOR_PRESETS = listOf(
    IdeColorPreset("Emerald Pro", "0xFF10B981", Color(0xFF10B981)),
    IdeColorPreset("Sky Cyan", "0xFF38BDF8", Color(0xFF38BDF8)),
    IdeColorPreset("Kotlin Violet", "0xFF7F52FF", Color(0xFF7F52FF)),
    IdeColorPreset("Apple Green", "0xFF32D74B", Color(0xFF32D74B)),
    IdeColorPreset("Coral Orange", "0xFFFF6B4A", Color(0xFFFF6B4A)),
    IdeColorPreset("Warm Amber", "0xFFFFBD2E", Color(0xFFFFBD2E)),
    IdeColorPreset("Crimson Red", "0xFFFF5F56", Color(0xFFFF5F56)),
    IdeColorPreset("Pure White", "0xFFFFFFFF", Color(0xFFFFFFFF))
)

private data class QuickSymbolKey(
    val label: String,
    val insertText: String,
    val cursorOffsetBack: Int = 0
)

private val QUICK_SYMBOL_KEYS = listOf(
    QuickSymbolKey("sout ⇥ println()", "println(\"\")", 2),
    QuickSymbolKey("Tab", "    ", 0),
    QuickSymbolKey("( )", "()", 1),
    QuickSymbolKey("{ }", "{}", 1),
    QuickSymbolKey("\" \"", "\"\"", 1),
    QuickSymbolKey("->", " -> ", 0)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KotlinCodeSectionCard(
    section: CodeSectionUiModel,
    isCodeFocused: Boolean,
    isExplanationFocused: Boolean,
    isSingleSectionFocusMode: Boolean = false,
    hideInlineExplanation: Boolean = false,
    requestAutoFocus: Boolean,
    onAutoFocusConsumed: () -> Unit,
    onCodeFocusChanged: (Boolean) -> Unit,
    onExplanationFocusChanged: (Boolean) -> Unit,
    onCodeChange: (String) -> Unit,
    onExplanationChange: (String) -> Unit,
    onSectionTitleChange: (String) -> Unit,
    onConceptTagChange: (String) -> Unit = {},
    onActiveCodeLineChanged: (Int, Int) -> Unit = { _, _ -> },
    onActiveExplanationLineChanged: (Int) -> Unit = {},
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
    var showLiveTemplatesBar by remember { mutableStateOf(false) }
    var showMetricsBar by remember { mutableStateOf(false) }
    var isEditingConceptTag by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var showAiStudioMenu by remember { mutableStateOf(false) }
    var terminalConsoleOutput by remember { mutableStateOf<String?>(null) }
    var isCodeFolded by remember { mutableStateOf(false) }
    // When writing in the Explanation Block, dock the code block into a compact 3-line preview
    // (like YouTube's docked video player when writing a comment!) unless the user explicitly expands it.
    var userOverrodeExplainDock by remember { mutableStateOf(false) }

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

    var previousCodeLineCount by remember(section.id) {
        mutableIntStateOf(section.lineCount)
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

    // YouTube-style automatic docking:
    // - Focusing Code unfolds the code editor so all lines are ready to edit.
    // - Focusing Explanation resets userOverrodeExplainDock so the code block docks into a compact
    //   3-line preview at the top and the Explanation Block slides right up above the keyboard!
    LaunchedEffect(isCodeFocused, isExplanationFocused) {
        if (isCodeFocused) {
            isCodeFolded = false
            userOverrodeExplainDock = false
        } else if (isExplanationFocused) {
            userOverrodeExplainDock = false
        }
    }

    val isDockedCompactForExplanation = isExplanationFocused && !userOverrodeExplainDock && !isCodeFolded

    // Keep the exact cursor line at the optimal viewing angle above the soft keyboard
    // and auto-scroll smoothly whenever a new line is added!
    fun bringActiveCodeLineIntoView(cursorOffset: Int, currentText: String) {
        val info = KotlinCodeEngine.getCursorPositionInfo(
            TextFieldValue(currentText, TextRange(cursorOffset.coerceIn(0, currentText.length)))
        )
        onActiveCodeLineChanged(info.line, info.totalLines)
        coroutineScope.launch {
            try {
                // Wait a frame if a new line was just added so layout updates its height first
                if (info.totalLines != previousCodeLineCount) {
                    previousCodeLineCount = info.totalLines
                    delay(35)
                }
                val layout = codeLayoutResult
                if (layout != null && currentText.isNotEmpty()) {
                    val safeOffset = cursorOffset.coerceIn(0, currentText.length)
                    val cursorRect = layout.getCursorRect(safeOffset)
                    val topPadPx = with(density) { 48.dp.toPx() }
                    val bottomPadPx = with(density) { 110.dp.toPx() }
                    val targetRect = Rect(
                        left = 0f,
                        top = (cursorRect.top - topPadPx).coerceAtLeast(0f),
                        right = cursorRect.right + 24f,
                        bottom = cursorRect.bottom + bottomPadPx
                    )
                    codeBringIntoViewRequester.bringIntoView(targetRect)
                } else {
                    codeBringIntoViewRequester.bringIntoView()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun bringActiveExplanationLineIntoView(cursorOffset: Int, currentText: String) {
        val safeCursor = cursorOffset.coerceIn(0, currentText.length)
        val currentLine = currentText.substring(0, safeCursor).count { it == '\n' } + 1
        onActiveExplanationLineChanged(currentLine)
        coroutineScope.launch {
            try {
                delay(25)
                val layout = explanationLayoutResult
                if (layout != null && currentText.isNotEmpty()) {
                    val cursorRect = layout.getCursorRect(safeCursor)
                    val topPadPx = with(density) { 44.dp.toPx() }
                    val bottomPadPx = with(density) { 110.dp.toPx() }
                    val targetRect = Rect(
                        left = 0f,
                        top = (cursorRect.top - topPadPx).coerceAtLeast(0f),
                        right = cursorRect.right + 24f,
                        bottom = cursorRect.bottom + bottomPadPx
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
                bringActiveCodeLineIntoView(codeFieldValue.selection.end, codeFieldValue.text)
            } catch (_: Exception) {
            }
            onAutoFocusConsumed()
        }
    }

    // Real-time IDE syntax diagnostics, detected colors, metrics, and cursor telemetry
    val diagnostics = remember(codeFieldValue.text) {
        KotlinCodeEngine.diagnoseKotlinSyntax(codeFieldValue.text)
    }
    val detectedColors = remember(codeFieldValue.text) {
        KotlinCodeEngine.detectColorCodes(codeFieldValue.text)
    }
    val codeMetrics = remember(codeFieldValue.text) {
        KotlinCodeEngine.analyzeCodeMetrics(codeFieldValue.text)
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
                conceptTag = section.effectiveLearnedConceptTag,
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isCodeFocused || isExplanationFocused) 8.dp else 3.dp
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
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // =========================================================================
                // A. APPLE macOS TERMINAL (`cmd`) WINDOW TITLE BAR + LEARNED CONCEPT TAG
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MacTerminalChromeBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Iconic macOS Traffic Lights (Red = Delete, Yellow = Fold Code, Green = Focus Mode)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
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
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(MacTrafficYellow)
                                .clickable {
                                    isCodeFolded = !isCodeFolded
                                    userOverrodeExplainDock = true
                                }
                                .testTag("fold_code_button_${section.orderIndex}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "−",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }

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
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Center: Learned Concept Tag Pill (AI-generated tag of what was learned in this block!)
                    Surface(
                        color = MacTerminalViolet.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MacTerminalViolet.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { isEditingConceptTag = !isEditingConceptTag }
                            .testTag("learned_concept_badge_${section.orderIndex}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Learned Concept Tag",
                                tint = MacTerminalViolet,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Learned: ${section.effectiveLearnedConceptTag}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MacTerminalBrightText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right: Live Syntax Diagnostic Badge + Focus Mode Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
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
                                    text = if (diagnostics.isEmpty()) "OK" else "${diagnostics.size}",
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
                                    contentDescription = "Toggle Focus View",
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

                // Inline Concept Tag Editor (when user taps the Learned Concept badge to customize it)
                AnimatedVisibility(visible = isEditingConceptTag) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalSubBarBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MacTerminalViolet,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Concept Tag:",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = MacTerminalMutedText
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MacTerminalCanvasBg)
                                .border(1.dp, MacTerminalViolet.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            BasicTextField(
                                value = section.explanationStyle.learnedConceptTag,
                                onValueChange = onConceptTagChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.5.sp,
                                    color = MacTerminalBrightText
                                ),
                                cursorBrush = SolidColor(MacTerminalViolet),
                                decorationBox = { inner ->
                                    if (section.explanationStyle.learnedConceptTag.isEmpty()) {
                                        Text(
                                            text = section.effectiveLearnedConceptTag,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.5.sp,
                                            color = MacTerminalMutedText
                                        )
                                    }
                                    inner()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Surface(
                            color = MacTerminalViolet.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { isEditingConceptTag = false }
                        ) {
                            Text(
                                text = "Done",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MacTerminalViolet,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // =========================================================================
                // B. SECTION TITLE & PRO IDE + AI STUDIO TOOLBAR
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
                                            "✨ AI naming & tagging concept…"
                                        } else {
                                            "Section #${section.orderIndex + 1} • ${section.effectiveLearnedConceptTag}"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MacTerminalMutedText,
                                        fontStyle = FontStyle.Italic,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                            .size(29.dp)
                            .testTag("run_kotlin_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run Kotlin Snippet in Terminal",
                            tint = MacTerminalPromptGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // ✨ AI Studio Dropdown Menu (Auto-Name + Concept Tag, Thorough Explain, Refactor, KDoc)
                    Box {
                        IconButton(
                            onClick = { showAiStudioMenu = true },
                            enabled = !section.isNamingInProgress,
                            modifier = Modifier
                                .size(29.dp)
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
                                        Text("✨ AI Name & Tag Learned Concept", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Sets section title + Learned Concept Tag for quick jump navigation",
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
                                        Text("📖 AI Thorough Deep-Dive Explanation", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Generates Concept Learned, Step-by-Step Flow & Idiomatic Takeaways",
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
                                            "Fixes syntax errors and upgrades to clean idiomatic Kotlin",
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
                                            "Inserts KDoc headers and helpful code comments",
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

                    // ⚡ Live Kotlin Templates button
                    IconButton(
                        onClick = { showLiveTemplatesBar = !showLiveTemplatesBar },
                        modifier = Modifier
                            .size(29.dp)
                            .testTag("live_templates_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Insert Kotlin Live Template",
                            tint = if (showLiveTemplatesBar) MacTrafficYellow else MacTerminalMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // 📊 Code Metrics & Immutability Inspector button
                    IconButton(
                        onClick = { showMetricsBar = !showMetricsBar },
                        modifier = Modifier
                            .size(29.dp)
                            .testTag("code_metrics_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Inspect Code Complexity & Immutability",
                            tint = if (showMetricsBar) MacTerminalPromptGreen else MacTerminalMutedText,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // 🔍 Find & Replace in Code button
                    IconButton(
                        onClick = { showFindReplaceBar = !showFindReplaceBar },
                        modifier = Modifier
                            .size(29.dp)
                            .testTag("find_replace_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Find and Replace in Code",
                            tint = if (showFindReplaceBar) MacTerminalCyan else MacTerminalMutedText,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // 🎨 Color Palette Picker button
                    IconButton(
                        onClick = { showColorPalettePicker = !showColorPalettePicker },
                        modifier = Modifier
                            .size(29.dp)
                            .testTag("color_palette_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Insert or Edit Color Code",
                            tint = if (showColorPalettePicker) MacTrafficYellow else MacTerminalMutedText,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // { } Format Kotlin Code button
                    IconButton(
                        onClick = onFormatCode,
                        modifier = Modifier
                            .size(29.dp)
                            .testTag("format_kotlin_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Format Kotlin Code",
                            tint = MacTrafficYellow,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Copy Kotlin Code button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(section.codeContent))
                        },
                        modifier = Modifier.size(29.dp)
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
                // B2. ⚡ KOTLIN LIVE TEMPLATES STRIP (When Toggled)
                // =========================================================================
                AnimatedVisibility(visible = showLiveTemplatesBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalChromeBg)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = "⚡ Templates:",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MacTrafficYellow
                        )
                        KotlinCodeEngine.LIVE_TEMPLATES.forEach { tpl ->
                            Surface(
                                color = MacTerminalCanvasBg,
                                shape = RoundedCornerShape(7.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .border(1.dp, MacTerminalBorder, RoundedCornerShape(7.dp))
                                    .clickable {
                                        val base = codeFieldValue.text.trimEnd()
                                        val updated = if (base.isEmpty()) tpl.snippet else "$base\n\n${tpl.snippet}"
                                        codeFieldValue = TextFieldValue(
                                            text = updated,
                                            selection = TextRange(updated.length)
                                        )
                                        onCodeChange(updated)
                                        showLiveTemplatesBar = false
                                        bringActiveCodeLineIntoView(updated.length, updated)
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = tpl.badge,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MacTerminalPromptGreen
                                    )
                                    Text(
                                        text = "• ${tpl.title}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = MacTerminalBrightText
                                    )
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // B3. 📊 CODE METRICS, COMPLEXITY & IMMUTABILITY INSPECTOR (When Toggled)
                // =========================================================================
                AnimatedVisibility(visible = showMetricsBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalChromeBg)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "📊 Complexity: ${codeMetrics.complexityLabel}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            color = MacTerminalCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Immutability: ${codeMetrics.immutabilityPercent}% (${codeMetrics.valCount} val / ${codeMetrics.varCount} var)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            color = if (codeMetrics.varCount == 0) MacTerminalPromptGreen else MacTrafficYellow
                        )
                        Text(
                            text = "• ${codeMetrics.functionCount} fun • ${codeMetrics.typeCount} types",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            color = MacTerminalMutedText
                        )

                        if (codeMetrics.varCount > 0) {
                            Surface(
                                color = MacTerminalPromptGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        val (converted, count) = KotlinCodeEngine.convertMutableVarsToVal(codeFieldValue.text)
                                        if (count > 0) {
                                            codeFieldValue = TextFieldValue(
                                                text = converted,
                                                selection = codeFieldValue.selection
                                            )
                                            onCodeChange(converted)
                                        }
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MacTerminalPromptGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Convert var → val",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MacTerminalPromptGreen
                                    )
                                }
                            }
                        }
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
                            if (isCodeFolded || isDockedCompactForExplanation) {
                                isCodeFolded = false
                                userOverrodeExplainDock = true
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MacTerminalPromptGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
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
                        if (isCodeFolded || isDockedCompactForExplanation) {
                            Surface(
                                color = MacTerminalCyan.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable {
                                    isCodeFolded = false
                                    userOverrodeExplainDock = true
                                }
                            ) {
                                Text(
                                    text = if (isDockedCompactForExplanation) {
                                        "📌 Docked Preview (${section.lineCount}L) • Tap to Expand"
                                    } else {
                                        "▸ Expand (${section.lineCount} lines)"
                                    },
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = MacTerminalCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Ln ${cursorInfo.line}/${cursorInfo.totalLines}, Col ${cursorInfo.column}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                color = MacTerminalPromptGreen
                            )
                        }
                    }
                }

                HorizontalDivider(color = MacTerminalBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                // =========================================================================
                // E2. YOUTUBE-COMMENT-STYLE DOCKED CODE PREVIEW (When Writing Explanation)
                // Slides the large code block into a compact 3-line docked preview at the top
                // so the Explanation Block sits right at eye level above the keyboard!
                // =========================================================================
                AnimatedVisibility(visible = isDockedCompactForExplanation) {
                    val previewSnippet = remember(codeFieldValue.text) {
                        codeFieldValue.text.lines().take(3).joinToString("\n")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalCanvasBg)
                            .clickable {
                                userOverrodeExplainDock = true
                                codeFocusRequester.requestFocus()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = previewSnippet.ifBlank { "// Empty Kotlin code block (tap to edit)" },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp,
                            color = MacTerminalBrightText.copy(alpha = 0.85f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // =========================================================================
                // F. AUTO-EXPANDING 4-LINE KOTLIN CODE BLOCK EDITOR (APPLE CMD CANVAS)
                // Auto-scrolls smoothly on every newly added line of code!
                // =========================================================================
                AnimatedVisibility(visible = !isCodeFolded && !isDockedCompactForExplanation) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalCanvasBg)
                            .heightIn(min = minCodeEditorHeight)
                            .bringIntoViewRequester(codeBringIntoViewRequester)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Line Numbers Gutter with active line highlight & error/warning dot indicator
                        Column(
                            modifier = Modifier
                                .width(46.dp)
                                .background(MacTerminalGutterBg.copy(alpha = 0.55f))
                                .padding(start = 4.dp, end = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (lineNum in 1..visibleLines) {
                                val lineDiag = diagnostics.firstOrNull { it.line == lineNum }
                                val isCurrentCursorLine = isCodeFocused && cursorInfo.line == lineNum
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(lineHeightDp)
                                        .background(
                                            if (isCurrentCursorLine) {
                                                MacTerminalActiveLineBg
                                            } else {
                                                Color.Transparent
                                            }
                                        ),
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
                                    } else if (isCurrentCursorLine) {
                                        Text(
                                            text = "▸",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = MacTerminalPromptGreen
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
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

                        // Kotlin Code Input with Live Syntax Highlighting, Auto-Closing Pairs & Real-Time Line Auto-Scroll
                        BasicTextField(
                            value = codeFieldValue,
                            onValueChange = { incomingValue ->
                                val processed = KotlinCodeEngine.processIdeTextEdit(
                                    previous = codeFieldValue,
                                    incoming = incomingValue
                                )
                                codeFieldValue = processed
                                onCodeChange(processed.text)
                                bringActiveCodeLineIntoView(processed.selection.end, processed.text)
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
                                            text = "// Apple Terminal Kotlin IDE (starts at 4 lines, auto-scrolls as you add lines)\nfun main() {\n    println(\"Hello from KtScribe!\")\n}",
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
                                            delay(100)
                                            bringActiveCodeLineIntoView(
                                                codeFieldValue.selection.end,
                                                codeFieldValue.text
                                            )
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
                AnimatedVisibility(visible = detectedColors.isNotEmpty() && !isDockedCompactForExplanation) {
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
                // J. IDE QUICK-ACTIONS, SURROUND REFACTORING, SYMBOL STRIP & AUTOCOMPLETE
                // =========================================================================
                AnimatedVisibility(visible = isCodeFocused && !isCodeFolded && !isDockedCompactForExplanation) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacTerminalChromeBg)
                    ) {
                        // 1. IDE Line Operations + Surround With + Quick Symbol Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            IdeActionChip(
                                label = "⌘/ Comment",
                                accentColor = MacTerminalCyan,
                                onClick = {
                                    val updated = KotlinCodeEngine.toggleLineCommentAtCursor(codeFieldValue)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                }
                            )

                            IdeActionChip(
                                label = "⌘D Dup Line",
                                accentColor = MacTerminalPromptGreen,
                                onClick = {
                                    val updated = KotlinCodeEngine.duplicateLineAtCursor(codeFieldValue)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                    bringActiveCodeLineIntoView(updated.selection.end, updated.text)
                                }
                            )

                            IdeActionChip(
                                label = "▲ Line",
                                accentColor = MacTrafficYellow,
                                onClick = {
                                    val updated = KotlinCodeEngine.moveLineVertically(codeFieldValue, moveUp = true)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                    bringActiveCodeLineIntoView(updated.selection.end, updated.text)
                                }
                            )

                            IdeActionChip(
                                label = "▼ Line",
                                accentColor = MacTrafficYellow,
                                onClick = {
                                    val updated = KotlinCodeEngine.moveLineVertically(codeFieldValue, moveUp = false)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                    bringActiveCodeLineIntoView(updated.selection.end, updated.text)
                                }
                            )

                            IdeActionChip(
                                label = "Wrap try/catch",
                                accentColor = MacTerminalViolet,
                                onClick = {
                                    val updated = KotlinCodeEngine.surroundSelectionOrLine(
                                        fieldValue = codeFieldValue,
                                        wrapperType = "try_catch"
                                    )
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                    bringActiveCodeLineIntoView(updated.selection.end, updated.text)
                                }
                            )

                            IdeActionChip(
                                label = "Wrap if {}",
                                accentColor = MacTerminalViolet,
                                onClick = {
                                    val updated = KotlinCodeEngine.surroundSelectionOrLine(
                                        fieldValue = codeFieldValue,
                                        wrapperType = "if_block"
                                    )
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                    bringActiveCodeLineIntoView(updated.selection.end, updated.text)
                                }
                            )

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
                                            bringActiveCodeLineIntoView(nextCur, updated)
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
                                    text = if (currentPrefix.isNotBlank()) "IDE Suggest ($currentPrefix):" else "Live Postfix (type sout, fori, main):",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    color = MacTerminalPromptGreen
                                )
                                suggestions.take(4).forEach { suggestion ->
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
                                            bringActiveCodeLineIntoView(newCursor, newCode)
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

                if (!hideInlineExplanation) {
                HorizontalDivider(color = MacTerminalBorder, thickness = 1.dp)

                // =========================================================================
                // K. PAIRED EXPLANATION RICH TEXT EDITOR (WITH YOUTUBE-STYLE SLIDE-UP & AI DEEP DIVE)
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .bringIntoViewRequester(explanationBringIntoViewRequester)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                text = "EXPLANATION & NOTES • ${section.effectiveLearnedConceptTag.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 1-tap Thorough AI Deep-Dive Explain button
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
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Deep-Dive Explain",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "AI Deep Explain",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Dock / Expand Code toggle
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (isDockedCompactForExplanation || isCodeFolded) {
                                            isCodeFolded = false
                                            userOverrodeExplainDock = true
                                        } else {
                                            isCodeFolded = true
                                        }
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCodeFolded || isDockedCompactForExplanation) {
                                            Icons.Default.KeyboardArrowDown
                                        } else {
                                            Icons.Default.KeyboardArrowUp
                                        },
                                        contentDescription = "Toggle Code Dock",
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (isCodeFolded || isDockedCompactForExplanation) "Full Code" else "Dock Code",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                            bringActiveExplanationLineIntoView(updatedValue.selection.end, updatedValue.text)
                        },
                        onTextLayout = { layoutResult ->
                            explanationLayoutResult = layoutResult
                        },
                        textStyle = TextStyle(
                            fontFamily = expStyle.fontChoice.toComposeFontFamily(),
                            fontSize = expStyle.fontSizeSp.sp,
                            lineHeight = (expStyle.fontSizeSp * 1.48f).sp,
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
                                    .heightIn(min = 64.dp)
                            ) {
                                if (explanationFieldValue.text.isEmpty()) {
                                    Text(
                                        text = "Write your explanation of what you learned in Section #${section.orderIndex + 1}, or tap '✨ AI Deep Explain' for a thorough 3-part breakdown…",
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
                                        delay(100)
                                        bringActiveExplanationLineIntoView(
                                            explanationFieldValue.selection.end,
                                            explanationFieldValue.text
                                        )
                                    }
                                }
                            }
                            .testTag("section_explanation_input_${section.orderIndex}")
                    )
                }
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
    conceptTag: String,
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
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                .clickable(onClick = onRemoveDivider)
                .padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "§$sectionNumber • $conceptTag",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(5.dp))
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
