package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EditorDao {

    @Transaction
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun observeAllDocumentsWithSections(): Flow<List<DocumentWithSections>>

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    fun observeDocumentWithSections(documentId: Long): Flow<DocumentWithSections?>

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    suspend fun getDocumentWithSectionsOnce(documentId: Long): DocumentWithSections?

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :documentId")
    suspend fun deleteDocumentById(documentId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: CodeSectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<CodeSectionEntity>): List<Long>

    @Update
    suspend fun updateSection(section: CodeSectionEntity)

    @Query("DELETE FROM code_sections WHERE id = :sectionId")
    suspend fun deleteSectionById(sectionId: Long)

    @Query("DELETE FROM code_sections WHERE documentId = :documentId")
    suspend fun deleteSectionsForDocument(documentId: Long)

    @Transaction
    suspend fun saveDocumentAndSections(
        document: DocumentEntity,
        sections: List<CodeSectionEntity>
    ) {
        updateDocument(document.copy(updatedAt = System.currentTimeMillis()))
        val existing = getDocumentWithSectionsOnce(document.id)?.sections.orEmpty()
        val incomingIds = sections.map { it.id }.filter { it != 0L }.toSet()
        for (old in existing) {
            if (old.id !in incomingIds) {
                deleteSectionById(old.id)
            }
        }
        sections.forEachIndexed { index, sec ->
            val ordered = sec.copy(documentId = document.id, orderIndex = index)
            insertSection(ordered)
        }
    }
}
