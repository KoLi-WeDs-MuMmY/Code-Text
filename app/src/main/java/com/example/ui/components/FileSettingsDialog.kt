package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeSectionUiModel
import com.example.model.LanguageType
import com.example.ui.theme.AppThemeId
import com.example.ui.theme.JetBrainsMonoFontFamily

private enum class FileInspectorTab(val id: String, val label: String, val icon: ImageVector) {
    ALL("all", "All", Icons.Default.Tune),
    FILE_LANG("file_lang", "File & Language", Icons.Default.Folder),
    STYLE("style", "Style & Theme", Icons.Default.Palette),
    AI_EXPORT("ai_export", "AI & Export", Icons.Default.AutoAwesome)
}

/**
 * Shared utility for exporting, saving via Android Share/Save dialog, and copying code/bundle files
 * with immediate Toast and status banner confirmation.
 */
fun exportOrCopyCodePayload(
    context: android.content.Context,
    clipboardManager: ClipboardManager,
    fileName: String,
    content: String,
    launchShareSheet: Boolean,
    onStatusBanner: (String) -> Unit = {}
) {
    val safeContent = content.ifBlank { "// Empty file: $fileName" }
    clipboardManager.setText(AnnotatedString(safeContent))

    val feedbackMessage = if (launchShareSheet) {
        "✓ Exported & copied $fileName (${safeContent.lines().size} lines)"
    } else {
        "✓ Copied $fileName to clipboard (${safeContent.lines().size} lines)"
    }
    onStatusBanner(feedbackMessage)

    try {
        Toast.makeText(context, feedbackMessage, Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        // Ignore in headless Robolectric/unit test environments
    }

    if (launchShareSheet) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, fileName)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_TEXT, safeContent)
            }
            val chooser = Intent.createChooser(sendIntent, "Save or Share $fileName").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Safe fallback if no activity handles chooser in test environment
        }
    }
}

/**
 * Restructured iOS-style grouped File Configuration Inspector.
 * Preserves guarded language switching with mandatory confirmation popup.
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
    var pendingLanguageChange by remember { mutableStateOf<LanguageType?>(null) }
    var exportStatusFeedback by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableStateOf(FileInspectorTab.ALL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1917),
        tonalElevation = 12.dp,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = activeSection.language.badgeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(7.dp),
                        modifier = Modifier.border(1.dp, activeSection.language.badgeColor, RoundedCornerShape(7.dp))
                    ) {
                        Text(
                            text = activeSection.language.shortLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeSection.language.badgeColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "File Inspector • ${activeSection.nativeFileName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1DC)
                        )
                        Text(
                            text = "${activeSection.language.directoryPath}${activeSection.nativeFileName}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFFA39C93)
                        )
                    }
                }

                // Segmented iOS Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141210))
                        .border(0.5.dp, Color(0xFF36312C), RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FileInspectorTab.entries.forEach { tab ->
                        val selected = activeTab == tab
                        Surface(
                            color = if (selected) Color(0xFF332C27) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            border = if (selected) BorderStroke(0.5.dp, Color(0xFFD4883A).copy(alpha = 0.6f)) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { activeTab = tab }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = if (selected) Color(0xFFD4883A) else Color(0xFFA39C93),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = tab.label,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color(0xFFE6E1DC) else Color(0xFFA39C93)
                                )
                            }
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // =========================================================================
                // CARD 1: FILE IDENTITY & LANGUAGE TYPE
                // =========================================================================
                if (activeTab == FileInspectorTab.ALL || activeTab == FileInspectorTab.FILE_LANG) {
                    IosSettingsGroupCard(
                        headerTitle = "FILE IDENTITY & TARGET DIRECTORY",
                        headerSubtitle = "Folder: $folderTitle  •  ${activeSection.language.directoryPath}"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fileNameInput,
                                onValueChange = {
                                    fileNameInput = it
                                    onUpdateFileName(it)
                                },
                                label = { Text("File Name (extension locked to language)", fontSize = 11.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF161412),
                                    unfocusedContainerColor = Color(0xFF161412),
                                    focusedBorderColor = Color(0xFFD4883A),
                                    unfocusedBorderColor = Color(0xFF36312C)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("file_settings_name_input")
                            )

                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF36312C))

                            Text(
                                text = "Language & Compiler Target (Guarded Switch)",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1DC)
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                LanguageType.entries.forEach { lang ->
                                    val isSelected = activeSection.language == lang
                                    Surface(
                                        color = if (isSelected) Color(0xFF332922) else Color(0xFF1A1715),
                                        shape = RoundedCornerShape(9.dp),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 0.5.dp,
                                            color = if (isSelected) lang.badgeColor else Color(0xFF36312C)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(9.dp))
                                            .clickable {
                                                if (lang != activeSection.language) {
                                                    pendingLanguageChange = lang
                                                }
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
                                                        text = "${lang.displayName} (${lang.nativeExtension})",
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.5.sp,
                                                        color = Color(0xFFE6E1DC)
                                                    )
                                                    Text(
                                                        text = "Dir: ${lang.directoryPath} • Bundle: ${lang.containerExtension}",
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontSize = 9.5.sp,
                                                        color = Color(0xFFA39C93)
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
                        }
                    }
                }

                // =========================================================================
                // CARD 2: PER-FILE THEME OVERRIDE & FORMATTING
                // =========================================================================
                if (activeTab == FileInspectorTab.ALL || activeTab == FileInspectorTab.STYLE) {
                    IosSettingsGroupCard(
                        headerTitle = "PER-FILE THEME & CODE FORMATTING",
                        headerSubtitle = "Override the global theme for this file or adjust indentation & line numbers"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = activeSection.themeOverride == null,
                                    onClick = { onUpdateThemeOverride(null) },
                                    label = { Text("Inherit Studio Theme", fontSize = 10.5.sp) },
                                    modifier = Modifier.testTag("file_theme_inherit")
                                )
                                AppThemeId.entries.forEach { theme ->
                                    FilterChip(
                                        selected = activeSection.themeOverride == theme,
                                        onClick = { onUpdateThemeOverride(theme) },
                                        label = { Text(theme.displayName, fontSize = 10.5.sp) },
                                        modifier = Modifier.testTag("file_theme_${theme.name.lowercase()}")
                                    )
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF36312C))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tab Indent:",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFE6E1DC)
                                )
                                listOf(2, 4).forEach { spaces ->
                                    FilterChip(
                                        selected = selectedIndentSpaces == spaces,
                                        onClick = { selectedIndentSpaces = spaces },
                                        label = { Text("$spaces Spaces", fontSize = 10.5.sp) },
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
                                    label = { Text(if (showLineNumbers) "Line Numbers: On" else "Line Numbers: Off", fontSize = 10.5.sp) },
                                    modifier = Modifier.testTag("file_toggle_line_numbers")
                                )
                                FilterChip(
                                    selected = wordWrapEnabled,
                                    onClick = { wordWrapEnabled = !wordWrapEnabled },
                                    label = { Text(if (wordWrapEnabled) "Wrap: On" else "Wrap: Off", fontSize = 10.5.sp) },
                                    modifier = Modifier.testTag("file_toggle_word_wrap")
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // CARD 3: AI ACTIONS, EXPORT & GLOBAL STUDIO LINK
                // =========================================================================
                if (activeTab == FileInspectorTab.ALL || activeTab == FileInspectorTab.AI_EXPORT) {
                    IosSettingsGroupCard(
                        headerTitle = "AI TOOLS & EXPORT BUNDLES",
                        headerSubtitle = "Semantic file naming, AI explanations, and shareable code bundles"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4883A)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("file_ai_explain_btn")
                                ) {
                                    Text("AI Explain Code", fontSize = 11.sp, maxLines = 1, color = Color(0xFF161412), fontWeight = FontWeight.Bold)
                                }
                            }

                            exportStatusFeedback?.let { statusMsg ->
                                Surface(
                                    color = Color(0xFF1E3323),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF81C784),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = statusMsg,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.5.sp,
                                            color = Color(0xFFE6E1DC),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    exportStatusFeedback = "Exported & copied ${activeSection.containerFileName} bundle!"
                                    onCopyFileBundle()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("file_copy_bundle_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export & Copy ${activeSection.containerFileName} Bundle", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    exportStatusFeedback = "Exported & copied ${activeSection.nativeFileName} raw code!"
                                    onCopyRawCode()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("file_copy_raw_btn")
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export & Copy Raw Code (${activeSection.nativeFileName})", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    exportStatusFeedback = "Exported & copied Folder Markdown (${folderTitle}.md)!"
                                    onCopyFolderMarkdown()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("file_copy_md_btn")
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export Entire Folder as Markdown", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // =========================================================================
                // LINK TO GLOBAL STUDIO SETTINGS & SDK MANAGER
                // =========================================================================
                Surface(
                    color = Color(0xFF28221E),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.75.dp, Color(0xFFD4883A).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color(0xFFD4883A),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Studio Settings & SDK Toolchain Manager...",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1DC)
                            )
                            Text(
                                text = "Toggle built-in engines, Termux export guide, themes & RPM quota",
                                fontSize = 10.sp,
                                color = Color(0xFFA39C93)
                            )
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
                Text(
                    text = "Done",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4883A)
                )
            }
        }
    )

    // Mandatory Confirmation Warning Popup before changing File Extension / Language Type
    pendingLanguageChange?.let { targetLang ->
        AlertDialog(
            onDismissRequest = { pendingLanguageChange = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFFB86C),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Change File Type to ${targetLang.nativeExtension}?",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFF1FA8C)
                    )
                }
            },
            text = {
                Text(
                    text = "You are changing '${activeSection.nativeFileName}' from ${activeSection.language.displayName} (${activeSection.language.nativeExtension}) to ${targetLang.displayName} (${targetLang.nativeExtension}).\n\n" +
                        "• Your existing code contents will be kept as-is.\n" +
                        "• Warning: The current file contents may not run, compile, or debug for ${targetLang.nativeExtension} because the syntax and runtime language differ.\n\n" +
                        "Do you want to change the file extension to ${targetLang.nativeExtension}?",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFE6E1DC)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateLanguage(targetLang)
                        fileNameInput = "${activeSection.nativeFileName.substringBeforeLast(".")}${targetLang.nativeExtension}"
                        pendingLanguageChange = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4883A)),
                    modifier = Modifier.testTag("confirm_language_change_btn")
                ) {
                    Text(
                        text = "Change to ${targetLang.nativeExtension}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF161412)
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingLanguageChange = null },
                    modifier = Modifier.testTag("cancel_language_change_btn")
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = JetBrainsMonoFontFamily,
                        color = Color(0xFFE6E1DC)
                    )
                }
            },
            containerColor = Color(0xFF1F1C1A),
            tonalElevation = 10.dp
        )
    }
}
