package com.example.ui.screens

import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.CodeLanguageEngine
import com.example.code.DiagnosticSeverity
import com.example.model.ActiveEditorTarget
import com.example.model.BottomDrawerTab
import com.example.model.CodeSectionUiModel
import com.example.model.DocumentDetailUiModel
import com.example.model.DocumentSummaryUiModel
import com.example.model.EditorFontChoice
import com.example.model.ExplanationWindowMode
import com.example.model.FloatingWindowType
import com.example.model.IslandDropdownMenu
import com.example.model.LanguageType
import com.example.model.LeftActivityTab
import com.example.model.SplitScreenOrientation
import com.example.model.TextAlignmentChoice
import com.example.model.ToolchainPluginItem
import com.example.ui.components.CommandPaletteDropdownContent
import com.example.ui.components.ExplanationWindowBodyContent
import com.example.ui.components.FileSettingsDialog
import com.example.ui.components.FolderAndSectionsDropdownContent
import com.example.ui.components.IdeBottomDrawer
import com.example.ui.components.KotlinCodeSectionCard
import com.example.ui.components.LeftRailIdeTool
import com.example.ui.components.MacDraggableFloatingWindow
import com.example.ui.components.ResizableIslandDropdownWindow
import com.example.ui.components.ToolchainPluginRowItem
import com.example.ui.components.exportOrCopyCodePayload
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Tier2CompletionSelectedBg
import com.example.ui.theme.Tier3FloatingWindowActiveBevel
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
    onUpdateSectionLanguage: (Long, LanguageType) -> Unit = { _, _ -> },
    onUpdateSectionThemeOverride: (Long, com.example.ui.theme.AppThemeId?) -> Unit = { _, _ -> },
    onInsertLanguageCodeBlock: (LanguageType) -> Unit = {},
    onToggleBottomDrawer: (BottomDrawerTab) -> Unit = {},
    onCloseBottomDrawer: () -> Unit = {},
    onSetBottomDrawerTab: (BottomDrawerTab) -> Unit = {},
    onSetBottomDrawerHeightFraction: (Float) -> Unit = {},
    onClearTerminal: () -> Unit = {},
    onDismissExtensionRenameWarning: () -> Unit = {},
    onShowStatusToast: (String) -> Unit = {},
    onToggleLeftActivityTab: (LeftActivityTab) -> Unit = {},
    onCloseEditorTab: (Long) -> Unit = {},
    onInstallOrTogglePlugin: (String) -> Unit = {},
    onInstallAllSdks: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current
    var activeLeftRailTool by remember { mutableStateOf(LeftRailIdeTool.NONE) }
    var showFileSettingsDialog by remember { mutableStateOf(false) }

    val openTabSections = remember(document.sections, uiState.closedTabSectionIds) {
        document.sections.filter { it.id !in uiState.closedTabSectionIds }
    }

    val activeSection = remember(document.sections, openTabSections, uiState.focusedSectionId, uiState.activeTarget) {
        val targetId = uiState.focusedSectionId
            ?: (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId
            ?: (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId
        openTabSections.firstOrNull { it.id == targetId }
            ?: openTabSections.firstOrNull()
    }

    val allDiagnostics = remember(activeSection?.codeContent, activeSection?.language) {
        if (activeSection == null) emptyList()
        else CodeLanguageEngine.getDiagnosticsForLanguage(activeSection.codeContent, activeSection.language)
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
        if (uiState.extensionRenameWarningDialog != null) {
            onDismissExtensionRenameWarning()
        } else if (uiState.isBottomDrawerOpen) {
            onCloseBottomDrawer()
        } else if (uiState.activeIslandDropdown != IslandDropdownMenu.NONE) {
            onCloseIslandDropdown()
        } else if (uiState.openFloatingWindows.isNotEmpty()) {
            onCloseFloatingWindow(uiState.openFloatingWindows.last())
        } else if (effectiveSplitWindow != null) {
            onCloseSplitScreenWindow()
        } else if (uiState.activeLeftActivityTab != LeftActivityTab.NONE) {
            onToggleLeftActivityTab(uiState.activeLeftActivityTab)
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
        containerColor = Color(0xFF1A1615),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F1B19))
            ) {
                // =========================================================================
                // 1. CODEASSIST + iOS GLASS TOP WORKSPACE BAR
                // =========================================================================
                CodeAssistTopWorkspaceBar(
                    activeFolder = document,
                    activeSection = activeSection,
                    activeLeftActivityTab = uiState.activeLeftActivityTab,
                    openFloatingWindows = uiState.openFloatingWindows,
                    activeDropdown = uiState.activeIslandDropdown,
                    isBottomDrawerOpen = uiState.isBottomDrawerOpen,
                    syntaxErrorCount = syntaxErrors.size,
                    hasUnsavedChanges = uiState.hasUnsavedChanges,
                    secondsUntilAutoSave = uiState.secondsUntilAutoSave,
                    canUndo = uiState.canUndo,
                    canRedo = uiState.canRedo,
                    onBackToFolders = onCloseDocument,
                    onToggleFilesSidebar = { onToggleLeftActivityTab(LeftActivityTab.FILES) },
                    onSaveNow = onSaveNow,
                    onUndo = onUndo,
                    onRedo = onRedo,
                    onRunCode = onRunActiveSectionInTerminal,
                    onToggleFloatingWindow = onToggleFloatingWindow,
                    onToggleTerminalDrawer = {
                        onToggleBottomDrawer(uiState.activeBottomDrawerTab)
                    },
                    onToggleCommandPalette = {
                        onToggleIslandDropdown(IslandDropdownMenu.COMMAND_PALETTE)
                    },
                    onOpenVaultModal = onOpenVaultModal,
                    onOpenFileSettings = {
                        if (activeSection != null) {
                            showFileSettingsDialog = true
                        } else {
                            onOpenSettings()
                        }
                    },
                    onOpenStudioSettings = onOpenSettings
                )

                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF332D29))

                // =========================================================================
                // 2. FEEDBACK / NOTIFICATION TOAST BANNER (Animated when active)
                // =========================================================================
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
                .background(Color(0xFF1A1615))
        ) {
            val totalHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }
            val totalWidthPx = with(density) { maxWidth.toPx().coerceAtLeast(1f) }

            // =========================================================================
            // MAIN WORKSPACE ROW:
            // 1. Slim CodeAssist Left Activity Bar (40.dp)
            // 2. Collapsible Left Side Panel (`Files` Tree / `Plugins & SDKs` / `Explain` / `Search`)
            // 3. Editor Area (File Tab Bar + Active Code Editor OR "No file open" Zero-State)
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Slim Left Activity Bar (CodeAssist style)
                CodeAssistSlimLeftActivityBar(
                    activeActivityTab = uiState.activeLeftActivityTab,
                    activeLeftRailTool = activeLeftRailTool,
                    syntaxErrorCount = syntaxErrors.size,
                    isBottomDrawerOpen = uiState.isBottomDrawerOpen,
                    onToggleActivityTab = onToggleLeftActivityTab,
                    onToggleBottomDrawer = { onToggleBottomDrawer(BottomDrawerTab.TERMINAL) },
                    onInsertNewFile = onInsertCodeBlock,
                    onFormatCode = { activeSection?.let { onFormatSectionCode(it.id) } },
                    onToggleDiagnosticsTool = {
                        val firstFixable = allDiagnostics.firstOrNull { it.fixedCode != null }
                        if (activeSection != null && firstFixable?.fixedCode != null) {
                            onUpdateSectionCode(activeSection.id, firstFixable.fixedCode)
                        } else {
                            activeLeftRailTool = if (activeLeftRailTool == LeftRailIdeTool.DIAGNOSTICS) {
                                LeftRailIdeTool.NONE
                            } else {
                                LeftRailIdeTool.DIAGNOSTICS
                            }
                        }
                    },
                    onExportOrCopyFile = {
                        if (activeSection != null) {
                            exportOrCopyCodePayload(
                                context = context,
                                clipboardManager = clipboardManager,
                                fileName = activeSection.nativeFileName,
                                content = activeSection.codeContent,
                                launchShareSheet = true,
                                onStatusBanner = onShowStatusToast
                            )
                        }
                    }
                )

                // 2. Collapsible CodeAssist Side Panel (Files Tree / Search / Structure / Explain / Plugins)
                AnimatedVisibility(
                    visible = uiState.activeLeftActivityTab != LeftActivityTab.NONE,
                    enter = expandHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = shrinkHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeOut()
                ) {
                    CodeAssistSidePanel(
                        activeTab = uiState.activeLeftActivityTab,
                        document = document,
                        allFolders = allFolders,
                        activeSection = activeSection,
                        uiState = uiState,
                        onSelectFileSection = onJumpToSectionConcept,
                        onInsertNewFile = onInsertCodeBlock,
                        onInsertLanguageFile = onInsertLanguageCodeBlock,
                        onSelectFolder = onSelectFolder,
                        onCreateNewFolder = onCreateNewFolder,
                        onOpenFileSettings = { showFileSettingsDialog = true },
                        onExportFile = { sec ->
                            exportOrCopyCodePayload(
                                context = context,
                                clipboardManager = clipboardManager,
                                fileName = sec.nativeFileName,
                                content = sec.codeContent,
                                launchShareSheet = true,
                                onStatusBanner = onShowStatusToast
                            )
                        },
                        onUpdateSectionExplanation = onUpdateSectionExplanation,
                        onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                        onFontChoiceChange = onFontChoiceChange,
                        onToggleBold = onToggleBold,
                        onToggleItalic = onToggleItalic,
                        onToggleUnderline = onToggleUnderline,
                        onRequestAiExplain = onRequestAiExplain,
                        onInstallOrTogglePlugin = onInstallOrTogglePlugin,
                        onInstallAllSdks = onInstallAllSdks,
                        onCollapsePanel = { onToggleLeftActivityTab(uiState.activeLeftActivityTab) }
                    )
                }

                // 3. Right Editor Column: Editor File Tab Bar + Main Code Canvas (or "No file open")
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // CodeAssist Editor File Tab Bar (`K Main.kt • ×`)
                    CodeAssistEditorTabBar(
                        allSections = document.sections,
                        openSections = openTabSections,
                        activeSection = activeSection,
                        hasUnsavedChanges = uiState.hasUnsavedChanges,
                        onSelectSection = onJumpToSectionConcept,
                        onCloseSectionTab = onCloseEditorTab,
                        onNavigateFocusedSection = onNavigateFocusedSection,
                        onInsertNewFile = onInsertCodeBlock,
                        onOpenFileSettings = { showFileSettingsDialog = true },
                        onExportActiveFile = {
                            val payload = activeSection?.toKtScribeFilePayload(document.title)
                                ?: onExportMarkdown()
                            val fileName = activeSection?.containerFileName ?: "${document.title}.md"
                            exportOrCopyCodePayload(
                                context = context,
                                clipboardManager = clipboardManager,
                                fileName = fileName,
                                content = payload,
                                launchShareSheet = true,
                                onStatusBanner = onShowStatusToast
                            )
                        }
                    )

                    // Main Code Canvas OR CodeAssist "No file open" Zero-State Screen
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(start = 2.dp, end = 6.dp, top = 2.dp, bottom = 6.dp)
                    ) {
                        if (activeSection == null) {
                            CodeAssistNoFileOpenZeroState(
                                document = document,
                                onSearchEverywhere = { onToggleIslandDropdown(IslandDropdownMenu.COMMAND_PALETTE) },
                                onGoToFile = { onToggleLeftActivityTab(LeftActivityTab.FILES) },
                                onRecentFiles = {
                                    document.sections.firstOrNull()?.let { onJumpToSectionConcept(it.id) }
                                        ?: onInsertCodeBlock()
                                },
                                onFindInFiles = { onToggleLeftActivityTab(LeftActivityTab.SEARCH) },
                                onNewFile = onInsertCodeBlock,
                                onProjectFiles = { onToggleLeftActivityTab(LeftActivityTab.FILES) }
                            )
                        } else {
                            val isCodeActive = (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId == activeSection.id
                            val isExplanationActive = (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId == activeSection.id

                            if (effectiveSplitWindow != null) {
                                // =========================================================================
                                // ADJUSTABLE SPLIT SCREEN (Side-by-Side or Bottom Split)
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
                                                onDeleteSection = onDeleteSection,
                                                onSelectLanguage = { lang -> onUpdateSectionLanguage(activeSection.id, lang) },
                                                onSelectThemeOverride = { theme -> onUpdateSectionThemeOverride(activeSection.id, theme) },
                                                onOpenFileSettings = { showFileSettingsDialog = true },
                                                onRunCode = onRunActiveSectionInTerminal,
                                                onExportOrCopyFile = { openShare ->
                                                    exportOrCopyCodePayload(
                                                        context = context,
                                                        clipboardManager = clipboardManager,
                                                        fileName = activeSection.nativeFileName,
                                                        content = activeSection.codeContent,
                                                        launchShareSheet = openShare,
                                                        onStatusBanner = onShowStatusToast
                                                    )
                                                }
                                            )
                                        }

                                        // Vertical Draggable Split Handle (Clamped between 25% and 75%)
                                        Column(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(22.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .pointerInput(totalWidthPx) {
                                                    detectDragGestures { change, dragAmount ->
                                                        change.consume()
                                                        val deltaRatio = dragAmount.x / totalWidthPx
                                                        onUpdateSplitRatio((uiState.splitScreenRatio + deltaRatio).coerceIn(0.25f, 0.75f))
                                                    }
                                                }
                                                .testTag("split_screen_resize_handle"),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Surface(
                                                color = if (uiState.splitScreenRatio <= 0.35f) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(3.dp),
                                                modifier = Modifier.clickable { onUpdateSplitRatio(0.30f) }
                                            ) {
                                                Text(
                                                    text = "30",
                                                    fontSize = 7.5.sp,
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (uiState.splitScreenRatio <= 0.35f) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height(44.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                color = if (uiState.splitScreenRatio >= 0.65f) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(3.dp),
                                                modifier = Modifier.clickable { onUpdateSplitRatio(0.70f) }
                                            ) {
                                                Text(
                                                    text = "70",
                                                    fontSize = 7.5.sp,
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (uiState.splitScreenRatio >= 0.65f) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                                                )
                                            }
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
                                                onRunActiveSectionInTerminal = onRunActiveSectionInTerminal,
                                                onInsertLanguageCodeBlock = onInsertLanguageCodeBlock
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
                                                onDeleteSection = onDeleteSection,
                                                onSelectLanguage = { lang -> onUpdateSectionLanguage(activeSection.id, lang) },
                                                onSelectThemeOverride = { theme -> onUpdateSectionThemeOverride(activeSection.id, theme) },
                                                onOpenFileSettings = { showFileSettingsDialog = true },
                                                onRunCode = onRunActiveSectionInTerminal,
                                                onExportOrCopyFile = { openShare ->
                                                    exportOrCopyCodePayload(
                                                        context = context,
                                                        clipboardManager = clipboardManager,
                                                        fileName = activeSection.nativeFileName,
                                                        content = activeSection.codeContent,
                                                        launchShareSheet = openShare,
                                                        onStatusBanner = onShowStatusToast
                                                    )
                                                }
                                            )
                                        }

                                        // Horizontal Draggable Split Handle
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(28.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .pointerInput(totalHeightPx) {
                                                    detectDragGestures { change, dragAmount ->
                                                        change.consume()
                                                        val deltaRatio = dragAmount.y / totalHeightPx
                                                        onUpdateSplitRatio((uiState.splitScreenRatio + deltaRatio).coerceIn(0.25f, 0.75f))
                                                    }
                                                }
                                                .testTag("split_screen_resize_handle"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DragHandle,
                                                        contentDescription = "Drag to resize split windows",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "${(uiState.splitScreenRatio * 100).toInt()}% / ${((1f - uiState.splitScreenRatio) * 100).toInt()}%",
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    listOf(0.30f to "30%", 0.50f to "50%", 0.70f to "70%").forEach { (ratio, label) ->
                                                        val isSelected = kotlin.math.abs(uiState.splitScreenRatio - ratio) < 0.08f
                                                        Surface(
                                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                            shape = RoundedCornerShape(4.dp),
                                                            modifier = Modifier.clickable { onUpdateSplitRatio(ratio) }
                                                        ) {
                                                            Text(
                                                                text = label,
                                                                fontSize = 8.5.sp,
                                                                fontFamily = JetBrainsMonoFontFamily,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
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
                                                onRunActiveSectionInTerminal = onRunActiveSectionInTerminal,
                                                onInsertLanguageCodeBlock = onInsertLanguageCodeBlock
                                            )
                                        }
                                    }
                                }
                            } else {
                                // =========================================================================
                                // FULL-SCREEN CODEASSIST + DARCULA CODE BLOCK
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
                                        onDeleteSection = onDeleteSection,
                                        onSelectLanguage = { lang -> onUpdateSectionLanguage(activeSection.id, lang) },
                                        onSelectThemeOverride = { theme -> onUpdateSectionThemeOverride(activeSection.id, theme) },
                                        onOpenFileSettings = { showFileSettingsDialog = true },
                                        onRunCode = onRunActiveSectionInTerminal,
                                        onExportOrCopyFile = { openShare ->
                                            exportOrCopyCodePayload(
                                                context = context,
                                                clipboardManager = clipboardManager,
                                                fileName = activeSection.nativeFileName,
                                                content = activeSection.codeContent,
                                                launchShareSheet = openShare,
                                                onStatusBanner = onShowStatusToast
                                            )
                                        }
                                    )
                                }
                            }

                            // =========================================================================
                            // DUAL FLOATING WINDOWS LAYER (Max 2 Open simultaneously)
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
                                            onRunActiveSectionInTerminal = onRunActiveSectionInTerminal,
                                            onInsertLanguageCodeBlock = onInsertLanguageCodeBlock
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // BOTTOM-DOCKED COMMAND BLOCK OVERLAY (`⌘P` Command Block)
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

            // =========================================================================
            // CRITICAL FIX: BOTTOM-ANCHORED IDE DRAWER (`Modifier.align(Alignment.BottomCenter)`)
            // Ensures Terminal / Build & Run / Problems / Debugger / Logcat ALWAYS pops up BELOW!
            // =========================================================================
            IdeBottomDrawer(
                isOpen = uiState.isBottomDrawerOpen,
                activeTab = uiState.activeBottomDrawerTab,
                activeSection = activeSection,
                executionOutput = uiState.terminalExecutionOutput,
                heightFraction = uiState.bottomDrawerHeightFraction,
                toolchainPlugins = uiState.toolchainPlugins,
                onTabSelected = onSetBottomDrawerTab,
                onHeightFractionChange = onSetBottomDrawerHeightFraction,
                onRunCode = onRunActiveSectionInTerminal,
                onClearTerminal = onClearTerminal,
                onCloseDrawer = onCloseBottomDrawer,
                onJumpToLine = { /* Line jumping */ },
                onInstallPlugin = onInstallOrTogglePlugin,
                onOpenPluginManager = { onToggleLeftActivityTab(LeftActivityTab.PLUGINS) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showFileSettingsDialog && activeSection != null) {
        FileSettingsDialog(
            activeSection = activeSection,
            folderTitle = document.title,
            onUpdateLanguage = { lang -> onUpdateSectionLanguage(activeSection.id, lang) },
            onUpdateThemeOverride = { theme -> onUpdateSectionThemeOverride(activeSection.id, theme) },
            onUpdateFileName = { name -> onUpdateSectionTitle(activeSection.id, name) },
            onCopyFileBundle = {
                val payload = activeSection.toKtScribeFilePayload(document.title)
                exportOrCopyCodePayload(
                    context = context,
                    clipboardManager = clipboardManager,
                    fileName = activeSection.containerFileName,
                    content = payload,
                    launchShareSheet = true,
                    onStatusBanner = onShowStatusToast
                )
            },
            onCopyRawCode = {
                exportOrCopyCodePayload(
                    context = context,
                    clipboardManager = clipboardManager,
                    fileName = activeSection.nativeFileName,
                    content = activeSection.codeContent,
                    launchShareSheet = true,
                    onStatusBanner = onShowStatusToast
                )
            },
            onCopyFolderMarkdown = {
                val md = onExportMarkdown()
                exportOrCopyCodePayload(
                    context = context,
                    clipboardManager = clipboardManager,
                    fileName = "${document.title}.md",
                    content = md,
                    launchShareSheet = true,
                    onStatusBanner = onShowStatusToast
                )
            },
            onRequestAiNaming = { onRequestAiNaming(activeSection.id) },
            onRequestAiExplain = { onRequestAiExplain(activeSection.id) },
            onOpenStudioSettings = onOpenSettings,
            onDismiss = { showFileSettingsDialog = false }
        )
    }

    // Extension Rename Warning Dialog (prevents altering language extension)
    uiState.extensionRenameWarningDialog?.let { warningMsg ->
        AlertDialog(
            onDismissRequest = onDismissExtensionRenameWarning,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFFB86C),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Language Extension Locked",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1FA8C),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Text(
                    text = warningMsg,
                    color = Color(0xFFE6E1DC),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismissExtensionRenameWarning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5555))
                ) {
                    Text("Acknowledge", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            tonalElevation = 8.dp
        )
    }
}

// =========================================================================
// CODEASSIST + iOS GLASS TOP WORKSPACE BAR
// =========================================================================
@Composable
private fun CodeAssistTopWorkspaceBar(
    activeFolder: DocumentDetailUiModel,
    activeSection: CodeSectionUiModel?,
    activeLeftActivityTab: LeftActivityTab,
    openFloatingWindows: List<FloatingWindowType>,
    activeDropdown: IslandDropdownMenu,
    isBottomDrawerOpen: Boolean,
    syntaxErrorCount: Int,
    hasUnsavedChanges: Boolean,
    secondsUntilAutoSave: Int,
    canUndo: Boolean,
    canRedo: Boolean,
    onBackToFolders: () -> Unit,
    onToggleFilesSidebar: () -> Unit,
    onSaveNow: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onRunCode: () -> Unit,
    onToggleFloatingWindow: (FloatingWindowType) -> Unit,
    onToggleTerminalDrawer: () -> Unit,
    onToggleCommandPalette: () -> Unit,
    onOpenVaultModal: () -> Unit,
    onOpenFileSettings: () -> Unit,
    onOpenStudioSettings: () -> Unit
) {
    val isExplainActive = FloatingWindowType.EXPLANATION in openFloatingWindows
    val isFilesFloatingActive = FloatingWindowType.FOLDERS in openFloatingWindows
    val isAiActive = FloatingWindowType.AI_STUDIO in openFloatingWindows
    val isRunActive = FloatingWindowType.RUN_OUTPUT in openFloatingWindows
    val isCmdActive = activeDropdown == IslandDropdownMenu.COMMAND_PALETTE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Cluster: Back Arrow `←`, Sidebar Toggle `◧`, Folder Icon `M`, Project Name, `✓ Indexed` Vault Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            IconButton(
                onClick = onBackToFolders,
                modifier = Modifier
                    .size(30.dp)
                    .testTag("back_to_documents_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Folders",
                    tint = Color(0xFFE6E1DC),
                    modifier = Modifier.size(18.dp)
                )
            }

            // CodeAssist Sidebar Toggle (`◧`)
            Surface(
                color = if (activeLeftActivityTab == LeftActivityTab.FILES) Color(0xFF3D322A) else Color(0xFF282320),
                shape = RoundedCornerShape(7.dp),
                border = BorderStroke(
                    0.75.dp,
                    if (activeLeftActivityTab == LeftActivityTab.FILES) Color(0xFFD4883A) else Color(0xFF3E3630)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .clickable(onClick = onToggleFilesSidebar)
                    .testTag("toggle_files_sidebar_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Toggle Project Files Tree",
                        tint = if (activeLeftActivityTab == LeftActivityTab.FILES) Color(0xFFD4883A) else Color(0xFFA39C93),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Project Badge (`M`) + Project Title
            Surface(
                color = Color(0xFFD4883A).copy(alpha = 0.22f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(0.75.dp, Color(0xFFD4883A))
            ) {
                Text(
                    text = activeFolder.title.firstOrNull()?.uppercase() ?: "M",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFC66D),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Text(
                text = activeFolder.title.ifBlank { "My App" },
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE6E1DC),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // CodeAssist `✓ Indexed` / Vault Status Pill
            Surface(
                color = Color(0xFF232C23),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(0.5.dp, Color(0xFF4E6E4E)),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onOpenVaultModal)
                    .testTag("open_vault_modal_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .testTag("autosave_status_pill")
                ) {
                    Text(
                        text = if (hasUnsavedChanges) "● Sync ${secondsUntilAutoSave}s" else "✓ Indexed",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasUnsavedChanges) Color(0xFFFFC66D) else Color(0xFF81C784)
                    )
                }
            }
        }

        // Right Cluster: Save, Undo, Redo, `▶ Run`, `>_ Terminal`, `Explain`, `Files`, `AI Guide`, `⌘P`, `Settings`
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            // Save button (CodeAssist floppy icon)
            Surface(
                color = Color(0xFF332922),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFF5C4938)),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSaveNow)
                    .testTag("save_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save Now",
                    tint = Color(0xFFFFC66D),
                    modifier = Modifier
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                        .size(14.dp)
                )
            }

            // Undo & Redo icons
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.size(28.dp).testTag("undo_top_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) Color(0xFFE6E1DC) else Color(0xFF5A534D),
                    modifier = Modifier.size(15.dp)
                )
            }
            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.size(28.dp).testTag("redo_top_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) Color(0xFFE6E1DC) else Color(0xFF5A534D),
                    modifier = Modifier.size(15.dp)
                )
            }

            // Primary CodeAssist `▶ Run` Pill
            Surface(
                color = if (isRunActive || isBottomDrawerOpen) Color(0xFF28422B) else Color(0xFF332922),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isRunActive || isBottomDrawerOpen) Color(0xFF6A8759) else Color(0xFFD4883A)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onRunCode)
                    .testTag("island_run_code_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run Code",
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Run",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE6E1DC)
                    )
                }
            }

            // `>_ Terminal` Bottom Drawer Toggle Pill
            CodeAssistTopPill(
                label = if (syntaxErrorCount > 0) ">_ Terminal ($syntaxErrorCount)" else ">_ Terminal",
                isActive = isBottomDrawerOpen,
                testTag = "island_terminal_drawer_button",
                onClick = onToggleTerminalDrawer
            )

            // `Explain` Floating/Split Window Toggle Pill
            CodeAssistTopPill(
                label = "Explain",
                isActive = isExplainActive,
                testTag = "island_explanation_window_toggle",
                onClick = { onToggleFloatingWindow(FloatingWindowType.EXPLANATION) }
            )

            // `Files` Floating/Split Window Toggle Pill
            CodeAssistTopPill(
                label = "Files ${activeFolder.sections.size}",
                isActive = isFilesFloatingActive,
                testTag = "island_folder_dropdown_button",
                onClick = { onToggleFloatingWindow(FloatingWindowType.FOLDERS) }
            )

            // `AI Guide` Floating Window Toggle Pill
            CodeAssistTopPill(
                label = "AI Guide",
                isActive = isAiActive,
                testTag = "island_ai_studio_button",
                onClick = { onToggleFloatingWindow(FloatingWindowType.AI_STUDIO) }
            )

            // `⌘P` Command Block Pill
            CodeAssistTopPill(
                label = "⌘P",
                isActive = isCmdActive,
                testTag = "island_command_palette_button",
                onClick = onToggleCommandPalette
            )

            // File Inspector & Studio Settings Pill
            Surface(
                color = Color(0xFF282320),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(0.75.dp, Color(0xFF453D37)),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onOpenFileSettings)
                    .testTag("editor_shortcut_settings_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "File & Studio Settings",
                        tint = Color(0xFFD4883A),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Settings",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE6E1DC)
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeAssistTopPill(
    label: String,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (isActive) Color(0xFF3B3028) else Color(0xFF26211E),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(
            width = if (isActive) 1.dp else 0.75.dp,
            color = if (isActive) Color(0xFFD4883A) else Color(0xFF3E3630)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) Color(0xFFFFC66D) else Color(0xFFD5CEC6),
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

// =========================================================================
// CODEASSIST SLIM LEFT ACTIVITY BAR
// =========================================================================
@Composable
private fun CodeAssistSlimLeftActivityBar(
    activeActivityTab: LeftActivityTab,
    activeLeftRailTool: LeftRailIdeTool,
    syntaxErrorCount: Int,
    isBottomDrawerOpen: Boolean,
    onToggleActivityTab: (LeftActivityTab) -> Unit,
    onToggleBottomDrawer: () -> Unit,
    onInsertNewFile: () -> Unit,
    onFormatCode: () -> Unit,
    onToggleDiagnosticsTool: () -> Unit,
    onExportOrCopyFile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(40.dp)
            .background(Color(0xFF1F1B19))
            .border(0.5.dp, Color(0xFF332D29))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
            .testTag("left_circular_ide_rail"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Project Files Tree (`FILES`)
            ActivityRailIconButton(
                icon = Icons.Default.Description,
                tooltip = "Project Files",
                isActive = activeActivityTab == LeftActivityTab.FILES,
                testTag = "activity_rail_files",
                onClick = { onToggleActivityTab(LeftActivityTab.FILES) }
            )

            // 2. Search in Files (`SEARCH`)
            ActivityRailIconButton(
                icon = Icons.Default.Search,
                tooltip = "Find in Files",
                isActive = activeActivityTab == LeftActivityTab.SEARCH,
                testTag = "activity_rail_search",
                onClick = { onToggleActivityTab(LeftActivityTab.SEARCH) }
            )

            // 3. Code Structure / Format (`<>`)
            ActivityRailIconButton(
                icon = Icons.Default.Code,
                tooltip = "Format & Structure",
                isActive = activeActivityTab == LeftActivityTab.STRUCTURE,
                testTag = "rail_tool_format",
                onClick = {
                    onFormatCode()
                    onToggleActivityTab(LeftActivityTab.STRUCTURE)
                }
            )

            // 4. Explanation & Concept Notes (`EXPLAIN`)
            ActivityRailIconButton(
                icon = Icons.Default.AutoAwesome,
                tooltip = "Explanation & Notes",
                isActive = activeActivityTab == LeftActivityTab.EXPLAIN,
                testTag = "activity_rail_explain",
                onClick = { onToggleActivityTab(LeftActivityTab.EXPLAIN) }
            )

            // 5. SDK & Plugin Toolchain Manager (`PLUGINS`)
            ActivityRailIconButton(
                icon = Icons.Default.Extension,
                tooltip = "SDK & Plugin Manager",
                isActive = activeActivityTab == LeftActivityTab.PLUGINS,
                testTag = "activity_rail_plugins",
                onClick = { onToggleActivityTab(LeftActivityTab.PLUGINS) }
            )

            // 6. Terminal & Build Drawer (`>_`)
            ActivityRailIconButton(
                icon = Icons.Default.Terminal,
                tooltip = "Terminal & Build Drawer",
                isActive = isBottomDrawerOpen,
                testTag = "activity_rail_terminal",
                onClick = onToggleBottomDrawer
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Quick Fix / Diagnostics badge
            ActivityRailIconButton(
                icon = Icons.Default.Warning,
                tooltip = "Diagnostics ($syntaxErrorCount)",
                isActive = activeLeftRailTool == LeftRailIdeTool.DIAGNOSTICS || syntaxErrorCount > 0,
                badgeColor = if (syntaxErrorCount > 0) Color(0xFFFF6B68) else null,
                testTag = "rail_tool_diagnostics",
                onClick = onToggleDiagnosticsTool
            )

            // New File `+`
            ActivityRailIconButton(
                icon = Icons.Default.Add,
                tooltip = "New File",
                isActive = false,
                testTag = "rail_tool_add_section",
                onClick = onInsertNewFile
            )

            // Export / Share
            ActivityRailIconButton(
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                tooltip = "Export / Share File",
                isActive = false,
                testTag = "rail_tool_share",
                onClick = onExportOrCopyFile
            )
        }
    }
}

@Composable
private fun ActivityRailIconButton(
    icon: ImageVector,
    tooltip: String,
    isActive: Boolean,
    badgeColor: Color? = null,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0xFF3E3127) else Color.Transparent)
            .border(
                width = if (isActive) 1.dp else 0.dp,
                color = if (isActive) Color(0xFFD4883A) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = badgeColor ?: if (isActive) Color(0xFFFFC66D) else Color(0xFFA39C93),
            modifier = Modifier.size(16.dp)
        )
    }
}

// =========================================================================
// CODEASSIST COLLAPSIBLE SIDE PANEL (FILES TREE / SEARCH / EXPLAIN / PLUGINS)
// =========================================================================
@Composable
private fun CodeAssistSidePanel(
    activeTab: LeftActivityTab,
    document: DocumentDetailUiModel,
    allFolders: List<DocumentSummaryUiModel>,
    activeSection: CodeSectionUiModel?,
    uiState: EditorUiState,
    onSelectFileSection: (Long) -> Unit,
    onInsertNewFile: () -> Unit,
    onInsertLanguageFile: (LanguageType) -> Unit,
    onSelectFolder: (Long) -> Unit,
    onCreateNewFolder: (String) -> Unit,
    onOpenFileSettings: () -> Unit,
    onExportFile: (CodeSectionUiModel) -> Unit,
    onUpdateSectionExplanation: (Long, String) -> Unit,
    onUpdateSectionConceptTag: (Long, String) -> Unit,
    onFontChoiceChange: (EditorFontChoice) -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onRequestAiExplain: (Long) -> Unit,
    onInstallOrTogglePlugin: (String) -> Unit,
    onInstallAllSdks: () -> Unit,
    onCollapsePanel: () -> Unit
) {
    var expandedAppRoot by remember { mutableStateOf(true) }
    var searchFilter by remember { mutableStateOf("") }

    Surface(
        color = Color(0xFF1E1A18),
        modifier = Modifier
            .fillMaxHeight()
            .width(235.dp)
            .border(0.5.dp, Color(0xFF332D29))
            .testTag("codeassist_side_panel")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Side Panel Header (`Files ... + -`)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF241F1C))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = activeTab.label,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1DC)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (activeTab == LeftActivityTab.FILES) {
                        IconButton(
                            onClick = onInsertNewFile,
                            modifier = Modifier.size(22.dp).testTag("tree_new_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New File",
                                tint = Color(0xFFD4883A),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onCollapsePanel,
                        modifier = Modifier.size(22.dp).testTag("collapse_side_panel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Collapse Panel",
                            tint = Color(0xFFA39C93),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF332D29))

            when (activeTab) {
                LeftActivityTab.FILES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 6.dp)
                            .testTag("codeassist_files_tree_panel")
                    ) {
                        // Root `▾ 📁 app`
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedAppRoot = !expandedAppRoot }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (expandedAppRoot) {
                                    Icons.Default.KeyboardArrowDown
                                } else {
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight
                                },
                                contentDescription = null,
                                tint = Color(0xFFA39C93),
                                modifier = Modifier.size(14.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = Color(0xFF7CB7FF),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "app (${document.title})",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE6E1DC),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (expandedAppRoot) {
                            // Group files by language directory (`app/src/main/kotlin`, etc.)
                            val groupedByLang = document.sections.groupBy { it.language }
                            groupedByLang.forEach { (lang, sectionsInLang) ->
                                // Source directory node (`▾ 📁 app/src/main/kotlin`)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 8.dp, top = 3.dp, bottom = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFFA39C93),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = lang.badgeColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "app/${lang.directoryPath.trimEnd('/')}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFD5CEC6),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Package node (`▾ ⊞ com.example.myapp`)
                                val pkgSlug = document.title.lowercase().replace(Regex("[^a-z0-9]"), "").ifBlank { "myapp" }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 34.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountTree,
                                        contentDescription = null,
                                        tint = Color(0xFFA39C93),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "com.example.$pkgSlug",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = Color(0xFFA39C93),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Individual source files (`K Main.kt`)
                                sectionsInLang.forEach { sec ->
                                    val isSelected = activeSection?.id == sec.id
                                    Surface(
                                        color = if (isSelected) Color(0xFF4A382B) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 1.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onSelectFileSection(sec.id) }
                                            .testTag("tree_file_item_${sec.orderIndex}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 36.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                // Circular language badge (`K`)
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .background(sec.language.badgeColor.copy(alpha = 0.25f))
                                                        .border(0.5.dp, sec.language.badgeColor, CircleShape)
                                                ) {
                                                    Text(
                                                        text = sec.language.badgeLabel.take(2),
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontSize = 7.5.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = sec.language.badgeColor
                                                    )
                                                }
                                                Text(
                                                    text = sec.nativeFileName.substringBeforeLast("."),
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color(0xFFFFE5CC) else Color(0xFFE6E1DC),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = "Export ${sec.nativeFileName}",
                                                tint = Color(0xFFA39C93),
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clickable { onExportFile(sec) }
                                            )
                                        }
                                    }
                                }
                            }

                            // Virtual `T module.toml` (opens File Inspector / Project Config)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onOpenFileSettings)
                                    .padding(start = 24.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD4883A).copy(alpha = 0.22f))
                                ) {
                                    Text(
                                        text = "T",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD4883A)
                                    )
                                }
                                Text(
                                    text = "module.toml",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFC5BEB6)
                                )
                            }

                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = Color(0xFF332D29),
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            // Quick Add Language File Strip inside Project Explorer
                            Text(
                                text = "New File by Language",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA39C93),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                LanguageType.entries.forEach { lang ->
                                    Surface(
                                        color = Color(0xFF282320),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.5.dp, lang.badgeColor.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onInsertLanguageFile(lang) }
                                    ) {
                                        Text(
                                            text = "+${lang.nativeExtension}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = lang.badgeColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                LeftActivityTab.SEARCH -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchFilter,
                            onValueChange = { searchFilter = it },
                            placeholder = { Text("Find in project files...", fontSize = 10.5.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF141210),
                                unfocusedContainerColor = Color(0xFF141210),
                                focusedBorderColor = Color(0xFFD4883A)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        val matches = remember(document.sections, searchFilter) {
                            if (searchFilter.isBlank()) document.sections
                            else document.sections.filter {
                                it.nativeFileName.contains(searchFilter, ignoreCase = true) ||
                                    it.codeContent.contains(searchFilter, ignoreCase = true) ||
                                    it.explanationText.contains(searchFilter, ignoreCase = true)
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            matches.forEach { sec ->
                                Surface(
                                    color = Color(0xFF26211E),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelectFileSection(sec.id) }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = sec.nativeFileName,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = sec.language.badgeColor
                                        )
                                        Text(
                                            text = sec.codeContent.lines().firstOrNull { it.isNotBlank() } ?: "// empty",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            color = Color(0xFFA39C93),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                LeftActivityTab.STRUCTURE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "AST & Symbol Outline",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4883A)
                        )
                        if (activeSection == null) {
                            Text("No active file selected.", fontSize = 10.5.sp, color = Color(0xFFA39C93))
                        } else {
                            val debugInfo = remember(activeSection.codeContent, activeSection.language) {
                                CodeLanguageEngine.executeAndDebug(activeSection.codeContent, activeSection.language)
                            }
                            Text(
                                text = "Entry: ${debugInfo.callStack.firstOrNull() ?: "main()"}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFF81C784)
                            )
                            debugInfo.variables.forEach { (varName, varVal) ->
                                Surface(
                                    color = Color(0xFF26211E),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = varName,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = Color(0xFFE6E1DC)
                                        )
                                        Text(
                                            text = varVal,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            color = Color(0xFFA39C93)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                LeftActivityTab.EXPLAIN -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .testTag("codeassist_explain_side_panel")
                    ) {
                        if (activeSection != null) {
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
                        } else {
                            Text("Open a file to edit its explanation.", fontSize = 11.sp, color = Color(0xFFA39C93))
                        }
                    }
                }

                LeftActivityTab.PLUGINS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                            .testTag("codeassist_plugins_side_panel"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Compilers & Debuggers",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4883A)
                            )
                            Surface(
                                color = Color(0xFFD4883A).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFD4883A)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onInstallAllSdks)
                            ) {
                                Text(
                                    text = "Install All",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFC66D),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        uiState.toolchainPlugins.forEach { plugin ->
                            ToolchainPluginRowItem(
                                plugin = plugin,
                                onActionClick = { onInstallOrTogglePlugin(plugin.id) }
                            )
                        }
                    }
                }

                LeftActivityTab.NONE -> Unit
            }
        }
    }
}

// =========================================================================
// CODEASSIST EDITOR FILE TAB BAR (`K Main.kt • ×`)
// =========================================================================
@Composable
private fun CodeAssistEditorTabBar(
    allSections: List<CodeSectionUiModel>,
    openSections: List<CodeSectionUiModel>,
    activeSection: CodeSectionUiModel?,
    hasUnsavedChanges: Boolean,
    onSelectSection: (Long) -> Unit,
    onCloseSectionTab: (Long) -> Unit,
    onNavigateFocusedSection: (Int) -> Unit,
    onInsertNewFile: () -> Unit,
    onOpenFileSettings: () -> Unit,
    onExportActiveFile: () -> Unit
) {
    Surface(
        color = Color(0xFF1F1B19),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF332D29))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .testTag("learned_concepts_navigator_bar"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sectionsToShow = if (openSections.isNotEmpty()) openSections else allSections
            sectionsToShow.forEach { sec ->
                val isCurrent = activeSection?.id == sec.id
                val langColor = sec.language.badgeColor
                Surface(
                    color = if (isCurrent) Color(0xFF47372B) else Color(0xFF26211E),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(
                        width = if (isCurrent) 1.dp else 0.5.dp,
                        color = if (isCurrent) Color(0xFFD4883A) else Color(0xFF3A332D)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onSelectSection(sec.id) }
                        .testTag("jump_concept_chip_${sec.orderIndex}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        // Circular Language Badge (`K`)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(langColor.copy(alpha = 0.25f))
                                .border(0.5.dp, langColor, CircleShape)
                        ) {
                            Text(
                                text = sec.language.badgeLabel.take(2),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = langColor
                            )
                        }

                        Text(
                            text = sec.nativeFileName,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) Color(0xFFFFE5CC) else Color(0xFFD5CEC6)
                        )

                        // Unsaved / Modified amber dot (`•`) like CodeAssist
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (hasUnsavedChanges) Color(0xFFFFC66D) else Color(0xFF81C784))
                            )
                        }

                        // Tab close button `×`
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close ${sec.nativeFileName} Tab",
                            tint = Color(0xFFA39C93),
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .clickable { onCloseSectionTab(sec.id) }
                                .testTag("close_tab_${sec.orderIndex}")
                        )
                    }
                }
            }

            // `+` New File Tab Pill
            Surface(
                color = Color(0xFF26211E),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(0.5.dp, Color(0xFF3A332D)),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onInsertNewFile)
                    .testTag("tab_bar_new_file_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New File",
                        tint = Color(0xFFD4883A),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            if (allSections.size > 1 && activeSection != null) {
                val curNum = activeSection.orderIndex + 1
                IconButton(
                    onClick = { onNavigateFocusedSection(-1) },
                    enabled = curNum > 1,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous File",
                        tint = Color(0xFFA39C93),
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(
                    onClick = { onNavigateFocusedSection(1) },
                    enabled = curNum < allSections.size,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next File",
                        tint = Color(0xFFA39C93),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Export / Copy Active File Bundle
            Surface(
                color = Color(0xFF26211E),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(0.5.dp, Color(0xFF3A332D)),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onExportActiveFile)
                    .testTag("export_markdown_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Export or Share File",
                        tint = Color(0xFFD4883A),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Export / Copy",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Color(0xFFE6E1DC)
                    )
                }
            }
        }
    }
}

// =========================================================================
// CODEASSIST "NO FILE OPEN" ZERO-STATE SCREEN (Screenshot 4)
// =========================================================================
@Composable
private fun CodeAssistNoFileOpenZeroState(
    document: DocumentDetailUiModel,
    onSearchEverywhere: () -> Unit,
    onGoToFile: () -> Unit,
    onRecentFiles: () -> Unit,
    onFindInFiles: () -> Unit,
    onNewFile: () -> Unit,
    onProjectFiles: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1615))
            .testTag("no_file_open_zero_state"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.width(340.dp)
        ) {
            Text(
                text = "No file open",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7D746D)
            )
            Spacer(modifier = Modifier.height(4.dp))

            val shortcuts = listOf(
                Triple("Search everywhere", "Double Shift", onSearchEverywhere),
                Triple("Go to file", "Ctrl + Shift + N", onGoToFile),
                Triple("Recent files", "Ctrl + E", onRecentFiles),
                Triple("Find in files", "Ctrl + Shift + F", onFindInFiles),
                Triple("New file", "Ctrl + N", onNewFile),
                Triple("Project files", "Alt + 1", onProjectFiles)
            )

            shortcuts.forEach { (label, keycap, action) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = action)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF8C827A)
                    )
                    Surface(
                        color = Color(0xFF282320),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, Color(0xFF3E3630))
                    ) {
                        Text(
                            text = keycap,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA39C93),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
    onRunActiveSectionInTerminal: () -> Unit,
    onInsertLanguageCodeBlock: (LanguageType) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(14.dp, RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .testTag("docked_split_window_${windowType.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
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
                    onRunActiveSectionInTerminal = onRunActiveSectionInTerminal,
                    onInsertLanguageCodeBlock = onInsertLanguageCodeBlock
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
    onRunActiveSectionInTerminal: () -> Unit,
    onInsertLanguageCodeBlock: (LanguageType) -> Unit = {}
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
                onInsertNewLanguageSection = onInsertLanguageCodeBlock,
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
                        text = "zsh • ${activeSection.language.displayName} ${activeSection.nativeFileName}",
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
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = uiState.terminalExecutionOutput
                            ?: CodeLanguageEngine.simulateExecutionForLanguage(activeSection.codeContent, activeSection.language),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface,
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
    onDeleteSection: (Long) -> Unit,
    onSelectLanguage: (LanguageType) -> Unit = {},
    onSelectThemeOverride: (com.example.ui.theme.AppThemeId?) -> Unit = {},
    onOpenFileSettings: () -> Unit = {},
    onRunCode: () -> Unit = {},
    onExportOrCopyFile: (Boolean) -> Unit = {}
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
        onDeleteSection = { onDeleteSection(section.id) },
        onSelectLanguage = onSelectLanguage,
        onSelectThemeOverride = onSelectThemeOverride,
        onOpenFileSettings = onOpenFileSettings,
        onRunCode = onRunCode,
        onExportOrCopyFile = onExportOrCopyFile
    )
}
