package com.example.bagit.ui.products

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bagit.ui.components.BagItTopBar
import com.example.bagit.ui.components.ProductCard
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsRoute(
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit = {},
    viewModel: ProductsViewModel = viewModel()
) {
    ProductsScreen(
        uiState = viewModel.uiState,
        filteredProducts = viewModel.getFilteredProducts(),
        onSearchChange = viewModel::onSearchChange,
        onCategorySelect = viewModel::onCategorySelect,
        onEditProduct = viewModel::onEditProduct,
        onDeleteProduct = viewModel::onDeleteProduct,
        onOpenDrawer = onOpenDrawer,
        modifier = modifier
    )
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
            BagItTopBar(
                showMenu = true,
                onMenuClick = onOpenDrawer,
                searchQuery = uiState.search,
                onSearchQueryChange = onSearchChange
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Chips de categorías
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
                                    text = category,
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

                // Row de botones (filtros visuales)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: implementar filtro real */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF2A2D3A),
                            contentColor = OnDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Todas las categorías ▾", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { /* TODO: implementar selector de cantidad */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF2A2D3A),
                            contentColor = OnDark
                        )
                    ) {
                        Text("Mostrar: ${uiState.pageSize} ▾", fontSize = 12.sp)
                    }
                }
            }

            // Lista de productos
            if (filteredProducts.isNotEmpty()) {
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
                }
            } else {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
}

@Preview(showBackground = true)
@Composable
fun ProductsScreenPreview() {
    BagItTheme {
        val mockInstant = java.time.Instant.now()
        val mockState = ProductsUiState(
            search = "",
            selectedCategory = "Todas las categorías",
            page = 1,
            pageSize = 10,
            allProducts = listOf(
                ProductUi("1", "Agua", "LIQUIDO", mockInstant),
                ProductUi("2", "Gatorade", "LIQUIDO", mockInstant),
                ProductUi("3", "Pan Integral", "PANADERIA", mockInstant)
            )
        )

        ProductsScreen(
            uiState = mockState,
            filteredProducts = mockState.allProducts,
            onSearchChange = {},
            onCategorySelect = {},
            onEditProduct = {},
            onDeleteProduct = {},
            onOpenDrawer = {}
        )
    }
}
