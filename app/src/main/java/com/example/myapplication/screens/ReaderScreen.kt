package com.example.myapplication.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.myapplication.data.Page
import com.example.myapplication.viewmodels.ReaderViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.io.File

@Composable
fun ReaderScreen(
    mangaId: String,
    chapterId: String,
    onNavigateBack: () -> Unit,
    onNextChapter: (String) -> Unit,
    onPrevChapter: (String) -> Unit,
    vm: ReaderViewModel = koinViewModel(
        parameters = { parametersOf(mangaId, chapterId) }
)) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val currentPageIndex by vm.currentPageIndex.collectAsStateWithLifecycle()
    val nextChapterId by vm.nextChapterId.collectAsStateWithLifecycle()
    val prevChapterId by vm.prevChapterId.collectAsStateWithLifecycle()
    //keep screen on while the user is reading
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }


    //to enter immersive mode while reading(hiding system bars)
    val window = (view.context as? Activity)?.window
    if (window != null) {
        val windowInsetsController = WindowCompat.getInsetsController(window, view)
        LaunchedEffect(Unit) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        DisposableEffect(Unit) {
            onDispose {
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Tap to toggle top/bottom bar visibility
    var barsVisible by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { barsVisible = !barsVisible }
                )
            }


    )
    {
        when (val state = uiState) {
            is ReaderViewModel.UiState.Error -> {
                ReaderErrorScreen(
                    message = state.message,
                    onRetry = vm::retryLoad,
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier.align(Alignment.Center)
                )

            }

            ReaderViewModel.UiState.Loading -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()){
                    CircularProgressIndicator()
                }
            }

            is ReaderViewModel.UiState.Success -> {
                val pages = state.pages
                if (pages.isEmpty()) {
                    Text(
                        text = "No pages found",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    return@Box
                }
                val pagerState = rememberPagerState(
                    initialPage = currentPageIndex.coerceIn(0, pages.size - 1),
                    pageCount = { pages.size }
                )
                LaunchedEffect(pagerState.currentPage) {
                    vm.onPageChanged(pagerState.currentPage)
                }
                LaunchedEffect(pagerState.currentPage) {
                    if (pagerState.currentPage == pages.size - 1) {
                        vm.onChapterCompleted()
                    }

                }
                HorizontalPager(
                    pagerState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    beyondViewportPageCount=2
                ) { pageIndex ->
                    MangaPageItem(
                        page = pages[pageIndex],
                        pageIndex = pageIndex,
                        totalPages = pages.size
                    )
                }
                // Top bar
                AnimatedVisibility(
                    visible = barsVisible,
                    enter = fadeIn() + slideInVertically { -it },
                    exit = fadeOut() + slideOutVertically { -it },
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    ReaderTopBar(
                        currentPage = pages.size - pagerState.currentPage,
                        totalPages = pages.size,
                        onNavigateBack = onNavigateBack
                    )
                }

                // ── Bottom bar ───────────────────────────────
                AnimatedVisibility(
                    visible = barsVisible,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    ReaderBottomBar(
                        currentPage = pages.size - pagerState.currentPage,
                        totalPages = pages.size,
                        pagerState = pagerState,
                        onPrevChapter = onPrevChapter,
                        onNextChapter = onNextChapter,
                        nextChapterId=nextChapterId,
                        prevChapterId=prevChapterId
                    )
                }
            }
        }
    }
}

@Composable
fun MangaPageItem(page : Page, pageIndex : Int, totalPages : Int) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformation= rememberTransformableState{zoomChange, panChange, _ ->
        scale=(scale*zoomChange).coerceIn(1f,5f)
        offset=if(scale>1f) offset+panChange else Offset.Zero
    }
    var zoomIn by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .transformable(transformation)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        zoomIn = !zoomIn
                        if (zoomIn) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 5f
                        }
                    }
                )
            },contentAlignment = Alignment.Center
    ){
        AsyncImage(
            model= ImageRequest.Builder(LocalContext.current)
                .data(
                    page.localPath?.let { File(it) } ?: page.imageUrl
                )
                .crossfade(true)
                .diskCacheKey(page.imageUrl)
                .memoryCacheKey(page.imageUrl)
                .build(),
            contentDescription = "Page ${pageIndex + 1} of $totalPages",
            contentScale = ContentScale.Fit,
            modifier=Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

// ReaderTopBar
@Composable
fun ReaderTopBar(
    currentPage: Int,
    totalPages: Int,
    onNavigateBack: () -> Unit
){
Box(Modifier
    .fillMaxWidth()
    .background(
        Brush.verticalGradient(
            colors = listOf(Color.Black.copy(0.7f), Color.Transparent)
        )
    )){

        // Back button
        IconButton(
            onClick = {onNavigateBack()},
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black.copy(alpha = 0.6f)
            )
        }

        // Page counter — RTL so counts down as you read
        Text(
            text = "$currentPage / $totalPages",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
    fun ReaderErrorScreen(
        message : String,
        onRetry : () -> Unit,
        onNavigateBack : () -> Unit,
        modifier : Modifier = Modifier
    ) {
        Column(
            modifier = modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Failed to load pages",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text("Go Back")
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text("Retry", color = Color.Black)
                }
            }


        }
    }

// ─── ReaderBottomBar ──────────────────────────────────────────

@Composable
fun ReaderBottomBar(
    currentPage : Int,
    totalPages : Int,
    pagerState : PagerState,
    onPrevChapter : (String) -> Unit,
    onNextChapter : (String) -> Unit,
    nextChapterId : String?,
    prevChapterId : String?
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    )
    {
        // Page slider — tap or drag to jump to any page
        // reverseLayout = true so slider moves right-to-left
        // matching the RTL reading direction
        Slider(
            value = (totalPages - currentPage).toFloat(),
            onValueChange = { newValue ->
                val targetPage =  newValue.toInt()
                scope.launch {
                    pagerState.animateScrollToPage(
                        targetPage.coerceIn(0, totalPages - 1)
                    )
                }
            },
            valueRange = 0f..totalPages.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Prev / Next chapter buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { onPrevChapter(prevChapterId?:"") },enabled = prevChapterId != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Prev Chapter", color = Color.White, fontSize = 12.sp)
            }
            TextButton(onClick = { onNextChapter(nextChapterId?:"") },enabled = nextChapterId != null) {
                Text("Next Chapter", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun PreviewFunctions(){
//   /* Box(
//        modifier=Modifier
//            .fillMaxSize()
//            .background(Color.Black)){
//    ReaderErrorScreen(
//        "hello hello",
//        {},
//        {},
//        modifier=Modifier.fillMaxSize()
//    )}
//    ReaderTopBar(3,10,{})*/
//    val pagerState = rememberPagerState(
//        initialPage = 0,
//        pageCount = { 10 }
//    )
//    ReaderBottomBar(
//        currentPage = 3,
//        totalPages = 10,
//        pagerState =pagerState,
//        onPrevChapter = {  }
//    ) { }}