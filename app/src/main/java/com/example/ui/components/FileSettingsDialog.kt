package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeSectionUiModel
import com.example.model.LanguageType
import com.example.ui.theme.AppThemeId
import com.example.ui.theme.JetBrainsMonoFontFamily

/**
 * File-specific settings dialog.
 * Differs from Main Studio Settings by focusing exclusively on active file properties:
 * - Active file name & extension
 * - Language selection & dedicated language directory (src/kotlin/, src/java/, src/python/, src/dart/)
 * - Per-file theme override
 * - File formatting & export options
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileSettingsDialog(
    activeSection: CodeSectionUiModel,
    folderTitle: String,
    onUpdateLanguage: (LanguageType) -> Unit,
    onUpdateThemeOverride: (AppThemeId?) -> Unit,
    onUpdateFileName: (String) -> Unit,
    onCopyFileBundle: () -> Unit,
    onCopyRawCode: () -> Unit,
    onCopyFolderMarkdown: () -> Unit,
    onRequestAiNaming: () -> Unit = {},
    onRequestAiExplain: () -> Unit = {},
    onOpenStudioSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    var fileNameInput by remember(activeSection.ktScribeFileName) { mutableStateOf(activeSection.ktScribeFileName) }
    var selectedIndentSpaces by remember { mutableStateOf(4) }
    var showLineNumbers by remember { mutableStateOf(true) }
    var wordWrapEnabled by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = activeSection.language.badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.border(1.dp, activeSection.language.badgeColor, RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = activeSection.language.shortLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = activeSection.language.badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Column {
                    Text(
                        text = "File Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Path: ${activeSection.language.directoryPath}${activeSection.ktScribeFileName}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. FILE NAME & DIRECTORY
                Text(
                    text = "File Name & Target Directory",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = activeSection.language.badgeColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Folder: $folderTitle  /  ${activeSection.language.directoryPath}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                OutlinedTextField(
                    value = fileNameInput,
                    onValueChange = {
                        fileNameInput = it
                        onUpdateFileName(it)
                    },
                    label = { Text("File Name", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("file_settings_name_input")
                )

                HorizontalDivider()

                // 2. LANGUAGE & DIRECTORY SPECIFICATION
                Text(
                    text = "Language & Directory Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Selecting a language places this file in its language directory with starter structure:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LanguageType.entries.forEach { lang ->
                        val isSelected = activeSection.language == lang
                        Surface(
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) lang.badgeColor else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onUpdateLanguage(lang)
                                }
                                .testTag("file_settings_lang_${lang.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(lang.badgeColor)
                                    )
                                    Column {
                                        Text(
                                            text = "${lang.displayName} (${lang.extension})",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Directory: ${lang.directoryPath}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Text(
                                        text = "Active",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = lang.badgeColor
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 3. PER-FILE THEME OVERRIDE
                Text(
                    text = "File Theme Override",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Override the Studio theme for this specific file, or inherit the global studio theme:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = activeSection.themeOverride == null,
                        onClick = { onUpdateThemeOverride(null) },
                        label = { Text("Inherit Studio Theme") },
                        modifier = Modifier.testTag("file_theme_inherit")
                    )
                    AppThemeId.entries.forEach { theme ->
                        FilterChip(
                            selected = activeSection.themeOverride == theme,
                            onClick = { onUpdateThemeOverride(theme) },
                            label = { Text(theme.displayName) },
                            modifier = Modifier.testTag("file_theme_${theme.name.lowercase()}")
                        )
                    }
                }

                HorizontalDivider()

                // 4. EDITOR FORMATTING
                Text(
                    text = "Formatting & Display",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tab Indent:",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    listOf(2, 4).forEach { spaces ->
                        FilterChip(
                            selected = selectedIndentSpaces == spaces,
                            onClick = { selectedIndentSpaces = spaces },
                            label = { Text("$spaces Spaces") },
                            modifier = Modifier.testTag("file_indent_$spaces")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showLineNumbers,
                        onClick = { showLineNumbers = !showLineNumbers },
                        label = { Text(if (showLineNumbers) "Line Numbers: On" else "Line Numbers: Off") },
                        modifier = Modifier.testTag("file_toggle_line_numbers")
                    )
                    FilterChip(
                        selected = wordWrapEnabled,
                        onClick = { wordWrapEnabled = !wordWrapEnabled },
                        label = { Text(if (wordWrapEnabled) "Wrap: On" else "Wrap: Off") },
                        modifier = Modifier.testTag("file_toggle_word_wrap")
                    )
                }

                HorizontalDivider()

                // 5. AI NAMING & EXPLANATION ACTIONS
                Text(
                    text = "AI Naming & Explanations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Generate a semantic name, concept tag, or detailed code explanation using integrated AI:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onRequestAiNaming()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("file_ai_naming_btn")
                    ) {
                        Text("AI Rename File", fontSize = 11.sp, maxLines = 1)
                    }
                    Button(
                        onClick = {
                            onRequestAiExplain()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("file_ai_explain_btn")
                    ) {
                        Text("AI Explain Code", fontSize = 11.sp, maxLines = 1)
                    }
                }

                HorizontalDivider()

                // 5. EXPORT & SHARING
                Text(
                    text = "Export Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopyFileBundle,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("file_copy_bundle_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy .ktscribe File Bundle", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = onCopyRawCode,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("file_copy_raw_btn")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Clean Raw Code (${activeSection.language.displayName})", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = onCopyFolderMarkdown,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("file_copy_md_btn")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Entire Folder as Markdown", fontSize = 11.sp)
                    }
                }

                HorizontalDivider()

                // 6. LINK TO GLOBAL STUDIO SETTINGS
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onDismiss()
                            onOpenStudioSettings()
                        }
                        .testTag("open_studio_settings_from_file")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Studio & Global Settings...",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Global themes, dark/light mode, shortcuts & RPM quota",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("file_settings_done_btn")
            ) {
                Text("Done")
            }
        }
    )
}
