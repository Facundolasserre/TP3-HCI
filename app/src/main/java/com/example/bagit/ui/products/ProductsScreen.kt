package com.example.bagit.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bagit.ui.components.ProductCard
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsRoute(
    onOpenDrawer: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToProducts: () -> Unit = {},
    viewModel: ProductsViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.85f)
            ) {
                com.example.bagit.ui.components.DrawerContent(
                    onSignOut = {
                        scope.launch {
                            drawerState.close()
                        }
                        onLogout()
                    },
                    onNavigateToProducts = {
                        scope.launch {
                            drawerState.close()
                        }
                        onNavigateToProducts()
                    },
                    onSettingsClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        // TODO: Navigate to settings screen
                    },
                    onToggleLanguage = {
                        // TODO: Implement language toggle
                    }
                )
            }
        },
        gesturesEnabled = true
    ) {
        ProductsScreen(
            uiState = viewModel.uiState,
            filteredProducts = viewModel.getFilteredProducts(),
            onSearchChange = viewModel::onSearchChange,
            onCategorySelect = viewModel::onCategorySelect,
            onEditProduct = viewModel::onEditProduct,
            onDeleteProduct = viewModel::onDeleteProduct,
            onPageChange = viewModel::onPageChange,
            onOpenDrawer = {
                scope.launch {
                    drawerState.open()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    uiState: ProductsUiState,
    filteredProducts: List<ProductUi>,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onEditProduct: (String) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onPageChange: (Int) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Todas las categorías",
        "LIQUIDO",
        "PANADERIA",
        "LACTEO",
        "FRUTA",
        "CEREAL",
        "LIMPIEZA"
    )

    Scaffold(
        topBar = {
            ProductsTopBar(
                onOpenDrawer = onOpenDrawer
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filters section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = uiState.search,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            "Buscar productos…",
                            color = Color(0xFF9E9E9E)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color(0xFFA594FF)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA594FF),
                        unfocusedBorderColor = Color(0xFF3D4052),
                        focusedTextColor = OnDark,
                        unfocusedTextColor = OnDark,
                        cursorColor = Color(0xFFA594FF),
                        focusedContainerColor = Color(0xFF2A2D3A),
                        unfocusedContainerColor = Color(0xFF2A2D3A)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { onCategorySelect(category) },
                            label = {
                                Text(
                                    category,
                                    fontSize = 14.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFA594FF),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF2A2D3A),
                                labelColor = Color(0xFF9E9E9E)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dropdowns row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category dropdown (UI-only)
                    OutlinedButton(
                        onClick = { /* UI-only */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF2A2D3A),
                            contentColor = OnDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Todas las categorías ▾",
                            fontSize = 12.sp
                        )
                    }

                    // Page size dropdown (UI-only)
                    OutlinedButton(
                        onClick = { /* UI-only */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF2A2D3A),
                            contentColor = OnDark
                        )
                    ) {
                        Text(
                            "Mostrar: ${uiState.pageSize} ▾",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Products list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onEdit = { onEditProduct(product.id) },
                        onDelete = { onDeleteProduct(product.id) }
                    )
                }

                // Empty state
                if (filteredProducts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron productos",
                                color = Color(0xFF9E9E9E),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Pagination
            PaginationControls(
                currentPage = uiState.page,
                onPageChange = onPageChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductsTopBar(
    onOpenDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkNavy)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Hamburger menu
            IconButton(
                onClick = onOpenDrawer
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Abrir menú",
                    tint = OnDark
                )
            }

            // Profile icon
            IconButton(
                onClick = { /* UI-only */ }
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF3D4052),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = OnDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title and subtitle
        Text(
            text = "Productos",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = OnDark
        )

        Text(
            text = "Gestioná tu inventario de productos",
            fontSize = 14.sp,
            color = Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun PaginationControls(
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        TextButton(
            onClick = {
                if (currentPage > 1) onPageChange(currentPage - 1)
            },
            enabled = currentPage > 1,
            colors = ButtonDefaults.textButtonColors(
                contentColor = OnDark,
                disabledContentColor = Color(0xFF5A5A5A)
            )
        ) {
            Text("Previous")
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Current page button
        Button(
            onClick = { },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA594FF),
                contentColor = Color.White
            ),
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = currentPage.toString(),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Next button
        TextButton(
            onClick = { onPageChange(currentPage + 1) },
            colors = ButtonDefaults.textButtonColors(
                contentColor = OnDark
            )
        ) {
            Text("Next")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductsScreenPreview() {
    BagItTheme {
        val mockState = ProductsUiState(
            search = "",
            selectedCategory = "Todas las categorías",
            page = 1,
            pageSize = 10,
            allProducts = listOf(
                ProductUi(
                    id = "1",
                    name = "Agua",
                    category = "LIQUIDO",
                    updatedAt = java.time.Instant.parse("2025-10-13T01:47:00Z")
                ),
                ProductUi(
                    id = "2",
                    name = "Gatorade",
                    category = "LIQUIDO",
                    updatedAt = java.time.Instant.parse("2025-10-13T01:47:00Z")
                ),
                ProductUi(
                    id = "3",
                    name = "Pan Integral",
                    category = "PANADERIA",
                    updatedAt = java.time.Instant.parse("2025-10-11T08:20:00Z")
                )
            )
        )

        ProductsScreen(
            uiState = mockState,
            filteredProducts = mockState.allProducts,
            onSearchChange = {},
            onCategorySelect = {},
            onEditProduct = {},
            onDeleteProduct = {},
            onPageChange = {},
            onOpenDrawer = {}
        )
    }
}

