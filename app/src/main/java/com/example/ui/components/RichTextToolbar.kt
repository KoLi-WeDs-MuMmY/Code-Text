package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.EditorFontChoice
import com.example.model.TextAlignmentChoice
import com.example.model.TextFormattingStyle

@Composable
fun RichTextToolbar(
    style: TextFormattingStyle,
    shortcutLetter: Char,
    canUndo: Boolean,
    canRedo: Boolean,
    onInsertCodeBlock: () -> Unit,
    onFontChoiceChange: (EditorFontChoice) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit,
    onAlignmentChange: (TextAlignmentChoice) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFontMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Primary + Code Block button with live Ctrl+<letter> shortcut label
            FilledTonalButton(
                onClick = onInsertCodeBlock,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("insert_code_block_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Insert Kotlin Code Block",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+ Code Block (Ctrl+$shortcutLetter)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            VerticalDivider(modifier = Modifier.height(28.dp))

            // Font Family Picker
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { showFontMenu = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("font_family_selector"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = "Select Font Family",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = style.fontChoice.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                DropdownMenu(
                    expanded = showFontMenu,
                    onDismissRequest = { showFontMenu = false }
                ) {
                    EditorFontChoice.entries.forEach { font ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = font.displayName,
                                        fontFamily = font.toComposeFontFamily(),
                                        fontWeight = if (font == style.fontChoice) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "(${font.badge})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onFontChoiceChange(font)
                                showFontMenu = false
                            }
                        )
                    }
                }
            }

            // Font Size Stepper + Dropdown
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("font_size_selector"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onFontSizeChange((style.fontSizeSp - 2).coerceAtLeast(12)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease Font Size",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showSizeMenu = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Font Size",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${style.fontSizeSp}sp",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = { onFontSizeChange((style.fontSizeSp + 2).coerceAtMost(32)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase Font Size",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSizeMenu,
                    onDismissRequest = { showSizeMenu = false }
                ) {
                    TextFormattingStyle.AVAILABLE_FONT_SIZES.forEach { sizeSp ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${sizeSp}sp",
                                    fontWeight = if (sizeSp == style.fontSizeSp) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onFontSizeChange(sizeSp)
                                showSizeMenu = false
                            }
                        )
                    }
                }
            }

            VerticalDivider(modifier = Modifier.height(28.dp))

            // Bold / Italic / Underline / Strikethrough toggles
            FormatToggleButton(
                icon = Icons.Default.FormatBold,
                contentDescription = "Bold",
                selected = style.isBold,
                onClick = onToggleBold,
                testTag = "toggle_bold_button"
            )
            FormatToggleButton(
                icon = Icons.Default.FormatItalic,
                contentDescription = "Italic",
                selected = style.isItalic,
                onClick = onToggleItalic,
                testTag = "toggle_italic_button"
            )
            FormatToggleButton(
                icon = Icons.Default.FormatUnderlined,
                contentDescription = "Underline",
                selected = style.isUnderline,
                onClick = onToggleUnderline,
                testTag = "toggle_underline_button"
            )
            FormatToggleButton(
                icon = Icons.Default.FormatStrikethrough,
                contentDescription = "Strikethrough",
                selected = style.isStrikethrough,
                onClick = onToggleStrikethrough,
                testTag = "toggle_strikethrough_button"
            )

            VerticalDivider(modifier = Modifier.height(28.dp))

            // Text Alignment toggles
            FormatToggleButton(
                icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                contentDescription = "Align Left",
                selected = style.alignment == TextAlignmentChoice.LEFT,
                onClick = { onAlignmentChange(TextAlignmentChoice.LEFT) },
                testTag = "align_left_button"
            )
            FormatToggleButton(
                icon = Icons.Default.FormatAlignCenter,
                contentDescription = "Align Center",
                selected = style.alignment == TextAlignmentChoice.CENTER,
                onClick = { onAlignmentChange(TextAlignmentChoice.CENTER) },
                testTag = "align_center_button"
            )
            FormatToggleButton(
                icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                contentDescription = "Align Right",
                selected = style.alignment == TextAlignmentChoice.RIGHT,
                onClick = { onAlignmentChange(TextAlignmentChoice.RIGHT) },
                testTag = "align_right_button"
            )

            VerticalDivider(modifier = Modifier.height(28.dp))

            // Undo & Redo
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.testTag("undo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo"
                )
            }
            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.testTag("redo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo"
                )
            }
        }
    }
}

@Composable
private fun FormatToggleButton(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bgColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val tintColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = Modifier.size(18.dp)
        )
    }
}
