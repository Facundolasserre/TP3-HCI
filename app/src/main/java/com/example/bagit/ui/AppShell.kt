package com.example.bagit.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bagit.ui.components.BottomDest
import com.example.bagit.ui.components.BottomNavBar
import com.example.bagit.ui.screens.AccountSettingsScreen
import com.example.bagit.ui.screens.FavoritesScreen
import com.example.bagit.ui.screens.HomeScreen
import com.example.bagit.ui.screens.NewListScreen
import com.example.bagit.ui.products.ProductsRoute

@Composable
fun AppShell(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Determinar si mostrar bottom bar (solo en las 3 pantallas principales)
    val showBottomBar = currentRoute in listOf("home", "favorites", "account_settings")

    // Mapear ruta actual a BottomDest
    val selectedDest = when (currentRoute) {
        "favorites" -> BottomDest.Favorites
        "account_settings" -> BottomDest.Profile
        else -> BottomDest.Home
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    selected = selectedDest,
                    onSelect = { dest ->
                        when (dest) {
                            BottomDest.Home -> {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                            BottomDest.Favorites -> {
                                navController.navigate("favorites") {
                                    launchSingleTop = true
                                }
                            }
                            BottomDest.Profile -> {
                                navController.navigate("account_settings") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    onLogout = onLogout,
                    onNavigateToNewList = {
                        navController.navigate("new_list")
                    },
                    onNavigateToProducts = {
                        navController.navigate("products")
                    }
                )
            }

            composable("favorites") {
                FavoritesScreen()
            }

            composable("account_settings") {
                AccountSettingsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onSignOut = {
                        // TODO: route "login" when implemented
                        // For now, call the logout callback
                        onLogout()
                    }
                )
            }

            composable("new_list") {
                NewListScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onListCreated = {
                        // TODO: Refresh lists in HomeScreen after creation
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("products") {
                ProductsRoute(
                    onLogout = onLogout,
                    onNavigateToProducts = {
                        // Already on products screen
                    }
                )
            }
        }
    }
}

