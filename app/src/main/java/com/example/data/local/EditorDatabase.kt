package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DocumentEntity::class, CodeSectionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EditorDatabase : RoomDatabase() {
    abstract fun editorDao(): EditorDao

    companion object {
        @Volatile
        private var INSTANCE: EditorDatabase? = null

        fun getInstance(context: Context): EditorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EditorDatabase::class.java,
                    "ktscribe_editor.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
