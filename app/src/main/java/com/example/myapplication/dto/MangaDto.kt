package com.example.myapplication.dto


import com.example.myapplication.data.Manga
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MangaListResponse(
    val result:String,
    val data: List<MangaDto>,
    val limit: Int,
    val offset:Int,
    val total:Int, // total available — used for pagination

    )

// Single manga wrapper — for /manga/{id}
@JsonClass(generateAdapter = true)
data class MangaSingleResponse(
    val result:String,
    val data: MangaDto
)

@JsonClass(generateAdapter = true)
data class MangaDto(
    val id: String,
    val type: String,
    val attributes: MangaAttributes,
    val relationships: List<Relationship>
){
    // Convenience function — find cover filename from relationships
    fun getCoverFileName(): String? =
        relationships.firstOrNull{it.type=="cover_art"}?.attributes?.fileName

    // Build full cover URL from filename
    // MangaDex cover URL format:
    // https://uploads.mangadex.org/covers/{mangaId}/{fileName}
    fun getCoverUrl():String{
        val fileName=getCoverFileName()?:return ""

        return "https://uploads.mangadex.org/covers/${id}/$fileName"
    }
    // Map DTO → Room Entity
    // This keeps your data layer clean — entities never touch DTOs
    fun toEntity(): Manga=Manga(
        id = id,
        coverUrl = getCoverUrl(),
        title = attributes.title["en"]?:attributes.title.values.firstOrNull()?:"Unknown title",
        description = attributes.description["en"]?:attributes.description.values.firstOrNull()?:"",
        status = attributes.status,
        contentRating = attributes.contentRating,
        author = relationships.firstOrNull{it.type=="author"}?.attributes?.name?:"unknown author",

        lastUpdated = System.currentTimeMillis()
    )
}
@JsonClass(generateAdapter=true)
data class MangaAttributes(
    // MangaDex titles are a map of language code → title
    // e.g. { "en": "One Piece", "ja": "ワンピース" }
    val title:Map<String,String>,
    val description: Map<String, String>,
    val status: String,                     // "ongoing" | "completed" | "hiatus"
    val contentRating: String,              // "safe" | "suggestive" | "erotica"
    val lastChapter: String?,               // nullable — ongoing manga won't have this
    val lastVolume: String?
)

@JsonClass(generateAdapter=true)
data class Relationship(
    val id:String,
    val type:String,// for author/artist
    val attributes:RelationshipAttributes?// for cover_art
)

data class RelationshipAttributes(
    val name: String?,                      // for author/artist
    val fileName: String?                   // for cover_art
)
