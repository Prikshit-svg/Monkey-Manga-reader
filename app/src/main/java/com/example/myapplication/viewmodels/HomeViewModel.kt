package com.example.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Manga
import com.example.myapplication.repositories.MangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlin.collections.emptyList

class HomeViewModel(
    private val mangaRepository : MangaRepository
): ViewModel() {
    sealed class UiState {
        object Idle: UiState()
        object Loading : UiState()
        data class Success(val manga : List<Manga>) : UiState()
        data class Error(val message : String) : UiState()
    }
    // Pagination state
    private val _currentOffset = MutableStateFlow(0)
    private val _totalManga = MutableStateFlow(0)
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState : StateFlow<UiState> = _uiState.asStateFlow()

    // True when all pages have been loaded
    val hasReachedEnd: StateFlow<Boolean> = combine(
        _currentOffset,
        _totalManga
    ) { offset, total ->
        total in 1..offset
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Content rating filter — default shows safe + suggestive
    // When user unlocks 18+, "erotica" is added here
    private val _contentRating = MutableStateFlow(listOf("safe", "suggestive"))
    val contentRating: StateFlow<List<String>> = _contentRating.asStateFlow()
    // Search query state updated as user types
    private val _searchQuery = MutableStateFlow("")
    val searchQuery : StateFlow<String> = _searchQuery.asStateFlow()

    // Derived Flow — reacts to searchQuery changes automatically.
    // flatMapLatest cancels the previous Flow when query changes,
    // so you never get stale results from an old query.
    private val _manga : StateFlow<List<Manga>> =combine(_searchQuery,_contentRating){
        query,contentRating->
        Pair(query,contentRating)
    }
        .flatMapLatest { (query,contentRating) ->
        if (query.isEmpty())
            mangaRepository.allManga
        else
            mangaRepository.searchManga(query)
    }.stateIn(
        scope = viewModelScope,
        // WhileSubscribed(5000): keeps the Flow alive for 5 seconds
        // after the last collector disappears (e.g. screen rotation).
        // This prevents restarting the DB query on every rotation.
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),

        )
    val manga : StateFlow<List<Manga>> = _manga

    init {
        syncManga()
    }
    fun syncManga() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                mangaRepository.syncManga(offset = _currentOffset.value,
                    _contentRating.value)
                _uiState.value = UiState.Success(emptyList()) // Flow handles the data
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    fun onLoadMore(){
        if (_isLoadingMore.value||hasReachedEnd.value) return
        viewModelScope.launch{
            _isLoadingMore.value = true
            val nextOffset=_currentOffset.value+20
            mangaRepository.syncNextPage(nextOffset,_contentRating.value)
                .onSuccess{total->
                    _currentOffset.value=nextOffset
                    _totalManga.value=total
        }
                .onFailure {
                    // Don't show error for pagination — silently fail
                    // user can scroll up and back down to retry

                }
            _isLoadingMore.value=false

        }
    }

    // Called on pull-to-refresh
    fun refresh() {
        _currentOffset.value = 0
        _totalManga.value = 0
        syncManga()
    }

    fun unlock18plus(){
        _contentRating.value=listOf("safe","suggestive", "erotica")
        refresh()
    }

    fun searchAndSync(query: String){
        viewModelScope.launch {
            mangaRepository.searchAndSync(query).onFailure {
                // Search API failed — Room still shows any cached results
            }
        }
    }

}

