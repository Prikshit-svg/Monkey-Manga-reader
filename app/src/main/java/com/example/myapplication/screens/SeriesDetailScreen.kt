package com.example.myapplication.screens

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.myapplication.data.Chapter
import com.example.myapplication.data.Manga
import com.example.myapplication.data.ReadingProgress
import com.example.myapplication.navigation.Screen
import com.example.myapplication.viewmodels.SeriesViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    mangaId: String,
    onChapterClick: (mangaId: String, chapterId: String) -> Unit,
    onNavigateBack: () -> Unit,
    vm: SeriesViewModel = koinViewModel(
        parameters = { parametersOf(mangaId) }
    ),
    isAdultUnlocked: Boolean,
    onAdultContentBlocked: (destination: String) -> Unit
) {
    val mangaDetailState by vm.mangaDetailState.collectAsStateWithLifecycle()
    val chapters by vm.chapters.collectAsStateWithLifecycle()
    val chapterListState by vm.chapterListState.collectAsStateWithLifecycle()
    val lastReadChapter by vm.lastReadChapter.collectAsStateWithLifecycle()
    val sortDescending by vm.sortDescending.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (mangaDetailState is SeriesViewModel.MangaDetailState.Success) {
                        Text(
                            text = (mangaDetailState as SeriesViewModel.MangaDetailState.Success).manga.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = mangaDetailState) {

            is SeriesViewModel.MangaDetailState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is SeriesViewModel.MangaDetailState.Error -> {
                Box(Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center) {
                    ErrorScreen(state.message, vm::retryLoad
                    )
                }
            }

            is SeriesViewModel.MangaDetailState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    item {
                        MangaDetailHeader(
                            manga = state.manga,
                            lastReadChapter = lastReadChapter,
                            onContinueReading = {
                                lastReadChapter?.let {
                                    onChapterClick(mangaId, it.chapterId)
                                }
                            },
                        )
                    }
                    // Chapter List Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically

                        ) {
                            Text(
                                text = "${chapters.size} Chapters",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = vm::toggleSortOrder
                            ) {
                                Icon(
                                    imageVector = if (sortDescending) Icons.Default.ArrowUpward
                                    else Icons.Default.ArrowDownward,

                                    contentDescription = "toggle sort order"
                                )

                            }
                        }
                    }
                    //Chapter List Loading
                    if (chapterListState is SeriesViewModel.ChapterListState.Loading
                        && chapters.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }

                        }
                    }
                    items(
                        items=chapters,
                        key={it.id}
                    ){chapter->
                        ChapterListItem(
                            chapter = chapter,
                            isLastRead = lastReadChapter?.chapterId == chapter.id,
                            onClick = {
val manga= (mangaDetailState as? SeriesViewModel.MangaDetailState.Success)?.manga
                                val destination = Screen.Reader.createRoute(mangaId, chapter.id)
                                if(manga?.contentRating=="erotica"&& !isAdultUnlocked){
                                    onAdultContentBlocked(destination)
                                }else{
                                    onChapterClick(mangaId, chapter.id)
                                }
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }


    }
}

@Composable
fun MangaDetailHeader(
    manga: Manga,
    lastReadChapter: ReadingProgress?,
    onContinueReading: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth()
    )
    {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = manga.coverUrl,
                contentDescription =manga.title,
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp))

            )
            Column(Modifier.fillMaxWidth(),verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                Text(
                    manga.title, style = MaterialTheme.typography.titleLarge,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 6.dp)

                )
                Text(
                    text = manga.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Status chip
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = manga.status.replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp
                        )
                    }
                )
                // Content rating chip
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = manga.contentRating.replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp
                        )
                    }
                )

            }

        }
        // Description
        Text(
            text = manga.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Continue reading button — only shown if user has progress
        if (lastReadChapter != null) {
            Button(
                onClick = onContinueReading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Continue Reading — Ch. ${lastReadChapter.chapterId}")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        HorizontalDivider()
    }
}

@Composable
fun ChapterListItem(
    chapter: Chapter,
    isLastRead: Boolean,
    onClick: () -> Unit
){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                // Highlight the last read chapter
                if (isLastRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Chapter ${chapter.chapterNumber}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isLastRead) FontWeight.Bold else FontWeight.Normal
            )
            if (!chapter.title.isNullOrBlank()) {
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Downloaded indicator
            if (chapter.isDownloaded) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Downloaded",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            // Last read indicator
            if (isLastRead) {
                Text(
                    text = "Last read",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

}

