package com.example.ui.screens

import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.DiagnosticSeverity
import com.example.code.KotlinCodeEngine
import com.example.model.ActiveEditorTarget
import com.example.model.DocumentDetailUiModel
import com.example.model.DocumentSummaryUiModel
import com.example.model.EditorFontChoice
import com.example.model.ExplanationWindowMode
import com.example.model.IslandDropdownMenu
import com.example.model.TextAlignmentChoice
import com.example.ui.components.CommandPaletteDropdownContent
import com.example.ui.components.FolderAndSectionsDropdownContent
import com.example.ui.components.KotlinCodeSectionCard
import com.example.ui.components.MacFloatingDynamicIsland
import com.example.ui.components.MacFloatingOrSplitExplanationWindow
import com.example.ui.components.ResizableIslandDropdownWindow
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.viewmodel.EditorUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DocumentEditorScreen(
    uiState: EditorUiState,
    document: DocumentDetailUiModel,
    allFolders: List<DocumentSummaryUiModel> = emptyList(),
    onCloseDocument: () -> Unit,
    onSelectFolder: (Long) -> Unit = {},
    onCreateNewFolder: (String) -> Unit = {},
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
    onToggleIslandDropdown: (IslandDropdownMenu) -> Unit = {},
    onCloseIslandDropdown: () -> Unit = {},
    onCycleExplanationWindowMode: () -> Unit = {},
    onSetExplanationWindowMode: (ExplanationWindowMode) -> Unit = {},
    onUpdateSplitRatio: (Float) -> Unit = {},
    onRunActiveSectionInTerminal: () -> Unit = {},
    onOpenVaultModal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val activeSection = remember(document.sections, uiState.focusedSectionId, uiState.activeTarget) {
        val targetId = uiState.focusedSectionId
            ?: (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId
            ?: (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId
        document.sections.firstOrNull { it.id == targetId } ?: document.sections.firstOrNull()
    }

    val syntaxErrors = remember(activeSection?.codeContent) {
        if (activeSection == null) emptyList()
        else KotlinCodeEngine.diagnoseKotlinSyntax(activeSection.codeContent)
            .filter { it.severity == DiagnosticSeverity.ERROR }
    }

    BackHandler {
        if (uiState.activeIslandDropdown != IslandDropdownMenu.NONE) {
            onCloseIslandDropdown()
        } else if (uiState.explanationWindowMode == ExplanationWindowMode.FLOATING_WINDOW) {
            onSetExplanationWindowMode(ExplanationWindowMode.HIDDEN)
        } else {
            focusManager.clearFocus()
            onCloseDocument()
        }
    }

    val expectedAndroidKeyCode = AndroidKeyEvent.KEYCODE_A + (uiState.shortcutLetter - 'A')
    val codeScrollState = rememberScrollState()

    LaunchedEffect(uiState.newlyInsertedSectionId, activeSection?.id) {
        if (uiState.newlyInsertedSectionId != null) {
            try {
                codeScrollState.animateScrollTo(0)
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = Color(0xFF090C12),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090C12))
            ) {
                // 1. Top-Center macOS Floating Dynamic Island Pill
                MacFloatingDynamicIsland(
                    activeFolder = document,
                    activeSection = activeSection,
                    activeDropdown = uiState.activeIslandDropdown,
                    explanationWindowMode = uiState.explanationWindowMode,
                    syntaxErrorCount = syntaxErrors.size,
                    hasUnsavedChanges = uiState.hasUnsavedChanges,
                    secondsUntilAutoSave = uiState.secondsUntilAutoSave,
                    shortcutLetter = uiState.shortcutLetter,
                    onToggleDropdown = onToggleIslandDropdown,
                    onCycleExplanationMode = onCycleExplanationWindowMode,
                    onSetExplanationMode = onSetExplanationWindowMode,
                    onRunKotlinCode = onRunActiveSectionInTerminal,
                    onOpenAllFoldersOverview = onCloseDocument,
                    onInsertNewSection = onInsertCodeBlock,
                    onOpenVaultModal = onOpenVaultModal,
                    onOpenSettings = onOpenSettings
                )

                // 2. Learned Concept Tags & Section Switcher Strip right below the Island
                if (document.sections.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("learned_concepts_navigator_bar"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        document.sections.forEachIndexed { idx, sec ->
                            val isCurrent = activeSection?.id == sec.id
                            Surface(
                                color = if (isCurrent) Color(0xFF0A84FF).copy(alpha = 0.25f) else Color(0xFF141923),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrent) Color(0xFF38BDF8) else Color(0xFF262F40),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        onJumpToSectionConcept(sec.id)
                                        coroutineScope.launch {
                                            codeScrollState.animateScrollTo(0)
                                        }
                                    }
                                    .testTag("jump_concept_chip_${sec.orderIndex}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = if (isCurrent) Color(0xFF38BDF8) else Color(0xFF10B981),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "§${idx + 1} ${sec.effectiveLearnedConceptTag}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = Color(0xFFF1F5F9)
                                    )
                                }
                            }
                        }

                        // Previous / Next Section Quick Buttons
                        if (document.sections.size > 1 && activeSection != null) {
                            val curNum = activeSection.orderIndex + 1
                            IconButton(
                                onClick = { onNavigateFocusedSection(-1) },
                                enabled = curNum > 1,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Section",
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onNavigateFocusedSection(1) },
                                enabled = curNum < document.sections.size,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Section",
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Copy Full Folder as Markdown
                        Surface(
                            color = Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val md = onExportMarkdown()
                                    if (md.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(md))
                                    }
                                }
                                .testTag("export_markdown_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Folder Markdown",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copy .md",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }

                // 3. AI Feedback Toast Banner
                AnimatedVisibility(
                    visible = uiState.aiStatusMessage != null || uiState.aiErrorBanner != null
                ) {
                    val isError = uiState.aiErrorBanner != null
                    val bannerText = uiState.aiErrorBanner ?: uiState.aiStatusMessage.orEmpty()
                    Surface(
                        color = if (isError) Color(0xFF3B1219) else Color(0xFF0E2923),
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
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = if (isError) Color(0xFFFF8080) else Color(0xFF34D399),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(
                                onClick = onDismissBanner,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss Banner",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
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
        // Keyboard-Aware Full-Screen Workspace Container (`imePadding` resizes when soft keyboard opens!)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(Color(0xFF090C12))
        ) {
            val totalHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }
            val totalWidthPx = with(density) { maxWidth.toPx().coerceAtLeast(1f) }
            val isWideScreen = maxWidth >= 680.dp

            if (activeSection == null) {
                // Empty Folder fallback
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = onInsertCodeBlock) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create First Kotlin Code Section in Folder")
                    }
                }
            } else {
                val isCodeActive = (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId == activeSection.id
                val isExplanationActive = (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId == activeSection.id

                if (uiState.explanationWindowMode == ExplanationWindowMode.SPLIT_SCREEN) {
                    // =========================================================================
                    // TWO-WINDOW RESIZABLE SPLIT-SCREEN MODE ("SET ASIDE")
                    // Divides the screen into Window 1 (Apple cmd Code Editor) and Window 2
                    // (Explanation Window) with an interactive drag-to-resize split bar!
                    // =========================================================================
                    if (isWideScreen) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("two_window_split_screen_container")
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(uiState.splitScreenRatio)
                                    .fillMaxHeight()
                                    .verticalScroll(codeScrollState)
                            ) {
                                FullScreenAppleCmdSectionEditor(
                                    section = activeSection,
                                    isCodeActive = isCodeActive,
                                    isExplanationActive = isExplanationActive,
                                    requestAutoFocus = uiState.newlyInsertedSectionId == activeSection.id,
                                    onAutoFocusConsumed = onClearNewSectionFocusFlag,
                                    onSetActiveTarget = onSetActiveTarget,
                                    onUpdateSectionCode = onUpdateSectionCode,
                                    onUpdateSectionExplanation = onUpdateSectionExplanation,
                                    onUpdateSectionTitle = onUpdateSectionTitle,
                                    onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                    onActiveCodeLineChanged = { cursorLine, _ ->
                                        coroutineScope.launch {
                                            val targetDp = ((cursorLine - 3).coerceAtLeast(0) * 22).dp
                                            val targetPx = with(density) { targetDp.toPx().toInt() }
                                            codeScrollState.animateScrollTo(
                                                targetPx.coerceAtMost(codeScrollState.maxValue)
                                            )
                                        }
                                    },
                                    onFormatSectionCode = onFormatSectionCode,
                                    onRequestAiNaming = onRequestAiNaming,
                                    onRequestAiExplain = onRequestAiExplain,
                                    onRequestAiRefactor = onRequestAiRefactor,
                                    onRequestAiKDoc = onRequestAiKDoc,
                                    onToggleSectionDivider = onToggleSectionDivider,
                                    onDeleteSection = onDeleteSection
                                )
                            }

                            // Vertical Draggable Split Handle
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(14.dp)
                                    .pointerInput(totalWidthPx) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val deltaRatio = dragAmount.x / totalWidthPx
                                            onUpdateSplitRatio(uiState.splitScreenRatio + deltaRatio)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF38BDF8))
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f - uiState.splitScreenRatio)
                                    .fillMaxHeight()
                            ) {
                                MacFloatingOrSplitExplanationWindow(
                                    section = activeSection,
                                    windowMode = ExplanationWindowMode.SPLIT_SCREEN,
                                    formattingStyle = uiState.activeFormattingStyle,
                                    onExplanationChange = { onUpdateSectionExplanation(activeSection.id, it) },
                                    onConceptTagChange = { onUpdateSectionConceptTag(activeSection.id, it) },
                                    onRequestAiExplain = { onRequestAiExplain(activeSection.id) },
                                    onToggleSplitMode = {
                                        onSetExplanationWindowMode(ExplanationWindowMode.FLOATING_WINDOW)
                                    },
                                    onCloseWindow = {
                                        onSetExplanationWindowMode(ExplanationWindowMode.HIDDEN)
                                    },
                                    onFontChoiceChange = onFontChoiceChange,
                                    onFontSizeChange = onFontSizeChange,
                                    onToggleBold = onToggleBold,
                                    onToggleItalic = onToggleItalic,
                                    onToggleUnderline = onToggleUnderline,
                                    onAlignmentChange = onAlignmentChange
                                )
                            }
                        }
                    } else {
                        // Portrait Top / Bottom Resizable Two-Window Split
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("two_window_split_screen_container")
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(uiState.splitScreenRatio)
                                    .fillMaxWidth()
                                    .verticalScroll(codeScrollState)
                            ) {
                                FullScreenAppleCmdSectionEditor(
                                    section = activeSection,
                                    isCodeActive = isCodeActive,
                                    isExplanationActive = isExplanationActive,
                                    requestAutoFocus = uiState.newlyInsertedSectionId == activeSection.id,
                                    onAutoFocusConsumed = onClearNewSectionFocusFlag,
                                    onSetActiveTarget = onSetActiveTarget,
                                    onUpdateSectionCode = onUpdateSectionCode,
                                    onUpdateSectionExplanation = onUpdateSectionExplanation,
                                    onUpdateSectionTitle = onUpdateSectionTitle,
                                    onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                    onActiveCodeLineChanged = { cursorLine, _ ->
                                        coroutineScope.launch {
                                            val targetDp = ((cursorLine - 3).coerceAtLeast(0) * 22).dp
                                            val targetPx = with(density) { targetDp.toPx().toInt() }
                                            codeScrollState.animateScrollTo(
                                                targetPx.coerceAtMost(codeScrollState.maxValue)
                                            )
                                        }
                                    },
                                    onFormatSectionCode = onFormatSectionCode,
                                    onRequestAiNaming = onRequestAiNaming,
                                    onRequestAiExplain = onRequestAiExplain,
                                    onRequestAiRefactor = onRequestAiRefactor,
                                    onRequestAiKDoc = onRequestAiKDoc,
                                    onToggleSectionDivider = onToggleSectionDivider,
                                    onDeleteSection = onDeleteSection
                                )
                            }

                            // Horizontal Draggable Split Divider Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .pointerInput(totalHeightPx) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val deltaRatio = dragAmount.y / totalHeightPx
                                            onUpdateSplitRatio(uiState.splitScreenRatio + deltaRatio)
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "Drag to resize split windows",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Drag to Resize 2 Windows (${(uiState.splitScreenRatio * 100).toInt()}% / ${((1f - uiState.splitScreenRatio) * 100).toInt()}%)",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f - uiState.splitScreenRatio)
                                    .fillMaxWidth()
                            ) {
                                MacFloatingOrSplitExplanationWindow(
                                    section = activeSection,
                                    windowMode = ExplanationWindowMode.SPLIT_SCREEN,
                                    formattingStyle = uiState.activeFormattingStyle,
                                    onExplanationChange = { onUpdateSectionExplanation(activeSection.id, it) },
                                    onConceptTagChange = { onUpdateSectionConceptTag(activeSection.id, it) },
                                    onRequestAiExplain = { onRequestAiExplain(activeSection.id) },
                                    onToggleSplitMode = {
                                        onSetExplanationWindowMode(ExplanationWindowMode.FLOATING_WINDOW)
                                    },
                                    onCloseWindow = {
                                        onSetExplanationWindowMode(ExplanationWindowMode.HIDDEN)
                                    },
                                    onFontChoiceChange = onFontChoiceChange,
                                    onFontSizeChange = onFontSizeChange,
                                    onToggleBold = onToggleBold,
                                    onToggleItalic = onToggleItalic,
                                    onToggleUnderline = onToggleUnderline,
                                    onAlignmentChange = onAlignmentChange
                                )
                            }
                        }
                    }
                } else {
                    // =========================================================================
                    // FULL-SCREEN APPLE `cmd` CODE WORKSPACE + DRAGGABLE/RESIZABLE FLOATING EXPLANATION WINDOW
                    // =========================================================================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(codeScrollState)
                            .padding(
                                start = 10.dp,
                                end = 10.dp,
                                top = 4.dp,
                                bottom = 280.dp // Extra bottom runway so newly typed lines auto-scroll above keyboard
                            )
                            .testTag("single_section_focused_scroll_container")
                    ) {
                        FullScreenAppleCmdSectionEditor(
                            section = activeSection,
                            isCodeActive = isCodeActive,
                            isExplanationActive = isExplanationActive,
                            requestAutoFocus = uiState.newlyInsertedSectionId == activeSection.id,
                            onAutoFocusConsumed = onClearNewSectionFocusFlag,
                            onSetActiveTarget = onSetActiveTarget,
                            onUpdateSectionCode = onUpdateSectionCode,
                            onUpdateSectionExplanation = onUpdateSectionExplanation,
                            onUpdateSectionTitle = onUpdateSectionTitle,
                            onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                            onActiveCodeLineChanged = { cursorLine, _ ->
                                coroutineScope.launch {
                                    val targetDp = ((cursorLine - 3).coerceAtLeast(0) * 22).dp
                                    val targetPx = with(density) { targetDp.toPx().toInt() }
                                    codeScrollState.animateScrollTo(
                                        targetPx.coerceAtMost(codeScrollState.maxValue)
                                    )
                                }
                            },
                            onFormatSectionCode = onFormatSectionCode,
                            onRequestAiNaming = onRequestAiNaming,
                            onRequestAiExplain = onRequestAiExplain,
                            onRequestAiRefactor = onRequestAiRefactor,
                            onRequestAiKDoc = onRequestAiKDoc,
                            onToggleSectionDivider = onToggleSectionDivider,
                            onDeleteSection = onDeleteSection
                        )
                    }

                    // Draggable & Resizable Floating Explanation Window Overlay (macOS Notes aesthetic)
                    if (uiState.explanationWindowMode == ExplanationWindowMode.FLOATING_WINDOW) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            MacFloatingOrSplitExplanationWindow(
                                section = activeSection,
                                windowMode = ExplanationWindowMode.FLOATING_WINDOW,
                                formattingStyle = uiState.activeFormattingStyle,
                                onExplanationChange = { onUpdateSectionExplanation(activeSection.id, it) },
                                onConceptTagChange = { onUpdateSectionConceptTag(activeSection.id, it) },
                                onRequestAiExplain = { onRequestAiExplain(activeSection.id) },
                                onToggleSplitMode = {
                                    onSetExplanationWindowMode(ExplanationWindowMode.SPLIT_SCREEN)
                                },
                                onCloseWindow = {
                                    onSetExplanationWindowMode(ExplanationWindowMode.HIDDEN)
                                },
                                onFontChoiceChange = onFontChoiceChange,
                                onFontSizeChange = onFontSizeChange,
                                onToggleBold = onToggleBold,
                                onToggleItalic = onToggleItalic,
                                onToggleUnderline = onToggleUnderline,
                                onAlignmentChange = onAlignmentChange
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // TOP FLOATING DYNAMIC ISLAND RESIZABLE DROP-DOWN WINDOWS OVERLAY
            // =========================================================================
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (uiState.activeIslandDropdown) {
                    IslandDropdownMenu.COMMAND_PALETTE -> {
                        ResizableIslandDropdownWindow(
                            title = "⌘P Command Palette",
                            subtitle = "Run a command, jump to a folder, section, or symbol",
                            initialWidthDp = 540f,
                            initialHeightDp = 390f,
                            onClose = onCloseIslandDropdown
                        ) {
                            CommandPaletteDropdownContent(
                                activeFolder = document,
                                allFolders = allFolders,
                                activeSection = activeSection,
                                onExecuteCommand = { cmd ->
                                    when (cmd) {
                                        "ai_explain" -> activeSection?.let { onRequestAiExplain(it.id) }
                                        "ai_refactor" -> activeSection?.let { onRequestAiRefactor(it.id) }
                                        "format_code" -> activeSection?.let { onFormatSectionCode(it.id) }
                                        "toggle_split" -> onSetExplanationWindowMode(ExplanationWindowMode.SPLIT_SCREEN)
                                        "float_explain" -> onSetExplanationWindowMode(ExplanationWindowMode.FLOATING_WINDOW)
                                        "run_code" -> onRunActiveSectionInTerminal()
                                        "new_section" -> onInsertCodeBlock()
                                        "open_vault" -> onOpenVaultModal()
                                    }
                                },
                                onSelectFolder = onSelectFolder,
                                onJumpToSection = onJumpToSectionConcept,
                                onClose = onCloseIslandDropdown
                            )
                        }
                    }

                    IslandDropdownMenu.FOLDER_AND_SECTIONS -> {
                        ResizableIslandDropdownWindow(
                            title = "📁 Folders & Kotlin Sections",
                            subtitle = "Switch folders, manage sections, or create a new folder",
                            initialWidthDp = 540f,
                            initialHeightDp = 410f,
                            onClose = onCloseIslandDropdown
                        ) {
                            FolderAndSectionsDropdownContent(
                                activeFolder = document,
                                allFolders = allFolders,
                                activeSectionId = activeSection?.id,
                                onFolderTitleChange = onTitleChange,
                                onSelectFolder = onSelectFolder,
                                onCreateNewFolder = onCreateNewFolder,
                                onOpenAllFoldersGrid = {
                                    onCloseIslandDropdown()
                                    onCloseDocument()
                                },
                                onSelectSection = onJumpToSectionConcept,
                                onInsertNewSection = onInsertCodeBlock,
                                onDeleteSection = onDeleteSection
                            )
                        }
                    }

                    IslandDropdownMenu.AI_STUDIO -> {
                        ResizableIslandDropdownWindow(
                            title = "✨ AI Concept & Kotlin Studio",
                            subtitle = "Gemini 3.x • Free-Tier Safe (${uiState.remainingAiRpmQuota}/10 RPM available)",
                            initialWidthDp = 500f,
                            initialHeightDp = 320f,
                            onClose = onCloseIslandDropdown
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (activeSection != null) {
                                    Text(
                                        text = "Active Section: §${activeSection.orderIndex + 1} • ${activeSection.displayTitle}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Learned Concept Tag: [${activeSection.effectiveLearnedConceptTag}]",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onRequestAiExplain(activeSection.id)
                                                onCloseIslandDropdown()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Thorough AI Explain", color = Color(0xFF022C22), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = {
                                                onRequestAiRefactor(activeSection.id)
                                                onCloseIslandDropdown()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("AI Fix & Refactor", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onRequestAiNaming(activeSection.id)
                                                onCloseIslandDropdown()
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("AI Name & Tag Section", fontSize = 11.sp, color = Color.White)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                onRequestAiKDoc(activeSection.id)
                                                onCloseIslandDropdown()
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Add KDoc Comments", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    IslandDropdownMenu.TERMINAL_OUTPUT -> {
                        ResizableIslandDropdownWindow(
                            title = "⌘ Apple Terminal — kotlinc Execution",
                            subtitle = "zsh • Section §${(activeSection?.orderIndex ?: 0) + 1} Output",
                            initialWidthDp = 520f,
                            initialHeightDp = 280f,
                            onClose = onCloseIslandDropdown
                        ) {
                            Surface(
                                color = Color(0xFF070A0F),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            ) {
                                Text(
                                    text = uiState.terminalExecutionOutput
                                        ?: "$ kotlinc section.kt\nReady to execute.",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(12.dp)
                                )
                            }
                        }
                    }

                    IslandDropdownMenu.NONE -> Unit
                }
            }
        }
    }
}

@Composable
private fun FullScreenAppleCmdSectionEditor(
    section: com.example.model.CodeSectionUiModel,
    isCodeActive: Boolean,
    isExplanationActive: Boolean,
    requestAutoFocus: Boolean,
    onAutoFocusConsumed: () -> Unit,
    onSetActiveTarget: (ActiveEditorTarget) -> Unit,
    onUpdateSectionCode: (Long, String) -> Unit,
    onUpdateSectionExplanation: (Long, String) -> Unit,
    onUpdateSectionTitle: (Long, String) -> Unit,
    onUpdateSectionConceptTag: (Long, String) -> Unit,
    onActiveCodeLineChanged: (Int, Int) -> Unit,
    onFormatSectionCode: (Long) -> Unit,
    onRequestAiNaming: (Long) -> Unit,
    onRequestAiExplain: (Long) -> Unit,
    onRequestAiRefactor: (Long) -> Unit,
    onRequestAiKDoc: (Long) -> Unit,
    onToggleSectionDivider: (Long) -> Unit,
    onDeleteSection: (Long) -> Unit
) {
    KotlinCodeSectionCard(
        section = section,
        isCodeFocused = isCodeActive,
        isExplanationFocused = isExplanationActive,
        isSingleSectionFocusMode = true,
        hideInlineExplanation = true, // Explanation is rendered in the Resizable Floating or Split Window!
        requestAutoFocus = requestAutoFocus,
        onAutoFocusConsumed = onAutoFocusConsumed,
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
        onActiveCodeLineChanged = onActiveCodeLineChanged,
        onFormatCode = { onFormatSectionCode(section.id) },
        onRequestAiNaming = { onRequestAiNaming(section.id) },
        onRequestAiExplain = { onRequestAiExplain(section.id) },
        onRequestAiRefactor = { onRequestAiRefactor(section.id) },
        onRequestAiKDoc = { onRequestAiKDoc(section.id) },
        onToggleFocusMode = {},
        onToggleDivider = { onToggleSectionDivider(section.id) },
        onDeleteSection = { onDeleteSection(section.id) }
    )
}
