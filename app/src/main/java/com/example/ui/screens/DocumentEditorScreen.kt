package com.example.ui.screens

import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    onUpdateSectionConceptTag: (Long, String) -> Unit = { _, _ -> },
    onJumpToSectionConcept: (Long) -> Unit = {},
    onExportMarkdown: () -> String = { "" },
    onFormatSectionCode: (Long) -> Unit,
    onRequestAiNaming: (Long) -> Unit,
    onRequestAiExplain: (Long) -> Unit = {},
    onRequestAiRefactor: (Long) -> Unit = {},
    onRequestAiKDoc: (Long) -> Unit = {},
    onToggleSectionDivider: (Long) -> Unit,
    onDeleteSection: (Long) -> Unit,
    onSetActiveTarget: (ActiveEditorTarget) -> Unit,
    onExitFocusMode: () -> Unit = {},
    onToggleFocusSection: (Long) -> Unit = {},
    onNavigateFocusedSection: (Int) -> Unit = {},
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
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var showConceptOutlineDialog by remember { mutableStateOf(false) }

    val focusedSection = if (uiState.isFocusedSingleSectionMode) {
        val targetId = uiState.focusedSectionId
            ?: (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId
            ?: (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId
        document.sections.firstOrNull { it.id == targetId }
    } else {
        null
    }

    BackHandler {
        if (focusedSection != null) {
            focusManager.clearFocus()
            onExitFocusMode()
        } else {
            onCloseDocument()
        }
    }

    val expectedAndroidKeyCode = AndroidKeyEvent.KEYCODE_A + (uiState.shortcutLetter - 'A')
    val listState = rememberLazyListState()
    val focusedSectionScrollState = rememberScrollState()

    // Smoothly scroll to newly inserted code block
    LaunchedEffect(uiState.newlyInsertedSectionId, focusedSection?.id) {
        val newId = uiState.newlyInsertedSectionId ?: return@LaunchedEffect
        if (focusedSection != null) {
            try {
                focusedSectionScrollState.animateScrollTo(0)
            } catch (_: Exception) {
            }
        } else {
            val idx = document.sections.indexOfFirst { it.id == newId }
            if (idx >= 0) {
                try {
                    listState.animateScrollToItem(index = idx + 2)
                } catch (_: Exception) {
                }
            }
        }
    }

    // YouTube-Comment-Style Slide-Up Viewport Positioning when switching between Code and Explanation
    LaunchedEffect(uiState.activeTarget, focusedSection?.id) {
        if (focusedSection != null) {
            delay(60)
            try {
                when (uiState.activeTarget) {
                    is ActiveEditorTarget.SectionCode -> {
                        focusedSectionScrollState.animateScrollTo(0)
                    }
                    is ActiveEditorTarget.SectionExplanation -> {
                        // Because the code block automatically docks into a compact 3-line header when
                        // writing an explanation, scrolling lightly brings the Explanation Block right to top eye-level!
                        val dockOffsetPx = with(density) { 88.dp.toPx().toInt() }
                        focusedSectionScrollState.animateScrollTo(
                            dockOffsetPx.coerceAtMost(focusedSectionScrollState.maxValue)
                        )
                    }
                    else -> {}
                }
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (focusedSection != null) {
                                    focusManager.clearFocus()
                                    onExitFocusMode()
                                } else {
                                    onCloseDocument()
                                }
                            },
                            modifier = Modifier.testTag("back_to_documents_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (focusedSection != null) {
                                    "Exit Single-Block Focus Mode"
                                } else {
                                    "Save and Back to Documents"
                                }
                            )
                        }
                    },
                    title = {
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
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
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
                        // 1-Tap Copy Full Notebook as Markdown (.md)
                        IconButton(
                            onClick = {
                                val md = onExportMarkdown()
                                if (md.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(md))
                                }
                            },
                            modifier = Modifier.testTag("export_markdown_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Full Notebook as Markdown"
                            )
                        }

                        IconButton(
                            onClick = onSaveNow,
                            modifier = Modifier.testTag("save_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Now"
                            )
                        }

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

                // =========================================================================
                // LEARNED CONCEPT TAGS NAVIGATOR BAR (Jump directly to any learned concept!)
                // =========================================================================
                if (document.sections.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("learned_concepts_navigator_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Open Concept Map Outline Dialog button
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showConceptOutlineDialog = true }
                                    .testTag("open_concept_map_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = "Open Learned Concept Map",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Concepts (${document.sections.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            document.sections.forEachIndexed { idx, sec ->
                                val isCurrentSection = focusedSection?.id == sec.id ||
                                    (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId == sec.id ||
                                    (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId == sec.id

                                Surface(
                                    color = if (isCurrentSection) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrentSection) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            onJumpToSectionConcept(sec.id)
                                            coroutineScope.launch {
                                                if (focusedSection != null) {
                                                    focusedSectionScrollState.animateScrollTo(0)
                                                } else {
                                                    try {
                                                        listState.animateScrollToItem(index = idx + 2)
                                                    } catch (_: Exception) {
                                                    }
                                                }
                                            }
                                        }
                                        .testTag("jump_concept_chip_${sec.orderIndex}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = if (isCurrentSection) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "§${idx + 1} ${sec.effectiveLearnedConceptTag}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = if (isCurrentSection) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrentSection) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Single-Section Focus Navigation & Viewport Angle Bar (shown when a main block is focused)
                AnimatedVisibility(visible = focusedSection != null) {
                    val activeSec = focusedSection
                    if (activeSec != null) {
                        val totalSections = document.sections.size
                        val currentNumber = activeSec.orderIndex + 1
                        Surface(
                            color = Color(0xFF141923),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("focused_section_mode_bar")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Exit Focus Mode button (Show All Main Blocks)
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            focusManager.clearFocus()
                                            onExitFocusMode()
                                        }
                                        .testTag("exit_focus_mode_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloseFullscreen,
                                            contentDescription = "Show All Blocks",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "All Blocks ($totalSections)",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }

                                // Quick Viewport Angle Switcher: Code Angle vs Explanation Angle
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val isCodeActive = uiState.activeTarget is ActiveEditorTarget.SectionCode
                                    val isExpActive = uiState.activeTarget is ActiveEditorTarget.SectionExplanation

                                    Surface(
                                        color = if (isCodeActive) Color(0xFF38BDF8).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                onSetActiveTarget(
                                                    ActiveEditorTarget.SectionCode(activeSec.id, activeSec.orderIndex)
                                                )
                                                coroutineScope.launch {
                                                    focusedSectionScrollState.animateScrollTo(0)
                                                }
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Terminal,
                                                contentDescription = null,
                                                tint = if (isCodeActive) Color(0xFF38BDF8) else Color(0xFF8B949E),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Code View",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.5.sp,
                                                color = if (isCodeActive) Color(0xFF38BDF8) else Color(0xFFE6EDF3)
                                            )
                                        }
                                    }

                                    Surface(
                                        color = if (isExpActive) Color(0xFF38BDF8).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                onSetActiveTarget(
                                                    ActiveEditorTarget.SectionExplanation(activeSec.id, activeSec.orderIndex)
                                                )
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = if (isExpActive) Color(0xFF38BDF8) else Color(0xFF8B949E),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Explain View",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.5.sp,
                                                color = if (isExpActive) Color(0xFF38BDF8) else Color(0xFFE6EDF3)
                                            )
                                        }
                                    }
                                }

                                // Previous / Next Main Block Switcher
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(
                                        onClick = { onNavigateFocusedSection(-1) },
                                        enabled = currentNumber > 1,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                            contentDescription = "Previous Main Block",
                                            tint = if (currentNumber > 1) Color(0xFFE6EDF3) else Color(0xFF8B949E).copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "#$currentNumber/$totalSections",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE6EDF3)
                                    )
                                    IconButton(
                                        onClick = { onNavigateFocusedSection(1) },
                                        enabled = currentNumber < totalSections,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Next Main Block",
                                            tint = if (currentNumber < totalSections) Color(0xFFE6EDF3) else Color(0xFF8B949E).copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

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
                                .padding(horizontal = 14.dp, vertical = 5.dp)
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
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss Banner",
                                    modifier = Modifier.size(14.dp)
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
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (focusedSection != null) {
                // =========================================================================
                // FOCUSED SINGLE-SECTION VIEWPORT MODE (YOUTUBE-STYLE SLIDE-UP & AUTO-SCROLL):
                // Only the active main section is displayed, and as you type or add extra lines
                // of code/explanation, `focusedSectionScrollState` automatically scrolls line-by-line
                // so your active cursor line stays right at eye-level above the keyboard!
                // =========================================================================
                val isCodeActive = (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId == focusedSection.id
                val isExplanationActive = (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId == focusedSection.id

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .verticalScroll(focusedSectionScrollState)
                        .padding(
                            start = 14.dp,
                            end = 14.dp,
                            top = 10.dp,
                            bottom = 260.dp // Generous scroll runway so newly added lines slide up smoothly above the keyboard
                        )
                        .testTag("single_section_focused_scroll_container"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KotlinCodeSectionCard(
                        section = focusedSection,
                        isCodeFocused = isCodeActive,
                        isExplanationFocused = isExplanationActive,
                        isSingleSectionFocusMode = true,
                        requestAutoFocus = uiState.newlyInsertedSectionId == focusedSection.id,
                        onAutoFocusConsumed = onClearNewSectionFocusFlag,
                        onCodeFocusChanged = { focused ->
                            if (focused) {
                                onSetActiveTarget(
                                    ActiveEditorTarget.SectionCode(focusedSection.id, focusedSection.orderIndex)
                                )
                            }
                        },
                        onExplanationFocusChanged = { focused ->
                            if (focused) {
                                onSetActiveTarget(
                                    ActiveEditorTarget.SectionExplanation(focusedSection.id, focusedSection.orderIndex)
                                )
                            }
                        },
                        onCodeChange = { newCode -> onUpdateSectionCode(focusedSection.id, newCode) },
                        onExplanationChange = { newExp -> onUpdateSectionExplanation(focusedSection.id, newExp) },
                        onSectionTitleChange = { newTitle -> onUpdateSectionTitle(focusedSection.id, newTitle) },
                        onConceptTagChange = { newTag -> onUpdateSectionConceptTag(focusedSection.id, newTag) },
                        onActiveCodeLineChanged = { cursorLine, _ ->
                            if (isCodeActive) {
                                coroutineScope.launch {
                                    // Smoothly slide the editor up by 22.dp for each line past line 3
                                    val targetDp = ((cursorLine - 3).coerceAtLeast(0) * 22).dp
                                    val targetPx = with(density) { targetDp.toPx().toInt() }
                                    focusedSectionScrollState.animateScrollTo(
                                        targetPx.coerceAtMost(focusedSectionScrollState.maxValue)
                                    )
                                }
                            }
                        },
                        onActiveExplanationLineChanged = { expLine ->
                            if (isExplanationActive) {
                                coroutineScope.launch {
                                    val baseDockDp = 88.dp
                                    val extraLineDp = ((expLine - 2).coerceAtLeast(0) * 24).dp
                                    val targetPx = with(density) { (baseDockDp + extraLineDp).toPx().toInt() }
                                    focusedSectionScrollState.animateScrollTo(
                                        targetPx.coerceAtMost(focusedSectionScrollState.maxValue)
                                    )
                                }
                            }
                        },
                        onFormatCode = { onFormatSectionCode(focusedSection.id) },
                        onRequestAiNaming = { onRequestAiNaming(focusedSection.id) },
                        onRequestAiExplain = { onRequestAiExplain(focusedSection.id) },
                        onRequestAiRefactor = { onRequestAiRefactor(focusedSection.id) },
                        onRequestAiKDoc = { onRequestAiKDoc(focusedSection.id) },
                        onToggleFocusMode = {
                            focusManager.clearFocus()
                            onExitFocusMode()
                        },
                        onToggleDivider = { onToggleSectionDivider(focusedSection.id) },
                        onDeleteSection = { onDeleteSection(focusedSection.id) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                onExitFocusMode()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloseFullscreen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Show All Sections",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = onInsertCodeBlock,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("bottom_add_code_block_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Next Code Block (Ctrl+${uiState.shortcutLetter})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // =========================================================================
                // FULL DOCUMENT OVERVIEW MODE
                // =========================================================================
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 760.dp),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 10.dp,
                        bottom = 84.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Document Title & Metadata Summary Header
                    item(key = "doc_header") {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicTextField(
                                value = document.title,
                                onValueChange = onTitleChange,
                                textStyle = MaterialTheme.typography.headlineSmall.copy(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (document.title.isEmpty()) {
                                        Text(
                                            text = "Document Title…",
                                            style = MaterialTheme.typography.headlineSmall,
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "${document.sections.size} Learned Concepts",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• ${document.totalCodeLines} Kotlin lines • ${document.totalWordCount} words",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• AI Quota: ${uiState.remainingAiRpmQuota}/10 RPM",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 2. Introductory Rich Text Area
                    item(key = "doc_intro") {
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
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "NOTEBOOK OVERVIEW & SUMMARY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
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
                                                .heightIn(min = 48.dp)
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
                            isSingleSectionFocusMode = false,
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
                            onConceptTagChange = { newTag -> onUpdateSectionConceptTag(section.id, newTag) },
                            onFormatCode = { onFormatSectionCode(section.id) },
                            onRequestAiNaming = { onRequestAiNaming(section.id) },
                            onRequestAiExplain = { onRequestAiExplain(section.id) },
                            onRequestAiRefactor = { onRequestAiRefactor(section.id) },
                            onRequestAiKDoc = { onRequestAiKDoc(section.id) },
                            onToggleFocusMode = { onToggleFocusSection(section.id) },
                            onToggleDivider = { onToggleSectionDivider(section.id) },
                            onDeleteSection = { onDeleteSection(section.id) }
                        )
                    }

                    // 4. Bottom Action Button to Append Next Code Block
                    item(key = "bottom_add_btn") {
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

    // =========================================================================
    // LEARNED CONCEPTS & CODE OUTLINE NAVIGATION MODAL
    // Allows jumping to any learned concept tag in Full View or Single-Section Focus Mode
    // =========================================================================
    if (showConceptOutlineDialog) {
        AlertDialog(
            onDismissRequest = { showConceptOutlineDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Learned Concepts Navigator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap any learned concept to jump directly to that part",
                            style = MaterialTheme.typography.labelSmall,
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    document.sections.forEachIndexed { idx, sec ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showConceptOutlineDialog = false
                                    onJumpToSectionConcept(sec.id)
                                    coroutineScope.launch {
                                        if (focusedSection == null) {
                                            try {
                                                listState.animateScrollToItem(index = idx + 2)
                                            } catch (_: Exception) {
                                            }
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(5.dp)
                                        ) {
                                            Text(
                                                text = "§${idx + 1}",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = sec.effectiveLearnedConceptTag,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = sec.displayTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${sec.lineCount} lines of Kotlin • ${if (sec.explanationText.isBlank()) "No explanation yet" else "Thorough explanation ready"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            showConceptOutlineDialog = false
                                            onToggleFocusSection(sec.id)
                                        }
                                ) {
                                    Text(
                                        text = "Focus",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showConceptOutlineDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
