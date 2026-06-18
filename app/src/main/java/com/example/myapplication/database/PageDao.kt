package com.example.myapplication.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.myapplication.data.Page

@Dao
interface PageDao {
    // ── READS ──────────────────────────────────────────────────

    // Fetches all pages for a chapter in display order.
    // This is called when the user opens a chapter in the reader.
    // ORDER BY page_index ASC = page 0 first (right side in RTL manga).
    // suspend not Flow — pages don't change after being loaded,
    // so no need for reactive stream here.
    @Query("SELECT * FROM pages WHERE chapterId=:chapterId ORDER BY pageIndex ASC")
    suspend fun getPagesForChapter(chapterId: String):List<Page>
    // For offline reading — checks if pages are already cached locally.
    // If localPath is not null, the image is on disk; skip the network.
    @Query("SELECT * FROM pages WHERE chapterId = :chapterId AND localPath IS NOT NULL")
    suspend fun getDownloadedPagesForChapter(chapterId : String):List<Page>

    // How many pages are already downloaded for this chapter.
    // Used to show download progress: "12 / 20 pages".
@Query("SELECT COUNT(*) FROM pages WHERE chapterId = :chapterId AND localPath IS NOT NULL")
    suspend fun getDownloadedPageCountForChapter(chapterId : String): Int

    // ── WRITES ─────────────────────────────────────────────────

    // Insert or update all pages for a chapter.
    // Called after fetching page URLs from the MangaDex at-home server.
    @Upsert
    suspend fun upsertPages(pages: List<Page>)

    // After a page image is downloaded to local storage,
    // update its localPath so the reader uses the file instead of the URL.
    @Query("UPDATE pages SET localPath=:localPath WHERE id=:pageId")
    suspend fun updateLocalPath(pageId:String,localPath:String)

    // Called when user deletes downloaded chapter — wipes local paths
    // so the reader falls back to network URLs.
    @Query("UPDATE pages SET localPath= NULL WHERE id=:chapterId")
    suspend fun clearLocalPathsForChapter(chapterId : String)

    // Full delete — called when CASCADE from Chapter delete doesn't
    // trigger (e.g. if you're managing page cleanup manually).
    @Query("DELETE FROM pages WHERE chapterId=:chapterId")
    suspend fun deletePagesForChapter(chapterId : String)
}
