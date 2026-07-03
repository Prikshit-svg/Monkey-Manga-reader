package com.example.myapplication.repositories

import com.example.myapplication.api.MangaDexApi
import com.example.myapplication.data.Chapter
import com.example.myapplication.database.ChapterDao
import com.example.myapplication.database.PageDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// ─── data/repository/ChapterRepository.kt ───────────────────

class ChapterRepository(
    private val chapterDao: ChapterDao,
    private val pageDao: PageDao,
    private val api: MangaDexApi
) {
    fun getChapters(mangaId: String): Flow<List<Chapter>> =
        chapterDao.getChaptersForManga(mangaId)

    suspend fun getChapterById(id: String): Chapter? =
        withContext(Dispatchers.IO) {
            chapterDao.getChapterById(id)
        }

    suspend fun getNextChapter(mangaId: String, currentNumber: Float): Chapter? =
        withContext(Dispatchers.IO) {
            chapterDao.getNextChapter(mangaId, currentNumber)
        }

    suspend fun getPreviousChapter(mangaId: String, currentNumber: Float): Chapter? =
        withContext(Dispatchers.IO) {
            chapterDao.getPreviousChapter(mangaId, currentNumber)
        }

    suspend fun syncChapters(mangaId: String, contentRating: List<String>): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                var offset = 0
                val allChapters = mutableListOf<Chapter>()
                do {
                    val response = api.getChapterFeed(
                        mangaId = mangaId,
                        limit = 100,
                        offset = offset,
                        language = listOf("en"),    // ← default lives here
                        order = "asc",
                        contentRating =contentRating
                    )
                    allChapters += response.data.map { it.toEntity(mangaId) }
                    offset += response.limit
                } while (offset < response.total)

                chapterDao.upsertChapters(allChapters)
            }
        }
}