package com.example.myapplication.repositories

import com.example.myapplication.api.MangaDexApi
import com.example.myapplication.data.Manga
import com.example.myapplication.database.MangaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class MangaRepository(
    private val mangaDao: MangaDao,
    private val api: MangaDexApi
) {

    val allManga: Flow<List<Manga>> = mangaDao.getAllManga()

    fun searchManga(query: String): Flow<List<Manga>> =
        mangaDao.searchManga(query)

    suspend fun getMangaById(id: String): Manga? =
        withContext(Dispatchers.IO) {
            mangaDao.getMangaById(id)
        }

    fun getMangaByRating(rating: String): Flow<List<Manga>> =
        mangaDao.getMangaByRating(rating)

    suspend fun syncManga(
        offset: Int = 0,
        contentRating: List<String> = listOf("safe", "suggestive")
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getMangaList(
                limit = 20,
                offset = offset,
                includes = listOf("cover_art", "author"),
                contentRating = contentRating,
                language = listOf("en"),            // ← defaults live here now
                hasAvailableChapters = true,
                order = "desc"
            )
            val entities = response.data.map { it.toEntity() }
            mangaDao.upsertAll(entities)
        }
    }

    suspend fun syncMangaById(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.getMangaById(
                    id = id,
                    includes = listOf("cover_art", "author")
                )
                mangaDao.upsertManga(response.data.toEntity())
            }
        }

    suspend fun searchAndSync(query: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.searchManga(
                    title = query,
                    limit = 20,
                    offset = 0,
                    includes = listOf("cover_art", "author"),
                    contentRating = listOf("safe", "suggestive"),
                    language = listOf("en"),
                    hasAvailableChapters = true
                )
                val entities = response.data.map { it.toEntity() }
                mangaDao.upsertAll(entities)
            }
        }

    suspend fun syncNextPage(
        offset: Int,
        contentRating: List<String> = listOf("safe", "suggestive")
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getMangaList(
                limit = 20,
                offset = offset,
                includes = listOf("cover_art", "author"),
                contentRating = contentRating,
                language = listOf("en"),
                hasAvailableChapters = true,
                order = "desc"
            )
            val entities = response.data.map { it.toEntity() }
            mangaDao.upsertAll(entities)
            response.total
        }
    }

    suspend fun deleteManga(manga: Manga) =
        withContext(Dispatchers.IO) {
            mangaDao.deleteManga(manga)
        }
}