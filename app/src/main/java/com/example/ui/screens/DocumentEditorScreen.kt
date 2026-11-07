package com.example.ui.screens

import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ActiveEditorTarget
import com.example.model.DocumentDetailUiModel
import com.example.model.EditorFontChoice
import com.example.model.TextAlignmentChoice
import com.example.ui.components.KotlinCodeSectionCard
import com.example.ui.components.RichTextToolbar
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.EditorUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEditorScreen(
    uiState: EditorUiState,
    document: DocumentDetailUiModel,
    onCloseDocument: () -> Unit,
    onSaveNow: () -> Unit,
    onTitleChange: (String) -> Unit,
    onIntroTextChange: (String) -> Unit,
    onInsertCodeBlock: () -> Unit,
    onUpdateSectionCode: (Long, String) -> Unit,
    onUpdateSectionExplanation: (Long, String) -> Unit,
    onUpdateSectionTitle: (Long, String) -> Unit,
    onFormatSectionCode: (Long) -> Unit,
    onRequestAiNaming: (Long) -> Unit,
    onToggleSectionDivider: (Long) -> Unit,
    onDeleteSection: (Long) -> Unit,
    onSetActiveTarget: (ActiveEditorTarget) -> Unit,
    onClearNewSectionFocusFlag: () -> Unit,
    onFontChoiceChange: (EditorFontChoice) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit,
    onAlignmentChange: (TextAlignmentChoice) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onCycleThemeMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ensure back press auto-saves immediately before returning to the document list
    BackHandler(onBack = onCloseDocument)

    val expectedAndroidKeyCode = AndroidKeyEvent.KEYCODE_A + (uiState.shortcutLetter - 'A')

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = onCloseDocument,
                            modifier = Modifier.testTag("back_to_documents_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Save and Back to Documents"
                            )
                        }
                    },
                    title = {
                        // Live Auto-Save Status Pill (30s countdown + immediate exit save)
                        Surface(
                            color = if (uiState.hasUnsavedChanges) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("autosave_status_pill")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.hasUnsavedChanges) {
                                        Icons.Default.Schedule
                                    } else {
                                        Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = if (uiState.hasUnsavedChanges) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.hasUnsavedChanges) {
                                        "Auto-save in ${uiState.secondsUntilAutoSave}s"
                                    } else {
                                        uiState.saveStatusBanner
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (uiState.hasUnsavedChanges) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }
                        }
                    },
                    actions = {
                        // Save Now button
                        IconButton(
                            onClick = onSaveNow,
                            modifier = Modifier.testTag("save_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Now"
                            )
                        }

                        // Ctrl+<Letter> Shortcut Setting Chip
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenSettings)
                                .testTag("editor_shortcut_settings_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = "Change Shortcut Key",
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ctrl+${uiState.shortcutLetter}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Dark / Light Mode Toggle
                        IconButton(
                            onClick = onCycleThemeMode,
                            modifier = Modifier.testTag("editor_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = when (uiState.themeMode) {
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                },
                                contentDescription = "Toggle Dark or Light Mode"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // Rich Text & Code Block Formatting Toolbar
                RichTextToolbar(
                    style = uiState.activeFormattingStyle,
                    shortcutLetter = uiState.shortcutLetter,
                    canUndo = uiState.canUndo,
                    canRedo = uiState.canRedo,
                    onInsertCodeBlock = onInsertCodeBlock,
                    onFontChoiceChange = onFontChoiceChange,
                    onFontSizeChange = onFontSizeChange,
                    onToggleBold = onToggleBold,
                    onToggleItalic = onToggleItalic,
                    onToggleUnderline = onToggleUnderline,
                    onToggleStrikethrough = onToggleStrikethrough,
                    onAlignmentChange = onAlignmentChange,
                    onUndo = onUndo,
                    onRedo = onRedo
                )

                // AI Section Naming Status / Error Banner
                AnimatedVisibility(
                    visible = uiState.aiStatusMessage != null || uiState.aiErrorBanner != null
                ) {
                    val isError = uiState.aiErrorBanner != null
                    val bannerText = uiState.aiErrorBanner ?: uiState.aiStatusMessage.orEmpty()
                    Surface(
                        color = if (isError) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_feedback_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = bannerText,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isError) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onDismissBanner,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss Banner",
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    keyEvent.isCtrlPressed &&
                    keyEvent.nativeKeyEvent.keyCode == expectedAndroidKeyCode
                ) {
                    onInsertCodeBlock()
                    true
                } else {
                    false
                }
            }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Document Title & Metadata Summary Header
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicTextField(
                            value = document.title,
                            onValueChange = onTitleChange,
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (document.title.isEmpty()) {
                                    Text(
                                        text = "Document Title…",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("document_title_input")
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${document.sections.size} Kotlin Sections",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• ${document.totalWordCount} words • ${document.totalCharacterCount} chars",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• Free-Tier AI: ${uiState.remainingAiRpmQuota}/10 RPM",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 2. Introductory Rich Text Area (above the first code block)
                item {
                    val introStyle = document.introStyle
                    val decorations = buildList {
                        if (introStyle.isUnderline) add(TextDecoration.Underline)
                        if (introStyle.isStrikethrough) add(TextDecoration.LineThrough)
                    }
                    val combinedDecoration = if (decorations.isEmpty()) {
                        TextDecoration.None
                    } else {
                        TextDecoration.combine(decorations)
                    }

                    val isIntroFocused = uiState.activeTarget is ActiveEditorTarget.Intro

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isIntroFocused) 1.5.dp else 1.dp,
                                color = if (isIntroFocused) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "DOCUMENT INTRODUCTION / PROSE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${introStyle.fontChoice.displayName} • ${introStyle.fontSizeSp}sp",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            BasicTextField(
                                value = document.introText,
                                onValueChange = onIntroTextChange,
                                textStyle = TextStyle(
                                    fontFamily = introStyle.fontChoice.toComposeFontFamily(),
                                    fontSize = introStyle.fontSizeSp.sp,
                                    lineHeight = (introStyle.fontSizeSp * 1.45f).sp,
                                    fontWeight = if (introStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (introStyle.isItalic) FontStyle.Italic else FontStyle.Normal,
                                    textDecoration = combinedDecoration,
                                    textAlign = introStyle.alignment.toComposeTextAlign(),
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 56.dp)
                                    ) {
                                        if (document.introText.isEmpty()) {
                                            Text(
                                                text = stringResource(
                                                    R.string.intro_placeholder,
                                                    uiState.shortcutLetter.toString()
                                                ),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            onSetActiveTarget(ActiveEditorTarget.Intro)
                                        }
                                    }
                                    .testTag("intro_text_input")
                            )
                        }
                    }
                }

                // 3. Ordered List of [Kotlin Code Block + Paired Explanation] Sections
                items(
                    items = document.sections,
                    key = { it.id }
                ) { section ->
                    val isCodeActive = (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId == section.id
                    val isExplanationActive = (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId == section.id

                    KotlinCodeSectionCard(
                        section = section,
                        isCodeFocused = isCodeActive,
                        isExplanationFocused = isExplanationActive,
                        requestAutoFocus = uiState.newlyInsertedSectionId == section.id,
                        onAutoFocusConsumed = onClearNewSectionFocusFlag,
                        onCodeFocusChanged = { focused ->
                            if (focused) {
                                onSetActiveTarget(
                                    ActiveEditorTarget.SectionCode(section.id, section.orderIndex)
                                )
                            }
                        },
                        onExplanationFocusChanged = { focused ->
                            if (focused) {
                                onSetActiveTarget(
                                    ActiveEditorTarget.SectionExplanation(section.id, section.orderIndex)
                                )
                            }
                        },
                        onCodeChange = { newCode -> onUpdateSectionCode(section.id, newCode) },
                        onExplanationChange = { newExp -> onUpdateSectionExplanation(section.id, newExp) },
                        onSectionTitleChange = { newTitle -> onUpdateSectionTitle(section.id, newTitle) },
                        onFormatCode = { onFormatSectionCode(section.id) },
                        onRequestAiNaming = { onRequestAiNaming(section.id) },
                        onToggleDivider = { onToggleSectionDivider(section.id) },
                        onDeleteSection = { onDeleteSection(section.id) }
                    )
                }

                // 4. Bottom Action Button to Append Next Code Block (and trigger AI naming on the previous one)
                item {
                    OutlinedButton(
                        onClick = onInsertCodeBlock,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("bottom_add_code_block_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Next Kotlin Code Block (Ctrl+${uiState.shortcutLetter}) & Auto-Name Previous Section",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
