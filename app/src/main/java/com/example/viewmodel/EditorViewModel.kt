package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AiAssistOutput
import com.example.ai.AiSectionNamingService
import com.example.ai.SectionNamingResult
import com.example.code.KotlinCodeEngine
import com.example.data.local.EditorDatabase
import com.example.data.local.EditorRepository
import com.example.data.local.VaultInspectionResult
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
import com.example.model.TextFormattingStyle
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class EditorUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val activeDocument: DocumentDetailUiModel? = null,
    val activeTarget: ActiveEditorTarget = ActiveEditorTarget.Intro,
    // Full-screen Apple cmd code editor focuses on the active .ktscribe file by default
    val isFocusedSingleSectionMode: Boolean = true,
    val focusedSectionId: Long? = null,
    val newlyInsertedSectionId: Long? = null,
    // Dual Floating Windows (max 2 concurrent excluding Command Block, ordered back-to-front by zIndex)
    val openFloatingWindows: List<FloatingWindowType> = emptyList(),
    val windowLimitShakeCount: Int = 0,
    // Adjustable Split-Screen Window State (any floating window can convert to Side-by-Side or Bottom Split)
    val dockedSplitWindow: FloatingWindowType? = null,
    val splitScreenOrientation: SplitScreenOrientation = SplitScreenOrientation.SIDE_BY_SIDE,
    // Command Block / Split Window state
    val activeIslandDropdown: IslandDropdownMenu = IslandDropdownMenu.NONE,
    val explanationWindowMode: ExplanationWindowMode = ExplanationWindowMode.HIDDEN,
    val splitScreenRatio: Float = 0.54f,
    val terminalExecutionOutput: String? = null,
    // First-Launch Storage Permission & Hidden Vault ("xyz") Recovery Gate
    val showVaultRecoveryDialog: Boolean = false,
    val vaultInspection: VaultInspectionResult? = null,
    val hasUnsavedChanges: Boolean = false,
    val secondsUntilAutoSave: Int = AUTO_SAVE_INTERVAL_SECONDS,
    val lastSavedTimestamp: Long? = null,
    val saveStatusBanner: String = "Synced to Hidden Vault",
    val aiStatusMessage: String? = null,
    val aiErrorBanner: String? = null,
    val remainingAiRpmQuota: Int = AiSectionNamingService.FREE_TIER_SAFE_RPM_LIMIT,
    val shortcutLetter: Char = 'K',
    val themeMode: ThemeMode = ThemeMode.DARK,
    val activeAppTheme: com.example.ui.theme.AppThemeId = com.example.ui.theme.AppThemeId.DARCULA_ESPRESSO,
    val defaultLanguageForNewFiles: com.example.model.LanguageType = com.example.model.LanguageType.KOTLIN,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    // Sliding IDE Bottom Command & Debug Drawer (matching VS Code / IntelliJ / CodeAssist layout)
    val isBottomDrawerOpen: Boolean = false,
    val activeBottomDrawerTab: com.example.model.BottomDrawerTab = com.example.model.BottomDrawerTab.TERMINAL,
    val bottomDrawerHeightFraction: Float = 0.42f,
    // File Extension Rename Guard Modal
    val extensionRenameWarningDialog: String? = null,
    // CodeAssist Left Activity Bar & Collapsible File Tree State
    val activeLeftActivityTab: com.example.model.LeftActivityTab = com.example.model.LeftActivityTab.FILES,
    // Closed Editor Tab IDs (allows closing tabs with 'x' to reveal the CodeAssist 'No file open' screen)
    val closedTabSectionIds: Set<Long> = emptySet(),
    // SDK & Plugin Toolchain Manager Catalog
    val toolchainPlugins: List<com.example.model.ToolchainPluginItem> = com.example.model.ToolchainPluginItem.defaultCatalog(),
    // CodeAssist Gutter Breakpoints per File Section (`sectionId -> Set<lineNumber>`)
    val breakpointsBySectionId: Map<Long, Set<Int>> = emptyMap(),
    // CodeAssist Debugger Expression Watch List
    val watchExpressions: List<String> = emptyList(),
    // Editor Line Jump Target (triggered from Problems tab, Debugger step, or Structure outline)
    val requestedJumpLine: Int? = null,
    val jumpLineTrigger: Int = 0,
    // Interactive Standard Input (stdin) for readln() / input() / Scanner / std::cin
    val terminalStdinInput: String = "",
    // Local On-Device Version History Snapshots (`sectionId -> List<LocalCodeSnapshot>`)
    val localSnapshotsBySectionId: Map<Long, List<com.example.model.LocalCodeSnapshot>> = emptyMap()
) {
    fun isLanguageSdkInstalled(language: com.example.model.LanguageType): Boolean {
        val sdkId = com.example.model.ToolchainPluginItem.sdkIdForLanguage(language)
        return toolchainPlugins.find { it.id == sdkId }?.isInstalled == true
    }
    val activeFormattingStyle: TextFormattingStyle
        get() {
            val doc = activeDocument ?: return TextFormattingStyle()
            return when (val target = activeTarget) {
                is ActiveEditorTarget.Intro -> doc.introStyle
                is ActiveEditorTarget.SectionExplanation -> {
                    doc.sections.find { it.id == target.sectionId }?.explanationStyle
                        ?: doc.introStyle
                }
                is ActiveEditorTarget.SectionCode -> {
                    doc.sections.find { it.id == target.sectionId }?.explanationStyle
                        ?: doc.introStyle
                }
            }
        }

    companion object {
        const val AUTO_SAVE_INTERVAL_SECONDS = 30
        const val MAX_CONCURRENT_FLOATING_WINDOWS = 2

        /**
         * Pure state transition for opening/toggling one of the 4 floating windows:
         * - If currently docked in split-screen, closes split-screen.
         * - If already open as a floating window, closes it.
         * - If fewer than 2 windows are open, opens it at the top of the z-order.
         * - If 2 windows are already open, keeps existing 2 windows and increments windowLimitShakeCount.
         */
        fun applyToggleFloatingWindow(
            current: EditorUiState,
            type: FloatingWindowType
        ): EditorUiState {
            if (current.dockedSplitWindow == type) {
                return current.copy(
                    dockedSplitWindow = null,
                    explanationWindowMode = ExplanationWindowMode.HIDDEN
                )
            }
            return if (type in current.openFloatingWindows) {
                val updated = current.openFloatingWindows - type
                current.copy(
                    openFloatingWindows = updated,
                    explanationWindowMode = if (type == FloatingWindowType.EXPLANATION) {
                        ExplanationWindowMode.HIDDEN
                    } else {
                        current.explanationWindowMode
                    }
                )
            } else if (current.openFloatingWindows.size < MAX_CONCURRENT_FLOATING_WINDOWS) {
                val updated = current.openFloatingWindows + type
                current.copy(
                    openFloatingWindows = updated,
                    explanationWindowMode = if (type == FloatingWindowType.EXPLANATION) {
                        ExplanationWindowMode.FLOATING_WINDOW
                    } else {
                        current.explanationWindowMode
                    }
                )
            } else {
                current.copy(
                    windowLimitShakeCount = current.windowLimitShakeCount + 1,
                    aiStatusMessage = "Max 2 floating windows open — cancel (✕) one to open ${type.pillLabel}"
                )
            }
        }

        /**
         * Converts a floating window into an adjustable Split-Screen pane alongside (or below) the `.ktscribe` code block.
         */
        fun applyDockWindowToSplitScreen(
            current: EditorUiState,
            type: FloatingWindowType
        ): EditorUiState {
            return current.copy(
                openFloatingWindows = current.openFloatingWindows - type,
                dockedSplitWindow = type,
                explanationWindowMode = if (type == FloatingWindowType.EXPLANATION) {
                    ExplanationWindowMode.SPLIT_SCREEN
                } else {
                    ExplanationWindowMode.HIDDEN
                },
                aiStatusMessage = "Docked ${type.pillLabel} into ${current.splitScreenOrientation.label}"
            )
        }

        fun applyToggleSplitOrientation(current: EditorUiState): EditorUiState {
            val next = if (current.splitScreenOrientation == SplitScreenOrientation.SIDE_BY_SIDE) {
                SplitScreenOrientation.BOTTOM_SPLIT
            } else {
                SplitScreenOrientation.SIDE_BY_SIDE
            }
            return current.copy(
                splitScreenOrientation = next,
                aiStatusMessage = "Switched to ${next.label}"
            )
        }
    }
}

class EditorViewModel(
    private val repository: EditorRepository,
    private val aiNamingService: AiSectionNamingService = AiSectionNamingService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditorUiState(
            shortcutLetter = repository.getShortcutLetter(),
            themeMode = repository.getThemeMode(),
            activeAppTheme = repository.getActiveAppTheme(),
            toolchainPlugins = com.example.model.ToolchainPluginItem.defaultCatalog(
                repository.getInstalledPluginIds()
            )
        )
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    val filteredDocuments: StateFlow<List<DocumentSummaryUiModel>> = combine(
        repository.allDocumentSummaries,
        _searchQuery
    ) { docs, query ->
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            docs
        } else {
            docs.filter { doc ->
                doc.title.contains(trimmed, ignoreCase = true) ||
                    doc.previewSnippet.contains(trimmed, ignoreCase = true) ||
                    doc.conceptTags.any { it.contains(trimmed, ignoreCase = true) }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val undoStack = ArrayDeque<DocumentDetailUiModel>()
    private val redoStack = ArrayDeque<DocumentDetailUiModel>()
    private var autoSaveTickerJob: Job? = null
    private val debouncedAiNamingJobs = mutableMapOf<Long, Job>()

    init {
        viewModelScope.launch {
            val vaultInfo = repository.inspectVaultState()
            repository.ensureSeedDocumentsIfNeeded()
            val quota = aiNamingService.getRemainingRpmSlots()
            val summaries = repository.allDocumentSummaries.firstOrNull().orEmpty()
            val initialFolderDetail = summaries.firstOrNull()?.let { repository.loadDocumentDetail(it.id) }
            val firstSec = initialFolderDetail?.sections?.firstOrNull()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    activeDocument = initialFolderDetail,
                    focusedSectionId = firstSec?.id,
                    isFocusedSingleSectionMode = firstSec != null,
                    activeTarget = if (firstSec != null) {
                        ActiveEditorTarget.SectionCode(firstSec.id, firstSec.orderIndex)
                    } else {
                        ActiveEditorTarget.Intro
                    },
                    vaultInspection = vaultInfo,
                    showVaultRecoveryDialog = vaultInfo.isFirstLaunchGatePending,
                    remainingAiRpmQuota = quota
                )
            }
        }
        startThirtySecondAutoSaveTicker()
    }

    private fun startThirtySecondAutoSaveTicker() {
        autoSaveTickerJob?.cancel()
        autoSaveTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000L)
                val current = _uiState.value
                if (current.activeDocument != null && current.hasUnsavedChanges) {
                    val nextSec = current.secondsUntilAutoSave - 1
                    if (nextSec <= 0) {
                        persistActiveDocumentInternal(reason = "Auto-saved to Hidden Vault (30s)")
                    } else {
                        _uiState.update {
                            it.copy(
                                secondsUntilAutoSave = nextSec,
                                saveStatusBanner = "Auto-save in ${nextSec}s"
                            )
                        }
                    }
                }
            }
        }
    }

    fun openVaultRecoveryDialog() {
        val vaultInfo = repository.inspectVaultState()
        _uiState.update {
            it.copy(
                vaultInspection = vaultInfo,
                showVaultRecoveryDialog = true
            )
        }
    }

    fun dismissVaultRecoveryDialog() {
        _uiState.update { it.copy(showVaultRecoveryDialog = false) }
    }

    fun recoverPreviousVaultData() {
        viewModelScope.launch {
            val recoveredDocId = repository.recoverFoldersFromHiddenVault()
            val updatedVaultInfo = repository.inspectVaultState()
            if (recoveredDocId != null) {
                openDocument(recoveredDocId)
            }
            _uiState.update {
                it.copy(
                    showVaultRecoveryDialog = false,
                    vaultInspection = updatedVaultInfo,
                    aiStatusMessage = "✓ Recovered previous folders from hidden vault (${updatedVaultInfo.vaultFolderName})"
                )
            }
        }
    }

    fun createNewCleanVaultFolder() {
        viewModelScope.launch {
            val firstId = repository.initializeFreshHiddenVault()
            val updatedVaultInfo = repository.inspectVaultState()
            if (firstId != null) {
                openDocument(firstId)
            }
            _uiState.update {
                it.copy(
                    showVaultRecoveryDialog = false,
                    vaultInspection = updatedVaultInfo,
                    aiStatusMessage = "✓ Initialized new hidden vault folder (.ktscribe_vault_xyz)"
                )
            }
        }
    }

    fun toggleFloatingWindow(type: FloatingWindowType) {
        if (type == FloatingWindowType.RUN_OUTPUT && type !in _uiState.value.openFloatingWindows) {
            val state = _uiState.value
            val doc = state.activeDocument
            val activeSec = doc?.sections?.find { it.id == state.focusedSectionId }
                ?: doc?.sections?.firstOrNull()
            val output = activeSec?.let {
                com.example.code.CodeLanguageEngine.executeAndDebug(
                    code = it.codeContent,
                    language = it.language,
                    isSdkInstalled = state.isLanguageSdkInstalled(it.language),
                    stdinInput = state.terminalStdinInput
                ).output
            }
            _uiState.update { current ->
                val nextState = EditorUiState.applyToggleFloatingWindow(current, type)
                nextState.copy(terminalExecutionOutput = output ?: current.terminalExecutionOutput)
            }
        } else {
            _uiState.update { current ->
                EditorUiState.applyToggleFloatingWindow(current, type)
            }
        }
    }

    fun closeFloatingWindow(type: FloatingWindowType) {
        _uiState.update { current ->
            current.copy(
                openFloatingWindows = current.openFloatingWindows - type,
                explanationWindowMode = if (type == FloatingWindowType.EXPLANATION &&
                    current.explanationWindowMode == ExplanationWindowMode.FLOATING_WINDOW
                ) {
                    ExplanationWindowMode.HIDDEN
                } else {
                    current.explanationWindowMode
                }
            )
        }
    }

    fun focusFloatingWindow(type: FloatingWindowType) {
        _uiState.update { current ->
            if (type !in current.openFloatingWindows || current.openFloatingWindows.lastOrNull() == type) {
                current
            } else {
                current.copy(openFloatingWindows = (current.openFloatingWindows - type) + type)
            }
        }
    }

    fun toggleIslandDropdown(menu: IslandDropdownMenu) {
        _uiState.update { current ->
            val next = if (current.activeIslandDropdown == menu) IslandDropdownMenu.NONE else menu
            current.copy(activeIslandDropdown = next)
        }
    }

    fun closeIslandDropdown() {
        _uiState.update { it.copy(activeIslandDropdown = IslandDropdownMenu.NONE) }
    }

    fun setExplanationWindowMode(mode: ExplanationWindowMode) {
        _uiState.update { current ->
            val updatedWindows = when (mode) {
                ExplanationWindowMode.FLOATING_WINDOW -> {
                    if (FloatingWindowType.EXPLANATION in current.openFloatingWindows) {
                        current.openFloatingWindows
                    } else if (current.openFloatingWindows.size < EditorUiState.MAX_CONCURRENT_FLOATING_WINDOWS) {
                        current.openFloatingWindows + FloatingWindowType.EXPLANATION
                    } else {
                        current.openFloatingWindows.drop(1) + FloatingWindowType.EXPLANATION
                    }
                }
                ExplanationWindowMode.SPLIT_SCREEN, ExplanationWindowMode.HIDDEN -> {
                    current.openFloatingWindows - FloatingWindowType.EXPLANATION
                }
            }
            val updatedDocked = when (mode) {
                ExplanationWindowMode.SPLIT_SCREEN -> FloatingWindowType.EXPLANATION
                else -> if (current.dockedSplitWindow == FloatingWindowType.EXPLANATION) null else current.dockedSplitWindow
            }
            current.copy(
                explanationWindowMode = mode,
                dockedSplitWindow = updatedDocked,
                openFloatingWindows = updatedWindows,
                activeIslandDropdown = IslandDropdownMenu.NONE
            )
        }
    }

    fun dockWindowToSplitScreen(type: FloatingWindowType) {
        if (type == FloatingWindowType.RUN_OUTPUT) {
            val state = _uiState.value
            val doc = state.activeDocument
            val activeSec = doc?.sections?.find { it.id == state.focusedSectionId }
                ?: doc?.sections?.firstOrNull()
            val output = activeSec?.let {
                com.example.code.CodeLanguageEngine.executeAndDebug(
                    code = it.codeContent,
                    language = it.language,
                    isSdkInstalled = state.isLanguageSdkInstalled(it.language),
                    stdinInput = state.terminalStdinInput
                ).output
            }
            _uiState.update { current ->
                EditorUiState.applyDockWindowToSplitScreen(current, type).copy(
                    terminalExecutionOutput = output ?: current.terminalExecutionOutput
                )
            }
        } else {
            _uiState.update { current ->
                EditorUiState.applyDockWindowToSplitScreen(current, type)
            }
        }
    }

    fun undockSplitWindowToFloating() {
        _uiState.update { current ->
            val docked = current.dockedSplitWindow ?: return@update current
            val cleared = current.copy(
                dockedSplitWindow = null,
                explanationWindowMode = ExplanationWindowMode.HIDDEN
            )
            if (cleared.openFloatingWindows.size < EditorUiState.MAX_CONCURRENT_FLOATING_WINDOWS) {
                EditorUiState.applyToggleFloatingWindow(cleared, docked)
            } else {
                val droppedOldest = cleared.copy(openFloatingWindows = cleared.openFloatingWindows.drop(1))
                EditorUiState.applyToggleFloatingWindow(droppedOldest, docked)
            }
        }
    }

    fun closeSplitScreenWindow() {
        _uiState.update { current ->
            current.copy(
                dockedSplitWindow = null,
                explanationWindowMode = ExplanationWindowMode.HIDDEN
            )
        }
    }

    fun toggleSplitScreenOrientation() {
        _uiState.update { current ->
            EditorUiState.applyToggleSplitOrientation(current)
        }
    }

    fun cycleExplanationWindowMode() {
        val current = _uiState.value
        val next = when {
            FloatingWindowType.EXPLANATION in current.openFloatingWindows -> ExplanationWindowMode.SPLIT_SCREEN
            current.explanationWindowMode == ExplanationWindowMode.SPLIT_SCREEN -> ExplanationWindowMode.HIDDEN
            else -> ExplanationWindowMode.FLOATING_WINDOW
        }
        setExplanationWindowMode(next)
    }

    fun updateSplitScreenRatio(newRatio: Float) {
        _uiState.update {
            it.copy(splitScreenRatio = newRatio.coerceIn(0.25f, 0.75f))
        }
    }

    fun runActiveSectionInTerminal() {
        val state = _uiState.value
        val doc = state.activeDocument ?: return
        val activeSec = doc.sections.find { it.id == state.focusedSectionId }
            ?: doc.sections.firstOrNull()
            ?: return
        val isSdkReady = state.isLanguageSdkInstalled(activeSec.language)
        val debugInfo = com.example.code.CodeLanguageEngine.executeAndDebug(
            code = activeSec.codeContent,
            language = activeSec.language,
            isSdkInstalled = isSdkReady,
            stdinInput = state.terminalStdinInput
        )
        _uiState.update { current ->
            current.copy(
                terminalExecutionOutput = debugInfo.output,
                isBottomDrawerOpen = true,
                activeBottomDrawerTab = com.example.model.BottomDrawerTab.TERMINAL
            )
        }
    }

    fun updateTerminalStdin(input: String) {
        _uiState.update { it.copy(terminalStdinInput = input) }
    }

    fun jumpToCodeLine(line: Int) {
        val targetLine = line.coerceAtLeast(1)
        _uiState.update { state ->
            state.copy(
                requestedJumpLine = targetLine,
                jumpLineTrigger = state.jumpLineTrigger + 1,
                aiStatusMessage = "→ Jumped to Ln $targetLine"
            )
        }
    }

    fun createLocalSnapshot(sectionId: Long, label: String = "Manual Snapshot") {
        val doc = _uiState.value.activeDocument ?: return
        val sec = doc.sections.find { it.id == sectionId } ?: return
        if (sec.codeContent.isBlank()) return
        val now = System.currentTimeMillis()
        val existing = _uiState.value.localSnapshotsBySectionId[sectionId].orEmpty()
        if (existing.firstOrNull()?.codeContent == sec.codeContent) {
            _uiState.update { it.copy(aiStatusMessage = "✓ Snapshot already up to date for ${sec.nativeFileName}") }
            return
        }
        val snapshot = com.example.model.LocalCodeSnapshot(
            id = now,
            sectionId = sectionId,
            label = label,
            codeContent = sec.codeContent,
            timestampMs = now
        )
        val updatedList = (listOf(snapshot) + existing).take(12)
        _uiState.update { state ->
            state.copy(
                localSnapshotsBySectionId = state.localSnapshotsBySectionId + (sectionId to updatedList),
                aiStatusMessage = "⏱ Saved local snapshot '$label' (${snapshot.lineCount} lines)"
            )
        }
    }

    fun restoreLocalSnapshot(sectionId: Long, snapshotId: Long) {
        val snapshots = _uiState.value.localSnapshotsBySectionId[sectionId].orEmpty()
        val target = snapshots.find { it.id == snapshotId } ?: return
        mutateActiveDocument(recordUndo = true) { current ->
            current.copy(
                sections = current.sections.map { sec ->
                    if (sec.id == sectionId) sec.copy(codeContent = target.codeContent) else sec
                }
            )
        }
        _uiState.update {
            it.copy(aiStatusMessage = "↺ Restored snapshot '${target.label}' (${target.lineCount} lines)")
        }
    }

    fun toggleLeftActivityTab(tab: com.example.model.LeftActivityTab) {
        _uiState.update { current ->
            val next = if (current.activeLeftActivityTab == tab) {
                com.example.model.LeftActivityTab.NONE
            } else {
                tab
            }
            current.copy(activeLeftActivityTab = next)
        }
    }

    fun closeEditorTab(sectionId: Long) {
        _uiState.update { current ->
            val doc = current.activeDocument ?: return@update current
            val newClosed = current.closedTabSectionIds + sectionId
            val openSections = doc.sections.filter { it.id !in newClosed }
            val nextFocused = if (current.focusedSectionId == sectionId) {
                openSections.firstOrNull()?.id
            } else {
                current.focusedSectionId
            }
            current.copy(
                closedTabSectionIds = newClosed,
                focusedSectionId = nextFocused
            )
        }
    }

    fun installOrTogglePlugin(pluginId: String) {
        val currentList = _uiState.value.toolchainPlugins
        val target = currentList.find { it.id == pluginId } ?: return
        if (target.isDownloading) return

        if (target.isInstalled) {
            // Uninstall / disable plugin
            val updated = currentList.map {
                if (it.id == pluginId) it.copy(isInstalled = false, isDownloading = false, downloadProgress = 0f) else it
            }
            val installedIds = updated.filter { it.isInstalled }.map { it.id }.toSet()
            repository.setInstalledPluginIds(installedIds)
            _uiState.update {
                it.copy(
                    toolchainPlugins = updated,
                    aiStatusMessage = "Removed ${target.name}"
                )
            }
        } else {
            // Download and install with animated progress
            viewModelScope.launch {
                _uiState.update { state ->
                    state.copy(
                        toolchainPlugins = state.toolchainPlugins.map {
                            if (it.id == pluginId) it.copy(isDownloading = true, downloadProgress = 0.25f) else it
                        },
                        aiStatusMessage = "⬇ Downloading ${target.name} (${target.sizeLabel})…"
                    )
                }
                delay(180)
                _uiState.update { state ->
                    state.copy(
                        toolchainPlugins = state.toolchainPlugins.map {
                            if (it.id == pluginId) it.copy(isDownloading = true, downloadProgress = 0.72f) else it
                        }
                    )
                }
                delay(180)
                val updated = _uiState.value.toolchainPlugins.map {
                    if (it.id == pluginId) {
                        it.copy(isInstalled = true, isDownloading = false, downloadProgress = 1f)
                    } else {
                        it
                    }
                }
                val installedIds = updated.filter { it.isInstalled }.map { it.id }.toSet()
                repository.setInstalledPluginIds(installedIds)
                _uiState.update {
                    it.copy(
                        toolchainPlugins = updated,
                        aiStatusMessage = "✓ Installed ${target.name} (${target.version}) — Debugger & Compiler Ready"
                    )
                }
            }
        }
    }

    fun installAllLanguageSdks() {
        viewModelScope.launch {
            val allUpdated = _uiState.value.toolchainPlugins.map {
                it.copy(isInstalled = true, isDownloading = false, downloadProgress = 1f)
            }
            val installedIds = allUpdated.map { it.id }.toSet()
            repository.setInstalledPluginIds(installedIds)
            _uiState.update {
                it.copy(
                    toolchainPlugins = allUpdated,
                    aiStatusMessage = "✓ All 6 Language SDKs & IDE Debugger Plugins installed"
                )
            }
        }
    }

    fun toggleLineBreakpoint(sectionId: Long, line: Int) {
        if (line < 1) return
        _uiState.update { state ->
            val currentSet = state.breakpointsBySectionId[sectionId] ?: emptySet()
            val updatedSet = if (line in currentSet) currentSet - line else (currentSet + line).sorted().toSet()
            val msg = if (line in updatedSet) {
                "● Breakpoint set at Ln $line"
            } else {
                "○ Breakpoint cleared at Ln $line"
            }
            state.copy(
                breakpointsBySectionId = state.breakpointsBySectionId + (sectionId to updatedSet),
                aiStatusMessage = msg
            )
        }
    }

    fun addWatchExpression(expression: String) {
        val clean = expression.trim()
        if (clean.isBlank()) return
        _uiState.update { state ->
            if (clean in state.watchExpressions) state
            else state.copy(watchExpressions = state.watchExpressions + clean)
        }
    }

    fun removeWatchExpression(expression: String) {
        _uiState.update { state ->
            state.copy(watchExpressions = state.watchExpressions - expression)
        }
    }

    fun optimizeImportsInSection(sectionId: Long) {
        val doc = _uiState.value.activeDocument ?: return
        val targetSection = doc.sections.find { it.id == sectionId } ?: return
        val optimized = com.example.code.CodeLanguageEngine.optimizeImportsForLanguage(
            code = targetSection.codeContent,
            language = targetSection.language
        )
        mutateActiveDocument(recordUndo = true) { current ->
            current.copy(
                sections = current.sections.map { sec ->
                    if (sec.id == sectionId) sec.copy(codeContent = optimized) else sec
                }
            )
        }
        _uiState.update {
            it.copy(aiStatusMessage = "✓ Optimized & sorted imports in ${targetSection.nativeFileName}")
        }
    }

    fun openBottomDrawer(tab: com.example.model.BottomDrawerTab = com.example.model.BottomDrawerTab.TERMINAL) {
        _uiState.update { it.copy(isBottomDrawerOpen = true, activeBottomDrawerTab = tab) }
    }

    fun closeBottomDrawer() {
        _uiState.update { it.copy(isBottomDrawerOpen = false) }
    }

    fun toggleBottomDrawer(tab: com.example.model.BottomDrawerTab? = null) {
        _uiState.update { current ->
            if (current.isBottomDrawerOpen && (tab == null || tab == current.activeBottomDrawerTab)) {
                current.copy(isBottomDrawerOpen = false)
            } else {
                current.copy(isBottomDrawerOpen = true, activeBottomDrawerTab = tab ?: current.activeBottomDrawerTab)
            }
        }
    }

    fun setBottomDrawerTab(tab: com.example.model.BottomDrawerTab) {
        _uiState.update { it.copy(activeBottomDrawerTab = tab, isBottomDrawerOpen = true) }
    }

    fun setBottomDrawerHeightFraction(fraction: Float) {
        _uiState.update { it.copy(bottomDrawerHeightFraction = fraction.coerceIn(0.25f, 0.85f)) }
    }

    fun dismissExtensionRenameWarning() {
        _uiState.update { it.copy(extensionRenameWarningDialog = null) }
    }

    fun clearTerminalOutput() {
        _uiState.update { it.copy(terminalExecutionOutput = "") }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setShortcutLetter(letter: Char) {
        val clean = letter.uppercaseChar().takeIf { it in 'A'..'Z' } ?: 'K'
        repository.setShortcutLetter(clean)
        _uiState.update {
            it.copy(
                shortcutLetter = clean,
                aiStatusMessage = "Code block shortcut set to Ctrl+$clean"
            )
        }
    }

    fun openDocument(documentId: Long) {
        viewModelScope.launch {
            if (_uiState.value.activeDocument != null && _uiState.value.hasUnsavedChanges) {
                persistActiveDocumentInternal("Auto-saved before switching folder")
            }
            val detail = repository.loadDocumentDetail(documentId) ?: return@launch
            undoStack.clear()
            redoStack.clear()
            val firstSec = detail.sections.firstOrNull()
            val initialTarget = if (firstSec != null) {
                ActiveEditorTarget.SectionCode(firstSec.id, firstSec.orderIndex)
            } else {
                ActiveEditorTarget.Intro
            }
            _uiState.update {
                it.copy(
                    activeDocument = detail,
                    activeTarget = initialTarget,
                    isFocusedSingleSectionMode = firstSec != null,
                    focusedSectionId = firstSec?.id,
                    closedTabSectionIds = emptySet(),
                    newlyInsertedSectionId = null,
                    activeIslandDropdown = IslandDropdownMenu.NONE,
                    hasUnsavedChanges = false,
                    secondsUntilAutoSave = EditorUiState.AUTO_SAVE_INTERVAL_SECONDS,
                    lastSavedTimestamp = detail.updatedAt,
                    saveStatusBanner = "Synced to Vault",
                    aiErrorBanner = null,
                    canUndo = false,
                    canRedo = false
                )
            }
        }
    }

    fun createAndOpenNewDocument(
        customTitle: String? = null,
        starterLanguage: com.example.model.LanguageType? = null
    ) {
        viewModelScope.launch {
            if (_uiState.value.activeDocument != null && _uiState.value.hasUnsavedChanges) {
                persistActiveDocumentInternal("Auto-saved before creating new folder")
            }
            val targetLang = starterLanguage ?: _uiState.value.defaultLanguageForNewFiles
            val defaultName = customTitle?.takeIf { it.isNotBlank() }
                ?: "New ${targetLang.displayName} Project"
            val newDocId = repository.createNewDocument(
                title = defaultName,
                withStarterSection = true
            )
            if (starterLanguage != null) {
                val createdDetail = repository.loadDocumentDetail(newDocId)
                if (createdDetail != null && createdDetail.sections.isNotEmpty()) {
                    val updatedSections = createdDetail.sections.mapIndexed { idx, sec ->
                        if (idx == 0) {
                            sec.copy(
                                language = starterLanguage,
                                sectionTitle = starterLanguage.defaultFileName,
                                codeContent = starterLanguage.starterCode
                            )
                        } else sec
                    }
                    repository.saveDocumentDetail(createdDetail.copy(sections = updatedSections))
                }
            }
            openDocument(newDocId)
        }
    }

    fun deleteDocument(documentId: Long) {
        viewModelScope.launch {
            if (_uiState.value.activeDocument?.id == documentId) {
                _uiState.update { it.copy(activeDocument = null, hasUnsavedChanges = false) }
            }
            repository.deleteDocument(documentId)
        }
    }

    fun closeActiveDocument() {
        viewModelScope.launch {
            debouncedAiNamingJobs.values.forEach { it.cancel() }
            debouncedAiNamingJobs.clear()
            if (_uiState.value.activeDocument != null && _uiState.value.hasUnsavedChanges) {
                persistActiveDocumentInternal("Auto-saved on leaving folder")
            }
            undoStack.clear()
            redoStack.clear()
            _uiState.update {
                it.copy(
                    activeDocument = null,
                    isFocusedSingleSectionMode = false,
                    focusedSectionId = null,
                    activeIslandDropdown = IslandDropdownMenu.NONE,
                    hasUnsavedChanges = false,
                    newlyInsertedSectionId = null,
                    aiErrorBanner = null,
                    aiStatusMessage = null
                )
            }
        }
    }

    fun onAppMinimizedOrClosed() {
        viewModelScope.launch {
            if (_uiState.value.activeDocument != null && _uiState.value.hasUnsavedChanges) {
                persistActiveDocumentInternal("Auto-saved to Hidden Vault")
            }
        }
    }

    fun saveNowManual() {
        val activeSecId = _uiState.value.focusedSectionId ?: _uiState.value.activeDocument?.sections?.firstOrNull()?.id
        if (activeSecId != null) {
            createLocalSnapshot(activeSecId, "Manual Save")
        }
        viewModelScope.launch {
            persistActiveDocumentInternal("Saved to Hidden Vault")
        }
    }

    fun setActiveTarget(target: ActiveEditorTarget) {
        when (target) {
            is ActiveEditorTarget.Intro -> {
                _uiState.update {
                    it.copy(
                        activeTarget = target,
                        isFocusedSingleSectionMode = false,
                        focusedSectionId = null
                    )
                }
            }
            is ActiveEditorTarget.SectionCode -> {
                _uiState.update {
                    it.copy(
                        activeTarget = target,
                        isFocusedSingleSectionMode = true,
                        focusedSectionId = target.sectionId
                    )
                }
            }
            is ActiveEditorTarget.SectionExplanation -> {
                _uiState.update {
                    it.copy(
                        activeTarget = target,
                        isFocusedSingleSectionMode = true,
                        focusedSectionId = target.sectionId
                    )
                }
            }
        }
    }

    fun exitFocusedSingleSectionMode() {
        _uiState.update {
            it.copy(
                isFocusedSingleSectionMode = false,
                focusedSectionId = null
            )
        }
    }

    fun toggleFocusedSingleSectionMode(sectionId: Long) {
        _uiState.update { current ->
            if (current.isFocusedSingleSectionMode && current.focusedSectionId == sectionId) {
                current.copy(isFocusedSingleSectionMode = false, focusedSectionId = null)
            } else {
                val idx = current.activeDocument?.sections?.indexOfFirst { it.id == sectionId } ?: 0
                current.copy(
                    isFocusedSingleSectionMode = true,
                    focusedSectionId = sectionId,
                    activeTarget = ActiveEditorTarget.SectionCode(sectionId, idx.coerceAtLeast(0))
                )
            }
        }
    }

    fun navigateFocusedSection(offset: Int) {
        val doc = _uiState.value.activeDocument ?: return
        if (doc.sections.isEmpty()) return
        val currentId = _uiState.value.focusedSectionId ?: doc.sections.first().id
        val currentIdx = doc.sections.indexOfFirst { it.id == currentId }.takeIf { it >= 0 } ?: 0
        val nextIdx = (currentIdx + offset).coerceIn(0, doc.sections.lastIndex)
        val nextSec = doc.sections[nextIdx]
        _uiState.update {
            it.copy(
                focusedSectionId = nextSec.id,
                isFocusedSingleSectionMode = true,
                activeTarget = ActiveEditorTarget.SectionCode(nextSec.id, nextSec.orderIndex)
            )
        }
    }

    fun clearNewlyInsertedSectionFocusFlag() {
        _uiState.update { it.copy(newlyInsertedSectionId = null) }
    }

    fun dismissBanners() {
        _uiState.update { it.copy(aiStatusMessage = null, aiErrorBanner = null) }
    }

    fun updateDocumentTitle(newTitle: String) {
        mutateActiveDocument(recordUndo = false) { doc ->
            doc.copy(title = newTitle)
        }
    }

    fun updateIntroText(newText: String) {
        mutateActiveDocument(recordUndo = true) { doc ->
            doc.copy(introText = newText)
        }
    }

    fun updateSectionCode(sectionId: Long, newCode: String) {
        mutateActiveDocument(recordUndo = true) { doc ->
            doc.copy(
                sections = doc.sections.map { sec ->
                    if (sec.id == sectionId) sec.copy(codeContent = newCode) else sec
                }
            )
        }
        scheduleDebouncedAiNamingIfComplete(sectionId)
    }

    fun updateSectionExplanation(sectionId: Long, newExplanation: String) {
        mutateActiveDocument(recordUndo = true) { doc ->
            doc.copy(
                sections = doc.sections.map { sec ->
                    if (sec.id == sectionId) sec.copy(explanationText = newExplanation) else sec
                }
            )
        }
        scheduleDebouncedAiNamingIfComplete(sectionId)
    }

    private fun scheduleDebouncedAiNamingIfComplete(sectionId: Long) {
        debouncedAiNamingJobs[sectionId]?.cancel()
        debouncedAiNamingJobs[sectionId] = viewModelScope.launch {
            delay(4_000L)
            val sec = _uiState.value.activeDocument?.sections?.find { it.id == sectionId } ?: return@launch
            if (sec.codeContent.isNotBlank() && sec.explanationText.length >= 12) {
                val hash = aiNamingService.computeContentHash(sec.codeContent, sec.explanationText)
                if (hash != sec.lastNamedHash) {
                    triggerAiNamingForSectionInternal(sectionId, forceOverwriteTitle = true)
                }
            }
        }
    }

    fun updateSectionTitleManual(sectionId: Long, newTitle: String) {
        val currentDoc = _uiState.value.activeDocument ?: return
        val targetSec = currentDoc.sections.find { it.id == sectionId } ?: return

        val warning = com.example.model.CodeSectionUiModel.validateFileExtensionRename(
            currentFileName = targetSec.nativeFileName,
            newFileName = newTitle,
            language = targetSec.language
        )
        if (warning != null) {
            _uiState.update { it.copy(extensionRenameWarningDialog = warning) }
            return
        }

        mutateActiveDocument(recordUndo = true) { doc ->
            doc.copy(
                sections = doc.sections.map { sec ->
                    if (sec.id == sectionId) {
                        sec.copy(
                            sectionTitle = newTitle,
                            isAiNamed = false
                        )
                    } else {
                        sec
                    }
                }
            )
        }
    }

    fun showStatusToast(message: String) {
        _uiState.update { it.copy(aiStatusMessage = message, aiErrorBanner = null) }
    }

    fun updateSectionLanguage(sectionId: Long, language: com.example.model.LanguageType) {
        mutateActiveDocument(recordUndo = true) { doc ->
            doc.copy(
                sections = doc.sections.map { sec ->
                    if (sec.id == sectionId) {
                        val rawStem = sec.nativeFileName
                            .substringBeforeLast(".")
                            .trim()
                            .ifBlank { "Main" }
                        val newTitle = "$rawStem${language.nativeExtension}"
                        sec.copy(
                            language = language,
                            sectionTitle = newTitle,
                            codeContent = if (sec.codeContent.isBlank()) language.starterCode else sec.codeContent
                        )
                    } else {
                        sec
                    }
                }
            )
        }
        _uiState.update {
            it.copy(
                aiStatusMessage = "✓ Switched file type to ${language.displayName} (${language.nativeExtension}) — code preserved",
                aiErrorBanner = null
            )
        }
    }

    fun updateSectionThemeOverride(sectionId: Long, theme: com.example.ui.theme.AppThemeId?) {
        mutateActiveDocument(recordUndo = true) { doc ->
            doc.copy(
                sections = doc.sections.map { sec ->
                    if (sec.id == sectionId) {
                        sec.copy(themeOverride = theme)
                    } else {
                        sec
                    }
                }
            )
        }
    }

    fun renameFolderById(documentId: Long, newTitle: String) {
        viewModelScope.launch {
            val trimmed = newTitle.trim().ifBlank { "Untitled Folder" }
            if (_uiState.value.activeDocument?.id == documentId) {
                updateDocumentTitle(trimmed)
                persistActiveDocumentInternal("Renamed active folder")
            } else {
                val doc = repository.loadDocumentDetail(documentId)
                if (doc != null) {
                    repository.saveDocumentDetail(doc.copy(title = trimmed))
                }
            }
        }
    }

    fun setActiveAppTheme(theme: com.example.ui.theme.AppThemeId) {
        repository.setActiveAppTheme(theme)
        _uiState.update { it.copy(activeAppTheme = theme) }
    }

    fun setDefaultLanguageForNewFiles(lang: com.example.model.LanguageType) {
        _uiState.update { it.copy(defaultLanguageForNewFiles = lang) }
    }

    fun updateSectionConceptTagManual(sectionId: Long, newConceptTag: String) {
        mutateActiveDocument(recordUndo = true) { doc ->
            doc.copy(
                sections = doc.sections.map { sec ->
                    if (sec.id == sectionId) {
                        sec.copy(
                            explanationStyle = sec.explanationStyle.copy(
                                learnedConceptTag = newConceptTag
                            )
                        )
                    } else {
                        sec
                    }
                }
            )
        }
    }

    fun jumpToSectionConcept(sectionId: Long) {
        val doc = _uiState.value.activeDocument ?: return
        val targetSec = doc.sections.find { it.id == sectionId } ?: return
        _uiState.update { state ->
            state.copy(
                activeTarget = ActiveEditorTarget.SectionCode(targetSec.id, targetSec.orderIndex),
                isFocusedSingleSectionMode = true,
                focusedSectionId = targetSec.id,
                closedTabSectionIds = state.closedTabSectionIds - targetSec.id,
                activeIslandDropdown = IslandDropdownMenu.NONE,
                aiStatusMessage = "Switched to §${targetSec.orderIndex + 1} [${targetSec.effectiveLearnedConceptTag}]"
            )
        }
    }

    fun exportActiveDocumentAsMarkdown(): String {
        val doc = _uiState.value.activeDocument ?: return ""
        return buildString {
            appendLine("# 📁 ${doc.title.ifBlank { "Untitled Project Folder" }}")
            if (doc.introText.isNotBlank()) {
                appendLine()
                appendLine(doc.introText.trim())
            }
            doc.sections.forEach { sec ->
                appendLine()
                appendLine("## §${sec.orderIndex + 1}. ${sec.displayTitle} (${sec.language.displayName})")
                appendLine("**Learned Concept Tag:** `${sec.effectiveLearnedConceptTag}`")
                appendLine()
                appendLine("```${sec.language.id}")
                appendLine(sec.codeContent.trim())
                appendLine("```")
                if (sec.explanationText.isNotBlank()) {
                    appendLine()
                    appendLine(sec.explanationText.trim())
                }
            }
        }
    }

    fun toggleSectionDivider(sectionId: Long) {
        mutateActiveDocument(recordUndo = true) { doc ->
            doc.copy(
                sections = doc.sections.map { sec ->
                    if (sec.id == sectionId) {
                        sec.copy(showDivider = !sec.showDivider)
                    } else {
                        sec
                    }
                }
            )
        }
    }

    fun formatCodeInSection(sectionId: Long) {
        val doc = _uiState.value.activeDocument ?: return
        val targetSection = doc.sections.find { it.id == sectionId } ?: return
        val formatted = com.example.code.CodeLanguageEngine.formatCodeForLanguage(
            code = targetSection.codeContent,
            language = targetSection.language
        )
        mutateActiveDocument(recordUndo = true) { current ->
            current.copy(
                sections = current.sections.map { sec ->
                    if (sec.id == sectionId) sec.copy(codeContent = formatted) else sec
                }
            )
        }
        _uiState.update {
            it.copy(aiStatusMessage = "✓ Formatted ${targetSection.language.displayName} code in ${targetSection.nativeFileName}")
        }
    }

    fun deleteSection(sectionId: Long) {
        debouncedAiNamingJobs.remove(sectionId)?.cancel()
        mutateActiveDocument(recordUndo = true) { doc ->
            val remaining = doc.sections.filterNot { it.id == sectionId }
                .mapIndexed { index, sec -> sec.copy(orderIndex = index) }
            doc.copy(sections = remaining)
        }
        if (_uiState.value.focusedSectionId == sectionId) {
            val nextFocus = _uiState.value.activeDocument?.sections?.firstOrNull()
            _uiState.update {
                it.copy(
                    isFocusedSingleSectionMode = nextFocus != null,
                    focusedSectionId = nextFocus?.id
                )
            }
        }
    }

    fun insertCodeBlockAtActivePosition(language: com.example.model.LanguageType? = null) {
        val effectiveLanguage = language ?: _uiState.value.defaultLanguageForNewFiles
        val currentDoc = _uiState.value.activeDocument ?: return
        viewModelScope.launch {
            recordUndoSnapshot(currentDoc)

            val currentSections = currentDoc.sections
            val insertAfterIndex = when (val target = _uiState.value.activeTarget) {
                is ActiveEditorTarget.Intro -> currentSections.size - 1
                is ActiveEditorTarget.SectionCode -> {
                    currentSections.indexOfFirst { it.id == target.sectionId }
                        .takeIf { it >= 0 } ?: (currentSections.size - 1)
                }
                is ActiveEditorTarget.SectionExplanation -> {
                    currentSections.indexOfFirst { it.id == target.sectionId }
                        .takeIf { it >= 0 } ?: (currentSections.size - 1)
                }
            }

            val completedPreviousSection: CodeSectionUiModel? = when {
                insertAfterIndex in currentSections.indices -> currentSections[insertAfterIndex]
                currentSections.isNotEmpty() -> currentSections.last()
                else -> null
            }

            val newOrderIndex = (insertAfterIndex + 1).coerceIn(0, currentSections.size)
            val inheritedStyle = _uiState.value.activeFormattingStyle

            val newSectionId = repository.insertEmptySectionInDb(
                documentId = currentDoc.id,
                orderIndex = newOrderIndex,
                defaultStyle = inheritedStyle
            )

            val baseStem = if (effectiveLanguage == com.example.model.LanguageType.PYTHON || effectiveLanguage == com.example.model.LanguageType.DART) "main" else "Main"
            val fileTitle = if (newOrderIndex == 0) "$baseStem${effectiveLanguage.extension}" else "${baseStem}${newOrderIndex + 1}${effectiveLanguage.extension}"

            val newSection = CodeSectionUiModel(
                id = newSectionId,
                documentId = currentDoc.id,
                orderIndex = newOrderIndex,
                sectionTitle = fileTitle,
                isAiNamed = false,
                codeContent = effectiveLanguage.starterCode,
                explanationText = "",
                explanationStyle = inheritedStyle,
                showDivider = true,
                language = effectiveLanguage
            )

            val updatedList = currentSections.toMutableList().apply {
                add(newOrderIndex, newSection)
            }.mapIndexed { idx, item -> item.copy(orderIndex = idx) }

            val updatedDoc = currentDoc.copy(
                sections = updatedList,
                updatedAt = System.currentTimeMillis()
            )

            _uiState.update {
                it.copy(
                    activeDocument = updatedDoc,
                    activeTarget = ActiveEditorTarget.SectionCode(newSectionId, newOrderIndex),
                    isFocusedSingleSectionMode = true,
                    focusedSectionId = newSectionId,
                    newlyInsertedSectionId = newSectionId,
                    activeIslandDropdown = IslandDropdownMenu.NONE,
                    hasUnsavedChanges = true,
                    secondsUntilAutoSave = EditorUiState.AUTO_SAVE_INTERVAL_SECONDS,
                    saveStatusBanner = "Created ${newSection.ktScribeFileName} • Auto-save in ${EditorUiState.AUTO_SAVE_INTERVAL_SECONDS}s",
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }

            persistActiveDocumentInternal("Saved new code section to Vault")

            if (completedPreviousSection != null &&
                (completedPreviousSection.codeContent.isNotBlank() || completedPreviousSection.explanationText.isNotBlank())
            ) {
                val currentHash = aiNamingService.computeContentHash(
                    completedPreviousSection.codeContent,
                    completedPreviousSection.explanationText
                )
                if (currentHash != completedPreviousSection.lastNamedHash ||
                    completedPreviousSection.sectionTitle.isBlank() ||
                    completedPreviousSection.sectionTitle.startsWith("Section ")
                ) {
                    triggerAiNamingForSectionInternal(
                        sectionId = completedPreviousSection.id,
                        forceOverwriteTitle = true
                    )
                }
            }
        }
    }

    fun requestAiTitleForSection(sectionId: Long) {
        debouncedAiNamingJobs.remove(sectionId)?.cancel()
        viewModelScope.launch {
            triggerAiNamingForSectionInternal(sectionId, forceOverwriteTitle = true)
        }
    }

    fun requestAiExplainForSection(sectionId: Long) {
        val doc = _uiState.value.activeDocument ?: return
        val sec = doc.sections.find { it.id == sectionId } ?: return
        viewModelScope.launch {
            _uiState.update { state ->
                val curDoc = state.activeDocument ?: return@update state
                state.copy(
                    activeDocument = curDoc.copy(
                        sections = curDoc.sections.map {
                            if (it.id == sectionId) it.copy(isNamingInProgress = true) else it
                        }
                    ),
                    aiStatusMessage = "⚡ Analyzing ${sec.language.displayName} AST to write explanation…"
                )
            }
            val result = aiNamingService.generateCodeExplanation(
                codeContent = sec.codeContent,
                existingExplanation = sec.explanationText,
                sectionNumber = sec.orderIndex + 1,
                language = sec.language
            )
            val (generatedExp, banner, quota) = when (result) {
                is AiAssistOutput.Success -> Triple(result.outputText, result.summaryBanner, result.remainingRpmQuota)
                is AiAssistOutput.FallbackApplied -> Triple(result.outputText, result.summaryBanner, result.remainingRpmQuota)
            }
            val learnedTag = when (result) {
                is AiAssistOutput.Success -> result.learnedConceptTag
                is AiAssistOutput.FallbackApplied -> result.learnedConceptTag
            }
            mutateActiveDocument(recordUndo = true) { curDoc ->
                curDoc.copy(
                    sections = curDoc.sections.map {
                        if (it.id == sectionId) {
                            it.copy(
                                explanationText = generatedExp,
                                explanationStyle = if (learnedTag.isNotBlank()) {
                                    it.explanationStyle.copy(learnedConceptTag = learnedTag)
                                } else {
                                    it.explanationStyle
                                },
                                isNamingInProgress = false
                            )
                        } else {
                            it
                        }
                    }
                )
            }
            _uiState.update {
                val updatedWindows = if (FloatingWindowType.EXPLANATION !in it.openFloatingWindows &&
                    it.openFloatingWindows.size < EditorUiState.MAX_CONCURRENT_FLOATING_WINDOWS &&
                    it.explanationWindowMode != ExplanationWindowMode.SPLIT_SCREEN
                ) {
                    it.openFloatingWindows + FloatingWindowType.EXPLANATION
                } else {
                    it.openFloatingWindows
                }
                it.copy(
                    aiStatusMessage = banner,
                    remainingAiRpmQuota = quota,
                    openFloatingWindows = updatedWindows,
                    explanationWindowMode = if (it.explanationWindowMode == ExplanationWindowMode.HIDDEN &&
                        FloatingWindowType.EXPLANATION in updatedWindows
                    ) {
                        ExplanationWindowMode.FLOATING_WINDOW
                    } else {
                        it.explanationWindowMode
                    }
                )
            }
            triggerAiNamingForSectionInternal(sectionId, forceOverwriteTitle = true)
        }
    }

    fun requestAiRefactorForSection(sectionId: Long) {
        val doc = _uiState.value.activeDocument ?: return
        val sec = doc.sections.find { it.id == sectionId } ?: return
        createLocalSnapshot(sectionId, "Pre-Refactor")
        viewModelScope.launch {
            _uiState.update { state ->
                val curDoc = state.activeDocument ?: return@update state
                state.copy(
                    activeDocument = curDoc.copy(
                        sections = curDoc.sections.map {
                            if (it.id == sectionId) it.copy(isNamingInProgress = true) else it
                        }
                    ),
                    aiStatusMessage = "⚡ Auto-fixing & formatting ${sec.language.displayName} syntax…"
                )
            }
            val result = aiNamingService.refactorAndFixKotlinCode(sec.codeContent, sec.language)
            val (refactoredCode, banner, quota) = when (result) {
                is AiAssistOutput.Success -> Triple(result.outputText, result.summaryBanner, result.remainingRpmQuota)
                is AiAssistOutput.FallbackApplied -> Triple(result.outputText, result.summaryBanner, result.remainingRpmQuota)
            }
            mutateActiveDocument(recordUndo = true) { curDoc ->
                curDoc.copy(
                    sections = curDoc.sections.map {
                        if (it.id == sectionId) {
                            it.copy(
                                codeContent = refactoredCode,
                                isNamingInProgress = false
                            )
                        } else {
                            it
                        }
                    }
                )
            }
            _uiState.update {
                it.copy(
                    aiStatusMessage = banner,
                    remainingAiRpmQuota = quota
                )
            }
        }
    }

    fun requestAiKDocForSection(sectionId: Long) {
        val doc = _uiState.value.activeDocument ?: return
        val sec = doc.sections.find { it.id == sectionId } ?: return
        createLocalSnapshot(sectionId, "Pre-Docstring")
        viewModelScope.launch {
            _uiState.update { state ->
                val curDoc = state.activeDocument ?: return@update state
                state.copy(
                    activeDocument = curDoc.copy(
                        sections = curDoc.sections.map {
                            if (it.id == sectionId) it.copy(isNamingInProgress = true) else it
                        }
                    ),
                    aiStatusMessage = "⚡ Generating ${sec.language.displayName} documentation comments…"
                )
            }
            val result = aiNamingService.addKDocAndCommentsToCode(sec.codeContent, sec.language)
            val (documentedCode, banner, quota) = when (result) {
                is AiAssistOutput.Success -> Triple(result.outputText, result.summaryBanner, result.remainingRpmQuota)
                is AiAssistOutput.FallbackApplied -> Triple(result.outputText, result.summaryBanner, result.remainingRpmQuota)
            }
            mutateActiveDocument(recordUndo = true) { curDoc ->
                curDoc.copy(
                    sections = curDoc.sections.map {
                        if (it.id == sectionId) {
                            it.copy(
                                codeContent = documentedCode,
                                isNamingInProgress = false
                            )
                        } else {
                            it
                        }
                    }
                )
            }
            _uiState.update {
                it.copy(
                    aiStatusMessage = banner,
                    remainingAiRpmQuota = quota
                )
            }
        }
    }

    private suspend fun triggerAiNamingForSectionInternal(
        sectionId: Long,
        forceOverwriteTitle: Boolean
    ) {
        val docBefore = _uiState.value.activeDocument ?: return
        val targetSection = docBefore.sections.find { it.id == sectionId } ?: return

        _uiState.update { state ->
            val doc = state.activeDocument ?: return@update state
            state.copy(
                activeDocument = doc.copy(
                    sections = doc.sections.map { sec ->
                        if (sec.id == sectionId) sec.copy(isNamingInProgress = true) else sec
                    }
                ),
                aiErrorBanner = null,
                aiStatusMessage = "Naming Section #${targetSection.orderIndex + 1} (${targetSection.language.displayName})…"
            )
        }

        val result = aiNamingService.generateSectionTitle(
            codeContent = targetSection.codeContent,
            explanationText = targetSection.explanationText,
            sectionNumber = targetSection.orderIndex + 1,
            language = targetSection.language
        )

        when (result) {
            is SectionNamingResult.Success -> {
                _uiState.update { state ->
                    val doc = state.activeDocument ?: return@update state
                    state.copy(
                        activeDocument = doc.copy(
                            sections = doc.sections.map { sec ->
                                if (sec.id == sectionId) {
                                    sec.copy(
                                        sectionTitle = result.sectionTitle,
                                        explanationStyle = if (result.learnedConceptTag.isNotBlank()) {
                                            sec.explanationStyle.copy(learnedConceptTag = result.learnedConceptTag)
                                        } else {
                                            sec.explanationStyle
                                        },
                                        isAiNamed = true,
                                        isNamingInProgress = false,
                                        lastNamedHash = result.contentHash
                                    )
                                } else {
                                    sec
                                }
                            }
                        ),
                        remainingAiRpmQuota = result.remainingRpmQuota,
                        aiStatusMessage = "Section #${targetSection.orderIndex + 1} • Concept [${result.learnedConceptTag}] • \"${result.sectionTitle}\"",
                        aiErrorBanner = null,
                        hasUnsavedChanges = true
                    )
                }
                persistActiveDocumentInternal("Saved AI section name & concept tag")
            }

            is SectionNamingResult.RateLimited -> {
                val quota = aiNamingService.getRemainingRpmSlots()
                _uiState.update { state ->
                    val doc = state.activeDocument ?: return@update state
                    state.copy(
                        activeDocument = doc.copy(
                            sections = doc.sections.map { sec ->
                                if (sec.id == sectionId) {
                                    val updatedTitle = if (forceOverwriteTitle || sec.sectionTitle.isBlank()) {
                                        result.fallbackTitle
                                    } else {
                                        sec.sectionTitle
                                    }
                                    sec.copy(
                                        sectionTitle = updatedTitle,
                                        explanationStyle = if (result.learnedConceptTag.isNotBlank()) {
                                            sec.explanationStyle.copy(learnedConceptTag = result.learnedConceptTag)
                                        } else {
                                            sec.explanationStyle
                                        },
                                        isAiNamed = true,
                                        isNamingInProgress = false,
                                        lastNamedHash = result.contentHash
                                    )
                                } else {
                                    sec
                                }
                            }
                        ),
                        remainingAiRpmQuota = quota,
                        aiStatusMessage = "Tagged [${result.learnedConceptTag}] • Named: \"${result.fallbackTitle}\"",
                        aiErrorBanner = null,
                        hasUnsavedChanges = true
                    )
                }
                persistActiveDocumentInternal("Saved smart fallback section name & concept tag")
            }

            is SectionNamingResult.Error -> {
                val quota = aiNamingService.getRemainingRpmSlots()
                _uiState.update { state ->
                    val doc = state.activeDocument ?: return@update state
                    state.copy(
                        activeDocument = doc.copy(
                            sections = doc.sections.map { sec ->
                                if (sec.id == sectionId) {
                                    val updatedTitle = if (forceOverwriteTitle || sec.sectionTitle.isBlank()) {
                                        result.fallbackTitle
                                    } else {
                                        sec.sectionTitle
                                    }
                                    sec.copy(
                                        sectionTitle = updatedTitle,
                                        explanationStyle = if (result.learnedConceptTag.isNotBlank()) {
                                            sec.explanationStyle.copy(learnedConceptTag = result.learnedConceptTag)
                                        } else {
                                            sec.explanationStyle
                                        },
                                        isAiNamed = true,
                                        isNamingInProgress = false,
                                        lastNamedHash = result.contentHash
                                    )
                                } else {
                                    sec
                                }
                            }
                        ),
                        remainingAiRpmQuota = quota,
                        aiStatusMessage = result.message,
                        aiErrorBanner = null,
                        hasUnsavedChanges = true
                    )
                }
                persistActiveDocumentInternal("Saved section name & concept tag")
            }
        }
    }

    fun setFontChoice(fontChoice: EditorFontChoice) {
        updateActiveFormattingStyle { it.copy(fontChoice = fontChoice) }
    }

    fun setFontSizeSp(sizeSp: Int) {
        updateActiveFormattingStyle { it.copy(fontSizeSp = sizeSp.coerceIn(12, 32)) }
    }

    fun toggleBold() {
        updateActiveFormattingStyle { it.copy(isBold = !it.isBold) }
    }

    fun toggleItalic() {
        updateActiveFormattingStyle { it.copy(isItalic = !it.isItalic) }
    }

    fun toggleUnderline() {
        updateActiveFormattingStyle { it.copy(isUnderline = !it.isUnderline) }
    }

    fun toggleStrikethrough() {
        updateActiveFormattingStyle { it.copy(isStrikethrough = !it.isStrikethrough) }
    }

    fun setTextAlignment(alignment: TextAlignmentChoice) {
        updateActiveFormattingStyle { it.copy(alignment = alignment) }
    }

    private fun updateActiveFormattingStyle(transform: (TextFormattingStyle) -> TextFormattingStyle) {
        val target = _uiState.value.activeTarget
        mutateActiveDocument(recordUndo = true) { doc ->
            when (target) {
                is ActiveEditorTarget.Intro -> {
                    doc.copy(introStyle = transform(doc.introStyle))
                }
                is ActiveEditorTarget.SectionExplanation -> {
                    doc.copy(
                        sections = doc.sections.map { sec ->
                            if (sec.id == target.sectionId) {
                                sec.copy(explanationStyle = transform(sec.explanationStyle))
                            } else {
                                sec
                            }
                        }
                    )
                }
                is ActiveEditorTarget.SectionCode -> {
                    doc.copy(
                        sections = doc.sections.map { sec ->
                            if (sec.id == target.sectionId) {
                                sec.copy(explanationStyle = transform(sec.explanationStyle))
                            } else {
                                sec
                            }
                        }
                    )
                }
            }
        }
    }

    fun undo() {
        val current = _uiState.value.activeDocument ?: return
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeLast()
        redoStack.addLast(current)
        _uiState.update {
            it.copy(
                activeDocument = previous,
                hasUnsavedChanges = true,
                secondsUntilAutoSave = EditorUiState.AUTO_SAVE_INTERVAL_SECONDS,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun redo() {
        val current = _uiState.value.activeDocument ?: return
        if (redoStack.isEmpty()) return
        val next = redoStack.removeLast()
        undoStack.addLast(current)
        _uiState.update {
            it.copy(
                activeDocument = next,
                hasUnsavedChanges = true,
                secondsUntilAutoSave = EditorUiState.AUTO_SAVE_INTERVAL_SECONDS,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    private fun recordUndoSnapshot(snapshot: DocumentDetailUiModel) {
        if (undoStack.size >= 25) {
            undoStack.removeFirst()
        }
        undoStack.addLast(snapshot)
        redoStack.clear()
    }

    private fun mutateActiveDocument(
        recordUndo: Boolean,
        mutator: (DocumentDetailUiModel) -> DocumentDetailUiModel
    ) {
        val current = _uiState.value.activeDocument ?: return
        if (recordUndo) {
            recordUndoSnapshot(current)
        }
        val updated = mutator(current).copy(updatedAt = System.currentTimeMillis())
        _uiState.update {
            it.copy(
                activeDocument = updated,
                hasUnsavedChanges = true,
                secondsUntilAutoSave = EditorUiState.AUTO_SAVE_INTERVAL_SECONDS,
                saveStatusBanner = "Auto-save in ${EditorUiState.AUTO_SAVE_INTERVAL_SECONDS}s",
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    private suspend fun persistActiveDocumentInternal(reason: String) {
        val doc = _uiState.value.activeDocument ?: return
        repository.saveDocumentDetail(doc)
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                hasUnsavedChanges = false,
                secondsUntilAutoSave = EditorUiState.AUTO_SAVE_INTERVAL_SECONDS,
                lastSavedTimestamp = now,
                saveStatusBanner = reason
            )
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = EditorDatabase.getInstance(context)
            val repo = EditorRepository(db.editorDao(), context)
            return EditorViewModel(repo) as T
        }
    }
}
