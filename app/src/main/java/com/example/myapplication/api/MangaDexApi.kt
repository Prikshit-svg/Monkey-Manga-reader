package com.example.myapplication.api


import com.example.myapplication.dto.AtHomeResponse
import com.example.myapplication.dto.ChapterListResponse
import com.example.myapplication.dto.MangaListResponse
import com.example.myapplication.dto.MangaSingleResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MangaDexApi {

    @GET("manga")
    suspend fun getMangaList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("includes[]") includes: List<String>,
        @Query("contentRating[]") contentRating: List<String>,
        @Query("availableTranslatedLanguage[]") language: List<String>,
        @Query("hasAvailableChapters") hasAvailableChapters: Boolean,
        @Query("order[latestUploadedChapter]") order: String
    ): MangaListResponse

    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("includes[]") includes: List<String>,
        @Query("contentRating[]") contentRating: List<String>,
        @Query("availableTranslatedLanguage[]") language: List<String>,
        @Query("hasAvailableChapters") hasAvailableChapters: Boolean
    ): MangaListResponse

    @GET("manga/{id}")
    suspend fun getMangaById(
        @Path("id") id: String,
        @Query("includes[]") includes: List<String>
    ): MangaSingleResponse

    @GET("manga/{id}/feed")
    suspend fun getChapterFeed(
        @Path("id") mangaId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("translatedLanguage[]") language: List<String>,
        @Query("order[chapter]") order: String
    ): ChapterListResponse

    @GET("at-home/server/{chapterId}")
    suspend fun getChapterPages(
        @Path("chapterId") chapterId: String
    ): AtHomeResponse
}