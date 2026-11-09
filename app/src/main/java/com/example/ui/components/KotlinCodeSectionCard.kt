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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.zIndex
import com.example.code.DiagnosticSeverity
import com.example.code.KotlinCodeEngine
import com.example.code.KotlinSuggestion
import com.example.model.CodeSectionUiModel
import com.example.ui.theme.DarkKotlinSyntaxColors
import com.example.ui.theme.IdeInlineErrorPillBg
import com.example.ui.theme.IdeInlineErrorPillBorder
import com.example.ui.theme.IdeInlineErrorPillText
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Tier1CodeBevelTop
import com.example.ui.theme.Tier1CodeCanvasBg
import com.example.ui.theme.Tier1CodeGutterBg
import com.example.ui.theme.Tier1CodeHeaderBg
import com.example.ui.theme.Tier1CodeOuterRim
import com.example.ui.theme.Tier2CompletionBg
import com.example.ui.theme.Tier2CompletionBorder
import com.example.ui.theme.Tier2CompletionDocBg
import com.example.ui.theme.Tier2CompletionSelectedBg
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Dynamic Theme IDE Tokens (Reacts instantly to active theme selection)
private val DarculaChromeBg: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.codeHeaderBackground
private val DarculaSubBarBg: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.codeHeaderBackground.copy(alpha = 0.75f)
private val DarculaCanvasBg: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.codeBackground
private val DarculaGutterBg: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.codeHeaderBackground.copy(alpha = 0.55f)
private val DarculaActiveLineBg: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.codeHeaderBackground.copy(alpha = 0.85f)
private val DarculaConsoleBg: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.codeBackground
private val DarculaBorder: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.codeBorder
private val MacTrafficRed = Color(0xFFD9534F)
private val MacTrafficYellow = Color(0xFFD49B35)
private val MacTrafficGreen = Color(0xFF6A9955)
private val DarculaWarmAmber: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.keyword
private val DarculaSageGreen: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.stringLiteral
private val DarculaSteelBlue: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.functionName
private val DarculaMutedText: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.lineNumber
private val DarculaBrightText: Color @Composable get() = com.example.ui.theme.LocalKotlinSyntaxColors.current.plainText

enum class LeftRailIdeTool {
    NONE,
    DIAGNOSTICS,
    STRUCTURE_AND_FIND,
    LINE_ACTIONS
}

private data class IdeColorPreset(
    val name: String,
    val hexLiteral: String,
    val color: Color
)

private val IDE_COLOR_PRESETS = listOf(
    IdeColorPreset("Warm Amber", "0xFFD4883A", Color(0xFFD4883A)),
    IdeColorPreset("Darcula Sage", "0xFF6A8759", Color(0xFF6A8759)),
    IdeColorPreset("Steel Blue", "0xFF6897BB", Color(0xFF6897BB)),
    IdeColorPreset("Terracotta", "0xFFCC7832", Color(0xFFCC7832)),
    IdeColorPreset("Warm Gold", "0xFFFFC66D", Color(0xFFFFC66D)),
    IdeColorPreset("Muted Plum", "0xFF9876AA", Color(0xFF9876AA)),
    IdeColorPreset("Brick Red", "0xFFBC3F3C", Color(0xFFBC3F3C)),
    IdeColorPreset("Warm Ivory", "0xFFE6E1DC", Color(0xFFE6E1DC))
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KotlinCodeSectionCard(
    section: CodeSectionUiModel,
    isCodeFocused: Boolean,
    isExplanationFocused: Boolean,
    isSingleSectionFocusMode: Boolean = false,
    hideInlineExplanation: Boolean = false,
    activeLeftRailTool: LeftRailIdeTool = LeftRailIdeTool.NONE,
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
    canUndo: Boolean = false,
    onUndo: () -> Unit = {},
    onDeactivateLeftRailTool: () -> Unit = {},
    onSelectLanguage: (com.example.model.LanguageType) -> Unit = {},
    onSelectThemeOverride: (com.example.ui.theme.AppThemeId?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val defaultSyntaxColors = com.example.ui.theme.LocalKotlinSyntaxColors.current
    val activeSyntaxColors = section.themeOverride?.toSyntaxColors() ?: defaultSyntaxColors
    val terminalSyntaxColors = activeSyntaxColors
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current
    val codeFocusRequester = remember { FocusRequester() }
    val explanationFocusRequester = remember { FocusRequester() }
    val codeBringIntoViewRequester = remember { BringIntoViewRequester() }
    val explanationBringIntoViewRequester = remember { BringIntoViewRequester() }
    val internalCodeScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var codeViewportHeightPx by remember { mutableIntStateOf(0) }
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
    var userOverrodeExplainDock by remember { mutableStateOf(false) }
    var selectedSuggestionIndex by remember { mutableIntStateOf(0) }
    var dismissedCompletionForPrefix by remember { mutableStateOf<String?>(null) }

    // Respond to the Left-Rail circular IDE buttons from DocumentEditorScreen — including turning them OFF when set to NONE!
    LaunchedEffect(activeLeftRailTool) {
        when (activeLeftRailTool) {
            LeftRailIdeTool.STRUCTURE_AND_FIND -> {
                showFindReplaceBar = true
                showLiveTemplatesBar = false
                showMetricsBar = false
            }
            LeftRailIdeTool.LINE_ACTIONS -> {
                showLiveTemplatesBar = true
                showFindReplaceBar = false
                showMetricsBar = false
            }
            LeftRailIdeTool.DIAGNOSTICS -> {
                showMetricsBar = true
                showFindReplaceBar = false
                showLiveTemplatesBar = false
            }
            LeftRailIdeTool.NONE -> {
                showFindReplaceBar = false
                showLiveTemplatesBar = false
                showMetricsBar = false
            }
        }
    }

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

    LaunchedEffect(isCodeFocused, isExplanationFocused) {
        if (isCodeFocused) {
            isCodeFolded = false
            userOverrodeExplainDock = false
        } else if (isExplanationFocused) {
            userOverrodeExplainDock = false
        }
    }

    val isDockedCompactForExplanation = isExplanationFocused && !userOverrodeExplainDock && !isCodeFolded && !hideInlineExplanation

    fun bringActiveCodeLineIntoView(cursorOffset: Int, currentText: String) {
        val info = KotlinCodeEngine.getCursorPositionInfo(
            TextFieldValue(currentText, TextRange(cursorOffset.coerceIn(0, currentText.length)))
        )
        onActiveCodeLineChanged(info.line, info.totalLines)
        coroutineScope.launch {
            try {
                val linePx = with(density) { 22.sp.toPx() }
                val cursorTopPx = ((info.line - 1).coerceAtLeast(0) * linePx).toInt()
                val cursorBottomPx = (cursorTopPx + linePx * 2.2f).toInt()
                val viewportH = codeViewportHeightPx
                if (viewportH > 0) {
                    val currentScroll = internalCodeScrollState.value
                    if (cursorBottomPx > currentScroll + viewportH) {
                        val target = (cursorBottomPx - viewportH + linePx.toInt()).coerceIn(0, internalCodeScrollState.maxValue)
                        internalCodeScrollState.animateScrollTo(target)
                    } else if (cursorTopPx < currentScroll) {
                        internalCodeScrollState.animateScrollTo(cursorTopPx.coerceAtLeast(0))
                    }
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

    // Real-time IDE syntax diagnostics, detected colors, structure outline, metrics, and cursor telemetry
    val diagnostics = remember(codeFieldValue.text) {
        KotlinCodeEngine.diagnoseKotlinSyntax(codeFieldValue.text)
    }
    val detectedColors = remember(codeFieldValue.text) {
        KotlinCodeEngine.detectColorCodes(codeFieldValue.text)
    }
    val codeMetrics = remember(codeFieldValue.text) {
        KotlinCodeEngine.analyzeCodeMetrics(codeFieldValue.text)
    }
    val structureSymbols = remember(codeFieldValue.text) {
        KotlinCodeEngine.extractStructureSymbols(codeFieldValue.text)
    }
    val cursorInfo = remember(codeFieldValue.text, codeFieldValue.selection.end) {
        KotlinCodeEngine.getCursorPositionInfo(codeFieldValue)
    }

    val syntaxVisualTransformation = remember(
        terminalSyntaxColors,
        codeFieldValue.selection.end,
        diagnostics,
        isCodeFocused,
        section.language
    ) {
        androidx.compose.ui.text.input.VisualTransformation { text ->
            val highlighted = com.example.code.CodeLanguageEngine.highlightCodeForLanguage(
                code = text.text,
                colors = terminalSyntaxColors,
                language = section.language,
                cursorOffset = if (isCodeFocused) codeFieldValue.selection.end else -1
            )
            androidx.compose.ui.text.input.TransformedText(
                text = highlighted,
                offsetMapping = androidx.compose.ui.text.input.OffsetMapping.Identity
            )
        }
    }

    val (currentPrefix, suggestions) = remember(codeFieldValue.text, codeFieldValue.selection.end, section.language) {
        com.example.code.CodeLanguageEngine.getSuggestionsForLanguage(
            code = codeFieldValue.text,
            cursorPosition = codeFieldValue.selection.end,
            language = section.language
        )
    }

    LaunchedEffect(currentPrefix) {
        selectedSuggestionIndex = 0
        if (currentPrefix != dismissedCompletionForPrefix) {
            dismissedCompletionForPrefix = null
        }
    }

    val showCursorCompletionPopup = isCodeFocused &&
        !isCodeFolded &&
        currentPrefix.isNotBlank() &&
        suggestions.isNotEmpty() &&
        dismissedCompletionForPrefix != currentPrefix

    val currentCodeLineCount = maxOf(codeFieldValue.text.count { it == '\n' } + 1, section.lineCount)
    val visibleLines = maxOf(currentCodeLineCount, 4)
    val lineHeightDp = with(density) { 22.sp.toDp() }
    val minCodeEditorHeight = (lineHeightDp * visibleLines) + 24.dp

    androidx.compose.runtime.CompositionLocalProvider(
        com.example.ui.theme.LocalKotlinSyntaxColors provides activeSyntaxColors
    ) {
        Column(
            modifier = modifier
                .then(
                    if (isSingleSectionFocusMode) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
                )
                .testTag("section_container_${section.orderIndex}")
        ) {
        // 1. Optional/Removable Top Section Divider (only in multi-section view)
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

        // 2. TIER 1 DEPTH: Full-Screen Beveled Matte Darcula `.ktscribe` Code Block Window
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarculaCanvasBg
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isCodeFocused || isExplanationFocused) 10.dp else 6.dp
            ),
            modifier = Modifier
                .then(
                    if (isSingleSectionFocusMode) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
                )
                .shadow(
                    elevation = if (isCodeFocused) 12.dp else 6.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .border(
                    width = 1.5.dp,
                    color = Tier1CodeOuterRim,
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(1.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (diagnostics.any { it.severity == DiagnosticSeverity.ERROR }) {
                                IdeInlineErrorPillBorder
                            } else if (isCodeFocused) {
                                Color(0xFF594E43)
                            } else {
                                Tier1CodeBevelTop
                            },
                            Color(0xFF221F1D)
                        )
                    ),
                    shape = RoundedCornerShape(21.dp)
                )
        ) {
            Column(
                modifier = if (isSingleSectionFocusMode) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
            ) {
                // =========================================================================
                // A. APPLE `cmd` / DARCULA WINDOW HEADER BAR (`• • •` + Concept Tag + Actions)
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarculaChromeBg)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Iconic macOS Traffic Lights (`• • •` matching Sketch 1)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                    // Center: Language Badge + Learned Concept Tag Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        var showLangMenu by remember { mutableStateOf(false) }
                        Surface(
                            color = section.language.badgeColor.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(1.dp, section.language.badgeColor, RoundedCornerShape(8.dp))
                                .clickable { showLangMenu = true }
                                .testTag("language_selector_chip_${section.orderIndex}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = section.language.shortLabel,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = section.language.badgeColor
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Switch Language",
                                    tint = section.language.badgeColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showLangMenu,
                                onDismissRequest = { showLangMenu = false }
                            ) {
                                com.example.model.LanguageType.entries.forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = lang.shortLabel,
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = lang.badgeColor
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = lang.displayName,
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        },
                                        onClick = {
                                            onSelectLanguage(lang)
                                            showLangMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Surface(
                            color = Color(0xFF2D2824),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF4A423B), RoundedCornerShape(8.dp))
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
                                    tint = DarculaWarmAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Learned: ${section.effectiveLearnedConceptTag}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarculaBrightText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Right: Live Syntax Diagnostic Badge + Focus Mode Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        val hasError = diagnostics.any { it.severity == DiagnosticSeverity.ERROR }
                        val statusColor = when {
                            hasError -> IdeInlineErrorPillText
                            diagnostics.isNotEmpty() -> MacTrafficYellow
                            else -> DarculaSageGreen
                        }
                        Surface(
                            color = if (hasError) IdeInlineErrorPillBg else Color(0xFF2A2724),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = if (hasError) IdeInlineErrorPillBorder else Color(0xFF3E3934),
                                shape = RoundedCornerShape(6.dp)
                            )
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
                            color = Color(0xFF2A2724),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF3E3934), RoundedCornerShape(6.dp))
                                .clickable(onClick = onToggleFocusMode)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInFull,
                                    contentDescription = "Toggle Focus View",
                                    tint = if (isSingleSectionFocusMode) DarculaWarmAmber else DarculaMutedText,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isSingleSectionFocusMode) "Focused" else "Focus",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isSingleSectionFocusMode) DarculaWarmAmber else DarculaMutedText
                                )
                            }
                        }
                    }
                }

                // Inline Concept Tag Editor
                AnimatedVisibility(visible = isEditingConceptTag) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarculaSubBarBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = DarculaWarmAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Concept Tag:",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = DarculaMutedText
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarculaCanvasBg)
                                .border(1.dp, DarculaBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            BasicTextField(
                                value = section.explanationStyle.learnedConceptTag,
                                onValueChange = onConceptTagChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.5.sp,
                                    color = DarculaBrightText
                                ),
                                cursorBrush = SolidColor(DarculaWarmAmber),
                                decorationBox = { inner ->
                                    if (section.explanationStyle.learnedConceptTag.isEmpty()) {
                                        Text(
                                            text = section.effectiveLearnedConceptTag,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.5.sp,
                                            color = DarculaMutedText
                                        )
                                    }
                                    inner()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Surface(
                            color = Tier2CompletionSelectedBg,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { isEditingConceptTag = false }
                        ) {
                            Text(
                                text = "Done",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarculaBrightText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // =========================================================================
                // B. SECTION TITLE & MATTE DARCULA IDE TOOLBAR
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarculaSubBarBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF2D2823),
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier.border(1.dp, Color(0xFF473F37), RoundedCornerShape(5.dp))
                    ) {
                        Text(
                            text = "§${section.orderIndex + 1}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = DarculaWarmAmber,
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
                                color = DarculaBrightText,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(DarculaWarmAmber),
                            decorationBox = { innerTextField ->
                                if (section.sectionTitle.isBlank()) {
                                    Text(
                                        text = if (section.isNamingInProgress) {
                                            "AI naming & tagging concept…"
                                        } else {
                                            "Section #${section.orderIndex + 1} • ${section.effectiveLearnedConceptTag}"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = DarculaMutedText,
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

                    // ▶ Run in Apple cmd Simulator button
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
                            tint = DarculaSageGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // AI Studio Dropdown Menu
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
                                    color = DarculaWarmAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Open AI Studio Actions",
                                    tint = DarculaWarmAmber,
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
                                        Text("AI Name & Tag Learned Concept", fontWeight = FontWeight.SemiBold)
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
                                        Text("AI Thorough Deep-Dive Explanation", fontWeight = FontWeight.SemiBold)
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
                                        Text("AI Refactor & Fix Syntax", fontWeight = FontWeight.SemiBold)
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
                                        Text("AI Add KDoc & Inline Comments", fontWeight = FontWeight.SemiBold)
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

                    // Live Kotlin Templates & Line Actions button
                    IconButton(
                        onClick = { showLiveTemplatesBar = !showLiveTemplatesBar },
                        modifier = Modifier
                            .size(29.dp)
                            .testTag("live_templates_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Insert Kotlin Live Template or Refactor Line",
                            tint = if (showLiveTemplatesBar) DarculaWarmAmber else DarculaMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Code Metrics & Immutability Inspector button
                    IconButton(
                        onClick = { showMetricsBar = !showMetricsBar },
                        modifier = Modifier
                            .size(29.dp)
                            .testTag("code_metrics_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Inspect Code Complexity & Immutability",
                            tint = if (showMetricsBar) DarculaSageGreen else DarculaMutedText,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Find, Replace & Structure Outline button
                    IconButton(
                        onClick = { showFindReplaceBar = !showFindReplaceBar },
                        modifier = Modifier
                            .size(29.dp)
                            .testTag("find_replace_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Structure Outline, Find and Replace in Code",
                            tint = if (showFindReplaceBar) DarculaSteelBlue else DarculaMutedText,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Color Palette Picker button
                    IconButton(
                        onClick = { showColorPalettePicker = !showColorPalettePicker },
                        modifier = Modifier
                            .size(29.dp)
                            .testTag("color_palette_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Insert or Edit Color Code",
                            tint = if (showColorPalettePicker) DarculaWarmAmber else DarculaMutedText,
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
                            tint = DarculaWarmAmber,
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
                            tint = DarculaMutedText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // =========================================================================
                // B2. LINE REFACTORING & LIVE TEMPLATES DRAWER (Triggered by Left Rail `⌘/`)
                // =========================================================================
                AnimatedVisibility(visible = showLiveTemplatesBar) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarculaChromeBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Line & Surround:",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarculaWarmAmber
                            )
                            IdeActionChip(
                                label = "⌘/ Comment",
                                accentColor = DarculaSteelBlue,
                                onClick = {
                                    val updated = KotlinCodeEngine.toggleLineCommentAtCursor(codeFieldValue)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                }
                            )
                            IdeActionChip(
                                label = "⌘D Dup Line",
                                accentColor = DarculaSageGreen,
                                onClick = {
                                    val updated = KotlinCodeEngine.duplicateLineAtCursor(codeFieldValue)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                    bringActiveCodeLineIntoView(updated.selection.end, updated.text)
                                }
                            )
                            IdeActionChip(
                                label = "▲ Move Up",
                                accentColor = DarculaWarmAmber,
                                onClick = {
                                    val updated = KotlinCodeEngine.moveLineVertically(codeFieldValue, moveUp = true)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                    bringActiveCodeLineIntoView(updated.selection.end, updated.text)
                                }
                            )
                            IdeActionChip(
                                label = "▼ Move Down",
                                accentColor = DarculaWarmAmber,
                                onClick = {
                                    val updated = KotlinCodeEngine.moveLineVertically(codeFieldValue, moveUp = false)
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                    bringActiveCodeLineIntoView(updated.selection.end, updated.text)
                                }
                            )
                            IdeActionChip(
                                label = "Wrap try/catch",
                                accentColor = DarculaWarmAmber,
                                onClick = {
                                    val updated = KotlinCodeEngine.surroundSelectionOrLine(codeFieldValue, "try_catch")
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                    bringActiveCodeLineIntoView(updated.selection.end, updated.text)
                                }
                            )
                            IdeActionChip(
                                label = "Wrap if {}",
                                accentColor = DarculaSteelBlue,
                                onClick = {
                                    val updated = KotlinCodeEngine.surroundSelectionOrLine(codeFieldValue, "if_block")
                                    codeFieldValue = updated
                                    onCodeChange(updated.text)
                                    bringActiveCodeLineIntoView(updated.selection.end, updated.text)
                                }
                            )
                            if (canUndo) {
                                IdeActionChip(
                                    label = "↶ Undo",
                                    accentColor = DarculaWarmAmber,
                                    onClick = onUndo
                                )
                            }
                            IdeActionChip(
                                label = "✕ Turn Off",
                                accentColor = MacTrafficRed,
                                onClick = {
                                    showLiveTemplatesBar = false
                                    onDeactivateLeftRailTool()
                                }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = "Templates:",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarculaMutedText
                            )
                            KotlinCodeEngine.LIVE_TEMPLATES.forEach { tpl ->
                                Surface(
                                    color = DarculaCanvasBg,
                                    shape = RoundedCornerShape(7.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .border(1.dp, DarculaBorder, RoundedCornerShape(7.dp))
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
                                            color = DarculaWarmAmber
                                        )
                                        Text(
                                            text = "• ${tpl.title}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = DarculaBrightText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // B3. CODE METRICS & IMMUTABILITY INSPECTOR
                // =========================================================================
                AnimatedVisibility(visible = showMetricsBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarculaChromeBg)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Complexity: ${codeMetrics.complexityLabel}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            color = DarculaSteelBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Immutability: ${codeMetrics.immutabilityPercent}% (${codeMetrics.valCount} val / ${codeMetrics.varCount} var)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            color = if (codeMetrics.varCount == 0) DarculaSageGreen else DarculaWarmAmber
                        )
                        Text(
                            text = "• ${codeMetrics.functionCount} fun • ${codeMetrics.typeCount} types",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            color = DarculaMutedText
                        )

                        if (codeMetrics.varCount > 0) {
                            Surface(
                                color = Color(0xFF283324),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, DarculaSageGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
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
                                        tint = DarculaSageGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Convert var → val",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarculaSageGreen
                                    )
                                }
                            }
                        }
                        if (canUndo) {
                            IdeActionChip(
                                label = "↶ Undo",
                                accentColor = DarculaWarmAmber,
                                onClick = onUndo
                            )
                        }
                        IdeActionChip(
                            label = "✕ Turn Off",
                            accentColor = MacTrafficRed,
                            onClick = {
                                showMetricsBar = false
                                onDeactivateLeftRailTool()
                            }
                        )
                    }
                }

                // =========================================================================
                // C. STRUCTURE OUTLINE & INTERACTIVE FIND/REPLACE BAR (Left Rail `🔍`)
                // =========================================================================
                AnimatedVisibility(visible = showFindReplaceBar) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarculaChromeBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (structureSymbols.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Structure:",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarculaWarmAmber
                                )
                                structureSymbols.forEach { sym ->
                                    Surface(
                                        color = DarculaCanvasBg,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, DarculaBorder, RoundedCornerShape(6.dp))
                                            .clickable {
                                                val lines = codeFieldValue.text.split('\n')
                                                val targetLineIdx = (sym.line - 1).coerceIn(0, (lines.size - 1).coerceAtLeast(0))
                                                val offset = lines.take(targetLineIdx).sumOf { it.length + 1 }
                                                codeFieldValue = codeFieldValue.copy(selection = TextRange(offset))
                                                codeFocusRequester.requestFocus()
                                                bringActiveCodeLineIntoView(offset, codeFieldValue.text)
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IdeCompletionBadgeIcon(badgeChar = sym.kindBadge)
                                            Text(
                                                text = "${sym.name} :${sym.line}",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.5.sp,
                                                color = DarculaBrightText
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarculaCanvasBg)
                                    .border(1.dp, DarculaBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                BasicTextField(
                                    value = findQuery,
                                    onValueChange = { findQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.5.sp,
                                        color = DarculaBrightText
                                    ),
                                    cursorBrush = SolidColor(DarculaWarmAmber),
                                    decorationBox = { inner ->
                                        if (findQuery.isEmpty()) {
                                            Text(
                                                text = "Find in code…",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 11.5.sp,
                                                color = DarculaMutedText
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
                                    .background(DarculaCanvasBg)
                                    .border(1.dp, DarculaBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                BasicTextField(
                                    value = replaceQuery,
                                    onValueChange = { replaceQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.5.sp,
                                        color = DarculaBrightText
                                    ),
                                    cursorBrush = SolidColor(DarculaWarmAmber),
                                    decorationBox = { inner ->
                                        if (replaceQuery.isEmpty()) {
                                            Text(
                                                text = "Replace with…",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 11.5.sp,
                                                color = DarculaMutedText
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }

                            Surface(
                                color = Tier2CompletionSelectedBg,
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
                                    color = DarculaBrightText,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }

                            if (canUndo) {
                                IdeActionChip(
                                    label = "↶ Undo",
                                    accentColor = DarculaWarmAmber,
                                    onClick = onUndo
                                )
                            }

                            IdeActionChip(
                                label = "✕ Turn Off",
                                accentColor = MacTrafficRed,
                                onClick = {
                                    showFindReplaceBar = false
                                    onDeactivateLeftRailTool()
                                }
                            )
                        }
                    }
                }

                // =========================================================================
                // D. INTERACTIVE IDE COLOR PALETTE PICKER STRIP
                // =========================================================================
                AnimatedVisibility(visible = showColorPalettePicker) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarculaChromeBg)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Color Literal:",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = DarculaMutedText
                        )
                        IDE_COLOR_PRESETS.forEach { preset ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarculaCanvasBg)
                                    .border(1.dp, DarculaBorder, RoundedCornerShape(8.dp))
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
                                    color = DarculaBrightText
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // E. APPLE `cmd` TERMINAL HEADER BREADCRUMB (`ktscribe@macbook ~ %`)
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarculaCanvasBg)
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
                            tint = DarculaWarmAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "ktscribe@macbook",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarculaWarmAmber
                        )
                        Text(
                            text = " ~ % ",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarculaSageGreen
                        )
                        Text(
                            text = "kotlinc ${section.ktScribeFileName}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = DarculaMutedText
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isCodeFolded || isDockedCompactForExplanation) {
                            Surface(
                                color = Color(0xFF2D2823),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable {
                                    isCodeFolded = false
                                    userOverrodeExplainDock = true
                                }
                            ) {
                                Text(
                                    text = if (isDockedCompactForExplanation) {
                                        "Docked Preview (${section.lineCount}L) • Tap to Expand"
                                    } else {
                                        "▸ Expand (${section.lineCount} lines)"
                                    },
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = DarculaWarmAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Ln ${cursorInfo.line}/${cursorInfo.totalLines}, Col ${cursorInfo.column}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                color = DarculaMutedText
                            )
                        }
                    }
                }

                HorizontalDivider(color = DarculaBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                // Docked compact preview when writing inline explanation
                AnimatedVisibility(visible = isDockedCompactForExplanation) {
                    val previewSnippet = remember(codeFieldValue.text) {
                        codeFieldValue.text.lines().take(3).joinToString("\n")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarculaCanvasBg)
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
                            color = DarculaBrightText.copy(alpha = 0.85f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // =========================================================================
                // F. MATTE DARCULA CODE EDITOR + INLINE LINE ERROR PILLS + CURSOR COMPLETION BOX
                // - Takes the full remaining height (`weight(1f)`) in single-file mode
                // - Scrolls internally so header & bottom shortcut bar remain anchored
                // - Renders the Suggestion Box as a ZERO-LAYOUT-IMPACT overlay (`layout(0, 0)`)
                //   that NEVER scrolls the screen up and down and automatically flips above
                //   the cursor line when the keyboard pops up!
                // =========================================================================
                if (!isCodeFolded && !isDockedCompactForExplanation) {
                    Box(
                        modifier = Modifier
                            .then(
                                if (isSingleSectionFocusMode) {
                                    Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                } else {
                                    Modifier.fillMaxWidth()
                                }
                            )
                            .background(DarculaCanvasBg)
                            .onSizeChanged { codeViewportHeightPx = it.height }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isSingleSectionFocusMode) {
                                        Modifier
                                            .fillMaxHeight()
                                            .verticalScroll(internalCodeScrollState)
                                    } else {
                                        Modifier.heightIn(min = minCodeEditorHeight)
                                    }
                                )
                                .bringIntoViewRequester(codeBringIntoViewRequester)
                                .padding(top = 8.dp, bottom = if (isSingleSectionFocusMode) 88.dp else 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Line Numbers Gutter (`1.`, `2.`, `3.` matching Sketch 1)
                            Column(
                                modifier = Modifier
                                    .width(48.dp)
                                    .background(DarculaGutterBg)
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
                                                    DarculaActiveLineBg
                                                } else {
                                                    Color.Transparent
                                                }
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (lineDiag != null) {
                                            Text(
                                                text = "✕",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (lineDiag.severity == DiagnosticSeverity.ERROR) MacTrafficRed else MacTrafficYellow
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                        }
                                        Text(
                                            text = "$lineNum.",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 12.sp,
                                            lineHeight = 22.sp,
                                            style = androidx.compose.ui.text.TextStyle(
                                                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                                                lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                                                    alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                                                    trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None
                                                )
                                            ),
                                            fontWeight = if (isCurrentCursorLine) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                lineDiag != null -> IdeInlineErrorPillText
                                                isCurrentCursorLine -> DarculaWarmAmber
                                                else -> DarculaMutedText.copy(alpha = 0.72f)
                                            }
                                        )
                                    }
                                }
                            }

                            // Subtle Vertical Gutter Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(lineHeightDp * visibleLines)
                                    .background(DarculaBorder)
                            )

                            // Main Code Text Field + Floating Cursor Completion Popup
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = lineHeightDp * visibleLines)
                            ) {
                                BasicTextField(
                                    value = codeFieldValue,
                                    onValueChange = { incomingValue ->
                                        val processed = KotlinCodeEngine.processIdeTextEdit(
                                            previous = codeFieldValue,
                                            incoming = incomingValue
                                        )
                                        codeFieldValue = processed
                                        onCodeChange(processed.text)
                                        if (processed.text.count { it == '\n' } != previousCodeLineCount) {
                                            previousCodeLineCount = processed.text.count { it == '\n' }
                                            bringActiveCodeLineIntoView(processed.selection.end, processed.text)
                                        }
                                    },
                                    onTextLayout = { layoutResult ->
                                        codeLayoutResult = layoutResult
                                    },
                                    textStyle = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 13.5.sp,
                                        lineHeight = 22.sp,
                                        color = DarculaBrightText,
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                                            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                                            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None
                                        )
                                    ),
                                    visualTransformation = syntaxVisualTransformation,
                                    cursorBrush = SolidColor(DarculaWarmAmber),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = lineHeightDp * visibleLines)
                                                .padding(horizontal = 10.dp)
                                        ) {
                                            if (codeFieldValue.text.isEmpty()) {
                                                Text(
                                                    text = "fun main() {\n    sout\n}",
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 13.sp,
                                                    lineHeight = 22.sp,
                                                    style = androidx.compose.ui.text.TextStyle(
                                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                                                        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                                                            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                                                            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None
                                                        )
                                                    ),
                                                    color = DarculaMutedText.copy(alpha = 0.45f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(codeFocusRequester)
                                        .onFocusChanged { focusState ->
                                            onCodeFocusChanged(focusState.isFocused)
                                        }
                                        .testTag("kotlin_code_input_${section.orderIndex}")
                                )

                                // Contextual Floating IDE Autocomplete Popup (Shifted +28dp right of cursor)
                                val cursorOffset = codeFieldValue.selection.end.coerceIn(0, codeFieldValue.text.length)
                                val cursorRect = codeLayoutResult?.let { layout ->
                                    try {
                                        layout.getCursorRect(cursorOffset)
                                    } catch (_: Exception) {
                                        null
                                    }
                                }

                                if (showCursorCompletionPopup && cursorRect != null) {
                                    val popupX = with(density) { (cursorRect.right.toDp() + 28.dp).coerceAtLeast(16.dp) }
                                    val popupY = with(density) { (cursorRect.top.toDp() - 4.dp).coerceAtLeast(0.dp) }

                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(10.dp),
                                        shadowElevation = 14.dp,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
                                        modifier = Modifier
                                            .offset(x = popupX, y = popupY)
                                            .widthIn(min = 200.dp, max = 300.dp)
                                            .heightIn(max = 220.dp)
                                            .zIndex(150f)
                                            .testTag("floating_cursor_completion_popup")
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Suggestions (${suggestions.size})",
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "Tab ↵ to accept",
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 8.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                            suggestions.take(8).forEachIndexed { idx, sugg ->
                                                val isSelected = idx == selectedSuggestionIndex
                                                Surface(
                                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            val (newCode, newCursor) = KotlinCodeEngine.applySuggestion(
                                                                code = codeFieldValue.text,
                                                                cursorPosition = codeFieldValue.selection.end,
                                                                suggestion = sugg
                                                            )
                                                            codeFieldValue = TextFieldValue(text = newCode, selection = TextRange(newCursor))
                                                            onCodeChange(newCode)
                                                            bringActiveCodeLineIntoView(newCursor, newCode)
                                                        }
                                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        IdeCompletionBadgeIcon(badgeChar = sugg.badgeChar)
                                                        Text(
                                                            text = sugg.label,
                                                            fontFamily = JetBrainsMonoFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        val detailText = if (sugg.tailSignature.isNotEmpty()) sugg.tailSignature else sugg.description
                                                        if (detailText.isNotEmpty()) {
                                                            Text(
                                                                text = detailText,
                                                                fontFamily = JetBrainsMonoFontFamily,
                                                                fontSize = 9.5.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Non-obstructive live line status:
                                // Errors are shown cleanly in the line number gutter on the left (with ✕ and severity color)
                                // and the docked bar above bottom keys, leaving the code line 100% unobstructed.
                            }
                        }

                    }
                }

                // =========================================================================
                // G. APPLE TERMINAL EXECUTION CONSOLE (`▶ Run` Output)
                // =========================================================================
                AnimatedVisibility(visible = terminalConsoleOutput != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarculaConsoleBg)
                            .border(0.5.dp, DarculaBorder)
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
                                        .background(DarculaSageGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TERMINAL OUTPUT — zsh",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarculaSageGreen
                                )
                            }
                            IconButton(
                                onClick = { terminalConsoleOutput = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Terminal Output",
                                    tint = DarculaMutedText,
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
                            color = DarculaBrightText
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
                            .background(DarculaSubBarBg)
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
                            color = DarculaMutedText
                        )
                        detectedColors.forEachIndexed { idx, detected ->
                            val swatchColor = Color(detected.colorInt)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarculaCanvasBg)
                                    .border(1.dp, DarculaBorder, RoundedCornerShape(6.dp))
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
                                    color = DarculaBrightText
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
                            .background(Color(0xFF281A19))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("syntax_diagnostics_panel_${section.orderIndex}"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        diagnostics.take(3).forEach { diag ->
                            val isErr = diag.severity == DiagnosticSeverity.ERROR
                            val accent = if (isErr) IdeInlineErrorPillText else MacTrafficYellow
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
                                        color = DarculaBrightText
                                    )
                                }

                                if (diag.quickFixLabel != null && diag.fixedCode != null) {
                                    Surface(
                                        color = Color(0xFF3A2818),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, DarculaWarmAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
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
                                                tint = DarculaWarmAmber,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = diag.quickFixLabel,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.5.sp,
                                                color = DarculaWarmAmber,
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
                // K. CONSTANT (`val`) 8-KEY SHORTCUT BAR AT BOTTOM OF CODE BLOCK
                // Matches the single horizontal bottom bar drawn in Sketch 1 & Sketch 2!
                // n(snippets) = val: strictly constant (`Tab`, `( )`, `{ }`, `" "`, `->`, `.`, `=`, `:`).
                // =========================================================================
                AnimatedVisibility(visible = !isCodeFolded && !isDockedCompactForExplanation) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarculaCanvasBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF23201E),
                            shape = RoundedCornerShape(18.dp),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF3E3832),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .testTag("constant_val_shortcut_bar_${section.orderIndex}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                KotlinCodeEngine.VAL_CONSTANT_SHORTCUT_KEYS.forEach { sym ->
                                    Surface(
                                        color = Color(0xFF2D2926),
                                        shape = RoundedCornerShape(9.dp),
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFF47403A),
                                                shape = RoundedCornerShape(9.dp)
                                            )
                                            .clickable {
                                                // If Tab is pressed while a completion suggestion is open, accept the top suggestion!
                                                if (sym.label == "Tab" && showCursorCompletionPopup && suggestions.isNotEmpty()) {
                                                    val chosen = suggestions[selectedSuggestionIndex.coerceIn(0, suggestions.lastIndex)]
                                                    val (newCode, newCursor) = KotlinCodeEngine.applySuggestion(
                                                        code = codeFieldValue.text,
                                                        cursorPosition = codeFieldValue.selection.end,
                                                        suggestion = chosen
                                                    )
                                                    codeFieldValue = TextFieldValue(
                                                        text = newCode,
                                                        selection = TextRange(newCursor)
                                                    )
                                                    onCodeChange(newCode)
                                                    bringActiveCodeLineIntoView(newCursor, newCode)
                                                } else {
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
                                            }
                                    ) {
                                        Text(
                                            text = sym.label,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarculaBrightText,
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (!hideInlineExplanation) {
                    HorizontalDivider(color = DarculaBorder, thickness = 1.dp)

                    // =========================================================================
                    // K. PAIRED EXPLANATION RICH TEXT EDITOR (Fallback when not in Floating Window)
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
                                            text = "Write your explanation of what you learned in Section #${section.orderIndex + 1}, or tap 'AI Deep Explain' for a thorough 3-part breakdown…",
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
}

/**
 * Renders the authentic JetBrains IDE Completion Badge (`{}`, `K`, `C`, `I`, `@`, `m`, `v`)
 * matching Screenshots 3 & 4.
 */
@Composable
fun IdeCompletionBadgeIcon(badgeChar: String) {
    val (bgColor, fgColor) = when (badgeChar) {
        "{}" -> Color(0xFF2E4229) to Color(0xFF8AA87B) // Sage green template icon `{}`
        "K" -> Color(0xFF472D18) to Color(0xFFCC7832)  // Terracotta keyword icon `K`
        "C" -> Color(0xFF45321B) to Color(0xFFD4883A)  // Ochre class icon `C`
        "I" -> Color(0xFF283D2B) to Color(0xFF7CA87E)  // Interface icon `I`
        "@" -> Color(0xFF453C1B) to Color(0xFFBBB529)  // Annotation icon `@`
        "m" -> Color(0xFF42341C) to Color(0xFFFFC66D)  // Method icon `m`
        else -> Color(0xFF233442) to Color(0xFF6897BB) // Variable/property icon `v`
    }
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(0.5.dp, fgColor.copy(alpha = 0.55f), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badgeChar,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = fgColor
        )
    }
}

@Composable
private fun IdeActionChip(
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF2A2623),
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
