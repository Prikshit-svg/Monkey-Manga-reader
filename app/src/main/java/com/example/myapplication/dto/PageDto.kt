package com.example.myapplication.dto

import com.squareup.moshi.JsonClass

// Response from GET /at-home/server/{chapterId}
// This is different from other endpoints — no "data" wrapper

@JsonClass(generateAdapter = true)
data class AtHomeResponse(
    val result:String,
    val baseUrl:String,
    val chapter:AtHomeChapter

)

@JsonClass(generateAdapter = true)
data class AtHomeChapter(
    val hash:String,
    val data:List<String>,
    val dataSaver:List<String>,

){
    // Build full image URLs from base URL + hash + filename
    // Full quality:    {baseUrl}/data/{hash}/{filename}
    // Data saver:      {baseUrl}/data-saver/{hash}/{filename}
    fun getPageUrls(baseUrl:String,datasaver:Boolean=false):List<String>{
        val files = if (datasaver) dataSaver else data
        val quality = if (datasaver) "data-saver" else "data"
        return files.map { fileName ->
            "$baseUrl/$quality/$hash/$fileName"
        }
    }
}