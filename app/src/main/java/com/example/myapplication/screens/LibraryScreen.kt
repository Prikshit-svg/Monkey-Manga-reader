package com.example.myapplication.screens

import android.widget.TableRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.viewmodels.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.myapplication.viewmodels.LibraryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onMangaClick: (String) -> Unit,
    vm: LibraryViewModel = koinViewModel()
) {
    val inProgress by vm.inProgressManga.collectAsStateWithLifecycle()
    val completed by vm.completedManga.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("In Progress", "Completed")
    Scaffold(
        topBar = {TopAppBar(
          title = {Text("Library",fontWeight= FontWeight.Bold)}
        )}
    ){padding->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Tab row
            PrimaryTabRow (selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            //tab content
            when (selectedTab) {
                0 -> {
                    if (inProgress.isEmpty()) {
                        LibraryEmptyState(
                            message = "Nothing in progress yet.\nStart reading a manga!"
                        )
                    } else {
                        LibraryMangaList(
                            items = inProgress,
                            onMangaClick = onMangaClick
                        )
                    }
                }
                1 -> {
                    if (completed.isEmpty()) {
                        LibraryEmptyState(
                            message = "No completed manga yet."
                        )
                    } else {
                        LibraryMangaList(
                            items = completed,
                            onMangaClick = onMangaClick
                        )
                    }
                }
            }
        }
    }
}

// ─── LibraryMangaList ─────────────────────────────────────────

@Composable
fun LibraryMangaList(
    items: List<LibraryItem>,
    onMangaClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = items,
            key = { it.manga.id }
        ) { item ->
            LibraryMangaRow(
                item = item,
                onClick = { onMangaClick(item.manga.id) }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

// ─── LibraryMangaRow ──────────────────────────────────────────

@Composable
fun LibraryMangaRow(
    item: LibraryItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cover thumbnail
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.manga.coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.manga.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp, 80.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        // Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.manga.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.manga.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.progress != null) {
                Text(
                    text = "Ch. ${item.progress.chapterId} · " +
                            "Page ${item.progress.lastPageIndex + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── LibraryEmptyState ────────────────────────────────────────

@Composable
fun LibraryEmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}