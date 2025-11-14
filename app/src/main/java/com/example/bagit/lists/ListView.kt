package com.example.bagit.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import com.example.bagit.R
import com.example.bagit.data.model.ListItem
import com.example.bagit.data.model.Product
import com.example.bagit.data.repository.Result
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.*
import com.example.bagit.ui.viewmodel.ListDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    listId: Long,
    onBack: () -> Unit = {},
    onShareMembers: (Long, String) -> Unit = { _, _ -> },
    viewModel: ListDetailViewModel = hiltViewModel()
) {
    val listState by viewModel.currentListState
    val listItemsState by viewModel.listItemsState
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showEditItemDialog by remember { mutableStateOf<ListItem?>(null) }
    val showMenuState = remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Load list info only once
    LaunchedEffect(listId) {
        viewModel.loadList(listId)
        viewModel.loadListItems(listId)
    }

    val contentPadding = getContentPadding()
    val maxContentWidth = getMaxContentWidth()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = listState) {
                        is Result.Success -> {
                            Text(
                                text = state.data.name,
                                fontWeight = FontWeight.SemiBold,
                                color = OnDark
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.list_title),
                                fontWeight = FontWeight.SemiBold,
                                color = OnDark
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.list_back),
                            tint = OnDark
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenuState.value = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.list_more_options),
                                tint = OnDark
                            )
                        }
                        DropdownMenu(
                            expanded = showMenuState.value,
                            onDismissRequest = { showMenuState.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.list_rename)) },
                                onClick = {
                                    when (val state = listState) {
                                        is Result.Success -> {
                                            onRenameClick(listId, state.data.name)
                                        }
                                        else -> {}
                                    }
                                    showMenuState.value = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.list_share)) },
                                onClick = {
                                    when (val state = listState) {
                                        is Result.Success -> {
                                            onShareMembers(listId, state.data.name)
                                        }
                                        else -> {}
                                    }
                                    showMenuState.value = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = OnDark,
                    navigationIconContentColor = OnDark
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddItemDialog = true },
                containerColor = Color(0xFF5249B6),
                contentColor = Color.White,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.list_add_item_icon))
            }
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search products...",
                        color = OnDark.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = OnDark.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = OnDark.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2A2D3E),
                    unfocusedContainerColor = Color(0xFF2A2D3E),
                    focusedTextColor = OnDark,
                    unfocusedTextColor = OnDark,
                    focusedBorderColor = Color(0xFF5249B6),
                    unfocusedBorderColor = Color(0xFF3D3F54),
                    cursorColor = Color(0xFF5249B6)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Contenido de la lista
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (val itemsState = listItemsState) {
                    is Result.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF5249B6))
                        }
                    }
                    is Result.Success -> {
                        val allItems = itemsState.data.data
                        val effectiveQuery = searchQuery.trim()
                        val shownItems = if (effectiveQuery.length >= 2) {
                            allItems.filter { it.product.name.contains(effectiveQuery, ignoreCase = true) }
                        } else allItems

                        if (shownItems.isEmpty()) {
                            if (effectiveQuery.isBlank()) {
                                EmptyListContent(
                                    onAddItem = { showAddItemDialog = true },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(contentPadding)
                                )
                            } else {
                                // No results for search
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = OnDark.copy(alpha = 0.3f),
                                            modifier = Modifier.size(80.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No products found",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = OnDark.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Try a different search term",
                                            fontSize = 14.sp,
                                            color = OnDark.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }
                        } else {
                            ListItemsContent(
                                items = shownItems,
                                onTogglePurchased = { item ->
                                    viewModel.toggleItemPurchased(listId, item.id, !item.purchased)
                                },
                                onEditItem = { item -> showEditItemDialog = item },
                                onDeleteItem = { item -> viewModel.deleteListItem(listId, item.id) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = contentPadding)
                            )
                        }
                    }
                    is Result.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = itemsState.message ?: "Error loading items",
                                    color = Color.Red,
                                    modifier = Modifier.padding(16.dp)
                                )
                                if (searchQuery.isNotBlank()) {
                                    Text(
                                        text = "Probá borrar o cambiar el término de búsqueda.",
                                        color = OnDark.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
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

    if (showAddItemDialog) {
        AddItemDialog(
            listId = listId,
            onDismiss = { showAddItemDialog = false },
            onAddItem = { productId, quantity, unit ->
                viewModel.addListItem(listId, productId, quantity, unit)
                showAddItemDialog = false
            },
            viewModel = viewModel
        )
    }

    showEditItemDialog?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { showEditItemDialog = null },
            onSave = { quantity, unit ->
                viewModel.updateListItem(listId, item.id, quantity, unit)
                showEditItemDialog = null
            }
        )
    }
}

@Composable
fun EmptyListContent(
    onAddItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = OnDark.copy(alpha = 0.3f),
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No items in this list yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = OnDark.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.list_empty_hint),
            fontSize = 14.sp,
            color = OnDark.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddItem,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5249B6)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.list_add_item))
        }
    }
}

@Composable
fun ListItemsContent(
    items: List<ListItem>,
    onTogglePurchased: (ListItem) -> Unit,
    onEditItem: (ListItem) -> Unit,
    onDeleteItem: (ListItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ListItemCard(
                item = item,
                onTogglePurchased = { onTogglePurchased(item) },
                onEdit = { onEditItem(item) },
                onDelete = { onDeleteItem(item) }
            )
        }
    }
}

@Composable
fun ListItemCard(
    item: ListItem,
    onTogglePurchased: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
            // Checkbox
            IconButton(
                onClick = onTogglePurchased,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (item.purchased) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (item.purchased) stringResource(R.string.list_purchased) else stringResource(R.string.list_not_purchased),
                    tint = if (item.purchased) Color(0xFF4CAF50) else OnDark.copy(alpha = 0.3f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Product info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnDark,
                    textDecoration = if (item.purchased) TextDecoration.LineThrough else null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        fontSize = 14.sp,
                        color = OnDark.copy(alpha = 0.6f)
                    )
                    if (item.product.category.name.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = OnDark.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.product.category.name,
                            fontSize = 12.sp,
                            color = OnDark.copy(alpha = 0.5f),
                            modifier = Modifier
                                .background(
                                    Color(0xFF3D3F54),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Edit button
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    listId: Long,
    onDismiss: () -> Unit,
    onAddItem: (productId: Long, quantity: Double, unit: String) -> Unit,
    viewModel: ListDetailViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("kg") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showCreateProductDialog by remember { mutableStateOf(false) }

    val productsState by viewModel.productsState
    val categoriesState by viewModel.categoriesState
    val isCreatingProduct by viewModel.isCreatingProduct
    val errorMessage by viewModel.errorMessage

    // Cargar categorías cuando se abre el diálogo de crear producto
    LaunchedEffect(showCreateProductDialog) {
        if (showCreateProductDialog) {
            viewModel.loadCategories()
        }
    }

    // Mostrar snackbar de error si hay uno
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    val createProductState by viewModel.createProductState
    
    // Cuando se crea el producto exitosamente, seleccionarlo y cerrar solo el diálogo de creación
    LaunchedEffect(createProductState) {
        if (createProductState is Result.Success && !isCreatingProduct) {
            val createdProduct = (createProductState as Result.Success<Product>).data

            // Seleccionar el producto creado
            selectedProduct = createdProduct
            searchQuery = createdProduct.name

            // Cerrar solo el diálogo de creación de producto
            showCreateProductDialog = false

            // Resetear el estado para futuras creaciones
            viewModel.resetCreateProductState()
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.searchProducts(searchQuery)
        }
    }

    // Resetear estado cuando se cierra el diálogo principal
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetCreateProductState()
        }
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2A2D3E)
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
            Text(
                text = stringResource(R.string.list_add_item),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.list_search_product), color = OnDark.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1F2E),
                    unfocusedContainerColor = Color(0xFF1E1F2E),
                    focusedTextColor = OnDark,
                    unfocusedTextColor = OnDark,
                    focusedBorderColor = Color(0xFF5249B6),
                    unfocusedBorderColor = Color(0xFF3D3F54),
                    cursorColor = Color(0xFF5249B6)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Products list
            if (searchQuery.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .background(Color(0xFF1E1F2E), RoundedCornerShape(8.dp))
                ) {
                    when (val state = productsState) {
                        is Result.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF5249B6),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        is Result.Success -> {
                            if (state.data.data.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.list_no_products_found),
                                        color = OnDark.copy(alpha = 0.5f)
                                    )
                                    Button(
                                        onClick = { showCreateProductDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF5249B6)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.list_create_product))
                                    }
                                }
                            } else {
                                LazyColumn {
                                    items(state.data.data, key = { it.id }) { product ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedProduct = product
                                                    searchQuery = product.name
                                                }
                                                .background(
                                                    if (selectedProduct?.id == product.id)
                                                        Color(0xFF5249B6).copy(alpha = 0.2f)
                                                    else
                                                        Color.Transparent
                                                )
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = product.name,
                                                    color = OnDark,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = product.category.name,
                                                    color = OnDark.copy(alpha = 0.5f),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                    // Agregar ítem al final para crear nuevo producto
                                    item {
                                        Divider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = OnDark.copy(alpha = 0.2f)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showCreateProductDialog = true }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Create new product",
                                                tint = Color(0xFF5249B6),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Create new product",
                                                color = Color(0xFF5249B6),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is Result.Error -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error loading products",
                                    color = Color.Red
                                )
                            }
                        }
                        null -> {}
                    }
                }
            } else {
                // Mostrar botón para crear producto cuando no hay búsqueda
                Button(
                    onClick = { showCreateProductDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5249B6)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.list_create_product))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity and unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { newValue ->
                        // Solo permitir números y punto decimal
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            quantity = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.list_quantity_label), color = OnDark.copy(alpha = 0.6f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1F2E),
                        unfocusedContainerColor = Color(0xFF1E1F2E),
                        focusedTextColor = OnDark,
                        unfocusedTextColor = OnDark,
                        focusedBorderColor = Color(0xFF5249B6),
                        unfocusedBorderColor = Color(0xFF3D3F54),
                        cursorColor = Color(0xFF5249B6)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )

                UnitSelector(
                    selectedUnit = unit,
                    quantity = quantity.toDoubleOrNull() ?: 1.0,
                    onUnitSelected = { unit = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.list_cancel), color = OnDark.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        selectedProduct?.let { product ->
                            val qty = quantity.toDoubleOrNull() ?: 1.0
                            val formattedUnit = formatUnit(unit, qty)
                            onAddItem(product.id, qty, formattedUnit)
                        }
                    },
                    enabled = selectedProduct != null && quantity.toDoubleOrNull() != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5249B6),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF5249B6),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    Text(stringResource(R.string.list_add))
                }
            }
                }
                
                // Snackbar para errores
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // Diálogo para crear producto
    if (showCreateProductDialog) {
        val categories = when (val state = categoriesState) {
            is Result.Success -> state.data.data
            else -> emptyList()
        }
        
        com.example.bagit.ui.products.CreateEditProductDialog(
            product = null,
            categories = categories,
            isSubmitting = isCreatingProduct,
            onDismiss = { 
                if (!isCreatingProduct) {
                    showCreateProductDialog = false
                    viewModel.resetCreateProductState()
                }
            },
            onConfirm = { name, categoryId, metadata ->
                // Solo crear el producto, no agregarlo a la lista aún
                // El usuario ingresará cantidad/unidad después
                viewModel.createProduct(
                    name = name,
                    categoryId = categoryId,
                    metadata = metadata
                )
            }
        )
    }
}

private fun onRenameClick(listId: Long, listName: String) {
    // Implementar lógica para renombrar la lista
    // Aquí irá la navegación a un diálogo de renombre o pantalla de edición
    println("Renombrar lista: $listId - $listName")
}

/**
 * Formatea la unidad según la cantidad.
 * Si la unidad base es "unit", cambia a "Unit" o "Units" según la cantidad.
 */
fun formatUnit(baseUnit: String, quantity: Double): String {
    return when (baseUnit.lowercase()) {
        "unit" -> if (quantity == 1.0) "Unit" else "Units"
        else -> baseUnit
    }
}

/**
 * Obtiene el nombre para mostrar de la unidad en el selector.
 */
fun getUnitDisplayName(baseUnit: String, quantity: Double): String {
    return formatUnit(baseUnit, quantity)
}

/**
 * Componente dropdown para seleccionar unidades.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelector(
    selectedUnit: String,
    quantity: Double,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val units = listOf("kg", "g", "unit")

    val displayUnit = getUnitDisplayName(selectedUnit, quantity)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayUnit,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.list_unit_label), color = OnDark.copy(alpha = 0.6f)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1F2E),
                unfocusedContainerColor = Color(0xFF1E1F2E),
                focusedTextColor = OnDark,
                unfocusedTextColor = OnDark,
                focusedBorderColor = Color(0xFF5249B6),
                unfocusedBorderColor = Color(0xFF3D3F54)
            ),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF2A2D3E))
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = getUnitDisplayName(unit, quantity),
                            color = OnDark,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = OnDark,
                        leadingIconColor = OnDark,
                        trailingIconColor = OnDark
                    )
                )
            }
        }
    }
}

/**
 * Diálogo para editar un item existente en la lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(
    item: ListItem,
    onDismiss: () -> Unit,
    onSave: (quantity: Double, unit: String) -> Unit
) {
    var quantity by remember { mutableStateOf(item.quantity.toString()) }
    var unit by remember { mutableStateOf(item.unit.lowercase()) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2A2D3E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Edit Item",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,

                    color = OnDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Product name (read-only)
                Text(
                    text = "Product",
                    fontSize = 12.sp,
                    color = OnDark.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity and unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newValue ->
                            // Solo permitir números y punto decimal
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                quantity = newValue
                            }
                        },
                        label = { Text(stringResource(R.string.list_quantity_label), color = OnDark.copy(alpha = 0.6f)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1F2E),
                            unfocusedContainerColor = Color(0xFF1E1F2E),
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark,
                            focusedBorderColor = Color(0xFF5249B6),
                            unfocusedBorderColor = Color(0xFF3D3F54),
                            cursorColor = Color(0xFF5249B6)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )

                    UnitSelector(
                        selectedUnit = unit,
                        quantity = quantity.toDoubleOrNull() ?: 1.0,
                        onUnitSelected = { unit = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.list_cancel), color = OnDark.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = quantity.toDoubleOrNull() ?: item.quantity
                            val formattedUnit = formatUnit(unit, qty)
                            onSave(qty, formattedUnit)
                        },
                        enabled = quantity.toDoubleOrNull() != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5249B6)
                        )
                    ) {
                        Text(stringResource(R.string.list_save))
                    }
                }
            }
        }
    }
}
