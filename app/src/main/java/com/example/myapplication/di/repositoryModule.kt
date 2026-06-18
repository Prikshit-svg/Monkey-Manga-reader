package com.example.myapplication.di

import com.example.myapplication.repositories.ChapterRepository
import com.example.myapplication.repositories.MangaRepository
import com.example.myapplication.repositories.PageRepository
import com.example.myapplication.repositories.ReadingProgressRepository
import org.koin.core.scope.get
import org.koin.dsl.module

val repositoryModule = module {
    // get() resolves MangaDao, ChapterDao etc. from databaseModule
    single { MangaRepository(get(), get()) }
    single { ChapterRepository(get(), get(),get()) }
    single { ChapterRepository(get(),get(),get()) }
    single{ PageRepository(get(), get()) }
    single { ReadingProgressRepository(get(), get()) }
}
