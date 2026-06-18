package com.example.myapplication.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun BottomNavBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom bar on main screens
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Library.route
    )

    AnimatedVisibility(
        visible = showBottomBar,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        NavigationBar {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.screen.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(item.screen.route) {
                                // Pop up to Home so back stack doesn't grow
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = { Text(item.label) }
                )
            }
        }
    }
}