package com.example.bagit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.R
import com.example.bagit.data.repository.Result
import com.example.bagit.ui.components.BagItTopBar
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.*
import com.example.bagit.ui.viewmodel.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateToList: (Long) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val listsState by viewModel.listsState
    var searchQuery by remember { mutableStateOf("") }

    // Load favorite lists
    LaunchedEffect(Unit) {
        viewModel.getFavoriteLists()
    }

    // Búsqueda cuando cambia el query
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.getFavoriteLists()
        } else {
            // Filtrar favoritos por nombre (client-side)
            viewModel.getFavoriteLists()
        }
    }

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
                is Result.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is Result.Success -> {
                    val favoriteLists = if (searchQuery.isNotBlank()) {
                        state.data.data.filter { 
                            it.name.contains(searchQuery, ignoreCase = true) 
                        }
                    } else {
                        state.data.data
                    }

                    if (favoriteLists.isEmpty()) {
                        EmptyFavoritesState(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding),
                            hasSearch = searchQuery.isNotBlank()
                        )
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(favoriteLists, key = { it.id }) { list ->
                                ShoppingListCard(
                                    list = list,
                                    onClick = { onNavigateToList(list.id) },
                                    onToggleFavorite = { listId, isFavorite ->
                                        viewModel.toggleFavorite(listId, isFavorite)
                                        // Recargar favoritos después de toggle
                                        viewModel.getFavoriteLists()
                                    },
                                    isFavorite = viewModel.isFavorite(list)
                                )
                            }
                        }
                    }
                }
                is Result.Error -> {
                    ErrorState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        onRetry = { viewModel.getFavoriteLists() }
                    )
                }
                null -> Unit
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState(
    modifier: Modifier = Modifier,
    hasSearch: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⭐",
            fontSize = 80.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(
                if (hasSearch) R.string.favorites_no_results_title
                else R.string.favorites_empty_title
            ),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = OnDark,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                if (hasSearch) R.string.favorites_no_results_subtitle
                else R.string.favorites_empty_subtitle
            ),
            fontSize = 16.sp,
            color = OnDark.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorState(
    modifier: Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.favorites_error_loading),
            color = Color.Red,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DarkNavy
            )
        ) {
            Text(stringResource(R.string.favorites_retry))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF171A26)
@Composable
fun FavoritesScreenPreview() {
    BagItTheme {
        FavoritesScreen()
    }
}

