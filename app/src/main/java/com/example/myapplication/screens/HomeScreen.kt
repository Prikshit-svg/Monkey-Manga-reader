package com.example.myapplication.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.myapplication.data.Manga
import com.example.myapplication.viewmodels.HomeViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navToAgeGate: () -> Unit,
    onSignOut: () -> Unit,
    isAdultUnlocked: Boolean,
    onMangaClick: (mangaId: String, contentRating: String) -> Unit,
    vm: HomeViewModel = koinViewModel()
) {
    // collectAsStateWithLifecycle = stops collecting when app is
    // backgrounded. Saves battery. Never misses updates when foregrounded.
    // Use this ALWAYS instead of collectAsState().
    val mangaList by vm.manga.collectAsStateWithLifecycle()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val searchQuery   by vm.searchQuery.collectAsStateWithLifecycle()
    val isLoadingMore by vm.isLoadingMore.collectAsStateWithLifecycle()
    val hasReachedEnd by vm.hasReachedEnd.collectAsStateWithLifecycle()
    val contentRating by vm.contentRating.collectAsStateWithLifecycle()
    val isRefreshing  by vm.isRefreshing.collectAsStateWithLifecycle()
    val isSearching   by vm.isSearching.collectAsStateWithLifecycle()
    val searchError   by vm.searchError.collectAsStateWithLifecycle()

    var showChecklist by remember { mutableStateOf(false) }
    val filterItems = listOf("Default", "suggestive", "erotica", "pornographic")

    fun tierFromRating(rating: List<String>) = when {
        "pornographic" in rating -> "pornographic"
        "erotica"      in rating -> "erotica"
        "suggestive"   in rating -> "suggestive"
        else                     -> "Default"
    }

    // Single selected item — drives the actual filter tier
    var selectedItem by remember { mutableStateOf(tierFromRating(contentRating)) }

    // Keep selectedItem in sync with actual ViewModel state (e.g. after returning from age gate)
    LaunchedEffect(contentRating) { selectedItem = tierFromRating(contentRating) }

    // Clear search query when leaving Home so it doesn't persist to other screens
    DisposableEffect(Unit) { onDispose { vm.onSearchQueryChanged("") } }

    // Apply pending tier once age gate verification completes
    LaunchedEffect(isAdultUnlocked) {
        if (isAdultUnlocked) vm.applyPendingTier()
    }

    // Grid scroll state — used to detect when user nears the bottom
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            lastVisible >= total - 4
        }.distinctUntilChanged()
            .filter { it }
            .collect { vm.onLoadMore() }
    }

    Column(Modifier.fillMaxSize()) {
        HomeTopBar(
            searchQuery = searchQuery,
            onSearchQueryChange = vm::onSearchQueryChanged,
            showFilter = showChecklist,
            onFilterClick = { showChecklist = !showChecklist },
            onDismissFilter = { showChecklist = false },
            filterItems = filterItems,
            selectedItem = selectedItem,
            isSearching = isSearching,
            onSignOut = onSignOut,
            onFilterItemSelected = { item ->
                showChecklist = false
                when (item) {
                    "Default" -> { selectedItem = "Default"; vm.relock() }
                    "suggestive" -> { selectedItem = "suggestive"; vm.unlockSuggestive() }
                    "erotica", "pornographic" -> {
                        if (isAdultUnlocked) {
                            selectedItem = item
                            if (item == "erotica") vm.unlockEroticaOnly() else vm.unlock18plus()
                        } else {
                            vm.requestTier(item)
                            navToAgeGate()
                        }
                    }
                }
            }
        )
        if (searchError != null) {
            Text(
                text = searchError!!,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        when (uiState) {
            is HomeViewModel.UiState.Loading if mangaList.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is HomeViewModel.UiState.Error if mangaList.isEmpty() -> {
                val msg = (uiState as HomeViewModel.UiState.Error).message
                ErrorScreen(message = msg, onRetry = vm::syncManga)
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = vm::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (mangaList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No results found")
                        }
                    } else {
                        MangaGrid(
                            mangaList,
                            gridState,
                            isLoadingMore,
                            hasReachedEnd,
                            onMangaClick,
                            Modifier
                                .padding(2.dp)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MangaGrid(
    manga: List<Manga>,
    gridState: LazyGridState,
    isLoadingMore: Boolean,
    hasReachedEnd: Boolean,
    onMangaClick: (mangaId:String,contentRating:String) -> Unit,
    modifier: Modifier = Modifier
){
    LazyVerticalGrid(
        columns= GridCells.Fixed(3),
        state = gridState,
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),

    ) {
        items(
            items = manga,
            key={it.id}
        ){manga->
            MangaCover(manga,{onMangaClick(manga.id,manga.contentRating)})
        }
        // Loading more indicator at bottom
        if (isLoadingMore) {
            item(span={ GridItemSpan(maxLineSpan)}) {
                Box(
                    modifier=Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator(Modifier.size(24.dp))
                }
            }
        }
    }

}

@Composable
fun MangaCover(
    manga:Manga,
    onMangaClicked : () -> Unit
){

        Card(
            onClick = onMangaClicked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(manga.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = manga.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                )

                // Gradient overlay for title readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                        .padding(6.dp)
                ) {
                    Text(
                        text = manga.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }



  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun HomeTopBar(
      searchQuery: String,
      onSearchQueryChange: (String) -> Unit,
      showFilter: Boolean,
      onFilterClick: () -> Unit,
      onDismissFilter: () -> Unit,
      filterItems: List<String>,
      selectedItem: String,
      isSearching: Boolean,
      onSignOut: () -> Unit,
      onFilterItemSelected: (String) -> Unit
  ) {
      Column {
          TopAppBar(
              title = {
                  OutlinedTextField(
                      value = searchQuery,
                      onValueChange = { onSearchQueryChange(it) },
                      placeholder = { Text("Search manga") },
                      singleLine = true,
                      leadingIcon = {
                          Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                      },
                      trailingIcon = {
                          if (searchQuery.isNotEmpty()) {
                              Icon(
                                  imageVector = Icons.Default.Clear,
                                  contentDescription = "Clear search",
                                  modifier = Modifier.clickable { onSearchQueryChange("") }
                              )
                          }
                      },
                      colors = OutlinedTextFieldDefaults.colors(
                          unfocusedBorderColor = Color.Transparent,
                          focusedBorderColor = Color.LightGray
                      ),
                      modifier = Modifier.fillMaxWidth()
                  )
              },
              actions = {
                  Box {
                      IconButton(onClick = onFilterClick) {
                          Icon(Icons.Default.FilterList, contentDescription = "Filter")
                      }
                      DropdownMenu(
                          expanded = showFilter,
                          onDismissRequest = onDismissFilter
                      ) {
                          filterItems.forEach { item ->
                              DropdownMenuItem(
                                  text = {
                                      Row(verticalAlignment = Alignment.CenterVertically) {
                                          RadioButton(
                                              selected = selectedItem == item,
                                              onClick = null
                                          )
                                          Text(
                                              text = item.replaceFirstChar { it.uppercase() },
                                              modifier = Modifier.padding(start = 8.dp)
                                          )
                                      }
                                  },
                                  onClick = { onFilterItemSelected(item) }
                              )
                          }
                      }
                  }
                  IconButton(onClick = onSignOut) {
                      Icon(Icons.Default.ExitToApp, contentDescription = "Sign out")
                  }
              }
          )
          if (isSearching) {
              LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
          }
      }
  }

@Preview(showBackground=true, showSystemUi = true)
@Composable
fun previewFunction(){
    val sampleManga = listOf(
        Manga(
            id = "1",
            title = "Jujutsu Kaisen",
            coverUrl = "",
            description = "",
            status = "Ongoing",
            contentRating = "10",
            author = "Gege Akutami",
            totalChapters = 250,
            lastUpdated = 0L
        ),
        Manga(
            id = "2",
            title = "One Piece",
            coverUrl = "",
            description = "",
            status = "Ongoing",
            contentRating = "10",
            author = "Eiichiro Oda",
            totalChapters = 1100,
            lastUpdated = 0L
        ),
        Manga(
            id = "3",
            title = "Chainsaw Man",
            coverUrl = "",
            description = "",
            status = "Ongoing",
            contentRating = "10",
            author = "Tatsuki Fujimoto",
            totalChapters = 150,
            lastUpdated = 0L
        )
    )

    val gridState = rememberLazyGridState()

    MangaGrid(
        manga = sampleManga,
        gridState = gridState,
        isLoadingMore = false,
        hasReachedEnd = false,
        onMangaClick = { _, _ -> },
    )
}