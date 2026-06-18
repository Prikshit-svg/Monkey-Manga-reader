package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = Manga::class,
            parentColumns = ["id"],
            childColumns = ["mangaId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("mangaId"), Index("chapterId")]
)
data class ReadingProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mangaId: String,
    val chapterId: String,
    val lastPageIndex: Int,
    val isCompleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
