package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AiSectionNamingService
import com.example.ai.SectionNamingResult
import com.example.code.KotlinCodeEngine
import com.example.data.local.EditorDatabase
import com.example.data.local.EditorRepository
import com.example.model.ActiveEditorTarget
import com.example.model.CodeSectionUiModel
import com.example.model.DocumentDetailUiModel
import com.example.model.DocumentSummaryUiModel
import com.example.model.EditorFontChoice
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class EditorUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val activeDocument: DocumentDetailUiModel? = null,
    val activeTarget: ActiveEditorTarget = ActiveEditorTarget.Intro,
    val newlyInsertedSectionId: Long? = null,
    val hasUnsavedChanges: Boolean = false,
    val secondsUntilAutoSave: Int = AUTO_SAVE_INTERVAL_SECONDS,
    val lastSavedTimestamp: Long? = null,
    val saveStatusBanner: String = "All changes saved locally",
    val aiStatusMessage: String? = null,
    val aiErrorBanner: String? = null,
    val remainingAiRpmQuota: Int = AiSectionNamingService.FREE_TIER_SAFE_RPM_LIMIT,
    val shortcutLetter: Char = 'K',
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
) {
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
    }
}

class EditorViewModel(
    private val repository: EditorRepository,
    private val aiNamingService: AiSectionNamingService = AiSectionNamingService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditorUiState(
            shortcutLetter = repository.getShortcutLetter(),
            themeMode = repository.getThemeMode()
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
                    doc.previewSnippet.contains(trimmed, ignoreCase = true)
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
            repository.ensureSeedDocumentsIfNeeded()
            val quota = aiNamingService.getRemainingRpmSlots()
            _uiState.update {
                it.copy(
                    isLoading = false,
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
                        persistActiveDocumentInternal(reason = "Auto-saved (30s cycle)")
                    } else {
                        _uiState.update {
                            it.copy(
                                secondsUntilAutoSave = nextSec,
                                saveStatusBanner = "Unsaved edits • Auto-save in ${nextSec}s"
                            )
                        }
                    }
                }
            }
        }
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
                persistActiveDocumentInternal("Auto-saved before switching document")
            }
            val detail = repository.loadDocumentDetail(documentId) ?: return@launch
            undoStack.clear()
            redoStack.clear()
            val initialTarget = if (detail.sections.isNotEmpty()) {
                val lastSec = detail.sections.last()
                ActiveEditorTarget.SectionCode(lastSec.id, lastSec.orderIndex)
            } else {
                ActiveEditorTarget.Intro
            }
            _uiState.update {
                it.copy(
                    activeDocument = detail,
                    activeTarget = initialTarget,
                    newlyInsertedSectionId = null,
                    hasUnsavedChanges = false,
                    secondsUntilAutoSave = EditorUiState.AUTO_SAVE_INTERVAL_SECONDS,
                    lastSavedTimestamp = detail.updatedAt,
                    saveStatusBanner = "Saved locally",
                    aiErrorBanner = null,
                    canUndo = false,
                    canRedo = false
                )
            }
        }
    }

    fun createAndOpenNewDocument(customTitle: String? = null) {
        viewModelScope.launch {
            if (_uiState.value.activeDocument != null && _uiState.value.hasUnsavedChanges) {
                persistActiveDocumentInternal("Auto-saved before creating new document")
            }
            val newDocId = repository.createNewDocument(
                title = customTitle?.takeIf { it.isNotBlank() } ?: "Untitled Kotlin Note",
                withStarterSection = true
            )
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
                persistActiveDocumentInternal("Auto-saved on leaving document")
            }
            undoStack.clear()
            redoStack.clear()
            _uiState.update {
                it.copy(
                    activeDocument = null,
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
                persistActiveDocumentInternal("Auto-saved on app minimize")
            }
        }
    }

    fun saveNowManual() {
        viewModelScope.launch {
            persistActiveDocumentInternal("Saved manually")
        }
    }

    fun setActiveTarget(target: ActiveEditorTarget) {
        _uiState.update { it.copy(activeTarget = target) }
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

    /**
     * Automatically triggers AI section naming after a 4-second pause when a section has both
     * Kotlin code and an explanation written, in addition to naming when the next code block is added.
     */
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
        mutateActiveDocument(recordUndo = true) { doc ->
            doc.copy(
                sections = doc.sections.map { sec ->
                    if (sec.id == sectionId) {
                        sec.copy(sectionTitle = newTitle, isAiNamed = false)
                    } else {
                        sec
                    }
                }
            )
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
        val formatted = KotlinCodeEngine.formatKotlinCode(targetSection.codeContent)
        mutateActiveDocument(recordUndo = true) { current ->
            current.copy(
                sections = current.sections.map { sec ->
                    if (sec.id == sectionId) sec.copy(codeContent = formatted) else sec
                }
            )
        }
        _uiState.update {
            it.copy(aiStatusMessage = "Formatted Kotlin code in Section ${targetSection.orderIndex + 1}")
        }
    }

    fun deleteSection(sectionId: Long) {
        debouncedAiNamingJobs.remove(sectionId)?.cancel()
        mutateActiveDocument(recordUndo = true) { doc ->
            val remaining = doc.sections.filterNot { it.id == sectionId }
                .mapIndexed { index, sec -> sec.copy(orderIndex = index) }
            doc.copy(sections = remaining)
        }
    }

    /**
     * Inserts a new 4-line auto-expanding Kotlin code block + explanation section right after
     * the currently active section (or at the end of the document), and immediately names the
     * just-completed previous section using AI.
     */
    fun insertCodeBlockAtActivePosition() {
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

            // Identify the completed section right before the newly inserted code block
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

            val newSection = CodeSectionUiModel(
                id = newSectionId,
                documentId = currentDoc.id,
                orderIndex = newOrderIndex,
                sectionTitle = "",
                isAiNamed = false,
                codeContent = "",
                explanationText = "",
                explanationStyle = inheritedStyle,
                showDivider = true
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
                    newlyInsertedSectionId = newSectionId,
                    hasUnsavedChanges = true,
                    secondsUntilAutoSave = EditorUiState.AUTO_SAVE_INTERVAL_SECONDS,
                    saveStatusBanner = "Added Code Block • Auto-save in ${EditorUiState.AUTO_SAVE_INTERVAL_SECONDS}s"
                )
            }

            persistActiveDocumentInternal("Saved new code block section")

            // Trigger AI section naming on the completed previous section
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
                aiStatusMessage = "Naming Section #${targetSection.orderIndex + 1} from Kotlin code & explanation…"
            )
        }

        val result = aiNamingService.generateSectionTitle(
            codeContent = targetSection.codeContent,
            explanationText = targetSection.explanationText,
            sectionNumber = targetSection.orderIndex + 1
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
                        aiStatusMessage = "Section #${targetSection.orderIndex + 1} named: \"${result.sectionTitle}\" (${result.remainingRpmQuota} RPM left)",
                        aiErrorBanner = null,
                        hasUnsavedChanges = true
                    )
                }
                persistActiveDocumentInternal("Saved AI section name")
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
                        aiStatusMessage = "Free-tier RPM limit (10/min) reached • Named: \"${result.fallbackTitle}\"",
                        aiErrorBanner = null,
                        hasUnsavedChanges = true
                    )
                }
                persistActiveDocumentInternal("Saved smart fallback section name")
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
                persistActiveDocumentInternal("Saved section name")
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
                saveStatusBanner = "Unsaved edits • Auto-save in ${EditorUiState.AUTO_SAVE_INTERVAL_SECONDS}s",
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
