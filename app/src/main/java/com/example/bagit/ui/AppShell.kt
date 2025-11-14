package com.example.bagit.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bagit.ui.components.BottomDest
import com.example.bagit.ui.components.BottomNavBar
import com.example.bagit.ui.components.DrawerContent
import com.example.bagit.ui.screens.AccountSettingsRoute
import com.example.bagit.ui.screens.FavoritesScreen
import com.example.bagit.ui.screens.HomeScreen
import com.example.bagit.ui.screens.ProfileRoute
import com.example.bagit.lists.ListDetailScreen
import com.example.bagit.lists.NewListScreen
import com.example.bagit.members.ShareMembersScreen
import com.example.bagit.ui.products.ProductsRoute
import com.example.bagit.ui.screens.ShoppingHistoryScreen
import com.example.bagit.ui.theme.DrawerBg
import com.example.bagit.ui.theme.OnDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ✅ Mostrar bottom bar en estas rutas
    val bottomBarRoutes = setOf("home", "favorites", "products", "profile")
    val showBottomBar = currentRoute in bottomBarRoutes

    // Mapear ruta actual a BottomDest
    val selectedDest = when (currentRoute) {
        "favorites" -> BottomDest.Favorites
        "profile" -> BottomDest.Profile
        else -> BottomDest.Home
    }

    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }

    fun closeDrawerAnd(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 280.dp, max = 360.dp),
                drawerContainerColor = DrawerBg,
                drawerContentColor = OnDrawer,
                drawerShape = RectangleShape
            ) {
                DrawerContent(
                    onSignOut = {
                        closeDrawerAnd { onLogout() }
                    },
                    onNavigateToProducts = {
                        closeDrawerAnd {
                            navController.navigate("products") {
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToLists = {
                        closeDrawerAnd {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToHistory = {
                        closeDrawerAnd {
                            navController.navigate("shopping_history") {
                                launchSingleTop = true
                            }
                        }
                    },
                    onSettingsClick = {
                        closeDrawerAnd {
                            navController.navigate("account_settings") {
                                launchSingleTop = true
                            }
                        }
                    },
                    onToggleLanguage = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
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
                        onNavigateToNewList = {
                            navController.navigate("new_list")
                        },
                        onNavigateToList = { listId ->
                            navController.navigate("list_detail/$listId")
                        },
                        onOpenDrawer = openDrawer
                    )
                }

                composable("favorites") {
                    FavoritesScreen(
                        onNavigateToList = { listId ->
                            navController.navigate("list_detail/$listId")
                        },
                        onOpenDrawer = openDrawer
                    )
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
                        onOpenDrawer = openDrawer
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

                composable("shopping_history") {
                    ShoppingHistoryScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}