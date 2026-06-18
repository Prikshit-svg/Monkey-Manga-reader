package com.example.myapplication.repositories

import com.example.myapplication.data.ReadingProgress
import com.example.myapplication.database.MangaDao
import com.example.myapplication.database.ReadingProgressDao
import com.example.myapplication.viewmodels.LibraryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class ReadingProgressRepository(
    private val readingProgressDao: ReadingProgressDao,
    private val mangaDao: MangaDao
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun saveProgress(mangaId: String, chapterId: String, pageIndex: Int)
        {
            withContext(Dispatchers.IO) {
                readingProgressDao.saveProgress(
                    ReadingProgress(
                       mangaId =  mangaId,
                        chapterId = chapterId,
                        lastPageIndex = pageIndex,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    suspend fun markMangaCompleted(mangaId: String)=readingProgressDao.markMangaCompleted(mangaId)

    suspend fun clearProgressForManga(mangaId: String)=readingProgressDao.clearProgressForManga(mangaId)

    suspend fun getProgress(mangaId: String, chapterId: String): ReadingProgress?=readingProgressDao.getProgress(mangaId,chapterId)
    suspend fun markCompleted(mangaId: String, chapterId: String) =
        withContext(Dispatchers.IO) {
            readingProgressDao.markChapterCompleted(mangaId, chapterId)
        }
    suspend fun getLastReadChapter(mangaId: String) =
        withContext(Dispatchers.IO) {
            readingProgressDao.getLastReadChapter(mangaId)
        }

    fun getInProgressManga(): Flow<List<ReadingProgress>> =
        readingProgressDao.getInProgressManga()


    fun getCompletedManga(): Flow<List<ReadingProgress>> =
        readingProgressDao.getCompletedManga()


}
