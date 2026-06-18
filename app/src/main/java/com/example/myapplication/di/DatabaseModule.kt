package com.example.myapplication.di

import androidx.room.Room
import com.example.myapplication.database.MIGRATION_1_2
import com.example.myapplication.database.MIGRATION_2_3
import com.example.myapplication.database.MIGRATION_3_4
import com.example.myapplication.database.MangaDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule= module{
    single{
        // THIS is where Room.databaseBuilder lives.
        // androidContext() is provided by Koin — it's your Application context.
        // Never use Activity context here — it causes memory leaks.
        Room.databaseBuilder(
            androidContext(),
            MangaDatabase::class.java,
            "manga.db"

        ).addMigrations(MIGRATION_1_2,MIGRATION_2_3, MIGRATION_3_4)
            .build()

    }
    single{get<MangaDatabase>().mangaDao()}
    single { get<MangaDatabase>().chapterDao() }
    single { get<MangaDatabase>().pageDao() }
    single { get<MangaDatabase>().readingProgressDao() }

}