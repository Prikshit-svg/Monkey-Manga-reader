package com.example.myapplication.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.myapplication.data.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {

    // ── READS ──────────────────────────────────────────────────

    // Returns Flow — Room watches the chapters table and re-emits
    // the full list whenever any chapter for this manga changes.
    // ORDER BY chapter_number ASC = chapter 1 first, latest last.
    @Query("SELECT * FROM chapter WHERE manga_id = :mangaId ORDER BY chapterNumber ASC")
    fun getChaptersForManga(mangaId: String): Flow<List<Chapter>>

    // suspend = runs on a background thread, doesn't block UI.
    // Returns null if chapter doesn't exist — safe to call without crash.
    @Query("SELECT * FROM chapter WHERE id = :id")
    suspend fun getChapterById(id: String): Chapter?

    // "Continue reading" feature — finds the very next chapter
    // after the one the user just finished.
    // LIMIT 1 = only the immediate next, not all remaining.
    @Query("""
        SELECT * FROM chapter
        WHERE manga_id = :mangaId
        AND chapterNumber > :currentNumber
        ORDER BY chapterNumber ASC
        LIMIT 1
    """)
    suspend fun getNextChapter(mangaId: String, currentNumber: Float): Chapter?

    // Previous chapter — same logic, reversed direction.
    @Query("""
        SELECT * FROM chapter
        WHERE manga_id = :mangaId
        AND chapterNumber < :currentNumber
        ORDER BY chapterNumber DESC
        LIMIT 1
    """)
    suspend fun getPreviousChapter(mangaId: String, currentNumber: Float): Chapter?

    // For the download manager — find all downloaded chapters of a manga.
    @Query("SELECT * FROM chapter WHERE manga_id = :mangaId AND isDownloaded = 1")
    fun getDownloadedChapters(mangaId: String): Flow<List<Chapter>>

    // Total downloaded chapter count — shown in library UI.
    @Query("SELECT COUNT(*) FROM chapter WHERE manga_id = :mangaId AND isDownloaded = 1")
    suspend fun getDownloadedCount(mangaId: String): Int

    // ── WRITES ─────────────────────────────────────────────────

    // @Upsert = INSERT OR REPLACE.
    // When you sync from MangaDex API, you don't check if chapters
    // already exist — just upsert the whole list. New ones get inserted,
    // existing ones get updated.
    @Upsert
    suspend fun upsertChapters(chapters: List<Chapter>)

    // Called after a chapter is fully downloaded to device storage.
    // Only updates one column — efficient, doesn't rewrite the whole row.
    @Query("UPDATE chapter SET isDownloaded = 1 WHERE id = :chapterId")
    suspend fun markAsDownloaded(chapterId: String)

    // Called when user deletes a downloaded chapter to free storage.
    @Query("UPDATE chapter SET isDownloaded = 0 WHERE id = :chapterId")
    suspend fun markAsNotDownloaded(chapterId: String)

    // Called when user deletes a manga from their library.
    // Normally handled by CASCADE from Manga delete,
    // but useful if you want to clear chapters without deleting the manga.
    @Query("DELETE FROM chapter WHERE manga_id = :mangaId")
    suspend fun deleteChaptersForManga(mangaId: String)
}