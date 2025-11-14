// app/src/main/java/com/example/bagit/ui/screens/HomeScreen.kt
package com.example.bagit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.toColorInt
import com.example.bagit.R
import com.example.bagit.ui.components.BagItTopBar
import com.example.bagit.ui.components.DrawerContent
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.Cream
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNewList: () -> Unit = {},
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

    // Tamaños responsivos
    val illustrationSize = when {
        isTablet && isLandscape -> 280.dp
        isTablet -> 260.dp
        else -> 220.dp
    }

    val iconSize = when {
        isTablet && isLandscape -> 160.dp
        isTablet -> 150.dp
        else -> 140.dp
    }

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
                    // Filtrar solo listas NO completadas (para "Edit Lists")
                    val activeLists = state.data.data.filter { list ->
                        !viewModel.isListCompleted(list.id)
                    }

                    if (activeLists.isEmpty()) {
                        if (isTablet && isLandscape) {
                            EmptyStateLandscape(
                                illustrationSize = illustrationSize,
                                iconSize = iconSize,
                                contentPadding = contentPadding,
                                onNavigateToNewList = onNavigateToNewList
                            )
                        } else {
                            EmptyStatePortrait(
                                illustrationSize = illustrationSize,
                                iconSize = iconSize,
                                contentPadding = contentPadding,
                                onNavigateToNewList = onNavigateToNewList,
                                isTablet = isTablet
                            )
                        }
                    } else {
                        val viewMode by viewModel.preferencesRepository.productViewMode.collectAsState(initial = "list")
                        ShoppingListsContent(
                            lists = activeLists,
                            viewMode = viewMode,
                            onListClick = onNavigateToList,
                            onAddList = onNavigateToNewList,
                            onToggleFavorite = { listId, isFavorite ->
                                viewModel.toggleFavorite(listId, isFavorite)
                            },
                            isFavorite = { list -> viewModel.isFavorite(list) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                is com.example.bagit.data.repository.Result.Error -> {
                    ErrorState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        onRetry = { viewModel.getShoppingLists() }
                    )
                }
                null -> Unit
            }
        }
    }
}

@Composable
private fun EmptyStateLandscape(
    illustrationSize: Dp,
    iconSize: Dp,
    contentPadding: Dp,
    onNavigateToNewList: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(illustrationSize),
                shape = CircleShape,
                color = Cream
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = stringResource(R.string.home_empty_cart_icon),
                        tint = Color(0xFF2E2A3A),
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = OnDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToNewList,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = DarkNavy
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(getResponsiveButtonHeight())
            ) {
                Text(
                    text = stringResource(R.string.home_add_list_button),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.home_add_list_icon))
            }
        }
    }
}

@Composable
private fun EmptyStatePortrait(
    illustrationSize: Dp,
    iconSize: Dp,
    contentPadding: Dp,
    onNavigateToNewList: () -> Unit,
    isTablet: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = contentPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(illustrationSize),
            shape = CircleShape,
            color = Cream
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = stringResource(R.string.home_empty_cart_icon),
                    tint = Color(0xFF2E2A3A),
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = if (isTablet) 36.sp else 28.sp
            ),
            fontWeight = FontWeight.Bold,
            color = OnDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNavigateToNewList,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DarkNavy
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(getResponsiveButtonHeight())
        ) {
            Text(
                text = stringResource(R.string.home_add_list_button),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (isTablet) 18.sp else 16.sp
                ),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.home_add_list_icon))
        }

        Spacer(modifier = Modifier.height(16.dp))
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
            text = stringResource(R.string.home_error_loading),
            color = Color.Red,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DarkNavy
            )
        ) {
            Text(stringResource(R.string.home_retry_button))
        }
    }
}

@Composable
fun ShoppingListsContent(
    lists: List<com.example.bagit.data.model.ShoppingList>,
    viewMode: String,
    onListClick: (Long) -> Unit,
    onAddList: () -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    isFavorite: (com.example.bagit.data.model.ShoppingList) -> Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (viewMode == "grid") {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lists, key = { it.id }) { list ->
                    ShoppingListGridCard(
                        list = list,
                        onClick = { onListClick(list.id) },
                        onToggleFavorite = onToggleFavorite,
                        isFavorite = isFavorite(list)
                    )
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lists, key = { it.id }) { list ->
                    ShoppingListCard(
                        list = list,
                        onClick = { onListClick(list.id) },
                        onToggleFavorite = onToggleFavorite,
                        isFavorite = isFavorite(list)
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddList,
            containerColor = Color.White,
            contentColor = DarkNavy,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.home_add_list_icon))
        }
    }
}

@Composable
fun ShoppingListCard(
    list: com.example.bagit.data.model.ShoppingList,
    onClick: () -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    val metadata = list.metadata
    val colorHex = metadata?.get("color") as? String ?: "#5249B6"
    val category = metadata?.get("category") as? String ?: "General"

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2D3E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = try {
                            Color(colorHex.toColorInt())
                        } catch (_: Exception) {
                            Color(0xFF5249B6)
                        },
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            // List info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        color = OnDark.copy(alpha = 0.6f)
                    )
                    if (list.description?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = OnDark.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = list.description,
                            fontSize = 12.sp,
                            color = OnDark.copy(alpha = 0.5f),
                            maxLines = 1
                        )
                    }
                }
                // Mostrar información del owner si la lista es compartida, o "No shared" si no lo está
                // IMPORTANTE: Usamos list.owner que viene del backend (el remitente real),
                // NO el usuario actual. Esto asegura que el receptor vea quién realmente compartió.
                // Siempre mostramos texto para mantener la altura consistente de las cards.
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (list.sharedWith?.isNotEmpty() == true) {
                        stringResource(R.string.home_list_shared_by, list.owner.name, list.owner.surname)
                    } else {
                        stringResource(R.string.home_list_not_shared)
                    },
                    fontSize = 12.sp,
                    color = OnDark.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }

            // Star icon (favorite toggle)
            IconButton(
                onClick = {
                    // Prevenir que el click en la estrella dispare la navegación
                    onToggleFavorite(list.id, isFavorite)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) stringResource(R.string.home_remove_from_favorites) else stringResource(R.string.home_add_to_favorites),
                    tint = if (isFavorite) Color(0xFFFFC107) else OnDark.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Arrow icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.home_open_list),
                tint = OnDark.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ShoppingListGridCard(
    list: com.example.bagit.data.model.ShoppingList,
    onClick: () -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    val metadata = list.metadata
    val colorHex = metadata?.get("color") as? String ?: "#5249B6"
    val category = metadata?.get("category") as? String ?: "General"

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2D3E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = try {
                            Color(colorHex.toColorInt())
                        } catch (_: Exception) {
                            Color(0xFF5249B6)
                        },
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // List name
            Text(
                text = list.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnDark,
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category
            Text(
                text = category,
                fontSize = 12.sp,
                color = OnDark.copy(alpha = 0.6f),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Description if available
            if (list.description?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = list.description,
                    fontSize = 11.sp,
                    color = OnDark.copy(alpha = 0.5f),
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Shared info - siempre mostrar para mantener altura consistente
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (list.sharedWith?.isNotEmpty() == true) {
                    stringResource(R.string.home_list_shared_by, list.owner.name, list.owner.surname)
                } else {
                    stringResource(R.string.home_list_not_shared)
                },
                fontSize = 10.sp,
                color = OnDark.copy(alpha = 0.5f),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Star icon (favorite toggle)
            IconButton(
                onClick = {
                    onToggleFavorite(list.id, isFavorite)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) stringResource(R.string.home_remove_from_favorites) else stringResource(R.string.home_add_to_favorites),
                    tint = if (isFavorite) Color(0xFFFFC107) else OnDark.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF171A26, name = "Home")
@Composable
fun HomeScreenPreview() {
    BagItTheme {
        HomeScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF171A26, name = "Home with Drawer Open")
@Composable
fun HomeScreenDrawerOpenPreview() {
    BagItTheme {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        var searchQuery by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            drawerState.open()
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.85f)
                ) {
                    DrawerContent(
                        onSignOut = { },
                        onSettingsClick = { }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    BagItTopBar(
                        showMenu = true,
                        onMenuClick = { /* no-op en preview */ },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchSubmit = { /* no-op */ }
                    )
                },
                containerColor = DarkNavy
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkNavy)
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(220.dp),
                            shape = CircleShape,
                            color = Cream
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = stringResource(R.string.home_empty_cart_icon),
                                    tint = Color(0xFF2E2A3A),
                                    modifier = Modifier.size(140.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.home_empty_title),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnDark,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = { },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DarkNavy
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                            .height(56.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.home_add_list_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.home_add_list_icon))
                    }
                }
            }
        }
    }
}