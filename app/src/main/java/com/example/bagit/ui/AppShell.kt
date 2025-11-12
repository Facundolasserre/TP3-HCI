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
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"

    // ✅ Mostrar bottom bar SOLO en estas rutas (excluye account_settings)
    val bottomBarRoutes = setOf("home", "favorites")
    val showBottomBar = currentRoute in bottomBarRoutes

    // Mapear ruta actual a BottomDest (si estás en account_settings no se muestra la bottom bar)
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
                                // Al tocar "Profile" navegás a una pantalla FULL y la bottom bar desaparece
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
                        onLogout()
                    }
                )
            }

            composable("new_list") {
                NewListScreen(
                    onBack = { navController.popBackStack() },
                    onListCreated = {
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
                    onNavigateToProducts = { /* ya estás en products */ }
                )
            }
        }
    }
}