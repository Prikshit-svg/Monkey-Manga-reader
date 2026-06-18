package com.example.myapplication.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    isAdultUnlocked: Boolean,
    onMangaClick: (mangaId: String, contentRating: String) -> Unit,
    vm: HomeViewModel= koinViewModel()
) {
    // collectAsStateWithLifecycle = stops collecting when app is
    // backgrounded. Saves battery. Never misses updates when foregrounded.
    // Use this ALWAYS instead of collectAsState().
    val mangaList by vm.manga.collectAsStateWithLifecycle()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val isLoadingMore by vm.isLoadingMore.collectAsStateWithLifecycle()
    val hasReachedEnd by vm.hasReachedEnd.collectAsStateWithLifecycle()

    // Grid scroll state — used to detect when user nears the bottom
    val gridState=rememberLazyGridState()

    LaunchedEffect(
        gridState
    ) {
        snapshotFlow {
            val lastVisible=gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?:0
            val total =gridState.layoutInfo.totalItemsCount
            lastVisible>=total-4
        }.distinctUntilChanged()
            .filter{it}
            .collect {
            vm.onLoadMore()
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = vm::onSearchQueryChanged
            ) 
        }
    ) {padding->
        when (uiState) {
            is HomeViewModel.UiState.Loading if mangaList.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is HomeViewModel.UiState.Error if mangaList.isEmpty() -> {
                val msg= (uiState as HomeViewModel.UiState.Error).message
                ErrorScreen(message = msg, onRetry = vm::syncManga)
            }

            else -> {
                MangaGrid(
                    mangaList,
                    gridState,
                    isLoadingMore,
                    hasReachedEnd,
                    onMangaClick,
                    Modifier.padding(padding)
                )
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
                    modifier=Modifier.fillMaxWidth().padding(16.dp),
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
      onSearchQueryChange: (String) -> Unit
  ){
      TopAppBar(
          title={
              OutlinedTextField(
                  value = searchQuery,
                  onValueChange = {onSearchQueryChange(it)},
                  placeholder={Text("Search manga")},
                  singleLine=true,
                  leadingIcon = {
                      Icon(
                          imageVector = Icons.Default.Search,
                          contentDescription = "Search"
                      )
                  },
                  trailingIcon = {
                      Icon(
                        imageVector=Icons.Default.Clear,
                          "Clear search"
                      )
                  },
                  modifier = Modifier.fillMaxWidth(),
                  colors = OutlinedTextFieldDefaults.colors(
                      unfocusedBorderColor = Color.Transparent,
                      focusedBorderColor = Color.LightGray
                  )

              )
          }
      )
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

    // 2. Initialize a dummy grid state
    val gridState = rememberLazyGridState()

    // 3. Call the MangaGrid with sample parameters
    MangaGrid(
        manga = sampleManga,
        gridState = gridState,
        isLoadingMore = true,      // Set to true to see the loading spinner at the bottom
        hasReachedEnd = false,
        onMangaClick = { mangaId,cR ->
            println("Clicked on $mangaId")
        },
        modifier = Modifier.fillMaxSize()
    )
}