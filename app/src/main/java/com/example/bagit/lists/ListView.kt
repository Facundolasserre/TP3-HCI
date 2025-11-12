package com.example.bagit.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
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
    val showMenuState = remember { mutableStateOf(false) }

    // Load data
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
                                text = "List",
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
                            contentDescription = "Back",
                            tint = OnDark
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenuState.value = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = OnDark
                            )
                        }
                        DropdownMenu(
                            expanded = showMenuState.value,
                            onDismissRequest = { showMenuState.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Renombrar") },
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
                                text = { Text("Compartir lista") },
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
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues)
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
                    if (itemsState.data.data.isEmpty()) {
                        EmptyListContent(
                            onAddItem = { showAddItemDialog = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding)
                        )
                    } else {
                        ListItemsContent(
                            items = itemsState.data.data,
                            onTogglePurchased = { item ->
                                viewModel.toggleItemPurchased(listId, item.id, !item.purchased)
                            },
                            onDeleteItem = { item ->
                                viewModel.deleteListItem(listId, item.id)
                            },
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
                        Text(
                            text = itemsState.message ?: "Error loading items",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                null -> {
                    // Initial state
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAddItem = { productId, quantity, unit ->
                viewModel.addListItem(listId, productId, quantity, unit)
                showAddItemDialog = false
            },
            viewModel = viewModel
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
            text = "Tap the + button to add items",
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
            Text("Add Item")
        }
    }
}

@Composable
fun ListItemsContent(
    items: List<ListItem>,
    onTogglePurchased: (ListItem) -> Unit,
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
                onDelete = { onDeleteItem(item) }
            )
        }
    }
}

@Composable
fun ListItemCard(
    item: ListItem,
    onTogglePurchased: () -> Unit,
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
                    contentDescription = if (item.purchased) "Purchased" else "Not purchased",
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
    onDismiss: () -> Unit,
    onAddItem: (productId: Long, quantity: Double, unit: String) -> Unit,
    viewModel: ListDetailViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("kg") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    val productsState by viewModel.productsState

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.searchProducts(searchQuery)
        }
    }

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
                text = "Add Item",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search product", color = OnDark.copy(alpha = 0.6f)) },
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
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No products found",
                                        color = OnDark.copy(alpha = 0.5f)
                                    )
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity and unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity", color = OnDark.copy(alpha = 0.6f)) },
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
                    singleLine = true
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit", color = OnDark.copy(alpha = 0.6f)) },
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
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = OnDark.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        selectedProduct?.let { product ->
                            val qty = quantity.toDoubleOrNull() ?: 1.0
                            onAddItem(product.id, qty, unit)
                        }
                    },
                    enabled = selectedProduct != null && quantity.toDoubleOrNull() != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5249B6)
                    )
                ) {
                    Text("Add")
                }
            }
        }
        }
    }
}

private fun onRenameClick(listId: Long, listName: String) {
    // Implementar lógica para renombrar la lista
    // Aquí irá la navegación a un diálogo de renombre o pantalla de edición
    println("Renombrar lista: $listId - $listName")
}



