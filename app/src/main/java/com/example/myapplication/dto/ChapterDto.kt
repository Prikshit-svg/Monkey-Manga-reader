package com.example.myapplication.dto

import com.example.myapplication.data.Chapter
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChapterListResponse(
   val result : String,
   val data: List<ChapterDto>,
val limit : Int,
val offset : Int,
val total : Int,
)

@JsonClass(generateAdapter = true)
data class ChapterDto (
    val id:String,
    val type:String,
    val attributes:ChapterAttributes

){
    fun toEntity(mangaId: String): Chapter=
        Chapter(
            id = id,
            mangaId = mangaId,
            chapterNumber = attributes.chapter?.toFloatOrNull()?:0f,
            pageCount = attributes.pages,
            publishedAt = 0L, // parse from attributes.publishAt if needed
            title = attributes.title,
            isDownloaded = false,
        )
}
@JsonClass(generateAdapter = true)
data class ChapterAttributes (
    val title:String?,
    val chapter:String?,// MangaDex sends chapter number as String!
    val pages:Int,
    val publishAt:String?, // ISO 8601 date string
    val translatedLanguage:String

)
