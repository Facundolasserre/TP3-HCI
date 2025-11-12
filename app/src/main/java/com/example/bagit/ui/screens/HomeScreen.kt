// app/src/main/java/com/example/bagit/ui/screens/HomeScreen.kt
package com.example.bagit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.bagit.ui.components.BagItTopBar
import com.example.bagit.ui.components.DrawerContent
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.Cream
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onNavigateToNewList: () -> Unit = {},
    onNavigateToList: (Long) -> Unit = {},
    onNavigateToProducts: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    viewModel: com.example.bagit.ui.viewmodel.ShoppingListViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Estado de búsqueda
    var searchQuery by remember { mutableStateOf("") }

    // Load shopping lists
    val listsState by viewModel.listsState

    LaunchedEffect(Unit) {
        viewModel.getShoppingLists()
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
                    onSignOut = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    onNavigateToProducts = {
                        scope.launch { drawerState.close() }
                        onNavigateToProducts()
                    },
                    onSettingsClick = {
                        scope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                    onToggleLanguage = onToggleLanguage
                )
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                BagItTopBar(
                    showMenu = true,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchSubmit = {
                        // ejemplo: viewModel.onSearch(searchQuery)
                    }
                    // titleWhenNoSearch = null // mantener la pill siempre
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
                        if (state.data.data.isEmpty()) {
                            // Empty state
                            if (isTablet && isLandscape) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(contentPadding),
                                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                                ) {
                                    // Izquierda: ilustración
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
                                                    contentDescription = "Empty cart",
                                                    tint = Color(0xFF2E2A3A),
                                                    modifier = Modifier.size(iconSize)
                                                )
                                            }
                                        }
                                    }

                                    // Derecha: texto + botón
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .align(Alignment.CenterVertically),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "No lists yet,\nstart now!",
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
                                                text = "Add List",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontSize = if (isTablet) 18.sp else 16.sp
                                                ),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                                        }
                                    }
                                }
                            } else {
                                // Teléfono o portrait: una columna
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
                                                contentDescription = "Empty cart",
                                                tint = Color(0xFF2E2A3A),
                                                modifier = Modifier.size(iconSize)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = "No lists yet,\nstart now!",
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
                                            text = "Add List",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontSize = if (isTablet) 18.sp else 16.sp
                                            ),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        } else {
                            // Show lists
                            ShoppingListsContent(
                                lists = state.data.data,
                                onListClick = onNavigateToList,
                                onAddList = onNavigateToNewList,
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
                                text = "Error loading lists",
                                color = Color.Red,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.getShoppingLists() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = DarkNavy
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                    null -> {
                        // Initial state
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListsContent(
    lists: List<com.example.bagit.data.model.ShoppingList>,
    onListClick: (Long) -> Unit,
    onAddList: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lists, key = { it.id }) { list ->
                ShoppingListCard(
                    list = list,
                    onClick = { onListClick(list.id) }
                )
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
            Icon(Icons.Filled.Add, contentDescription = "Add List")
        }
    }
}

@Composable
fun ShoppingListCard(
    list: com.example.bagit.data.model.ShoppingList,
    onClick: () -> Unit,
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
            }

            // Arrow icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open list",
                tint = OnDark.copy(alpha = 0.5f)
            )
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
                        onSettingsClick = { },
                        onToggleLanguage = { }
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
                                    contentDescription = "Empty cart",
                                    tint = Color(0xFF2E2A3A),
                                    modifier = Modifier.size(140.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "No lists yet,\nstart now!",
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
                            text = "Add List",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                    }
                }
            }
        }
    }
}