package com.example.bagit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.bagit.R
import com.example.bagit.ui.components.BagItTopBar
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.*

/**
 * Pantalla que muestra el historial de listas de compras completadas.
 * Una lista se considera completada cuando TODOS sus productos están marcados como comprados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListHistoryScreen(
    onNavigateToList: (Long) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    viewModel: com.example.bagit.ui.viewmodel.ShoppingListViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    // Estado de búsqueda
    var searchQuery by remember { mutableStateOf("") }

    // Load shopping lists
    val listsState by viewModel.listsState
    val completedListsMap by viewModel.completedListsMap

    // Búsqueda inicial
    LaunchedEffect(Unit) {
        viewModel.getShoppingLists()
    }

    // Verificar completitud de listas cuando cambian
    LaunchedEffect(listsState) {
        if (listsState is com.example.bagit.data.repository.Result.Success) {
            viewModel.checkAllListsCompletion()
        }
    }

    // Búsqueda cuando cambia el query
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.getShoppingLists()
        } else {
            viewModel.getShoppingLists(name = searchQuery)
        }
    }

    val isLandscape = isLandscape()
    val isTablet = isTablet()
    val contentPadding = getContentPadding()

    Scaffold(
        topBar = {
            BagItTopBar(
                showMenu = true,
                onMenuClick = onOpenDrawer,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchSubmit = { /* Búsqueda en tiempo real */ }
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues)
                .navigationBarsPadding()
        ) {
            when (val state = listsState) {
                is com.example.bagit.data.repository.Result.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is com.example.bagit.data.repository.Result.Success -> {
                    // Filtrar solo listas COMPLETADAS (para "Shopping List History")
                    val completedLists = state.data.data.filter { list ->
                        viewModel.isListCompleted(list.id)
                    }

                    if (completedLists.isEmpty()) {
                        EmptyHistoryState(
                            contentPadding = contentPadding,
                            isTablet = isTablet
                        )
                    } else {
                        val viewMode by viewModel.preferencesRepository.productViewMode.collectAsState(initial = "list")
                        ShoppingListsContent(
                            lists = completedLists,
                            viewMode = viewMode,
                            onListClick = onNavigateToList,
                            onAddList = {},
                            onToggleFavorite = { listId, isFavorite ->
                                viewModel.toggleFavorite(listId, isFavorite)
                            },
                            isFavorite = { list -> viewModel.isFavorite(list) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                is com.example.bagit.data.repository.Result.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.shopping_history_error_loading),
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.getShoppingLists() }) {
                            Text(stringResource(R.string.common_retry))
                        }
                    }
                }
                null -> Unit
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(
    contentPadding: Dp,
    isTablet: Boolean
) {
    val illustrationSize = if (isTablet) 260.dp else 220.dp
    val iconSize = if (isTablet) 150.dp else 140.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(illustrationSize),
            shape = CircleShape,
            color = com.example.bagit.ui.theme.Cream
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = Color(0xFF2E2A3A),
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.shopping_history_no_completed),
            fontSize = if (isTablet) 26.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            color = OnDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.shopping_history_empty_hint),
            fontSize = if (isTablet) 18.sp else 16.sp,
            color = OnDark.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

