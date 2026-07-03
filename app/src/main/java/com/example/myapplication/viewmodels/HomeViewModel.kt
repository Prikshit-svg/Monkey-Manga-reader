package com.example.myapplication.viewmodels

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Manga
import com.example.myapplication.repositories.MangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val mangaRepository: MangaRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val ADULT_UNLOCKED_KEY = booleanPreferencesKey("adult_unlocked")
        val FILTER_TIER_KEY    = stringPreferencesKey("filter_tier")
    }

    sealed class UiState {
        object Idle    : UiState()
        object Loading : UiState()
        data class Success(val manga: List<Manga>) : UiState()
        data class Error(val message: String)      : UiState()
    }

    // ── Pagination ────────────────────────────────────────────────
    private val _currentOffset  = MutableStateFlow(0)
    private val _totalManga     = MutableStateFlow(0)
    private val _isLoadingMore  = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val hasReachedEnd: StateFlow<Boolean> = combine(_currentOffset, _totalManga) { offset, total ->
        total in 1..offset
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ── UI state ──────────────────────────────────────────────────
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // ── Search ────────────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // ── Content rating ────────────────────────────────────────────
    private val _contentRating = MutableStateFlow(listOf("safe"))
    val contentRating: StateFlow<List<String>> = _contentRating.asStateFlow()

    // ── Adult unlock — persisted in DataStore ─────────────────────
    val isAdultUnlocked: StateFlow<Boolean> = dataStore.data
        .map { it[ADULT_UNLOCKED_KEY] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ── Manga list — derived from search + content rating ─────────
    private val _manga: StateFlow<List<Manga>> = combine(_searchQuery, _contentRating) { q, r ->
        Pair(q, r)
    }.flatMapLatest { (query, _) ->
        if (query.isEmpty()) mangaRepository.allManga
        else mangaRepository.searchManga(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val manga: StateFlow<List<Manga>> = _manga

    // ── Pending tier (set before age gate, applied after) ─────────
    private val _pendingTier = MutableStateFlow<String?>(null)

    init {
        // Restore saved filter tier from DataStore before first sync
        viewModelScope.launch {
            val savedTier = dataStore.data.first()[FILTER_TIER_KEY] ?: "Default"
            applyTierToRating(savedTier)
            syncManga()
        }
        // Debounced network search triggered by query changes
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotEmpty()) doSearchAndSync(query)
                    else _searchError.value = null
                }
        }
    }

    // ── Public API ────────────────────────────────────────────────

    fun syncManga() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                mangaRepository.syncManga(offset = _currentOffset.value, _contentRating.value)
                _uiState.value = UiState.Success(emptyList())
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onLoadMore() {
        if (_isLoadingMore.value || hasReachedEnd.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextOffset = _currentOffset.value + 20
            mangaRepository.syncNextPage(nextOffset, _contentRating.value)
                .onSuccess { total ->
                    _currentOffset.value = nextOffset
                    _totalManga.value    = total
                }
            _isLoadingMore.value = false
        }
    }

    fun refresh() {
        _currentOffset.value = 0
        _totalManga.value    = 0
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                mangaRepository.syncManga(0, _contentRating.value)
            } catch (_: Exception) { }
            _isRefreshing.value = false
        }
    }

    fun requestTier(tier: String) { _pendingTier.value = tier }

    fun applyPendingTier() {
        when (_pendingTier.value) {
            "suggestive"   -> unlockSuggestive()
            "erotica"      -> unlockEroticaOnly()
            "pornographic" -> unlock18plus()
        }
        _pendingTier.value = null
    }

    fun setAdultUnlocked(value: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[ADULT_UNLOCKED_KEY] = value }
        }
    }

    fun unlockSuggestive() {
        _contentRating.value = listOf("safe", "suggestive")
        persistTier("suggestive")
        refresh()
    }

    fun unlockEroticaOnly() {
        _contentRating.value = listOf("safe", "suggestive", "erotica")
        persistTier("erotica")
        refresh()
    }

    fun unlock18plus() {
        _contentRating.value = listOf("safe", "suggestive", "erotica", "pornographic")
        persistTier("pornographic")
        refresh()
    }

    fun relock() {
        _contentRating.value = listOf("safe")
        persistTier("Default")
        setAdultUnlocked(false)
        refresh()
    }

    // ── Private helpers ───────────────────────────────────────────

    private fun applyTierToRating(tier: String) {
        _contentRating.value = when (tier) {
            "suggestive"   -> listOf("safe", "suggestive")
            "erotica"      -> listOf("safe", "suggestive", "erotica")
            "pornographic" -> listOf("safe", "suggestive", "erotica", "pornographic")
            else           -> listOf("safe")
        }
    }

    private fun persistTier(tier: String) {
        viewModelScope.launch {
            dataStore.edit { it[FILTER_TIER_KEY] = tier }
        }
    }

    private suspend fun doSearchAndSync(query: String) {
        _isSearching.value  = true
        _searchError.value  = null
        mangaRepository.searchAndSync(query, _contentRating.value)
            .onFailure { _searchError.value = "Search failed — showing cached results." }
        _isSearching.value = false
    }
}
