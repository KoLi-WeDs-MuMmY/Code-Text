package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.code.KotlinCodeEngine
import com.example.model.CodeSectionUiModel
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalKotlinSyntaxColors

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

    // Maintain TextFieldValue so cursor selection works smoothly with autocomplete insertions
    var codeFieldValue by remember(section.id) {
        mutableStateOf(
            TextFieldValue(
                text = section.codeContent,
                selection = TextRange(section.codeContent.length)
            )
        )
    }

    // Keep text synchronized when external formatting or undo/redo updates section.codeContent
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
            } catch (_: Exception) {
            }
            onAutoFocusConsumed()
        }
    }

    val syntaxVisualTransformation = remember(syntaxColors) {
        KotlinCodeEngine.KotlinSyntaxVisualTransformation(syntaxColors)
    }

    val (currentPrefix, suggestions) = remember(codeFieldValue.text, codeFieldValue.selection.end) {
        KotlinCodeEngine.getAutocompleteSuggestions(
            code = codeFieldValue.text,
            cursorPosition = codeFieldValue.selection.end
        )
    }

    // Minimum 4 lines height, expanding automatically as lines of code increase
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
        // 1. Optional/Removable Top Section Divider separating this Code Block Section from above content
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

        // 2. Unified Section Card (Header + Auto-Expanding Kotlin Code Block + Paired Explanation)
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
                    color = if (isCodeFocused || isExplanationFocused) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        syntaxColors.codeBorder
                    },
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // A. Section Header: Section Badge + Editable/AI Title + Kotlin Tools
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(syntaxColors.codeHeaderBackground)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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

                    // Editable Section Title (automatically populated by AI when next code block is placed)
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
                                            "AI naming section from code & explanation…"
                                        } else {
                                            "Section ${section.orderIndex + 1} (AI names when next block added)"
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

                    if (section.isAiNamed && !section.isNamingInProgress) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // AI Name / Rename Section button
                    IconButton(
                        onClick = onRequestAiNaming,
                        enabled = !section.isNamingInProgress,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("ai_name_button_${section.orderIndex}")
                    ) {
                        if (section.isNamingInProgress) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Generate Section Name with AI",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Language-specific Kotlin Formatter button
                    IconButton(
                        onClick = onFormatCode,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("format_kotlin_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Format Kotlin Code",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Copy Kotlin Code button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(section.codeContent))
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Kotlin Code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete Section button
                    IconButton(
                        onClick = onDeleteSection,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("delete_section_button_${section.orderIndex}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Section",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // B. Auto-Expanding 4-Line Kotlin Code Block Editor
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(syntaxColors.codeBackground)
                        .heightIn(min = minCodeEditorHeight)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Line Numbers Gutter (always renders at least 4 lines, expanding with content)
                    Column(
                        modifier = Modifier
                            .width(38.dp)
                            .padding(start = 8.dp, end = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (lineNum in 1..visibleLines) {
                            Box(
                                modifier = Modifier.height(lineHeightDp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = lineNum.toString(),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 12.sp,
                                    lineHeight = 22.sp,
                                    color = syntaxColors.lineNumber
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

                    // Kotlin Code Input with Live Syntax Highlighting
                    BasicTextField(
                        value = codeFieldValue,
                        onValueChange = { updatedValue ->
                            codeFieldValue = updatedValue
                            onCodeChange(updatedValue.text)
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
                                        text = "// Write Kotlin code (4 lines min, expands automatically)\nfun example() {\n    \n}",
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
                            .onFocusChanged { onCodeFocusChanged(it.isFocused) }
                            .testTag("kotlin_code_input_${section.orderIndex}")
                    )
                }

                // C. Live Kotlin Autocomplete Bar (visible while editing this code block)
                AnimatedVisibility(visible = isCodeFocused && suggestions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(syntaxColors.codeHeaderBackground)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("autocomplete_bar_${section.orderIndex}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (currentPrefix.isNotBlank()) "Suggest ($currentPrefix):" else "Kotlin:",
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
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = syntaxColors.codeBorder, thickness = 1.dp)

                // D. Paired Explanation Rich Text Editor
                // ("After each code block is written the next text till the next code block must be
                // considered as the part of code block + its explanation")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(syntaxColors.explanationBackground)
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
                        onValueChange = onExplanationChange,
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
                                    .heightIn(min = 52.dp)
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
                            .onFocusChanged { onExplanationFocusChanged(it.isFocused) }
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
