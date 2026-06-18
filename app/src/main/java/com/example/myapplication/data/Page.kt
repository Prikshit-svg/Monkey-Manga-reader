package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    "pages",
    foreignKeys = [
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],indices = [Index("chapterId")]

)
data class Page(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chapterId: String,
    val pageIndex: Int,
    val imageUrl: String,
val localPath:String?=null
    )
