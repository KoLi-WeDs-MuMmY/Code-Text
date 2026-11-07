package com.example.data.local

import android.content.Context
import com.example.model.CodeSectionUiModel
import com.example.model.DocumentDetailUiModel
import com.example.model.DocumentSummaryUiModel
import com.example.model.EditorFontChoice
import com.example.model.TextAlignmentChoice
import com.example.model.TextFormattingStyle
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class EditorRepository(
    private val dao: EditorDao,
    context: Context
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(
        "ktscribe_user_prefs",
        Context.MODE_PRIVATE
    )

    val allDocumentSummaries: Flow<List<DocumentSummaryUiModel>> =
        dao.observeAllDocumentsWithSections().map { list ->
            list.map { item ->
                val sortedSections = item.sections.sortedBy { it.orderIndex }
                val firstSnippet = when {
                    item.document.introText.isNotBlank() -> item.document.introText.trim()
                    sortedSections.isNotEmpty() -> {
                        val first = sortedSections.first()
                        first.explanationText.ifBlank { first.codeContent }.trim()
                    }
                    else -> "Empty Kotlin folder"
                }
                val totalWords = countWords(item.document.introText) +
                    sortedSections.sumOf { countWords(it.codeContent) + countWords(it.explanationText) }

                val conceptTags = sortedSections.map { sec ->
                    val style = TextFormattingStyle.fromJsonString(sec.explanationStyleJson)
                    style.learnedConceptTag.ifBlank {
                        CodeSectionUiModel.inferSmartConceptTagFromCode(
                            code = sec.codeContent,
                            sectionTitle = sec.sectionTitle,
                            sectionNumber = sec.orderIndex + 1
                        )
                    }
                }.distinct().take(4)

                DocumentSummaryUiModel(
                    id = item.document.id,
                    title = item.document.title.ifBlank { "Untitled Folder" },
                    previewSnippet = firstSnippet.replace("\n", " ").take(120),
                    sectionCount = sortedSections.size,
                    updatedAt = item.document.updatedAt,
                    wordCount = totalWords,
                    conceptTags = conceptTags
                )
            }
        }

    fun inspectVaultState(): VaultInspectionResult {
        return VaultStorageManager.inspectVaultState(appContext)
    }

    suspend fun recoverFoldersFromHiddenVault(): Long? {
        val recovered = VaultStorageManager.readRecoverableFolders(appContext)
        var firstRecoveredId: Long? = null
        val now = System.currentTimeMillis()
        for (folder in recovered) {
            val docId = dao.insertDocument(
                DocumentEntity(
                    title = folder.title.ifBlank { "Recovered Folder" },
                    introText = folder.introText,
                    introStyleJson = folder.introStyle.toJsonString(),
                    createdAt = now,
                    updatedAt = now
                )
            )
            if (firstRecoveredId == null) firstRecoveredId = docId
            val sectionEntities = folder.sections.mapIndexed { idx, sec ->
                CodeSectionEntity(
                    documentId = docId,
                    orderIndex = idx,
                    sectionTitle = sec.sectionTitle,
                    isAiNamed = sec.isAiNamed,
                    lastNamedHash = sec.lastNamedHash,
                    codeContent = sec.codeContent,
                    explanationText = sec.explanationText,
                    explanationStyleJson = sec.explanationStyle.toJsonString(),
                    showDivider = sec.showDivider
                )
            }
            dao.insertSections(sectionEntities)
        }
        VaultStorageManager.markFirstLaunchGateCompleted(appContext)
        syncAllFoldersToHiddenVault()
        return firstRecoveredId
    }

    suspend fun initializeFreshHiddenVault(): Long? {
        VaultStorageManager.resetCleanHiddenVault(appContext)
        ensureSeedDocumentsIfNeeded()
        syncAllFoldersToHiddenVault()
        val firstDoc = dao.observeAllDocumentsWithSections().firstOrNull()?.firstOrNull()
        return firstDoc?.document?.id
    }

    suspend fun syncAllFoldersToHiddenVault() {
        val allRecords = dao.observeAllDocumentsWithSections().firstOrNull() ?: return
        val details = allRecords.map { it.toDetailUiModel() }
        VaultStorageManager.writeFoldersToHiddenVault(appContext, details)
    }

    fun getShortcutLetter(): Char {
        val raw = prefs.getString(KEY_SHORTCUT_LETTER, "K") ?: "K"
        return raw.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' } ?: 'K'
    }

    fun setShortcutLetter(letter: Char) {
        val clean = letter.uppercaseChar().takeIf { it in 'A'..'Z' } ?: 'K'
        prefs.edit().putString(KEY_SHORTCUT_LETTER, clean.toString()).apply()
    }

    fun getThemeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        return ThemeMode.entries.find { it.name == raw } ?: ThemeMode.DARK
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    suspend fun loadDocumentDetail(documentId: Long): DocumentDetailUiModel? {
        val record = dao.getDocumentWithSectionsOnce(documentId) ?: return null
        return record.toDetailUiModel()
    }

    suspend fun createNewDocument(
        title: String = "New Kotlin Folder",
        withStarterSection: Boolean = true
    ): Long {
        val now = System.currentTimeMillis()
        val defaultStyle = TextFormattingStyle(
            fontChoice = EditorFontChoice.PLUS_JAKARTA,
            fontSizeSp = 16
        )
        val docId = dao.insertDocument(
            DocumentEntity(
                title = title,
                introText = "",
                introStyleJson = defaultStyle.toJsonString(),
                createdAt = now,
                updatedAt = now
            )
        )
        if (withStarterSection) {
            dao.insertSection(
                CodeSectionEntity(
                    documentId = docId,
                    orderIndex = 0,
                    sectionTitle = "Section 1 • Entry Point",
                    isAiNamed = false,
                    codeContent = "fun main() {\n    val message = \"Hello, KtScribe!\"\n    println(message)\n}",
                    explanationText = "",
                    explanationStyleJson = defaultStyle.toJsonString(),
                    showDivider = true
                )
            )
        }
        syncAllFoldersToHiddenVault()
        return docId
    }

    suspend fun saveDocumentDetail(detail: DocumentDetailUiModel) {
        val docEntity = DocumentEntity(
            id = detail.id,
            title = detail.title.ifBlank { "Untitled Kotlin Folder" },
            introText = detail.introText,
            introStyleJson = detail.introStyle.toJsonString(),
            updatedAt = System.currentTimeMillis()
        )
        val sectionEntities = detail.sections.mapIndexed { idx, sec ->
            CodeSectionEntity(
                id = if (sec.id > 0L) sec.id else 0L,
                documentId = detail.id,
                orderIndex = idx,
                sectionTitle = sec.sectionTitle,
                isAiNamed = sec.isAiNamed,
                lastNamedHash = sec.lastNamedHash,
                codeContent = sec.codeContent,
                explanationText = sec.explanationText,
                explanationStyleJson = sec.explanationStyle.toJsonString(),
                showDivider = sec.showDivider
            )
        }
        dao.saveDocumentAndSections(docEntity, sectionEntities)
        syncAllFoldersToHiddenVault()
    }

    suspend fun insertEmptySectionInDb(
        documentId: Long,
        orderIndex: Int,
        defaultStyle: TextFormattingStyle = TextFormattingStyle()
    ): Long {
        return dao.insertSection(
            CodeSectionEntity(
                documentId = documentId,
                orderIndex = orderIndex,
                sectionTitle = "",
                isAiNamed = false,
                codeContent = "",
                explanationText = "",
                explanationStyleJson = defaultStyle.toJsonString(),
                showDivider = true
            )
        )
    }

    suspend fun deleteDocument(documentId: Long) {
        dao.deleteDocumentById(documentId)
        syncAllFoldersToHiddenVault()
    }

    suspend fun ensureSeedDocumentsIfNeeded() {
        if (dao.getDocumentCount() > 0) return

        val now = System.currentTimeMillis()
        val introStyle = TextFormattingStyle(
            fontChoice = EditorFontChoice.PLUS_JAKARTA,
            fontSizeSp = 16
        )
        val serifStyle = TextFormattingStyle(
            fontChoice = EditorFontChoice.MERRIWEATHER,
            fontSizeSp = 16
        )

        val doc1Id = dao.insertDocument(
            DocumentEntity(
                title = "Kotlin Coroutines & Flow Architecture",
                introText = "Folder module for reactive state streams in Kotlin. Use the top macOS Dynamic Island to switch sections, open the floating Explanation window, or trigger live IDE abbreviations like sout -> println().",
                introStyleJson = introStyle.toJsonString(),
                createdAt = now - 3600_000L,
                updatedAt = now - 120_000L
            )
        )

        dao.insertSections(
            listOf(
                CodeSectionEntity(
                    documentId = doc1Id,
                    orderIndex = 0,
                    sectionTitle = "Sealed UI State & Reactive StateFlow",
                    isAiNamed = true,
                    codeContent = """
                        sealed interface NoteUiState {
                            data object Loading : NoteUiState
                            data class Ready(val count: Int, val summary: String) : NoteUiState
                        }

                        class NoteStateHolder {
                            private val _state = MutableStateFlow<NoteUiState>(NoteUiState.Loading)
                            val state: StateFlow<NoteUiState> = _state
                        }
                    """.trimIndent(),
                    explanationText = "Concept Learned: Sealed Hierarchy\n\nBy modeling state with a sealed interface and exposing a read-only StateFlow, the UI layer observes deterministic state transitions without race conditions.",
                    explanationStyleJson = serifStyle.copy(learnedConceptTag = "Sealed Hierarchy").toJsonString(),
                    showDivider = true
                ),
                CodeSectionEntity(
                    documentId = doc1Id,
                    orderIndex = 1,
                    sectionTitle = "Collection Pipeline & Sequence Filtering",
                    isAiNamed = true,
                    codeContent = """
                        fun filterActiveTasks(tasks: List<String>): List<String> {
                            return tasks
                                .filter { it.isNotBlank() }
                                .map { it.trim().uppercase() }
                        }
                    """.trimIndent(),
                    explanationText = "Concept Learned: Collection Pipeline\n\nThis helper cleans raw task titles by filtering out blank lines and normalizing casing. Try typing sout in the code editor to see live println() completion!",
                    explanationStyleJson = introStyle.copy(learnedConceptTag = "Collection Pipeline").toJsonString(),
                    showDivider = true
                )
            )
        )

        val doc2Id = dao.insertDocument(
            DocumentEntity(
                title = "Kotlin Data Classes & Extension Idioms",
                introText = "Folder module for idiomatic Kotlin extension functions, value classes, and null-safety patterns.",
                introStyleJson = introStyle.copy(alignment = TextAlignmentChoice.LEFT).toJsonString(),
                createdAt = now - 7200_000L,
                updatedAt = now - 1800_000L
            )
        )

        dao.insertSections(
            listOf(
                CodeSectionEntity(
                    documentId = doc2Id,
                    orderIndex = 0,
                    sectionTitle = "Immutable Data Class With Computed Property",
                    isAiNamed = true,
                    codeContent = """
                        data class CodeSnippet(
                            val title: String,
                            val lines: List<String>
                        ) {
                            val preview: String
                                get() = lines.take(4).joinToString("\n")
                        }
                    """.trimIndent(),
                    explanationText = "Concept Learned: Immutable Data Class\n\nData classes automatically generate equals, hashCode, toString, and copy while allowing lightweight computed properties like preview.",
                    explanationStyleJson = introStyle.copy(learnedConceptTag = "Immutable Data Class").toJsonString(),
                    showDivider = true
                )
            )
        )
        syncAllFoldersToHiddenVault()
    }

    private fun DocumentWithSections.toDetailUiModel(): DocumentDetailUiModel {
        val sorted = sections.sortedBy { it.orderIndex }.mapIndexed { idx, entity ->
            CodeSectionUiModel(
                id = entity.id,
                documentId = entity.documentId,
                orderIndex = idx,
                sectionTitle = entity.sectionTitle,
                isAiNamed = entity.isAiNamed,
                isNamingInProgress = false,
                lastNamedHash = entity.lastNamedHash,
                codeContent = entity.codeContent,
                explanationText = entity.explanationText,
                explanationStyle = TextFormattingStyle.fromJsonString(entity.explanationStyleJson),
                showDivider = entity.showDivider
            )
        }
        return DocumentDetailUiModel(
            id = document.id,
            title = document.title,
            introText = document.introText,
            introStyle = TextFormattingStyle.fromJsonString(document.introStyleJson),
            sections = sorted,
            updatedAt = document.updatedAt
        )
    }

    private fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    }

    companion object {
        private const val KEY_SHORTCUT_LETTER = "shortcut_letter"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
