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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.learning.LearningTrack
import com.example.learning.StudentCurriculumCatalog
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
import com.example.model.LocalCodeSnapshot
import com.example.model.SplitScreenOrientation
import com.example.ui.components.ActiveLessonBannerCard
import com.example.ui.components.CodeAssistBottomSheetPeekBar
import com.example.ui.components.CodeAssistDrawerContent
import com.example.ui.components.CodeAssistOpenFileTabs
import com.example.ui.components.CodeAssistThemeQuickPickerDialog
import com.example.ui.components.CommandPaletteDropdownContent
import com.example.ui.components.ExplanationWindowBodyContent
import com.example.ui.components.FileSettingsDialog
import com.example.ui.components.FolderAndSectionsDropdownContent
import com.example.ui.components.IdeBottomDrawer
import com.example.ui.components.KotlinCodeSectionCard
import com.example.ui.components.LeftRailIdeTool
import com.example.ui.components.MacDraggableFloatingWindow
import com.example.ui.components.RecentFilesModalDialog
import com.example.ui.components.ResizableIslandDropdownWindow
import com.example.ui.components.exportOrCopyCodePayload
import com.example.ui.theme.AppThemeId
import com.example.ui.theme.CodeAssistAppTheme
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAdaptiveThemeSpec
import com.example.ui.theme.Tier2CompletionSelectedBg
import com.example.ui.theme.Tier3FloatingWindowActiveBevel
import com.example.viewmodel.EditorUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    onAlignmentChange: (com.example.model.TextAlignmentChoice) -> Unit,
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
    onUpdateSectionThemeOverride: (Long, AppThemeId?) -> Unit = { _, _ -> },
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
    onToggleLineBreakpoint: (Long, Int) -> Unit = { _, _ -> },
    onAddWatchExpression: (String) -> Unit = {},
    onRemoveWatchExpression: (String) -> Unit = {},
    onOptimizeSectionImports: (Long) -> Unit = {},
    onJumpToCodeLine: (Int) -> Unit = {},
    onUpdateTerminalStdin: (String) -> Unit = {},
    onCreateLocalSnapshot: (Long) -> Unit = {},
    onRestoreLocalSnapshot: (Long, Long) -> Unit = { _, _ -> },
    onCompileActiveSection: (Boolean) -> Unit = {},
    onDebugResume: () -> Unit = {},
    onDebugStepOver: () -> Unit = {},
    onDebugReset: () -> Unit = {},
    onEvaluateRepl: (String) -> Unit = {},
    onClearRepl: () -> Unit = {},
    onReorderEditorTab: (Long, Int) -> Unit = { _, _ -> },
    onRunAndGradeLesson: () -> Unit = {},
    onLoadLessonSolution: () -> Unit = {},
    onCloseLessonMode: () -> Unit = {},
    onOpenQuiz: (LearningTrack?) -> Unit = {},
    onToggleGuidedBeginnerMode: () -> Unit = {},
    onToggleRecentFilesDialog: (Boolean?) -> Unit = {},
    onExecuteShellCommand: (String) -> Unit = {},
    onRunAndroidBuildPipeline: () -> Unit = {},
    onCreateGitCommit: (String) -> Unit = {},
    onSwitchGitBranch: (String) -> Unit = {},
    onAddManifestPermission: (String) -> Unit = {},
    onCreateStarterFile: (String, String) -> Unit = { _, _ -> },
    onSelectCodeAssistAppTheme: (CodeAssistAppTheme) -> Unit = {},
    onSelectEditorScheme: (AppThemeId) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current
    val adaptive = LocalAdaptiveThemeSpec.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showFileSettingsDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showThemeQuickPicker by remember { mutableStateOf(false) }
    var expandActiveLessonCard by remember { mutableStateOf(false) }
    var cursorLine by remember { mutableIntStateOf(1) }
    var cursorColumn by remember { mutableIntStateOf(1) }

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

    val allProjectSymbols = remember(document.sections) {
        document.sections.flatMap { sec ->
            CodeLanguageEngine.extractSymbolsForSection(
                code = sec.codeContent,
                language = sec.language,
                sectionId = sec.id,
                fileName = sec.nativeFileName
            )
        }
    }

    val allDiagnostics = remember(activeSection?.codeContent, activeSection?.language) {
        if (activeSection == null) emptyList()
        else CodeLanguageEngine.getDiagnosticsForLanguage(activeSection.codeContent, activeSection.language)
    }

    val syntaxErrors = remember(allDiagnostics) {
        allDiagnostics.filter { it.severity == DiagnosticSeverity.ERROR }
    }
    val warningCount = remember(allDiagnostics) {
        allDiagnostics.count { it.severity == DiagnosticSeverity.WARNING }
    }

    val effectiveSplitWindow: FloatingWindowType? = uiState.dockedSplitWindow
        ?: if (uiState.explanationWindowMode == ExplanationWindowMode.SPLIT_SCREEN) {
            FloatingWindowType.EXPLANATION
        } else {
            null
        }

    BackHandler {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (uiState.showRecentFilesDialog) {
            onToggleRecentFilesDialog(false)
        } else if (uiState.extensionRenameWarningDialog != null) {
            onDismissExtensionRenameWarning()
        } else if (uiState.isBottomDrawerOpen) {
            onCloseBottomDrawer()
        } else if (uiState.activeIslandDropdown != IslandDropdownMenu.NONE) {
            onCloseIslandDropdown()
        } else if (uiState.openFloatingWindows.isNotEmpty()) {
            onCloseFloatingWindow(uiState.openFloatingWindows.last())
        } else if (effectiveSplitWindow != null) {
            onCloseSplitScreenWindow()
        } else {
            focusManager.clearFocus()
            onCloseDocument()
        }
    }

    val expectedAndroidKeyCode = AndroidKeyEvent.KEYCODE_A + (uiState.shortcutLetter - 'A')

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            CodeAssistDrawerContent(
                document = document,
                activeSectionId = activeSection?.id,
                activeGitBranch = uiState.activeGitBranch,
                hasUnsavedChanges = uiState.hasUnsavedChanges,
                onSelectFile = { sec -> onJumpToSectionConcept(sec.id) },
                onNewFileLanguage = { lang -> onInsertLanguageCodeBlock(lang) },
                onCreateStarterFile = onCreateStarterFile,
                onDeleteFile = onDeleteSection,
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // =========================================================================
                    // 1. 1:1 CODEASSIST SINGLE-ROW TOP APP BAR (Drawer Menu, Title, Undo/Redo, Save, Run, Overflow)
                    // =========================================================================
                    TopAppBar(
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        if (drawerState.isOpen) drawerState.close() else drawerState.open()
                                    }
                                },
                                modifier = Modifier.testTag("toggle_files_sidebar_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Project File Tree"
                                )
                            }
                        },
                        title = {
                            Column {
                                Text(
                                    text = document.title.ifBlank { "Untitled Project" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = buildString {
                                        append(activeSection?.nativeFileName ?: "${document.sections.size} files")
                                        append(" • ")
                                        append(
                                            if (uiState.hasUnsavedChanges) {
                                                "Sync ${uiState.secondsUntilAutoSave}s"
                                            } else {
                                                "Saved"
                                            }
                                        )
                                    },
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (uiState.hasUnsavedChanges) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.testTag("autosave_status_pill")
                                )
                            }
                        },
                        actions = {
                            if (!adaptive.useCompactTopActions) {
                                IconButton(
                                    onClick = onUndo,
                                    enabled = uiState.canUndo,
                                    modifier = Modifier.testTag("undo_top_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Undo,
                                        contentDescription = "Undo"
                                    )
                                }
                                IconButton(
                                    onClick = onRedo,
                                    enabled = uiState.canRedo,
                                    modifier = Modifier.testTag("redo_top_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Redo,
                                        contentDescription = "Redo"
                                    )
                                }
                            }

                            IconButton(
                                onClick = onSaveNow,
                                modifier = Modifier.testTag("save_now_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Project",
                                    tint = if (uiState.hasUnsavedChanges) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }

                            FilledTonalIconButton(
                                onClick = onRunActiveSectionInTerminal,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("island_run_code_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run & Build"
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { showOverflowMenu = true },
                                    modifier = Modifier.testTag("editor_topbar_overflow_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Workspace Menu"
                                    )
                                }

                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    if (adaptive.useCompactTopActions) {
                                        DropdownMenuItem(
                                            text = { Text("Undo") },
                                            enabled = uiState.canUndo,
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null) },
                                            onClick = { showOverflowMenu = false; onUndo() },
                                            modifier = Modifier.testTag("undo_top_button")
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Redo") },
                                            enabled = uiState.canRedo,
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = null) },
                                            onClick = { showOverflowMenu = false; onRedo() },
                                            modifier = Modifier.testTag("redo_top_button")
                                        )
                                        HorizontalDivider()
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Recent Files (Ctrl+E)") },
                                        onClick = { showOverflowMenu = false; onToggleRecentFilesDialog(true) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Command Palette (⌘P)") },
                                        onClick = {
                                            showOverflowMenu = false
                                            onToggleIslandDropdown(IslandDropdownMenu.COMMAND_PALETTE)
                                        },
                                        modifier = Modifier.testTag("island_command_palette_button")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Layout & Compose UI Preview") },
                                        onClick = {
                                            showOverflowMenu = false
                                            onSetBottomDrawerTab(BottomDrawerTab.UI_PREVIEW)
                                            if (!uiState.isBottomDrawerOpen) onToggleBottomDrawer(BottomDrawerTab.UI_PREVIEW)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Android Manifest & Resources") },
                                        onClick = {
                                            showOverflowMenu = false
                                            onSetBottomDrawerTab(BottomDrawerTab.ANDROID_STUDIO)
                                            if (!uiState.isBottomDrawerOpen) onToggleBottomDrawer(BottomDrawerTab.ANDROID_STUDIO)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Memory & Stack Diagrams") },
                                        onClick = {
                                            showOverflowMenu = false
                                            onCompileActiveSection(false)
                                            onSetBottomDrawerTab(BottomDrawerTab.MEMORY_STACK)
                                            if (!uiState.isBottomDrawerOpen) onToggleBottomDrawer(BottomDrawerTab.MEMORY_STACK)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("AST & Bytecode Compiler") },
                                        onClick = { showOverflowMenu = false; onCompileActiveSection(true) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Optimize Imports") },
                                        onClick = {
                                            showOverflowMenu = false
                                            activeSection?.let { onOptimizeSectionImports(it.id) }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Format Active File") },
                                        onClick = {
                                            showOverflowMenu = false
                                            activeSection?.let { onFormatSectionCode(it.id) }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Explain & Notes Window") },
                                        onClick = {
                                            showOverflowMenu = false
                                            onToggleFloatingWindow(FloatingWindowType.EXPLANATION)
                                        },
                                        modifier = Modifier.testTag("island_explanation_window_toggle")
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("CodeAssist Themes & Schemes") },
                                        leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                                        onClick = { showOverflowMenu = false; showThemeQuickPicker = true }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("File & Bundle Settings") },
                                        onClick = {
                                            showOverflowMenu = false
                                            if (activeSection != null) showFileSettingsDialog = true else onOpenSettings()
                                        },
                                        modifier = Modifier.testTag("editor_shortcut_settings_button")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Studio Settings") },
                                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                        onClick = { showOverflowMenu = false; onOpenSettings() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Vault 'xyz' Recovery") },
                                        onClick = { showOverflowMenu = false; onOpenVaultModal() },
                                        modifier = Modifier.testTag("open_vault_modal_button")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Close Project") },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) },
                                        onClick = { showOverflowMenu = false; onCloseDocument() },
                                        modifier = Modifier.testTag("back_to_documents_button")
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )

                    // =========================================================================
                    // 2. FEEDBACK / NOTIFICATION TOAST BANNER
                    // =========================================================================
                    AnimatedVisibility(
                        visible = uiState.aiStatusMessage != null || uiState.aiErrorBanner != null
                    ) {
                        val isError = uiState.aiErrorBanner != null
                        val bannerText = uiState.aiErrorBanner ?: uiState.aiStatusMessage.orEmpty()
                        Surface(
                            color = if (isError) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
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
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = if (isError) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    },
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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

                    // =========================================================================
                    // 3. 1:1 CODEASSIST OPEN-FILE SCROLLABLE TAB ROW
                    // =========================================================================
                    CodeAssistOpenFileTabs(
                        openSections = openTabSections,
                        activeSectionId = activeSection?.id,
                        hasUnsavedChanges = uiState.hasUnsavedChanges,
                        onSelectSection = { sec -> onJumpToSectionConcept(sec.id) },
                        onCloseTab = onCloseEditorTab,
                        onAddTab = onInsertCodeBlock
                    )
                    HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            },
            modifier = modifier
                .fillMaxSize()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            expectedAndroidKeyCode -> {
                                onInsertCodeBlock()
                                true
                            }
                            AndroidKeyEvent.KEYCODE_E -> {
                                onToggleRecentFilesDialog(null)
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Optional compact 1-line lesson strip (keeps editor spacious; expands on tap)
                val activeLesson = remember(uiState.activeLessonId) {
                    StudentCurriculumCatalog.lessonById(uiState.activeLessonId)
                }
                if (activeLesson != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandActiveLessonCard = !expandActiveLessonCard }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Lesson: ${activeLesson.title}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable(onClick = onRunAndGradeLesson)
                            ) {
                                Text(
                                    text = "Check ▶",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                            Icon(
                                imageVector = if (expandActiveLessonCard) {
                                    Icons.Default.KeyboardArrowUp
                                } else {
                                    Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = "Toggle Lesson Guide",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = expandActiveLessonCard) {
                        ActiveLessonBannerCard(
                            lesson = activeLesson,
                            gradeResult = uiState.lastAutoGradeResult,
                            isGuidedBeginnerMode = uiState.isGuidedBeginnerMode,
                            onToggleGuidedMode = onToggleGuidedBeginnerMode,
                            onRunAndGrade = onRunAndGradeLesson,
                            onOpenQuiz = { onOpenQuiz(activeLesson.track) },
                            onLoadSolution = onLoadLessonSolution,
                            onCloseLessonMode = onCloseLessonMode,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }

                // =========================================================================
                // MAIN CODE EDITOR AREA (Unobstructed 100% Width)
                // =========================================================================
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val totalHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }
                    val totalWidthPx = with(density) { maxWidth.toPx().coerceAtLeast(1f) }

                    if (activeSection == null) {
                        CodeAssistNoFileOpenZeroState(
                            onSearchEverywhere = { onToggleIslandDropdown(IslandDropdownMenu.COMMAND_PALETTE) },
                            onGoToFile = { coroutineScope.launch { drawerState.open() } },
                            onRecentFiles = { onToggleRecentFilesDialog(true) },
                            onNewFile = onInsertCodeBlock,
                            onProjectFiles = { coroutineScope.launch { drawerState.open() } }
                        )
                    } else {
                        val isCodeActive = (uiState.activeTarget as? ActiveEditorTarget.SectionCode)?.sectionId == activeSection.id
                        val isExplanationActive = (uiState.activeTarget as? ActiveEditorTarget.SectionExplanation)?.sectionId == activeSection.id

                        if (effectiveSplitWindow != null) {
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
                                            activeLeftRailTool = LeftRailIdeTool.NONE,
                                            canUndo = uiState.canUndo,
                                            requestAutoFocus = uiState.newlyInsertedSectionId == activeSection.id,
                                            onAutoFocusConsumed = onClearNewSectionFocusFlag,
                                            onSetActiveTarget = onSetActiveTarget,
                                            onUpdateSectionCode = onUpdateSectionCode,
                                            onUpdateSectionExplanation = onUpdateSectionExplanation,
                                            onUpdateSectionTitle = onUpdateSectionTitle,
                                            onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                            onActiveCodeLineChanged = { ln, col ->
                                                cursorLine = ln
                                                cursorColumn = col
                                            },
                                            onFormatSectionCode = onFormatSectionCode,
                                            onUndo = onUndo,
                                            onDeactivateLeftRailTool = {},
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
                                            },
                                            activeBreakpoints = uiState.breakpointsBySectionId[activeSection.id] ?: emptySet(),
                                            onToggleBreakpoint = { line -> onToggleLineBreakpoint(activeSection.id, line) },
                                            onOptimizeImports = { onOptimizeSectionImports(activeSection.id) },
                                            projectSymbols = allProjectSymbols,
                                            onNavigateToSymbolDefinition = { sym ->
                                                onJumpToSectionConcept(sym.sectionId)
                                                onJumpToCodeLine(sym.line)
                                            },
                                            requestedJumpLine = uiState.requestedJumpLine,
                                            jumpLineTrigger = uiState.jumpLineTrigger,
                                            localSnapshots = uiState.localSnapshotsBySectionId[activeSection.id].orEmpty(),
                                            onCreateSnapshot = { onCreateLocalSnapshot(activeSection.id) },
                                            onRestoreSnapshot = { snapId -> onRestoreLocalSnapshot(activeSection.id, snapId) },
                                            isGuidedBeginnerMode = uiState.isGuidedBeginnerMode
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(18.dp)
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
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary)
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
                                            activeLeftRailTool = LeftRailIdeTool.NONE,
                                            canUndo = uiState.canUndo,
                                            requestAutoFocus = uiState.newlyInsertedSectionId == activeSection.id,
                                            onAutoFocusConsumed = onClearNewSectionFocusFlag,
                                            onSetActiveTarget = onSetActiveTarget,
                                            onUpdateSectionCode = onUpdateSectionCode,
                                            onUpdateSectionExplanation = onUpdateSectionExplanation,
                                            onUpdateSectionTitle = onUpdateSectionTitle,
                                            onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                            onActiveCodeLineChanged = { ln, col ->
                                                cursorLine = ln
                                                cursorColumn = col
                                            },
                                            onFormatSectionCode = onFormatSectionCode,
                                            onUndo = onUndo,
                                            onDeactivateLeftRailTool = {},
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
                                            },
                                            activeBreakpoints = uiState.breakpointsBySectionId[activeSection.id] ?: emptySet(),
                                            onToggleBreakpoint = { line -> onToggleLineBreakpoint(activeSection.id, line) },
                                            onOptimizeImports = { onOptimizeSectionImports(activeSection.id) },
                                            projectSymbols = allProjectSymbols,
                                            onNavigateToSymbolDefinition = { sym ->
                                                onJumpToSectionConcept(sym.sectionId)
                                                onJumpToCodeLine(sym.line)
                                            },
                                            requestedJumpLine = uiState.requestedJumpLine,
                                            jumpLineTrigger = uiState.jumpLineTrigger,
                                            localSnapshots = uiState.localSnapshotsBySectionId[activeSection.id].orEmpty(),
                                            onCreateSnapshot = { onCreateLocalSnapshot(activeSection.id) },
                                            onRestoreSnapshot = { snapId -> onRestoreLocalSnapshot(activeSection.id, snapId) },
                                            isGuidedBeginnerMode = uiState.isGuidedBeginnerMode
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(22.dp)
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
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "Drag to resize split",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
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
                            // Full-width, distraction-free CodeAssist code canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("single_section_focused_scroll_container")
                            ) {
                                FullScreenAppleCmdSectionEditor(
                                    section = activeSection,
                                    isCodeActive = isCodeActive,
                                    isExplanationActive = isExplanationActive,
                                    activeLeftRailTool = LeftRailIdeTool.NONE,
                                    canUndo = uiState.canUndo,
                                    requestAutoFocus = uiState.newlyInsertedSectionId == activeSection.id,
                                    onAutoFocusConsumed = onClearNewSectionFocusFlag,
                                    onSetActiveTarget = onSetActiveTarget,
                                    onUpdateSectionCode = onUpdateSectionCode,
                                    onUpdateSectionExplanation = onUpdateSectionExplanation,
                                    onUpdateSectionTitle = onUpdateSectionTitle,
                                    onUpdateSectionConceptTag = onUpdateSectionConceptTag,
                                    onActiveCodeLineChanged = { ln, col ->
                                        cursorLine = ln
                                        cursorColumn = col
                                    },
                                    onFormatSectionCode = onFormatSectionCode,
                                    onUndo = onUndo,
                                    onDeactivateLeftRailTool = {},
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
                                    },
                                    activeBreakpoints = uiState.breakpointsBySectionId[activeSection.id] ?: emptySet(),
                                    onToggleBreakpoint = { line -> onToggleLineBreakpoint(activeSection.id, line) },
                                    onOptimizeImports = { onOptimizeSectionImports(activeSection.id) },
                                    projectSymbols = allProjectSymbols,
                                    onNavigateToSymbolDefinition = { sym ->
                                        onJumpToSectionConcept(sym.sectionId)
                                        onJumpToCodeLine(sym.line)
                                    },
                                    requestedJumpLine = uiState.requestedJumpLine,
                                    jumpLineTrigger = uiState.jumpLineTrigger,
                                    localSnapshots = uiState.localSnapshotsBySectionId[activeSection.id].orEmpty(),
                                    onCreateSnapshot = { onCreateLocalSnapshot(activeSection.id) },
                                    onRestoreSnapshot = { snapId -> onRestoreLocalSnapshot(activeSection.id, snapId) },
                                    isGuidedBeginnerMode = uiState.isGuidedBeginnerMode
                                )
                            }
                        }

                        // Optional Floating Windows when explicitly requested from menu/command palette
                        if (uiState.openFloatingWindows.isNotEmpty()) {
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
                                        initialWidthDp = 360f,
                                        initialHeightDp = 290f,
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
                                        } else null
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

                    // Bottom Command Palette Overlay (`⌘P`)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = uiState.activeIslandDropdown == IslandDropdownMenu.COMMAND_PALETTE,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                            ) + fadeOut()
                        ) {
                            ResizableIslandDropdownWindow(
                                title = "⌘P Command Palette",
                                subtitle = "Run an IDE command, switch projects, or jump to a file",
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
                                            "optimize_imports" -> activeSection?.let { onOptimizeSectionImports(it.id) }
                                            "toggle_split" -> onDockWindowToSplitScreen(FloatingWindowType.EXPLANATION)
                                            "float_explain" -> onToggleFloatingWindow(FloatingWindowType.EXPLANATION)
                                            "run_code" -> onRunActiveSectionInTerminal()
                                            "debug_vm" -> onDebugResume()
                                            "open_repl" -> {
                                                onSetBottomDrawerTab(BottomDrawerTab.REPL)
                                                if (!uiState.isBottomDrawerOpen) onToggleBottomDrawer(BottomDrawerTab.REPL)
                                            }
                                            "inspect_bytecode" -> onCompileActiveSection(true)
                                            "open_git_diff" -> {
                                                onSetBottomDrawerTab(BottomDrawerTab.GIT_DIFF)
                                                if (!uiState.isBottomDrawerOpen) onToggleBottomDrawer(BottomDrawerTab.GIT_DIFF)
                                            }
                                            "memory_stack" -> {
                                                onCompileActiveSection(false)
                                                onSetBottomDrawerTab(BottomDrawerTab.MEMORY_STACK)
                                                if (!uiState.isBottomDrawerOpen) onToggleBottomDrawer(BottomDrawerTab.MEMORY_STACK)
                                            }
                                            "ui_preview" -> {
                                                onSetBottomDrawerTab(BottomDrawerTab.UI_PREVIEW)
                                                if (!uiState.isBottomDrawerOpen) onToggleBottomDrawer(BottomDrawerTab.UI_PREVIEW)
                                            }
                                            "open_quiz" -> onOpenQuiz(null)
                                            "recent_files" -> onToggleRecentFilesDialog(true)
                                            "android_inspector" -> {
                                                onSetBottomDrawerTab(BottomDrawerTab.ANDROID_STUDIO)
                                                if (!uiState.isBottomDrawerOpen) onToggleBottomDrawer(BottomDrawerTab.ANDROID_STUDIO)
                                            }
                                            "toggle_guided_mode" -> onToggleGuidedBeginnerMode()
                                            "display_profile" -> onShowStatusToast(
                                                "Screen: ${adaptive.profile.widthDp}x${adaptive.profile.heightDp}dp (${adaptive.profile.aspectRatioLabel})"
                                            )
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

                // =========================================================================
                // 1:1 CODEASSIST COLLAPSED PEEK BAR OR EXPANDED BOTTOM SHEET
                // =========================================================================
                if (!uiState.isBottomDrawerOpen) {
                    CodeAssistBottomSheetPeekBar(
                        errorCount = syntaxErrors.size,
                        warningCount = warningCount,
                        cursorLine = cursorLine,
                        cursorColumn = cursorColumn,
                        onOpenTab = { tab ->
                            onSetBottomDrawerTab(tab)
                            onToggleBottomDrawer(tab)
                        }
                    )
                } else {
                    IdeBottomDrawer(
                        isOpen = true,
                        activeTab = uiState.activeBottomDrawerTab,
                        activeSection = activeSection,
                        executionOutput = uiState.terminalExecutionOutput,
                        heightFraction = uiState.bottomDrawerHeightFraction,
                        toolchainPlugins = uiState.toolchainPlugins,
                        activeBreakpoints = uiState.breakpointsBySectionId[activeSection?.id ?: -1L] ?: emptySet(),
                        watchExpressions = uiState.watchExpressions,
                        stdinInput = uiState.terminalStdinInput,
                        compilationArtifact = uiState.latestCompilation,
                        vmDebugState = uiState.vmDebugState,
                        vmTrace = uiState.vmTrace,
                        vmTraceIndex = uiState.vmTraceIndex,
                        replHistory = uiState.replHistory,
                        localSnapshots = uiState.localSnapshotsBySectionId[activeSection?.id ?: -1L].orEmpty(),
                        onUpdateStdinInput = onUpdateTerminalStdin,
                        onTabSelected = onSetBottomDrawerTab,
                        onHeightFractionChange = onSetBottomDrawerHeightFraction,
                        onRunCode = onRunActiveSectionInTerminal,
                        onClearTerminal = onClearTerminal,
                        onCloseDrawer = onCloseBottomDrawer,
                        onJumpToLine = onJumpToCodeLine,
                        onInstallPlugin = onInstallOrTogglePlugin,
                        onOpenPluginManager = { onOpenSettings() },
                        onToggleBreakpoint = { line -> activeSection?.let { onToggleLineBreakpoint(it.id, line) } },
                        onAddWatchExpression = onAddWatchExpression,
                        onRemoveWatchExpression = onRemoveWatchExpression,
                        onApplyQuickFixCode = { fixedCode ->
                            activeSection?.let { onUpdateSectionCode(it.id, fixedCode) }
                        },
                        onCompileAstBytecode = { onCompileActiveSection(true) },
                        onDebugResumeVm = onDebugResume,
                        onDebugStepOverVm = onDebugStepOver,
                        onDebugResetVm = onDebugReset,
                        onEvaluateRepl = onEvaluateRepl,
                        onClearRepl = onClearRepl,
                        onCreateSnapshot = { activeSection?.let { onCreateLocalSnapshot(it.id) } },
                        onRestoreSnapshot = { snapId -> activeSection?.let { onRestoreLocalSnapshot(it.id, snapId) } },
                        onInsertUiSample = { snippet ->
                            activeSection?.let { sec ->
                                onUpdateSectionCode(sec.id, sec.codeContent + snippet)
                            }
                        },
                        allSections = document.sections,
                        isGuidedBeginnerMode = uiState.isGuidedBeginnerMode,
                        activeGitBranch = uiState.activeGitBranch,
                        localGitCommits = uiState.localGitCommits,
                        buildTasks = uiState.latestBuildTasks,
                        onExecuteShellCommand = onExecuteShellCommand,
                        onRunBuildPipeline = onRunAndroidBuildPipeline,
                        onCreateGitCommit = onCreateGitCommit,
                        onSwitchGitBranch = onSwitchGitBranch,
                        onAddManifestPermission = onAddManifestPermission,
                        onCreateStarterFile = onCreateStarterFile
                    )
                }
            }
        }
    }

    if (showThemeQuickPicker) {
        CodeAssistThemeQuickPickerDialog(
            currentAppTheme = uiState.codeAssistAppTheme,
            currentEditorScheme = uiState.activeAppTheme,
            onSelectAppTheme = onSelectCodeAssistAppTheme,
            onSelectEditorScheme = onSelectEditorScheme,
            onDismiss = { showThemeQuickPicker = false }
        )
    }

    if (uiState.showRecentFilesDialog) {
        RecentFilesModalDialog(
            sections = document.sections,
            recentSectionIds = uiState.recentSectionIds,
            focusedSectionId = activeSection?.id,
            onSelectSection = { secId ->
                onJumpToSectionConcept(secId)
                onToggleRecentFilesDialog(false)
            },
            onDismiss = { onToggleRecentFilesDialog(false) }
        )
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

    uiState.extensionRenameWarningDialog?.let { warningMsg ->
        AlertDialog(
            onDismissRequest = onDismissExtensionRenameWarning,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Language Extension Locked",
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Text(
                    text = warningMsg,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(onClick = onDismissExtensionRenameWarning) {
                    Text("Acknowledge", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun CodeAssistNoFileOpenZeroState(
    onSearchEverywhere: () -> Unit,
    onGoToFile: () -> Unit,
    onRecentFiles: () -> Unit,
    onNewFile: () -> Unit,
    onProjectFiles: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("no_file_open_zero_state"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .width(340.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "No file open",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val shortcuts = listOf(
                Triple("Open Project Files", "Drawer", onProjectFiles),
                Triple("Recent files", "Ctrl + E", onRecentFiles),
                Triple("Search everywhere", "⌘P", onSearchEverywhere),
                Triple("Go to file", "Ctrl + Shift + N", onGoToFile),
                Triple("New file", "Ctrl + N", onNewFile)
            )

            shortcuts.forEach { (label, keycap, action) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = action)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = keycap,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            .shadow(8.dp, RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .testTag("docked_split_window_${windowType.name.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${windowType.windowTitle} (${((1f - splitRatio) * 100).toInt()}%)",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onToggleOrientation,
                        modifier = Modifier.size(28.dp).testTag("split_orientation_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerticalSplit,
                            contentDescription = "Switch Split Orientation",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onUndockToFloating,
                        modifier = Modifier.size(28.dp).testTag("split_undock_to_floating_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Undock to Floating",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onCloseSplit,
                        modifier = Modifier.size(28.dp).testTag("split_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Split",
                            modifier = Modifier.size(16.dp)
                        )
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Concept Tag: [${activeSection.effectiveLearnedConceptTag}] • Quota: ${uiState.remainingAiRpmQuota}/10 RPM",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onRequestAiExplain(activeSection.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Explain", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onRequestAiRefactor(activeSection.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Refactor", fontSize = 11.sp)
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
                        Text("Name & Tag", fontSize = 10.5.sp)
                    }
                    OutlinedButton(
                        onClick = { onRequestAiKDoc(activeSection.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add KDoc", fontSize = 10.5.sp)
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
                        text = "${activeSection.language.displayName} • ${activeSection.nativeFileName}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Button(onClick = onRunActiveSectionInTerminal) {
                        Text("Re-Run", fontSize = 10.5.sp)
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
    onSelectThemeOverride: (AppThemeId?) -> Unit = {},
    onOpenFileSettings: () -> Unit = {},
    onRunCode: () -> Unit = {},
    onExportOrCopyFile: (Boolean) -> Unit = {},
    activeBreakpoints: Set<Int> = emptySet(),
    onToggleBreakpoint: (Int) -> Unit = {},
    onOptimizeImports: () -> Unit = {},
    projectSymbols: List<CodeLanguageEngine.CodeSymbolDefinition> = emptyList(),
    onNavigateToSymbolDefinition: (CodeLanguageEngine.CodeSymbolDefinition) -> Unit = {},
    requestedJumpLine: Int? = null,
    jumpLineTrigger: Int = 0,
    localSnapshots: List<LocalCodeSnapshot> = emptyList(),
    onCreateSnapshot: () -> Unit = {},
    onRestoreSnapshot: (Long) -> Unit = {},
    isGuidedBeginnerMode: Boolean = true
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
        onExportOrCopyFile = onExportOrCopyFile,
        activeBreakpoints = activeBreakpoints,
        onToggleBreakpoint = onToggleBreakpoint,
        onOptimizeImports = onOptimizeImports,
        projectSymbols = projectSymbols,
        onNavigateToSymbolDefinition = onNavigateToSymbolDefinition,
        requestedJumpLine = requestedJumpLine,
        jumpLineTrigger = jumpLineTrigger,
        localSnapshots = localSnapshots,
        onCreateSnapshot = onCreateSnapshot,
        onRestoreSnapshot = onRestoreSnapshot,
        isGuidedBeginnerMode = isGuidedBeginnerMode
    )
}
