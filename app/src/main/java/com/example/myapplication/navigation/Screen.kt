package com.example.myapplication.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    // Main screens
    object Home : Screen("home")
    object Library : Screen("library")

    // Detail screens — take arguments
    object SeriesDetail : Screen("series/{mangaId}") {
        fun createRoute(mangaId: String) = "series/$mangaId"
    }

    object Reader : Screen("reader/{mangaId}/{chapterId}") {
        fun createRoute(mangaId: String, chapterId: String) =
            "reader/$mangaId/$chapterId"
    }

    // Age gate — shown before 18+ content
    object AgeGate : Screen("age_gate/{destination}") {
        fun createRoute(destination: String) =
            "age_gate/${Uri.encode(destination)}"   // encode in case destination has slashes
    }
}
// Bottom nav items — only screens shown in bottom bar
sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        screen = Screen.Home,
        label = "Home",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )
    object Library : BottomNavItem(
        screen = Screen.Library,
        label = "Library",
        icon = Icons.Outlined.BookmarkBorder,
        selectedIcon = Icons.Filled.Bookmark
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Library
)