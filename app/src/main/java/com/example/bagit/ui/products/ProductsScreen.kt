package com.example.bagit.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.R
import com.example.bagit.ui.components.BagItTopBar
import com.example.bagit.ui.components.ProductCard
import com.example.bagit.ui.components.ProductGridCard
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.shouldUseTwoPaneLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsRoute(
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit = {},
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val dialogState = viewModel.dialogState
    val viewMode by viewModel.preferencesRepository.productViewMode.collectAsState(initial = "list")

    Scaffold(
        topBar = {
            BagItTopBar(
                showMenu = true,
                onMenuClick = onOpenDrawer,
                searchQuery = if (uiState is ProductsUiState.Success) uiState.searchQuery else "",
                onSearchQueryChange = viewModel::onSearchChange
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                containerColor = Color(0xFF5249B6),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.products_create_icon)
                )
            }
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is ProductsUiState.Loading -> {
                    LoadingState()
                }
                is ProductsUiState.Success -> {
                    SuccessState(
                        state = uiState,
                        viewMode = viewMode,
                        onCategorySelect = viewModel::onCategorySelect,
                        onPageChange = viewModel::onPageChange,
                        onEditProduct = viewModel::showEditDialog,
                        onDeleteProduct = viewModel::showDeleteDialog
                    )
                }
                is ProductsUiState.Error -> {
                    ErrorState(
                        message = uiState.message,
                        onRetry = viewModel::retry
                    )
                }
                is ProductsUiState.Empty -> {
                    EmptyState()
                }
            }
        }
    }

    // Diálogos
    if (dialogState.showCreateDialog) {
        val categories = (uiState as? ProductsUiState.Success)?.categories ?: emptyList()
        CreateEditProductDialog(
            product = null,
            categories = categories,
            isSubmitting = dialogState.isSubmitting,
            onDismiss = viewModel::dismissDialogs,
            onConfirm = { name, categoryId, metadata ->
                viewModel.createProduct(name, categoryId, metadata)
            }
        )
    }

    if (dialogState.showEditDialog && dialogState.selectedProduct != null) {
        val categories = (uiState as? ProductsUiState.Success)?.categories ?: emptyList()
        CreateEditProductDialog(
            product = dialogState.selectedProduct,
            categories = categories,
            isSubmitting = dialogState.isSubmitting,
            onDismiss = viewModel::dismissDialogs,
            onConfirm = { name, categoryId, metadata ->
                viewModel.updateProduct(dialogState.selectedProduct.id, name, categoryId, metadata)
            }
        )
    }

    if (dialogState.showDeleteDialog && dialogState.selectedProduct != null) {
        ConfirmDeleteDialog(
            productName = dialogState.selectedProduct.name,
            isDeleting = dialogState.isSubmitting,
            onDismiss = viewModel::dismissDialogs,
            onConfirm = {
                viewModel.deleteProduct(dialogState.selectedProduct.id)
            }
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFFA594FF)
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2D3A)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.products_error_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color(0xFFB0B0B0),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5249B6)
                    )
                ) {
                    Text(stringResource(R.string.products_retry_button))
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.products_empty_state),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9E9E9E)
            )
            Text(
                text = stringResource(R.string.products_empty_hint),
                fontSize = 14.sp,
                color = Color(0xFF7E7E7E)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessState(
    state: ProductsUiState.Success,
    viewMode: String,
    onCategorySelect: (Long?) -> Unit,
    onPageChange: (Int) -> Unit,
    onEditProduct: (com.example.bagit.data.model.Product) -> Unit,
    onDeleteProduct: (com.example.bagit.data.model.Product) -> Unit
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }
    val useTwoPane = shouldUseTwoPaneLayout()

    // Obtener categorías únicas de los productos actuales
    // Incluir también la categoría seleccionada si existe, aunque no esté en los productos visibles
    val availableCategories = remember(state.products, state.selectedCategoryId, state.categories) {
        val categoriesFromProducts = state.products
            .mapNotNull { it.category }
            .distinctBy { it.id }
        
        val selectedCategory = state.selectedCategoryId?.let { selectedId ->
            state.categories.find { it.id == selectedId }
        }
        
        (categoriesFromProducts + listOfNotNull(selectedCategory))
            .distinctBy { it.id }
            .sortedBy { it.name }
    }

    val selectedCategoryName = if (state.selectedCategoryId == null) {
        stringResource(R.string.products_all_categories)
    } else {
        availableCategories.find { it.id == state.selectedCategoryId }?.name
            ?: stringResource(R.string.products_all_categories)
    }

    if (useTwoPane) {
        // Layout de dos paneles en landscape: filtros a la izquierda, productos a la derecha
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // Panel izquierdo: Filtros
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .background(Color(0xFF1A1D28))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.products_filters),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Divider(color = OnDark.copy(alpha = 0.2f))
                
                // Dropdown de categorías
                Column {
                    Text(
                        text = stringResource(R.string.products_categories),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnDark.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box {
                        OutlinedButton(
                            onClick = { showCategoryDropdown = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF2A2D3A),
                                contentColor = OnDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${selectedCategoryName} ▾", fontSize = 12.sp)
                        }

                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.products_all_categories)) },
                                onClick = {
                                    onCategorySelect(null)
                                    showCategoryDropdown = false
                                }
                            )
                            availableCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        onCategorySelect(category.id)
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Panel derecho: Productos
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
            ) {
                // Lista o cuadrícula de productos
                if (viewMode == "grid") {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.products, key = { it.id }) { product ->
                            ProductGridCard(
                                product = product,
                                onEdit = { onEditProduct(product) },
                                onDelete = { onDeleteProduct(product) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(state.products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onEdit = { onEditProduct(product) },
                                onDelete = { onDeleteProduct(product) }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Layout de una columna en portrait
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Filtros
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Dropdown de categorías
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showCategoryDropdown = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF2A2D3A),
                            contentColor = OnDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${selectedCategoryName} ▾", fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.products_all_categories)) },
                            onClick = {
                                onCategorySelect(null)
                                showCategoryDropdown = false
                            }
                        )
                        availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    onCategorySelect(category.id)
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }

        // Lista o cuadrícula de productos
        if (viewMode == "grid") {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.products, key = { it.id }) { product ->
                    ProductGridCard(
                        product = product,
                        onEdit = { onEditProduct(product) },
                        onDelete = { onDeleteProduct(product) }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(state.products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onEdit = { onEditProduct(product) },
                        onDelete = { onDeleteProduct(product) }
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    hasNext: Boolean,
    hasPrev: Boolean,
    onPageChange: (Int) -> Unit
) {
    Surface(
        color = Color(0xFF2A2D3A),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Previous
            IconButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = hasPrev
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.products_previous_page),
                    tint = if (hasPrev) Color.White else Color(0xFF5E5E5E)
                )
            }

            // Indicador de página
            Text(
                text = stringResource(R.string.products_page_info, currentPage, totalPages),
                color = Color.White,
                fontSize = 14.sp
            )

            // Botón Next
            IconButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = hasNext
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = stringResource(R.string.products_next_page),
                    tint = if (hasNext) Color.White else Color(0xFF5E5E5E)
                )
            }
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    productName: String,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text(stringResource(R.string.product_delete_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.product_delete_message, productName))
                if (isDeleting) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFFF6B6B)
                )
            ) {
                Text(stringResource(R.string.product_delete_confirm_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(stringResource(R.string.product_delete_cancel_button))
            }
        }
    )
}
