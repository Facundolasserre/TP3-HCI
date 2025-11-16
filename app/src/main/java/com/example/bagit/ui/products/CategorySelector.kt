package com.example.bagit.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.R
import com.example.bagit.data.model.Category
import com.example.bagit.ui.theme.OnDark

/**
 * Componente de selector de categorías con búsqueda y creación.
 *
 * @param selectedCategory Categoría actualmente seleccionada
 * @param onCategorySelected Callback cuando se selecciona una categoría
 * @param modifier Modificador opcional
 * @param viewModel ViewModel inyectado por Hilt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategorySelectorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    // Sincronizar selección externa con ViewModel
    LaunchedEffect(selectedCategory) {
        if (selectedCategory != null) {
            viewModel.selectCategory(selectedCategory)
        } else {
            viewModel.clearSelection()
        }
    }

    // Notificar cambios de selección al padre
    LaunchedEffect(uiState) {
        if (uiState is CategorySelectorUiState.Success) {
            val selected = (uiState as CategorySelectorUiState.Success).selectedCategory
            if (selected != selectedCategory) {
                onCategorySelected(selected)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Campo de búsqueda
        OutlinedTextField(
            value = if (uiState is CategorySelectorUiState.Success) {
                (uiState as CategorySelectorUiState.Success).searchQuery
            } else "",
            onValueChange = viewModel::onSearchQueryChanged,
            label = { Text(stringResource(R.string.category_selector_search)) },
            placeholder = { Text(stringResource(R.string.category_selector_search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.category_selector_search_icon),
                    tint = Color(0xFFB0B0B0)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botón para crear nueva categoría
        OutlinedButton(
            onClick = { viewModel.showCreateDialog() },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF2A2D3A),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.category_selector_add_icon),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.category_selector_new_button), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lista de categorías
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2D3A)
            )
        ) {
            when (val state = uiState) {
                is CategorySelectorUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFA594FF)
                        )
                    }
                }
                is CategorySelectorUiState.Success -> {
                    CategoryList(
                        categories = state.categories,
                        selectedCategory = state.selectedCategory,
                        onCategoryClick = { category ->
                            viewModel.selectCategory(category)
                            onCategorySelected(category)
                        }
                    )
                }
                is CategorySelectorUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = Color(0xFFFF6B6B),
                                fontSize = 14.sp
                            )
                            TextButton(onClick = { viewModel.retry() }) {
                                Text(stringResource(R.string.category_selector_retry))
                            }
                        }
                    }
                }
                is CategorySelectorUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.category_selector_no_results),
                            color = Color(0xFF9E9E9E),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Diálogo de crear categoría
    if (dialogState.isVisible) {
        CreateCategoryDialog(
            isSubmitting = dialogState.isSubmitting,
            errorMessage = dialogState.errorMessage,
            onDismiss = { viewModel.dismissCreateDialog() },
            onConfirm = { name -> viewModel.createCategory(name) },
            onClearError = { viewModel.clearDialogError() }
        )
    }
}

/**
 * Lista de categorías con selección
 */
@Composable
private fun CategoryList(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategoryClick: (Category) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryItem(
                category = category,
                isSelected = category.id == selectedCategory?.id,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

/**
 * Item individual de categoría
 */
@Composable
private fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = category.name,
            fontSize = 15.sp,
            color = if (isSelected) Color(0xFFA594FF) else Color.White,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.category_selector_selected),
                tint = Color(0xFFA594FF),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    HorizontalDivider(
        color = Color(0xFF3D4052),
        thickness = 1.dp
    )
}

/**
 * Diálogo para crear nueva categoría
 */
@Composable
fun CreateCategoryDialog(
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onClearError: () -> Unit = {}
) {
    var categoryName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    // Limpiar error local cuando se muestra error del server
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            showError = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text(
                text = stringResource(R.string.create_category_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnDark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        if (it.length <= 50) {
                            categoryName = it
                            showError = false
                            if (errorMessage != null) {
                                onClearError()
                            }
                        }
                    },
                    label = {
                        Text(
                            stringResource(R.string.create_category_name_label),
                            color = OnDark.copy(alpha = 0.7f)
                        )
                    },
                    placeholder = {
                        Text(
                            stringResource(R.string.create_category_name_placeholder),
                            color = OnDark.copy(alpha = 0.5f)
                        )
                    },
                    isError = showError || errorMessage != null,
                    supportingText = {
                        when {
                            errorMessage != null -> Text(
                                text = errorMessage,
                                color = Color(0xFFFF6B6B)
                            )
                            showError -> Text(
                                text = stringResource(R.string.create_category_name_required),
                                color = Color(0xFFFF6B6B)
                            )
                            else -> Text(
                                text = stringResource(R.string.create_category_max_length),
                                color = OnDark.copy(alpha = 0.6f)
                            )
                        }
                    },
                    enabled = !isSubmitting,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1F2E),
                        unfocusedContainerColor = Color(0xFF1E1F2E),
                        focusedTextColor = OnDark,
                        unfocusedTextColor = OnDark,
                        focusedBorderColor = Color(0xFF5249B6),
                        unfocusedBorderColor = Color(0xFF3D3F54),
                        cursorColor = Color(0xFF5249B6),
                        errorBorderColor = Color(0xFFFF6B6B),
                        errorContainerColor = Color(0xFF1E1F2E),
                        errorTextColor = OnDark
                    )
                )

                if (isSubmitting) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFA594FF)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isBlank()) {
                        showError = true
                    } else {
                        onConfirm(categoryName)
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5249B6),
                    contentColor = OnDark,
                    disabledContainerColor = Color(0xFF3D3F54),
                    disabledContentColor = OnDark.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_category_button),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text(
                    stringResource(R.string.create_category_cancel),
                    color = OnDark.copy(alpha = 0.7f)
                )
            }
        },
        containerColor = Color(0xFF1E1F2E),
        titleContentColor = OnDark,
        textContentColor = OnDark
    )
}

