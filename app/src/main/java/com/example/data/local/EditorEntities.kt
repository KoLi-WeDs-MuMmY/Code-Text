package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val introText: String = "",
    val introStyleJson: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "code_sections",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class CodeSectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val documentId: Long,
    val orderIndex: Int,
    val sectionTitle: String = "",
    val isAiNamed: Boolean = false,
    val lastNamedHash: String = "",
    val codeContent: String = "",
    val explanationText: String = "",
    val explanationStyleJson: String = "",
    val showDivider: Boolean = true
)

data class DocumentWithSections(
    @Embedded val document: DocumentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "documentId"
    )
    val sections: List<CodeSectionEntity>
)

@Entity(tableName = "concept_mastery")
data class ConceptMasteryEntity(
    @PrimaryKey val conceptTag: String,
    val trackId: String,
    val xp: Int = 0,
    val masteryPercent: Int = 0,
    val completedLessons: Int = 0,
    val passedQuizzes: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val trackId: String,
    val passedTests: Int = 0,
    val totalTests: Int = 0,
    val isCompleted: Boolean = false,
    val lastCodeContent: String = "",
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val questionId: String,
    val conceptTag: String,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val answeredAt: Long = System.currentTimeMillis()
)
