package com.example.myapplication.repositories

import com.example.myapplication.api.MangaDexApi
import com.example.myapplication.data.Page
import com.example.myapplication.database.ChapterDao
import com.example.myapplication.database.PageDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PageRepository(
    private val pageDao : PageDao,
    private val mangaApi: MangaDexApi
) {
    // Get pages from Room — called by reader screen
    // If pages exist locally (downloaded), uses those.
    // If not, falls back to network URLs.
    suspend fun getPagesForChapter(chapterId:String): List<Page> =
        withContext(Dispatchers.IO){
            val cached=pageDao.getPagesForChapter(chapterId)
            if (cached.isNotEmpty()) return@withContext cached

            // Not cached — fetch from MangaDex at-home server
            val response = mangaApi.getChapterPages(chapterId)
            val urls = response.chapter.getPageUrls(
                baseUrl = response.baseUrl,
                datasaver = false
            )
            val pages = urls.mapIndexed { index, url ->
                Page(
                    chapterId = chapterId,
                    pageIndex = index,
                    imageUrl = url
                )
            }
            pageDao.upsertPages(pages)
            pages

        }
    }
