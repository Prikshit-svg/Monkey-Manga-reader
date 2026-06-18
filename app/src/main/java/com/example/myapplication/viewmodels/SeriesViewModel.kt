package com.example.myapplication.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Chapter
import com.example.myapplication.data.Manga
import com.example.myapplication.data.ReadingProgress
import com.example.myapplication.repositories.ChapterRepository
import com.example.myapplication.repositories.MangaRepository
import com.example.myapplication.repositories.ReadingProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


// ─── ui/series/SeriesViewModel.kt ────────────────────────────

class SeriesViewModel(
    private val mangaRepository: MangaRepository,
    private val chapterRepository: ChapterRepository,
    private val progressRepository: ReadingProgressRepository,
    private val mangaId: String
) : ViewModel() {



    sealed class MangaDetailState {
        object Loading : MangaDetailState()
        data class Success(val manga: Manga) : MangaDetailState()
        data class Error(val message: String) : MangaDetailState()
    }

    sealed class ChapterListState {
        object Loading : ChapterListState()
        data class Success(val chapters: List<Chapter>) : ChapterListState()
        data class Error(val message: String) : ChapterListState()
    }



    private val _mangaDetailState = MutableStateFlow<MangaDetailState>(MangaDetailState.Loading)
    val mangaDetailState: StateFlow<MangaDetailState> = _mangaDetailState.asStateFlow()

    private val _chapterListState = MutableStateFlow<ChapterListState>(ChapterListState.Loading)
    val chapterListState: StateFlow<ChapterListState> = _chapterListState.asStateFlow()

    // Last read chapter — powers "Continue Reading" button
    private val _lastReadChapter = MutableStateFlow<ReadingProgress?>(null)
    val lastReadChapter: StateFlow<ReadingProgress?> = _lastReadChapter.asStateFlow()

    // Chapter sort order — user can toggle
    private val _sortDescending = MutableStateFlow(false)
    val sortDescending: StateFlow<Boolean> = _sortDescending.asStateFlow()

    // Chapters derived from sort order
    val chapters: StateFlow<List<Chapter>> = combine(
        _chapterListState,
        _sortDescending
    ) { state, descending ->
        if (state is ChapterListState.Success) {
            if (descending) state.chapters.sortedByDescending { it.chapterNumber }
            else state.chapters.sortedBy { it.chapterNumber }
        } else emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )



    init {
        loadMangaDetail()
        loadChapters()
        loadLastReadChapter()
    }



    private fun loadMangaDetail() {
        viewModelScope.launch {
            _mangaDetailState.value = MangaDetailState.Loading
            try {
                // First try local Room cache
                val local = mangaRepository.getMangaById(mangaId)
                if (local != null) {
                    _mangaDetailState.value = MangaDetailState.Success(local)
                }

                // Then sync fresh data from API in background
                mangaRepository.syncMangaById(mangaId).onFailure { e ->
                    // Only show error if we have nothing cached
                    if (local == null) {
                        _mangaDetailState.value =
                            MangaDetailState.Error(e.message ?: "Failed to load manga")
                    }
                }

                // After sync, reload from Room to show updated data
                val updated = mangaRepository.getMangaById(mangaId)
                if (updated != null) {
                    _mangaDetailState.value = MangaDetailState.Success(updated)
                }

            } catch (e: Exception) {
                _mangaDetailState.value =
                    MangaDetailState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    private fun loadChapters() {
        viewModelScope.launch {
            // Collect local chapters from Room as Flow
            chapterRepository.getChapters(mangaId).collect { chapters ->
                if (chapters.isNotEmpty()) {
                    _chapterListState.value = ChapterListState.Success(chapters)
                }
            }
        }

        // Sync chapters from API separately
        viewModelScope.launch {
            chapterRepository.syncChapters(mangaId).onFailure { e ->
                if (_chapterListState.value !is ChapterListState.Success) {
                    _chapterListState.value =
                        ChapterListState.Error(e.message ?: "Failed to load chapters")
                }
            }
        }
    }

    private fun loadLastReadChapter() {
        viewModelScope.launch {
            _lastReadChapter.value = progressRepository.getLastReadChapter(mangaId)
        }
    }



    fun toggleSortOrder() {
        _sortDescending.value = !_sortDescending.value
    }

    fun retryLoad() {
        loadMangaDetail()
        loadChapters()
    }
}