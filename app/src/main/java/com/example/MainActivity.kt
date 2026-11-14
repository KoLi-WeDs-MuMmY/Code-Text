package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FirstLaunchVaultRecoveryDialog
import com.example.ui.components.ShortcutAndSettingsDialog
import com.example.ui.screens.DocumentEditorScreen
import com.example.ui.screens.DocumentListScreen
import com.example.ui.theme.KtScribeTheme
import com.example.ui.theme.ThemeMode
import com.example.util.configureAppCheck
import com.example.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        // STEP 2: Enable edge-to-edge rendering
        enableEdgeToEdge()

        setContent {
            KtScribeApp()
        }
    }
}

@Composable
fun KtScribeApp(
    editorViewModel: EditorViewModel = viewModel(
        factory = EditorViewModel.Factory(LocalContext.current)
    )
) {
    val uiState by editorViewModel.uiState.collectAsStateWithLifecycle()
    val documents by editorViewModel.filteredDocuments.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Observe Activity lifecycle to immediately auto-save ongoing edits and sync to Hidden Vault when minimizing or closing the app
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                editorViewModel.onAppMinimizedOrClosed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val cycleThemeMode = {
        val next = when (uiState.themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SYSTEM
        }
        editorViewModel.setThemeMode(next)
    }

    val effectiveTheme = uiState.activeDocument?.let { doc ->
        val targetId = uiState.focusedSectionId ?: doc.sections.firstOrNull()?.id
        doc.sections.find { it.id == targetId }?.themeOverride
    } ?: uiState.activeAppTheme

    KtScribeTheme(
        themeMode = uiState.themeMode,
        activeTheme = effectiveTheme
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val activeDoc = uiState.activeDocument
            AnimatedContent(
                targetState = activeDoc != null,
                transitionSpec = {
                    fadeIn(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) togetherWith fadeOut(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                },
                label = "CodeAssistNavigationTransition"
            ) { isEditorOpen ->
                val currentDoc = uiState.activeDocument
                if (!isEditorOpen || currentDoc == null) {
                    DocumentListScreen(
                        documents = documents,
                        searchQuery = uiState.searchQuery,
                        shortcutLetter = uiState.shortcutLetter,
                        themeMode = uiState.themeMode,
                        toolchainPlugins = uiState.toolchainPlugins,
                        onSearchQueryChange = editorViewModel::updateSearchQuery,
                        onOpenDocument = editorViewModel::openDocument,
                        onCreateDocument = { editorViewModel.createAndOpenNewDocument() },
                        onCreateDocumentWithTemplate = { title, lang ->
                            editorViewModel.createAndOpenNewDocument(title, lang)
                        },
                        onDeleteDocument = editorViewModel::deleteDocument,
                        onRenameDocument = editorViewModel::renameFolderById,
                        onCycleThemeMode = cycleThemeMode,
                        onOpenSettings = { showSettingsDialog = true },
                        onOpenVaultModal = editorViewModel::openVaultRecoveryDialog,
                        onInstallAllSdks = editorViewModel::installAllLanguageSdks
                    )
                } else {
                    DocumentEditorScreen(
                        uiState = uiState,
                        document = currentDoc,
                        allFolders = documents,
                        onCloseDocument = editorViewModel::closeActiveDocument,
                        onSelectFolder = editorViewModel::openDocument,
                        onCreateNewFolder = { title -> editorViewModel.createAndOpenNewDocument(title) },
                        onSaveNow = editorViewModel::saveNowManual,
                        onTitleChange = editorViewModel::updateDocumentTitle,
                        onIntroTextChange = editorViewModel::updateIntroText,
                        onInsertCodeBlock = editorViewModel::insertCodeBlockAtActivePosition,
                        onUpdateSectionCode = editorViewModel::updateSectionCode,
                        onUpdateSectionExplanation = editorViewModel::updateSectionExplanation,
                        onUpdateSectionTitle = editorViewModel::updateSectionTitleManual,
                        onUpdateSectionConceptTag = editorViewModel::updateSectionConceptTagManual,
                        onJumpToSectionConcept = editorViewModel::jumpToSectionConcept,
                        onExportMarkdown = editorViewModel::exportActiveDocumentAsMarkdown,
                        onFormatSectionCode = editorViewModel::formatCodeInSection,
                        onRequestAiNaming = editorViewModel::requestAiTitleForSection,
                        onRequestAiExplain = editorViewModel::requestAiExplainForSection,
                        onRequestAiRefactor = editorViewModel::requestAiRefactorForSection,
                        onRequestAiKDoc = editorViewModel::requestAiKDocForSection,
                        onToggleSectionDivider = editorViewModel::toggleSectionDivider,
                        onDeleteSection = editorViewModel::deleteSection,
                        onSetActiveTarget = editorViewModel::setActiveTarget,
                        onExitFocusMode = editorViewModel::exitFocusedSingleSectionMode,
                        onToggleFocusSection = editorViewModel::toggleFocusedSingleSectionMode,
                        onNavigateFocusedSection = editorViewModel::navigateFocusedSection,
                        onClearNewSectionFocusFlag = editorViewModel::clearNewlyInsertedSectionFocusFlag,
                        onFontChoiceChange = editorViewModel::setFontChoice,
                        onFontSizeChange = editorViewModel::setFontSizeSp,
                        onToggleBold = editorViewModel::toggleBold,
                        onToggleItalic = editorViewModel::toggleItalic,
                        onToggleUnderline = editorViewModel::toggleUnderline,
                        onToggleStrikethrough = editorViewModel::toggleStrikethrough,
                        onAlignmentChange = editorViewModel::setTextAlignment,
                        onUndo = editorViewModel::undo,
                        onRedo = editorViewModel::redo,
                        onCycleThemeMode = cycleThemeMode,
                        onOpenSettings = { showSettingsDialog = true },
                        onDismissBanner = editorViewModel::dismissBanners,
                        onToggleIslandDropdown = editorViewModel::toggleIslandDropdown,
                        onCloseIslandDropdown = editorViewModel::closeIslandDropdown,
                        onCycleExplanationWindowMode = editorViewModel::cycleExplanationWindowMode,
                        onSetExplanationWindowMode = editorViewModel::setExplanationWindowMode,
                        onUpdateSplitRatio = editorViewModel::updateSplitScreenRatio,
                        onDockWindowToSplitScreen = editorViewModel::dockWindowToSplitScreen,
                        onUndockSplitWindowToFloating = editorViewModel::undockSplitWindowToFloating,
                        onCloseSplitScreenWindow = editorViewModel::closeSplitScreenWindow,
                        onToggleSplitScreenOrientation = editorViewModel::toggleSplitScreenOrientation,
                        onRunActiveSectionInTerminal = editorViewModel::runActiveSectionInTerminal,
                        onOpenVaultModal = editorViewModel::openVaultRecoveryDialog,
                        onToggleFloatingWindow = editorViewModel::toggleFloatingWindow,
                        onCloseFloatingWindow = editorViewModel::closeFloatingWindow,
                        onFocusFloatingWindow = editorViewModel::focusFloatingWindow,
                        onUpdateSectionLanguage = editorViewModel::updateSectionLanguage,
                        onUpdateSectionThemeOverride = editorViewModel::updateSectionThemeOverride,
                        onInsertLanguageCodeBlock = editorViewModel::insertCodeBlockAtActivePosition,
                        onToggleBottomDrawer = editorViewModel::toggleBottomDrawer,
                        onCloseBottomDrawer = editorViewModel::closeBottomDrawer,
                        onSetBottomDrawerTab = editorViewModel::setBottomDrawerTab,
                        onSetBottomDrawerHeightFraction = editorViewModel::setBottomDrawerHeightFraction,
                        onClearTerminal = editorViewModel::clearTerminalOutput,
                        onDismissExtensionRenameWarning = editorViewModel::dismissExtensionRenameWarning,
                        onShowStatusToast = editorViewModel::showStatusToast,
                        onToggleLeftActivityTab = editorViewModel::toggleLeftActivityTab,
                        onCloseEditorTab = editorViewModel::closeEditorTab,
                        onInstallOrTogglePlugin = editorViewModel::installOrTogglePlugin,
                        onInstallAllSdks = editorViewModel::installAllLanguageSdks,
                        onToggleLineBreakpoint = editorViewModel::toggleLineBreakpoint,
                        onAddWatchExpression = editorViewModel::addWatchExpression,
                        onRemoveWatchExpression = editorViewModel::removeWatchExpression,
                        onOptimizeSectionImports = editorViewModel::optimizeImportsInSection,
                        onJumpToCodeLine = editorViewModel::jumpToCodeLine,
                        onUpdateTerminalStdin = editorViewModel::updateTerminalStdin,
                        onCreateLocalSnapshot = { secId -> editorViewModel.createLocalSnapshot(secId) },
                        onRestoreLocalSnapshot = editorViewModel::restoreLocalSnapshot,
                        onCompileActiveSection = editorViewModel::compileActiveSection,
                        onDebugResume = editorViewModel::startOrResumeDebugSession,
                        onDebugStepOver = editorViewModel::debugStepOver,
                        onDebugReset = editorViewModel::debugReset,
                        onEvaluateRepl = editorViewModel::evaluateRepl,
                        onClearRepl = editorViewModel::clearRepl,
                        onReorderEditorTab = editorViewModel::reorderEditorTab
                    )
                }
            }

            // First-Launch Storage Permission & Hidden Vault ("xyz") Recovery Dialog
            val vaultInfo = uiState.vaultInspection
            if (uiState.showVaultRecoveryDialog && vaultInfo != null) {
                FirstLaunchVaultRecoveryDialog(
                    vaultInspection = vaultInfo,
                    onRecoverPreviousData = editorViewModel::recoverPreviousVaultData,
                    onCreateNewVaultFolder = editorViewModel::createNewCleanVaultFolder,
                    onDismiss = editorViewModel::dismissVaultRecoveryDialog
                )
            }

            if (showSettingsDialog) {
                ShortcutAndSettingsDialog(
                    currentShortcutLetter = uiState.shortcutLetter,
                    currentThemeMode = uiState.themeMode,
                    currentAppTheme = uiState.activeAppTheme,
                    remainingAiQuota = uiState.remainingAiRpmQuota,
                    defaultLanguage = uiState.defaultLanguageForNewFiles,
                    toolchainPlugins = uiState.toolchainPlugins,
                    onSelectDefaultLanguage = editorViewModel::setDefaultLanguageForNewFiles,
                    onSelectShortcutLetter = editorViewModel::setShortcutLetter,
                    onSelectThemeMode = editorViewModel::setThemeMode,
                    onSelectAppTheme = editorViewModel::setActiveAppTheme,
                    onInstallOrTogglePlugin = editorViewModel::installOrTogglePlugin,
                    onInstallAllSdks = editorViewModel::installAllLanguageSdks,
                    onDismiss = { showSettingsDialog = false }
                )
            }
        }
    }
}
