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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.DiagnosticSeverity
import com.example.code.KotlinCodeEngine
import com.example.model.CodeSectionUiModel
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalKotlinSyntaxColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class IdeColorPreset(
    val name: String,
    val hexLiteral: String,
    val color: Color
)

private val IDE_COLOR_PRESETS = listOf(
    IdeColorPreset("Kotlin Violet", "0xFF7F52FF", Color(0xFF7F52FF)),
    IdeColorPreset("Coral Orange", "0xFFFF6B4A", Color(0xFFFF6B4A)),
    IdeColorPreset("Mint Emerald", "0xFF10B981", Color(0xFF10B981)),
    IdeColorPreset("Sky Cyan", "0xFF38BDF8", Color(0xFF38BDF8)),
    IdeColorPreset("Warm Amber", "0xFFF59E0B", Color(0xFFF59E0B)),
    IdeColorPreset("Crimson Red", "0xFFEF4444", Color(0xFFEF4444)),
    IdeColorPreset("Neon Rose", "0xFFEC4899", Color(0xFFEC4899)),
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
    QuickSymbolKey("\" \"", "\"\"", 1),
    QuickSymbolKey("->", " -> ", 0),
    QuickSymbolKey("=", " = ", 0),
    QuickSymbolKey(":", ": ", 0),
    QuickSymbolKey(".", ".", 0),
    QuickSymbolKey("?", "?", 0),
    QuickSymbolKey("val", "val ", 0),
    QuickSymbolKey("fun", "fun ", 0)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KotlinCodeSectionCard(
    section: CodeSectionUiModel,
    isCodeFocused: Boolean,
    isExplanationFocused: Boolean,
    requestAutoFocus: Boolean,
    onAutoFocusConsumed: () -> Unit,
    onCodeFocusChanged: (Boolean) -> Unit,
    onExplanationFocusChanged: (Boolean) -> Unit,
    onCodeChange: (String) -> Unit,
    onExplanationChange: (String) -> Unit,
    onSectionTitleChange: (String) -> Unit,
    onFormatCode: () -> Unit,
    onRequestAiNaming: () -> Unit,
    onToggleDivider: () -> Unit,
    onDeleteSection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val syntaxColors = LocalKotlinSyntaxColors.current
    val clipboardManager = LocalClipboardManager.current
    val codeFocusRequester = remember { FocusRequester() }
    val codeBringIntoViewRequester = remember { BringIntoViewRequester() }
    val explanationBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    var showColorPalettePicker by remember { mutableStateOf(false) }

    var codeFieldValue by remember(section.id) {
        mutableStateOf(
            TextFieldValue(
                text = section.codeContent,
                selection = TextRange(section.codeContent.length)
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

    LaunchedEffect(requestAutoFocus) {
        if (requestAutoFocus) {
            try {
                codeFocusRequester.requestFocus()
                delay(120)
                codeBringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {
            }
            onAutoFocusConsumed()
        }
    }

    // Real-time IDE syntax diagnostics & color literal detection
    val diagnostics = remember(codeFieldValue.text) {
        KotlinCodeEngine.diagnoseKotlinSyntax(codeFieldValue.text)
    }
    val detectedColors = remember(codeFieldValue.text) {
        KotlinCodeEngine.detectColorCodes(codeFieldValue.text)
    }

    val syntaxVisualTransformation = remember(syntaxColors, codeFieldValue.selection.end, diagnostics) {
        KotlinCodeEngine.KotlinSyntaxVisualTransformation(
            colors = syntaxColors,
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
        if (section.showDivider) {
            SectionDividerBar(
                sectionNumber = section.orderIndex + 1,
                onRemoveDivider = onToggleDivider,
                testTag = "top_divider_section_${section.orderIndex}"
            )
        } else {
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

        // 2. Unified Section Card (Header + Auto-Expanding Kotlin IDE Code Block + Paired Explanation)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isCodeFocused || isExplanationFocused) 1.5.dp else 1.dp,
                    color = when {
                        diagnostics.any { it.severity == DiagnosticSeverity.ERROR } -> Color(0xFFEF4444)
                        isCodeFocused || isExplanationFocused -> MaterialTheme.colorScheme.primary
                        else -> syntaxColors.codeBorder
                    },
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // A. Section Header: Section Badge + Editable/AI Title + Syntax Status + IDE Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(syntaxColors.codeHeaderBackground)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "KT #${section.orderIndex + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = section.sectionTitle,
                            onValueChange = onSectionTitleChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (section.sectionTitle.isBlank()) {
                                    Text(
                                        text = if (section.isNamingInProgress) {
                                            "AI naming section…"
                                        } else {
                                            "Section ${section.orderIndex + 1} (Tap ✨ or add next block to AI-name)"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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

                    // Live Syntax Status Pill
                    if (codeFieldValue.text.isNotBlank()) {
                        val hasError = diagnostics.any { it.severity == DiagnosticSeverity.ERROR }
                        val statusColor = when {
                            hasError -> Color(0xFFEF4444)
                            diagnostics.isNotEmpty() -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
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
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (diagnostics.isEmpty()) "OK" else "${diagnostics.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // AI Name / Rename Section button
                    IconButton(
                        onClick = onRequestAiNaming,
                        enabled = !section.isNamingInProgress,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("ai_name_button_${section.orderIndex}")
                    ) {
                        if (section.isNamingInProgress) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(15.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Generate Section Name with AI",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Color Palette Picker button
                    IconButton(
                        onClick = { showColorPalettePicker = !showColorPalettePicker },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("color_palette_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Insert or Edit Color Code",
                            tint = if (showColorPalettePicker) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            },
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Language-specific Kotlin Formatter button
                    IconButton(
                        onClick = onFormatCode,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("format_kotlin_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Format Kotlin Code",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Copy Kotlin Code button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(section.codeContent))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Kotlin Code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Delete Section button
                    IconButton(
                        onClick = onDeleteSection,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_section_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Section",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // B. Interactive IDE Color Palette Picker Strip (when toggled)
                AnimatedVisibility(visible = showColorPalettePicker) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(syntaxColors.codeHeaderBackground.copy(alpha = 0.85f))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Insert Color:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IDE_COLOR_PRESETS.forEach { preset ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = 1.dp,
                                        color = syntaxColors.codeBorder,
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
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // C. Auto-Expanding 4-Line Kotlin Code Block Editor with Smart Auto-Pairs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(syntaxColors.codeBackground)
                        .heightIn(min = minCodeEditorHeight)
                        .bringIntoViewRequester(codeBringIntoViewRequester)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Line Numbers Gutter with per-line error dot indicator
                    Column(
                        modifier = Modifier
                            .width(42.dp)
                            .padding(start = 6.dp, end = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (lineNum in 1..visibleLines) {
                            val lineDiag = diagnostics.firstOrNull { it.line == lineNum }
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
                                                    Color(0xFFEF4444)
                                                } else {
                                                    Color(0xFFF59E0B)
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
                                    color = if (lineDiag != null) {
                                        Color(0xFFEF4444)
                                    } else {
                                        syntaxColors.lineNumber
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
                            .background(syntaxColors.codeBorder)
                    )

                    // Kotlin Code Input with Live Syntax Highlighting & Auto-Closing Pairs
                    BasicTextField(
                        value = codeFieldValue,
                        onValueChange = { incomingValue ->
                            val processed = KotlinCodeEngine.processIdeTextEdit(
                                previous = codeFieldValue,
                                incoming = incomingValue
                            )
                            codeFieldValue = processed
                            onCodeChange(processed.text)
                            coroutineScope.launch {
                                try {
                                    codeBringIntoViewRequester.bringIntoView()
                                } catch (_: Exception) {
                                }
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 13.5.sp,
                            lineHeight = 22.sp,
                            color = syntaxColors.plainText
                        ),
                        visualTransformation = syntaxVisualTransformation,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = (visibleLines * 22).dp)
                                    .padding(horizontal = 10.dp)
                            ) {
                                if (codeFieldValue.text.isEmpty()) {
                                    Text(
                                        text = "// Write Kotlin code (4 lines min, auto-closes (), {}, \"\" & checks syntax)\nfun example() {\n    \n}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 13.sp,
                                        lineHeight = 22.sp,
                                        color = syntaxColors.comment.copy(alpha = 0.65f)
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
                                        try {
                                            codeBringIntoViewRequester.bringIntoView()
                                        } catch (_: Exception) {
                                        }
                                    }
                                }
                            }
                            .testTag("kotlin_code_input_${section.orderIndex}")
                    )
                }

                // D. Inline Detected Color Swatch Bar (shows live preview of any color literals in the code)
                AnimatedVisibility(visible = detectedColors.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(syntaxColors.codeHeaderBackground.copy(alpha = 0.6f))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("color_swatches_bar_${section.orderIndex}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Color Codes:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        detectedColors.forEachIndexed { idx, detected ->
                            val swatchColor = Color(detected.colorInt)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = 1.dp,
                                        color = syntaxColors.codeBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        // Cycle to the next preset color on tap for quick visual tuning
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
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(swatchColor)
                                        .border(0.5.dp, Color.Gray, RoundedCornerShape(3.dp))
                                )
                                Text(
                                    text = "${detected.rawToken} (L${detected.line})",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // E. Real-Time Syntax Diagnostics & 1-Tap Quick-Fix Panel
                AnimatedVisibility(visible = diagnostics.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEF4444).copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("syntax_diagnostics_panel_${section.orderIndex}"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        diagnostics.take(3).forEach { diag ->
                            val isErr = diag.severity == DiagnosticSeverity.ERROR
                            val accent = if (isErr) Color(0xFFEF4444) else Color(0xFFF59E0B)
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
                                        text = "Line ${diag.line}: ${diag.message}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (diag.quickFixLabel != null && diag.fixedCode != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
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
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = diag.quickFixLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // F. IDE Quick-Symbol & Autocomplete Strip (visible when code block is focused)
                AnimatedVisibility(visible = isCodeFocused) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(syntaxColors.codeHeaderBackground)
                    ) {
                        // 1. Quick Symbol Strip for fast mobile coding (`()`, `{}`, `""`, `->`, `=`, etc.)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            QUICK_SYMBOL_KEYS.forEach { sym ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = 1.dp,
                                            color = syntaxColors.codeBorder,
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
                                        }
                                ) {
                                    Text(
                                        text = sym.label,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
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
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        },
                                        label = {
                                            Text(
                                                text = suggestion.label,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 11.5.sp
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier.height(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = syntaxColors.codeBorder, thickness = 1.dp)

                // G. Paired Explanation Rich Text Editor
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(syntaxColors.explanationBackground)
                        .bringIntoViewRequester(explanationBringIntoViewRequester)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "EXPLANATION & CONTEXT • SECTION #${section.orderIndex + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${section.explanationStyle.fontChoice.badge} • ${section.explanationStyle.fontSizeSp}sp",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                        value = section.explanationText,
                        onValueChange = { updatedText ->
                            onExplanationChange(updatedText)
                            coroutineScope.launch {
                                try {
                                    explanationBringIntoViewRequester.bringIntoView()
                                } catch (_: Exception) {
                                }
                            }
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
                                    .heightIn(min = 48.dp)
                            ) {
                                if (section.explanationText.isEmpty()) {
                                    Text(
                                        text = "Explain how this Kotlin snippet works… (Bound to Section #${section.orderIndex + 1} until the next code block is placed)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                onExplanationFocusChanged(focusState.isFocused)
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(100)
                                        try {
                                            explanationBringIntoViewRequester.bringIntoView()
                                        } catch (_: Exception) {
                                        }
                                    }
                                }
                            }
                            .testTag("section_explanation_input_${section.orderIndex}")
                    )
                }
            }
        }

        // 3. Optional Bottom Divider separating this section from subsequent blocks
        if (section.showDivider) {
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
