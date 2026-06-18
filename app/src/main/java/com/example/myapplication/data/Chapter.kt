package com.example.myapplication.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapter",
    foreignKeys = [
        ForeignKey(
            entity = Manga::class,
            parentColumns = ["id"],//The column being referenced in the parent
            childColumns = ["manga_id"],//The column in this table that holds the reference
            onDelete = CASCADE,//it says if the original row(id) of the Manga table gets deleted then this chapter table for that particular manga will also be deleted

        )
    ],
    indices = [Index("manga_id")]
)
data class Chapter(
    @PrimaryKey
    val id: String,//This chapter's own identity
    @ColumnInfo(name = "manga_id")
  val mangaId: String , //A pointer to the parent
//Stores the id of whichever Manga this chapter belongs to. Multiple chapters can share the same mangaId — that's the whole point. It's the answer to "which manga does this chapter belong to?"
    val chapterNumber: Float,
    val pageCount: Int,
    val publishedAt: Long,
    val title:String?, // as not every chapter in manga dex has title
    val isDownloaded: Boolean = false
)
