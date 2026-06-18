package com.example.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Manga
import com.example.myapplication.data.ReadingProgress
import com.example.myapplication.repositories.MangaRepository
import com.example.myapplication.repositories.ReadingProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Data class combining manga + its latest progress
data class LibraryItem(
    val manga: Manga,
    val progress: ReadingProgress?
)

class LibraryViewModel(
    private val progressRepository: ReadingProgressRepository,
    private val mangaRepository: MangaRepository
): ViewModel() {
    val inProgressManga: StateFlow<List<LibraryItem>> =progressRepository.getInProgressManga()
        .map{ progressList->
            progressList.mapNotNull{progress ->
                val manga=mangaRepository.getMangaById(progress.mangaId)
                manga?.let {
                    LibraryItem(it, progress = progress)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedManga: StateFlow<List<LibraryItem>> =progressRepository.getCompletedManga()
        .map{ progressList->
            progressList.mapNotNull{progress ->
                val manga=mangaRepository.getMangaById(progress.mangaId)
                manga?.let {
                    LibraryItem(it, progress = progress)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}