package com.example.bagit.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bagit.ui.components.BottomDest
import com.example.bagit.ui.components.BottomNavBar
import com.example.bagit.ui.screens.AccountSettingsRoute
import com.example.bagit.ui.screens.FavoritesScreen
import com.example.bagit.ui.screens.HomeScreen
import com.example.bagit.ui.screens.ProfileRoute
import com.example.bagit.lists.ListDetailScreen
import com.example.bagit.lists.NewListScreen
import com.example.bagit.members.ShareMembersScreen
import com.example.bagit.ui.products.ProductsRoute

@Composable
fun AppShell(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"

    // ✅ Mostrar bottom bar en estas rutas
    val bottomBarRoutes = setOf("home", "favorites", "products", "profile")
    val showBottomBar = currentRoute in bottomBarRoutes

    // Mapear ruta actual a BottomDest
    val selectedDest = when (currentRoute) {
        "favorites" -> BottomDest.Favorites
        "profile" -> BottomDest.Profile
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
                                navController.navigate("profile") {
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
                    onNavigateToList = { listId ->
                        navController.navigate("list_detail/$listId")
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
                AccountSettingsRoute(
                    onBack = {
                        navController.popBackStack()
                    },
                    onSignOut = {
                        onLogout()
                    },
                    onAccountDeleted = {
                        onLogout()
                    }
                )
            }

            composable("new_list") {
                NewListScreen(
                    onBack = { navController.popBackStack() },
                    onListCreated = { listId ->
                        navController.navigate("list_detail/$listId") {
                            popUpTo("home") {
                                inclusive = false
                            }
                        }
                    },
                    onShareList = { listName ->
                        navController.navigate("share_members/0/${listName.replace(" ", "_")}")
                    }
                )
            }

            composable(
                route = "list_detail/{listId}",
                arguments = listOf(navArgument("listId") { type = NavType.LongType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getLong("listId") ?: return@composable
                ListDetailScreen(
                    listId = listId,
                    onBack = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onShareMembers = { listId, listName ->
                        navController.navigate("share_members/$listId/${listName.replace(" ", "_")}")
                    }
                )
            }

            composable(
                route = "share_members/{listId}/{listName}",
                arguments = listOf(
                    navArgument("listId") { type = NavType.LongType },
                    navArgument("listName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getLong("listId") ?: return@composable
                val listName = backStackEntry.arguments?.getString("listName")?.replace("_", " ") ?: ""
                ShareMembersScreen(
                    listId = listId,
                    listName = listName,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("products") {
                ProductsRoute(
                    onLogout = onLogout,
                    onNavigateToProducts = { /* ya estás en products */ }
                )
            }

            composable("profile") {
                ProfileRoute(
                    onBack = {
                        navController.popBackStack()
                    },
                    onSettingsAction = {
                        navController.navigate("account_settings")
                    }
                )
            }
        }
    }
}