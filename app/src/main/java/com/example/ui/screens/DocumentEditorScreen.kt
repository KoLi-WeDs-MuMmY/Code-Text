package com.example.ui.screens

import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.model.CodeSectionUiModel
import com.example.model.DocumentDetailUiModel
import com.example.model.DocumentSummaryUiModel
import com.example.model.EditorFontChoice
import com.example.model.ExplanationWindowMode
import com.example.model.FloatingWindowType
import com.example.model.IslandDropdownMenu
import com.example.model.SplitScreenOrientation
import com.example.model.TextAlignmentChoice
import com.example.ui.components.CommandPaletteDropdownContent
import com.example.ui.components.ExplanationWindowBodyContent
import com.example.ui.components.FolderAndSectionsDropdownContent
import com.example.ui.components.KotlinCodeSectionCard
import com.example.ui.components.LeftRailIdeTool
import com.example.ui.components.MacDraggableFloatingWindow
import com.example.ui.components.ResizableIslandDropdownWindow
import com.example.ui.components.SketchLeftCircularIdeRail
import com.example.ui.components.SketchTopBarWithFourPills
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Tier0OuterTrenchBg
import com.example.ui.theme.Tier2CompletionSelectedBg
import com.example.ui.theme.Tier3FloatingWindowActiveBevel
import com.example.ui.theme.Tier3FloatingWindowActiveBg
import com.example.ui.theme.Tier3FloatingWindowActiveHeader
import com.example.viewmodel.EditorUiState

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
    onToggleFloatingWindow: (FloatingWindowType) -> Unit = {},
    onCloseFloatingWindow: (FloatingWindowType) -> Unit = {},
    onFocusFloatingWindow: (FloatingWindowType) -> Unit = {},
    onToggleIslandDropdown: (IslandDropdownMenu) -> Unit = {},
    onCloseIslandDropdown: () -> Unit = {},
    onCycleExplanationWindowMode: () -> Unit = {},
    onSetExplanationWindowMode: (ExplanationWindowMode) -> Unit = {},
    onUpdateSplitRatio: (Float) -> Unit = {},
    onDockWindowToSplitScreen: (FloatingWindowType) -> Unit = {},
    onUndockSplitWindowToFloating: () -> Unit = {},
    onCloseSplitScreenWindow: () -> Unit = {},
    onToggleSplitScreenOrientation: () -> Unit = {},
    onRunActiveSectionInTerminal: () -> Unit = {},
    onOpenVaultModal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current
    var activeLeftRailTool by remember { mutableStateOf(LeftRailIdeTool.NONE) }

    val activeSection = remember(document.sections, uiState.focusedSectionId, uiState.activeTarget) {
        val targetId = uiState.focusedSectionId
            ?: (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId
            ?: (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId
        document.sections.firstOrNull { it.id == targetId } ?: document.sections.firstOrNull()
    }

    val allDiagnostics = remember(activeSection?.codeContent) {
        if (activeSection == null) emptyList()
        else KotlinCodeEngine.diagnoseKotlinSyntax(activeSection.codeContent)
    }

    val syntaxErrors = remember(allDiagnostics) {
        allDiagnostics.filter { it.severity == DiagnosticSeverity.ERROR }
    }

    val effectiveSplitWindow: FloatingWindowType? = uiState.dockedSplitWindow
        ?: if (uiState.explanationWindowMode == ExplanationWindowMode.SPLIT_SCREEN) {
            FloatingWindowType.EXPLANATION
        } else {
            null
        }

    BackHandler {
        if (uiState.activeIslandDropdown != IslandDropdownMenu.NONE) {
            onCloseIslandDropdown()
        } else if (uiState.openFloatingWindows.isNotEmpty()) {
            onCloseFloatingWindow(uiState.openFloatingWindows.last())
        } else if (effectiveSplitWindow != null) {
            onCloseSplitScreenWindow()
        } else if (activeLeftRailTool != LeftRailIdeTool.NONE) {
            activeLeftRailTool = LeftRailIdeTool.NONE
        } else {
            focusManager.clearFocus()
            onCloseDocument()
        }
    }

    val expectedAndroidKeyCode = AndroidKeyEvent.KEYCODE_A + (uiState.shortcutLetter - 'A')

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = Tier0OuterTrenchBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tier0OuterTrenchBg)
            ) {
                // 1. Bezel-Safe Top Frame Bar:
                // Left back arrow `←` + `.ktscribe` file badge + Top-Right 4 horizontal oval pills
                SketchTopBarWithFourPills(
                    activeFolder = document,
                    activeSection = activeSection,
                    openFloatingWindows = uiState.openFloatingWindows,
                    activeDropdown = uiState.activeIslandDropdown,
                    syntaxErrorCount = syntaxErrors.size,
                    hasUnsavedChanges = uiState.hasUnsavedChanges,
                    secondsUntilAutoSave = uiState.secondsUntilAutoSave,
                    onBackToFolders = onCloseDocument,
                    onToggleFloatingWindow = onToggleFloatingWindow,
                    onToggleCommandPalette = {
                        onToggleIslandDropdown(IslandDropdownMenu.COMMAND_PALETTE)
                    },
                    onOpenVaultModal = onOpenVaultModal,
                    onOpenSettings = onOpenSettings
                )

                // 2. `.ktscribe` File Switcher Strip (1 File = 1 Code Block inside Folder)
                if (document.sections.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 3.dp)
                            .testTag("learned_concepts_navigator_bar"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        document.sections.forEachIndexed { _, sec ->
                            val isCurrent = activeSection?.id == sec.id
                            Surface(
                                color = if (isCurrent) Tier2CompletionSelectedBg else Color(0xFF1F1C1A),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrent) Color(0xFFD4883A) else Color(0xFF36312C),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        onJumpToSectionConcept(sec.id)
                                    }
                                    .testTag("jump_concept_chip_${sec.orderIndex}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (isCurrent) Color(0xFFE6E1DC) else Color(0xFFD4883A),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${sec.ktScribeFileName} • ${sec.effectiveLearnedConceptTag}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = Color(0xFFE6E1DC)
                                    )
                                }
                            }
                        }

                        if (document.sections.size > 1 && activeSection != null) {
                            val curNum = activeSection.orderIndex + 1
                            IconButton(
                                onClick = { onNavigateFocusedSection(-1) },
                                enabled = curNum > 1,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous .ktscribe File",
                                    tint = Color(0xFFA39C93),
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
                                    contentDescription = "Next .ktscribe File",
                                    tint = Color(0xFFA39C93),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Copy Active `.ktscribe` File Bundle or Full Folder
                        Surface(
                            color = Color(0xFF1F1C1A),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF36312C), RoundedCornerShape(8.dp))
                                .clickable {
                                    val payload = activeSection?.toKtScribeFilePayload(document.title)
                                        ?: onExportMarkdown()
                                    if (payload.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(payload))
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
                                    contentDescription = "Copy .ktscribe File",
                                    tint = Color(0xFFA39C93),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copy .ktscribe",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = Color(0xFFE6E1DC)
                                )
                            }
                        }
                    }
                }

                // 3. Feedback / Notification Toast Banner
                AnimatedVisibility(
                    visible = uiState.aiStatusMessage != null || uiState.aiErrorBanner != null
                ) {
                    val isError = uiState.aiErrorBanner != null
                    val bannerText = uiState.aiErrorBanner ?: uiState.aiStatusMessage.orEmpty()
                    Surface(
                        color = if (isError) Color(0xFF3B1A18) else Color(0xFF2B241E),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, Color(0xFF5C4B3B))
                            .testTag("ai_feedback_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = bannerText,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = if (isError) Color(0xFFF2B8B5) else Color(0xFFE6C39C),
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
                                    tint = Color(0xFFA39C93),
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
        // Keyboard-Aware Full-Screen Workspace Container (`imePadding` resizes when soft keyboard opens)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(Tier0OuterTrenchBg)
        ) {
            val totalHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }
            val totalWidthPx = with(density) { maxWidth.toPx().coerceAtLeast(1f) }

            if (activeSection == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onInsertCodeBlock,
                        colors = ButtonDefaults.buttonColors(containerColor = Tier2CompletionSelectedBg)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFE6E1DC))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create First .ktscribe File in Folder", color = Color(0xFFE6E1DC))
                    }
                }
            } else {
                val isCodeActive = (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId == activeSection.id
                val isExplanationActive = (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId == activeSection.id

                // OUTER WORKSPACE ROW:
                // - Left: Bezel-Safe Circular IDE Tool Rail (`SketchLeftCircularIdeRail`) with Undo, Toggle-Off & Collapse
                // - Right: Full-Screen 100%-Height Code Block (or Adjustable Side-by-Side / Bottom Split Screen)
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    SketchLeftCircularIdeRail(
                        activeTool = activeLeftRailTool,
                        syntaxErrorCount = syntaxErrors.size,
                        canUndo = uiState.canUndo,
                        onUndo = onUndo,
                        onFormatCode = { onFormatSectionCode(activeSection.id) },
                        onSelectTool = { activeLeftRailTool = it },
                        onApplyQuickFixOrDiagnostics = {
                            val firstFixable = allDiagnostics.firstOrNull { it.fixedCode != null }
                            if (firstFixable?.fixedCode != null) {
                                onUpdateSectionCode(activeSection.id, firstFixable.fixedCode)
                            } else {
                                activeLeftRailTool = if (activeLeftRailTool == LeftRailIdeTool.DIAGNOSTICS) {
                                    LeftRailIdeTool.NONE
                                } else {
                                    LeftRailIdeTool.DIAGNOSTICS
                                }
                            }
                        },
                        onInsertNewSection = onInsertCodeBlock
                    )

                    // Main Full-Screen Code Block / Adjustable Split-Screen Workspace
                    // Leaves bezel-safe breathing space (`end = 14.dp, bottom = 12.dp`) from curved screen edges
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 12.dp)
                    ) {
                        if (effectiveSplitWindow != null) {
                            // =========================================================================
                            // ADJUSTABLE SPLIT SCREEN (Converts ANY Floating Window into Side-by-Side
                            // or Bottom Split with a draggable resize handle)
                            // =========================================================================
                            if (uiState.splitScreenOrientation == SplitScreenOrientation.SIDE_BY_SIDE) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("two_window_split_screen_container")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(uiState.splitScreenRatio)
                                            .fillMaxHeight()
                                    ) {
                                        FullScreenAppleCmdSectionEditor(
                                            section = activeSection,
                                            isCodeActive = isCodeActive,
                                            isExplanationActive = isExplanationActive,
                                            activeLeftRailTool = activeLeftRailTool,
                                            canUndo = uiState.canUndo,
                                            requestAutoFocus = uiState.newlyInsertedSectionId == activeSection.id,
                                            onAutoFocusConsumed = onClearNewSectionFocusFlag,
                                            onSetActiveTarget = onSetActiveTarget,
                                            onUpdateSectionCode = onUpdateSectionCode,
                                            onUpdateSectionExplanation = onUpdateSectionExplanation,
                                            onUpdateSectionTitle = onUpdateSectionTitle,
                                            onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                            onActiveCodeLineChanged = { _, _ -> },
                                            onFormatSectionCode = onFormatSectionCode,
                                            onUndo = onUndo,
                                            onDeactivateLeftRailTool = {
                                                activeLeftRailTool = LeftRailIdeTool.NONE
                                            },
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
                                            .width(16.dp)
                                            .pointerInput(totalWidthPx) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val deltaRatio = dragAmount.x / totalWidthPx
                                                    onUpdateSplitRatio(uiState.splitScreenRatio + deltaRatio)
                                                }
                                            }
                                            .testTag("split_screen_resize_handle"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(54.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFD4883A))
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f - uiState.splitScreenRatio)
                                            .fillMaxHeight()
                                    ) {
                                        DockedSplitWindowContainer(
                                            windowType = effectiveSplitWindow,
                                            orientation = uiState.splitScreenOrientation,
                                            splitRatio = uiState.splitScreenRatio,
                                            activeFolder = document,
                                            allFolders = allFolders,
                                            activeSection = activeSection,
                                            uiState = uiState,
                                            onToggleOrientation = onToggleSplitScreenOrientation,
                                            onUndockToFloating = onUndockSplitWindowToFloating,
                                            onCloseSplit = onCloseSplitScreenWindow,
                                            onFolderTitleChange = onTitleChange,
                                            onSelectFolder = onSelectFolder,
                                            onCreateNewFolder = onCreateNewFolder,
                                            onCloseDocument = onCloseDocument,
                                            onJumpToSectionConcept = onJumpToSectionConcept,
                                            onInsertCodeBlock = onInsertCodeBlock,
                                            onDeleteSection = onDeleteSection,
                                            onUpdateSectionExplanation = onUpdateSectionExplanation,
                                            onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                            onFontChoiceChange = onFontChoiceChange,
                                            onToggleBold = onToggleBold,
                                            onToggleItalic = onToggleItalic,
                                            onToggleUnderline = onToggleUnderline,
                                            onRequestAiExplain = onRequestAiExplain,
                                            onRequestAiRefactor = onRequestAiRefactor,
                                            onRequestAiNaming = onRequestAiNaming,
                                            onRequestAiKDoc = onRequestAiKDoc,
                                            onRunActiveSectionInTerminal = onRunActiveSectionInTerminal
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("two_window_split_screen_container")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(uiState.splitScreenRatio)
                                            .fillMaxWidth()
                                    ) {
                                        FullScreenAppleCmdSectionEditor(
                                            section = activeSection,
                                            isCodeActive = isCodeActive,
                                            isExplanationActive = isExplanationActive,
                                            activeLeftRailTool = activeLeftRailTool,
                                            canUndo = uiState.canUndo,
                                            requestAutoFocus = uiState.newlyInsertedSectionId == activeSection.id,
                                            onAutoFocusConsumed = onClearNewSectionFocusFlag,
                                            onSetActiveTarget = onSetActiveTarget,
                                            onUpdateSectionCode = onUpdateSectionCode,
                                            onUpdateSectionExplanation = onUpdateSectionExplanation,
                                            onUpdateSectionTitle = onUpdateSectionTitle,
                                            onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                            onActiveCodeLineChanged = { _, _ -> },
                                            onFormatSectionCode = onFormatSectionCode,
                                            onUndo = onUndo,
                                            onDeactivateLeftRailTool = {
                                                activeLeftRailTool = LeftRailIdeTool.NONE
                                            },
                                            onRequestAiNaming = onRequestAiNaming,
                                            onRequestAiExplain = onRequestAiExplain,
                                            onRequestAiRefactor = onRequestAiRefactor,
                                            onRequestAiKDoc = onRequestAiKDoc,
                                            onToggleSectionDivider = onToggleSectionDivider,
                                            onDeleteSection = onDeleteSection
                                        )
                                    }

                                    // Horizontal Draggable Split Handle
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
                                            }
                                            .testTag("split_screen_resize_handle"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Surface(
                                            color = Color(0xFF23201E),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DragHandle,
                                                    contentDescription = "Drag to resize split windows",
                                                    tint = Color(0xFFD4883A),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Drag Split (${(uiState.splitScreenRatio * 100).toInt()}% / ${((1f - uiState.splitScreenRatio) * 100).toInt()}%)",
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 9.5.sp,
                                                    color = Color(0xFFA39C93)
                                                )
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f - uiState.splitScreenRatio)
                                            .fillMaxWidth()
                                    ) {
                                        DockedSplitWindowContainer(
                                            windowType = effectiveSplitWindow,
                                            orientation = uiState.splitScreenOrientation,
                                            splitRatio = uiState.splitScreenRatio,
                                            activeFolder = document,
                                            allFolders = allFolders,
                                            activeSection = activeSection,
                                            uiState = uiState,
                                            onToggleOrientation = onToggleSplitScreenOrientation,
                                            onUndockToFloating = onUndockSplitWindowToFloating,
                                            onCloseSplit = onCloseSplitScreenWindow,
                                            onFolderTitleChange = onTitleChange,
                                            onSelectFolder = onSelectFolder,
                                            onCreateNewFolder = onCreateNewFolder,
                                            onCloseDocument = onCloseDocument,
                                            onJumpToSectionConcept = onJumpToSectionConcept,
                                            onInsertCodeBlock = onInsertCodeBlock,
                                            onDeleteSection = onDeleteSection,
                                            onUpdateSectionExplanation = onUpdateSectionExplanation,
                                            onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                            onFontChoiceChange = onFontChoiceChange,
                                            onToggleBold = onToggleBold,
                                            onToggleItalic = onToggleItalic,
                                            onToggleUnderline = onToggleUnderline,
                                            onRequestAiExplain = onRequestAiExplain,
                                            onRequestAiRefactor = onRequestAiRefactor,
                                            onRequestAiNaming = onRequestAiNaming,
                                            onRequestAiKDoc = onRequestAiKDoc,
                                            onRunActiveSectionInTerminal = onRunActiveSectionInTerminal
                                        )
                                    }
                                }
                            }
                        } else {
                            // =========================================================================
                            // FULL-SCREEN ROUNDED DARCULA CODE BLOCK (100% of workspace height & width)
                            // Internal code lines scroll while header and constant 8-key bar stay pinned.
                            // =========================================================================
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("single_section_focused_scroll_container")
                            ) {
                                FullScreenAppleCmdSectionEditor(
                                    section = activeSection,
                                    isCodeActive = isCodeActive,
                                    isExplanationActive = isExplanationActive,
                                    activeLeftRailTool = activeLeftRailTool,
                                    canUndo = uiState.canUndo,
                                    requestAutoFocus = uiState.newlyInsertedSectionId == activeSection.id,
                                    onAutoFocusConsumed = onClearNewSectionFocusFlag,
                                    onSetActiveTarget = onSetActiveTarget,
                                    onUpdateSectionCode = onUpdateSectionCode,
                                    onUpdateSectionExplanation = onUpdateSectionExplanation,
                                    onUpdateSectionTitle = onUpdateSectionTitle,
                                    onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                    onActiveCodeLineChanged = { _, _ -> },
                                    onFormatSectionCode = onFormatSectionCode,
                                    onUndo = onUndo,
                                    onDeactivateLeftRailTool = {
                                        activeLeftRailTool = LeftRailIdeTool.NONE
                                    },
                                    onRequestAiNaming = onRequestAiNaming,
                                    onRequestAiExplain = onRequestAiExplain,
                                    onRequestAiRefactor = onRequestAiRefactor,
                                    onRequestAiKDoc = onRequestAiKDoc,
                                    onToggleSectionDivider = onToggleSectionDivider,
                                    onDeleteSection = onDeleteSection
                                )
                            }
                        }

                        // =========================================================================
                        // DUAL FLOATING WINDOWS LAYER (Max 2 Open simultaneously, Tier 3 3D Depth)
                        // Can pass smoothly through the middle of the screen across the entire display,
                        // or convert into an Adjustable Split Screen (`Split` button).
                        // =========================================================================
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            FloatingWindowType.entries.forEach { winType ->
                                val isOpen = winType in uiState.openFloatingWindows
                                val slotIdx = uiState.openFloatingWindows.indexOf(winType).coerceAtLeast(0)
                                val isForeground = uiState.openFloatingWindows.lastOrNull() == winType

                                MacDraggableFloatingWindow(
                                    windowType = winType,
                                    isOpen = isOpen,
                                    isForegroundWindow = isForeground,
                                    windowSlotIndex = slotIdx,
                                    shakeTriggerCount = uiState.windowLimitShakeCount,
                                    workspaceWidthPx = totalWidthPx,
                                    workspaceHeightPx = totalHeightPx,
                                    initialWidthDp = when (winType) {
                                        FloatingWindowType.EXPLANATION -> 375f
                                        FloatingWindowType.FOLDERS -> 390f
                                        FloatingWindowType.AI_STUDIO -> 360f
                                        FloatingWindowType.RUN_OUTPUT -> 380f
                                    },
                                    initialHeightDp = when (winType) {
                                        FloatingWindowType.EXPLANATION -> 305f
                                        FloatingWindowType.FOLDERS -> 340f
                                        FloatingWindowType.AI_STUDIO -> 280f
                                        FloatingWindowType.RUN_OUTPUT -> 260f
                                    },
                                    onFocusWindow = { onFocusFloatingWindow(winType) },
                                    onDockToSplitScreen = { onDockWindowToSplitScreen(winType) },
                                    onCancelWindow = { onCloseFloatingWindow(winType) },
                                    headerExtraActions = if (winType == FloatingWindowType.EXPLANATION) {
                                        {
                                            Surface(
                                                color = Tier2CompletionSelectedBg,
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { onRequestAiExplain(activeSection.id) }
                                                    .testTag("ai_explain_button_${activeSection.orderIndex}")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = "AI Explain",
                                                        tint = Color(0xFFE6E1DC),
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "AI",
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFE6E1DC)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        null
                                    }
                                ) {
                                    FloatingOrSplitWindowContentBody(
                                        windowType = winType,
                                        activeFolder = document,
                                        allFolders = allFolders,
                                        activeSection = activeSection,
                                        uiState = uiState,
                                        onFolderTitleChange = onTitleChange,
                                        onSelectFolder = onSelectFolder,
                                        onCreateNewFolder = onCreateNewFolder,
                                        onCloseWindowAndExit = {
                                            onCloseFloatingWindow(FloatingWindowType.FOLDERS)
                                            onCloseDocument()
                                        },
                                        onJumpToSectionConcept = onJumpToSectionConcept,
                                        onInsertCodeBlock = onInsertCodeBlock,
                                        onDeleteSection = onDeleteSection,
                                        onUpdateSectionExplanation = onUpdateSectionExplanation,
                                        onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                        onFontChoiceChange = onFontChoiceChange,
                                        onToggleBold = onToggleBold,
                                        onToggleItalic = onToggleItalic,
                                        onToggleUnderline = onToggleUnderline,
                                        onRequestAiExplain = onRequestAiExplain,
                                        onRequestAiRefactor = onRequestAiRefactor,
                                        onRequestAiNaming = onRequestAiNaming,
                                        onRequestAiKDoc = onRequestAiKDoc,
                                        onRunActiveSectionInTerminal = onRunActiveSectionInTerminal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // BOTTOM-DOCKED COMMAND BLOCK OVERLAY (`⌘P` Command Block — spawns below
            // like in any desktop IDE, exempt from the 2-window limit)
            // =========================================================================
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                AnimatedVisibility(
                    visible = uiState.activeIslandDropdown == IslandDropdownMenu.COMMAND_PALETTE,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = 0.82f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeOut()
                ) {
                    ResizableIslandDropdownWindow(
                        title = "⌘P Command Block (Bottom IDE Panel)",
                        subtitle = "Run an IDE command, switch folders, or jump to a .ktscribe file",
                        initialWidthDp = 640f,
                        initialHeightDp = 310f,
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
                                    "toggle_split" -> onDockWindowToSplitScreen(FloatingWindowType.EXPLANATION)
                                    "float_explain" -> onToggleFloatingWindow(FloatingWindowType.EXPLANATION)
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
            }
        }
    }
}

@Composable
private fun DockedSplitWindowContainer(
    windowType: FloatingWindowType,
    orientation: SplitScreenOrientation,
    splitRatio: Float,
    activeFolder: DocumentDetailUiModel,
    allFolders: List<DocumentSummaryUiModel>,
    activeSection: CodeSectionUiModel,
    uiState: EditorUiState,
    onToggleOrientation: () -> Unit,
    onUndockToFloating: () -> Unit,
    onCloseSplit: () -> Unit,
    onFolderTitleChange: (String) -> Unit,
    onSelectFolder: (Long) -> Unit,
    onCreateNewFolder: (String) -> Unit,
    onCloseDocument: () -> Unit,
    onJumpToSectionConcept: (Long) -> Unit,
    onInsertCodeBlock: () -> Unit,
    onDeleteSection: (Long) -> Unit,
    onUpdateSectionExplanation: (Long, String) -> Unit,
    onUpdateSectionConceptTag: (Long, String) -> Unit,
    onFontChoiceChange: (EditorFontChoice) -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onRequestAiExplain: (Long) -> Unit,
    onRequestAiRefactor: (Long) -> Unit,
    onRequestAiNaming: (Long) -> Unit,
    onRequestAiKDoc: (Long) -> Unit,
    onRunActiveSectionInTerminal: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(14.dp, RoundedCornerShape(18.dp))
            .border(1.5.dp, Tier3FloatingWindowActiveBevel, RoundedCornerShape(18.dp))
            .testTag("docked_split_window_${windowType.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Tier3FloatingWindowActiveBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Split Window Header with Traffic Lights, Orientation Switch (Side / Bottom), Float, and Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tier3FloatingWindowActiveHeader)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5F57))
                            .clickable(onClick = onCloseSplit)
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEBC2E))
                            .clickable(onClick = onUndockToFloating)
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF28C840))
                            .clickable(onClick = onToggleOrientation)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${windowType.windowTitle} (${((1f - splitRatio) * 100).toInt()}%)",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEAEFF5),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Toggle between Side-by-Side and Bottom Split
                    Surface(
                        color = Color(0xFF28323C),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF4F5D6B), RoundedCornerShape(6.dp))
                            .clickable(onClick = onToggleOrientation)
                            .testTag("split_orientation_toggle_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerticalSplit,
                                contentDescription = "Switch Split Orientation",
                                tint = Color(0xFF8CA6B8),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (orientation == SplitScreenOrientation.SIDE_BY_SIDE) "Bottom Split" else "Side Split",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8CA6B8)
                            )
                        }
                    }

                    // Undock back to Floating Window
                    Surface(
                        color = Color(0xFF28323C),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF4F5D6B), RoundedCornerShape(6.dp))
                            .clickable(onClick = onUndockToFloating)
                            .testTag("split_undock_to_floating_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Undock to Floating Window",
                                tint = Color(0xFFD4883A),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Float",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1DC)
                            )
                        }
                    }

                    // Close Split Screen
                    Surface(
                        color = Color(0xFF3A2323),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF6B3E3E), RoundedCornerShape(6.dp))
                            .clickable(onClick = onCloseSplit)
                            .testTag("split_close_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Split Window",
                                tint = Color(0xFFF2B8B5),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Tier3FloatingWindowActiveBevel)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                FloatingOrSplitWindowContentBody(
                    windowType = windowType,
                    activeFolder = activeFolder,
                    allFolders = allFolders,
                    activeSection = activeSection,
                    uiState = uiState,
                    onFolderTitleChange = onFolderTitleChange,
                    onSelectFolder = onSelectFolder,
                    onCreateNewFolder = onCreateNewFolder,
                    onCloseWindowAndExit = {
                        onCloseSplit()
                        onCloseDocument()
                    },
                    onJumpToSectionConcept = onJumpToSectionConcept,
                    onInsertCodeBlock = onInsertCodeBlock,
                    onDeleteSection = onDeleteSection,
                    onUpdateSectionExplanation = onUpdateSectionExplanation,
                    onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                    onFontChoiceChange = onFontChoiceChange,
                    onToggleBold = onToggleBold,
                    onToggleItalic = onToggleItalic,
                    onToggleUnderline = onToggleUnderline,
                    onRequestAiExplain = onRequestAiExplain,
                    onRequestAiRefactor = onRequestAiRefactor,
                    onRequestAiNaming = onRequestAiNaming,
                    onRequestAiKDoc = onRequestAiKDoc,
                    onRunActiveSectionInTerminal = onRunActiveSectionInTerminal
                )
            }
        }
    }
}

@Composable
private fun FloatingOrSplitWindowContentBody(
    windowType: FloatingWindowType,
    activeFolder: DocumentDetailUiModel,
    allFolders: List<DocumentSummaryUiModel>,
    activeSection: CodeSectionUiModel,
    uiState: EditorUiState,
    onFolderTitleChange: (String) -> Unit,
    onSelectFolder: (Long) -> Unit,
    onCreateNewFolder: (String) -> Unit,
    onCloseWindowAndExit: () -> Unit,
    onJumpToSectionConcept: (Long) -> Unit,
    onInsertCodeBlock: () -> Unit,
    onDeleteSection: (Long) -> Unit,
    onUpdateSectionExplanation: (Long, String) -> Unit,
    onUpdateSectionConceptTag: (Long, String) -> Unit,
    onFontChoiceChange: (EditorFontChoice) -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onRequestAiExplain: (Long) -> Unit,
    onRequestAiRefactor: (Long) -> Unit,
    onRequestAiNaming: (Long) -> Unit,
    onRequestAiKDoc: (Long) -> Unit,
    onRunActiveSectionInTerminal: () -> Unit
) {
    when (windowType) {
        FloatingWindowType.EXPLANATION -> {
            ExplanationWindowBodyContent(
                section = activeSection,
                formattingStyle = uiState.activeFormattingStyle,
                onExplanationChange = { onUpdateSectionExplanation(activeSection.id, it) },
                onConceptTagChange = { onUpdateSectionConceptTag(activeSection.id, it) },
                onFontChoiceChange = onFontChoiceChange,
                onToggleBold = onToggleBold,
                onToggleItalic = onToggleItalic,
                onToggleUnderline = onToggleUnderline
            )
        }

        FloatingWindowType.FOLDERS -> {
            FolderAndSectionsDropdownContent(
                activeFolder = activeFolder,
                allFolders = allFolders,
                activeSectionId = activeSection.id,
                onFolderTitleChange = onFolderTitleChange,
                onSelectFolder = onSelectFolder,
                onCreateNewFolder = onCreateNewFolder,
                onOpenAllFoldersGrid = onCloseWindowAndExit,
                onSelectSection = onJumpToSectionConcept,
                onInsertNewSection = onInsertCodeBlock,
                onDeleteSection = onDeleteSection
            )
        }

        FloatingWindowType.AI_STUDIO -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Active File: ${activeSection.ktScribeFileName}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1DC)
                )
                Text(
                    text = "Concept Tag: [${activeSection.effectiveLearnedConceptTag}] • Quota: ${uiState.remainingAiRpmQuota}/10 RPM",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.5.sp,
                    color = Color(0xFF8AA87B)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onRequestAiExplain(activeSection.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Tier2CompletionSelectedBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFE6E1DC),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "Thorough Explain",
                            color = Color(0xFFE6E1DC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                    Button(
                        onClick = { onRequestAiRefactor(activeSection.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF36454F)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = Color(0xFFE6E1DC),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "Fix & Refactor",
                            color = Color(0xFFE6E1DC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onRequestAiNaming(activeSection.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Name & Tag Concept", fontSize = 10.5.sp, color = Color(0xFFE6E1DC))
                    }
                    OutlinedButton(
                        onClick = { onRequestAiKDoc(activeSection.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add KDoc Comments", fontSize = 10.5.sp, color = Color(0xFFE6E1DC))
                    }
                }
            }
        }

        FloatingWindowType.RUN_OUTPUT -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "zsh • kotlinc ${activeSection.ktScribeFileName}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        color = Color(0xFFD4883A),
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = Tier2CompletionSelectedBg,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onRunActiveSectionInTerminal)
                    ) {
                        Text(
                            text = "Re-Run ▶",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1DC),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Surface(
                    color = Color(0xFF141312),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF36312C), RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = uiState.terminalExecutionOutput
                            ?: KotlinCodeEngine.simulateKotlinExecution(activeSection.codeContent),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFFE6E1DC),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenAppleCmdSectionEditor(
    section: CodeSectionUiModel,
    isCodeActive: Boolean,
    isExplanationActive: Boolean,
    activeLeftRailTool: LeftRailIdeTool,
    canUndo: Boolean,
    requestAutoFocus: Boolean,
    onAutoFocusConsumed: () -> Unit,
    onSetActiveTarget: (ActiveEditorTarget) -> Unit,
    onUpdateSectionCode: (Long, String) -> Unit,
    onUpdateSectionExplanation: (Long, String) -> Unit,
    onUpdateSectionTitle: (Long, String) -> Unit,
    onUpdateSectionConceptTag: (Long, String) -> Unit,
    onActiveCodeLineChanged: (Int, Int) -> Unit,
    onFormatSectionCode: (Long) -> Unit,
    onUndo: () -> Unit,
    onDeactivateLeftRailTool: () -> Unit,
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
        hideInlineExplanation = true,
        activeLeftRailTool = activeLeftRailTool,
        canUndo = canUndo,
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
        onUndo = onUndo,
        onDeactivateLeftRailTool = onDeactivateLeftRailTool,
        onRequestAiNaming = { onRequestAiNaming(section.id) },
        onRequestAiExplain = { onRequestAiExplain(section.id) },
        onRequestAiRefactor = { onRequestAiRefactor(section.id) },
        onRequestAiKDoc = { onRequestAiKDoc(section.id) },
        onToggleFocusMode = {},
        onToggleDivider = { onToggleSectionDivider(section.id) },
        onDeleteSection = { onDeleteSection(section.id) }
    )
}
