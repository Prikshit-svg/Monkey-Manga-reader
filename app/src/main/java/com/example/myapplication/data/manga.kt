package com.example.myapplication.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "manga",
    indices = [Index(value = ["title"])]
)
data class Manga(
    @PrimaryKey
    val id: String,  // MangaDex UUID e.g. "a96676e5-8ae2-425e-b549-7f15dd34a6d8"

    @ColumnInfo("cover_url")
    val coverUrl: String,//The Kotlin field is camelCase (coverUrl), but the DB column will be
    // snake_case (cover_url). Without this annotation, Room would name the column coverUrl
    // — which works, but breaks SQL conventions.

    val title: String,
    val description: String,
    val status: String,
    @ColumnInfo("content_rating")
    val contentRating: String  ,// ⚠️ column will literally be named "contentRating"
    val author: String,

    @ColumnInfo(defaultValue="0")
val totalChapters: Int = 0,

    @ColumnInfo("last_updated")
    val lastUpdated: Long
)
