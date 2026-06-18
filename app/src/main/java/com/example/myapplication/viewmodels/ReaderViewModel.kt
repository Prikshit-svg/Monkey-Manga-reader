package com.example.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Page
import com.example.myapplication.repositories.ChapterRepository
import com.example.myapplication.repositories.PageRepository
import com.example.myapplication.repositories.ReadingProgressRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val pageRepository: PageRepository,
    private val chapterRepository: ChapterRepository,
    private val progressRepository: ReadingProgressRepository,
    private val mangaId: String,
    private val chapterId: String
): ViewModel() {
    sealed class UiState {
        object Loading : UiState()
        data class Success(val pages: List<Page>) : UiState()
        data class Error(val message: String) : UiState()
    }
private val _uiState= MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _nextChapterId = MutableStateFlow<String?>(null)
    val nextChapterId: StateFlow<String?> = _nextChapterId.asStateFlow()

    private val _prevChapterId = MutableStateFlow<String?>(null)
    val prevChapterId: StateFlow<String?> = _prevChapterId.asStateFlow()
    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> =_currentPageIndex.asStateFlow()

    // Debounce job — prevents hitting DB on every single page swipe
    private var saveProgressJob: Job? = null
    init{
        loadPages()
    }
    private fun loadPages(){
        viewModelScope.launch {
            _uiState.value= UiState.Loading
            try {
                // Step 1: load pages from Room or network
                val pages = pageRepository.getPagesForChapter(chapterId)

                // Step 2: resume from last saved page
                val progress=progressRepository.getProgress(mangaId,chapterId)
                _currentPageIndex.value=progress?.lastPageIndex?:0
                //Step 3:Load the adjacent chapters
                val currentChapter = chapterRepository.getChapterById(chapterId)
                if (currentChapter != null) {
                    _nextChapterId.value =
                        chapterRepository.getNextChapter(mangaId, currentChapter.chapterNumber)?.id
                    _prevChapterId.value =
                        chapterRepository.getPreviousChapter(mangaId, currentChapter.chapterNumber)?.id
                }
                _uiState.value = UiState.Success(pages)
            }catch (e: Exception){
                _uiState.value = UiState.Error(e.message ?: "Failed to load pages")
            }
        }
    }
    fun onPageChanged(pageIndex: Int) {
        _currentPageIndex.value = pageIndex

        // Cancel previous pending save and debounce by 500ms
        // so we don't write to DB on every single swipe
        saveProgressJob?.cancel()
        saveProgressJob = viewModelScope.launch {
            delay(500)
            progressRepository.saveProgress(
                mangaId = mangaId,
                chapterId = chapterId,
                pageIndex = pageIndex
            )
        }
    }
    fun onChapterCompleted(){
        viewModelScope.launch {
            progressRepository.markCompleted(mangaId,chapterId)
        }
    }
    fun retryLoad() = loadPages()
}