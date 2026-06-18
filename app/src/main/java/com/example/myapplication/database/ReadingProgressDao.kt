package com.example.myapplication.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.myapplication.data.ReadingProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {

    // ── READS ──────────────────────────────────────────────────

    // Exact progress for a specific chapter.
    // Called when user reopens a chapter — to resume from last page.
    // Returns null if the user has never opened this chapter.
    @Query("""
        SELECT * FROM reading_progress
        WHERE mangaId = :mangaId AND chapterId = :chapterId
        LIMIT 1
    """)
    suspend fun getProgress(mangaId: String, chapterId: String): ReadingProgress?

    // The most recently read chapter for a manga.
    // Powers the "Continue Reading" button on the series detail screen.
    // ORDER BY updated_at DESC = last touched chapter first.
    @Query("""
        SELECT * FROM reading_progress
        WHERE mangaId = :mangaId
        ORDER BY updatedAt DESC
        LIMIT 1
    """)
    suspend fun getLastReadChapter(mangaId: String): ReadingProgress?

    // All manga the user is currently reading (not completed).
    // Powers the "In Progress" section of the Library screen.
    // Flow = list updates automatically when user reads a new chapter.
    @Query("""
        SELECT * FROM reading_progress
        WHERE isCompleted = 0
        ORDER BY updatedAt DESC
    """)
    fun getInProgressManga(): Flow<List<ReadingProgress>>

    // All completed manga — for the "Completed" library tab.
    @Query("""
        SELECT * FROM reading_progress
        WHERE isCompleted = 1
        ORDER BY updatedAt DESC
    """)
    fun getCompletedManga(): Flow<List<ReadingProgress>>

    // Check if a specific chapter is fully read.
    // Used to show a checkmark on the chapter list.
    @Query("""
        SELECT isCompleted FROM reading_progress
        WHERE mangaId = :mangaId AND chapterId = :chapterId
        LIMIT 1
    """)
    suspend fun isChapterCompleted(mangaId: String, chapterId: String): Boolean?

    // ── WRITES ─────────────────────────────────────────────────

    // The most important write operation in the whole app.
    // Called every time the user turns a page in the reader.
    // @Upsert handles both first-time open and subsequent page turns.
    @Upsert
    suspend fun saveProgress(progress: ReadingProgress)

    // Called when user marks a manga as completed manually,
    // or when the last chapter's last page is reached.
    @Query("""
        UPDATE reading_progress
        SET isCompleted = 1, updatedAt = :timestamp
        WHERE mangaId = :mangaId
    """)
    suspend fun markMangaCompleted(mangaId: String, timestamp: Long = System.currentTimeMillis())
    @Query("""
      UPDATE reading_progress
      SET isCompleted = 1, updatedAt = :timestamp
      WHERE mangaId = :mangaId AND chapterId = :chapterId
  """)
    suspend fun markChapterCompleted(
        mangaId: String,
        chapterId: String,
        timestamp: Long = System.currentTimeMillis()
    )
    // Called when user wants to re-read from scratch.
    @Query("DELETE FROM reading_progress WHERE mangaId = :mangaId")
    suspend fun clearProgressForManga(mangaId: String)
}