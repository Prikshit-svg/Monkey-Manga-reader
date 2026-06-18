package com.example.myapplication.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.myapplication.data.Manga
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga ORDER BY last_updated DESC")
    fun getAllManga(): Flow<List<Manga>>

    @Query("SELECT * FROM manga WHERE id = :id")
    fun getMangaById(id:String):Manga?//The :id syntax is Room's parameter binding — it safely injects the id argument into the query

    @Query("SELECT * FROM manga WHERE title LIKE '%' || :query || '%'")
    fun searchManga(query:String):Flow<List<Manga>>

    @Query("SELECT * FROM manga WHERE content_rating = :rating ORDER BY title DESC")
    fun getMangaByRating(rating: String): Flow<List<Manga>>

    // --- WRITES ---
    // @Upsert = INSERT OR REPLACE — inserts if new, updates if ID already exists
    // Perfect for syncing from API without checking existence first
    @Upsert
    suspend fun upsertManga(manga:Manga)//used for fetching manga from api and storing them into local db

    @Upsert
    suspend fun upsertAll(manga: List<Manga>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIfDoesNotExist(manga: Manga)//Prevent Duplicate Downloads

    @Update
    suspend fun updateManga(manga: Manga)//Used for:
    //favorites
//    watched/unwatched
//    downloaded
//    read/unread
//Progress Updates

    @Delete
    suspend fun deleteManga(manga : Manga)

    @Query("DELETE FROM manga WHERE id = :id")
    suspend fun deleteMangaById(id : String)



}