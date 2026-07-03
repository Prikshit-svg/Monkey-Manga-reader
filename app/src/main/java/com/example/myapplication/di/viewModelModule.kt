package com.example.myapplication.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.myapplication.GoogleAuthViewModel
import com.example.myapplication.viewmodels.HomeViewModel
import com.example.myapplication.viewmodels.LibraryViewModel
import com.example.myapplication.viewmodels.ReaderViewModel
import com.example.myapplication.viewmodels.SeriesViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule= module {
    // viewModel { } is Koin's special scope for ViewModels.
    // It ties the ViewModel lifecycle to the Compose NavBackStackEntry
    // or Activity — auto-cleared when the screen leaves the backstack.
    viewModel { HomeViewModel(get(), get<DataStore<Preferences>>()) }
    viewModel { GoogleAuthViewModel(authTokenStore = get(),application = androidApplication()) }
    viewModel { (mangaId: String,contentRating:List<String>) -> SeriesViewModel(get(),
        get(),
        get(),
        mangaId,contentRating) }
    viewModel { (mangaId: String, chapterId: String) ->
        ReaderViewModel(get(),
            get(),
            get(),
            mangaId, chapterId)
    }
    viewModel{
        LibraryViewModel(get(),get())
    }

}