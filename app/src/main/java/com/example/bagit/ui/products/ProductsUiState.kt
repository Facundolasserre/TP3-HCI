package com.example.bagit.ui.products

import com.example.bagit.data.model.Category
import com.example.bagit.data.model.Pagination
import com.example.bagit.data.model.Product

/**
 * Estado UI para la pantalla de productos.
 * Maneja el estado de carga, datos paginados, filtros y diálogos.
 */
sealed class ProductsUiState {
    object Loading : ProductsUiState()

    data class Success(
        val products: List<Product>,
        val pagination: Pagination,
        val categories: List<Category> = emptyList(),
        val searchQuery: String = "",
        val selectedCategoryId: Long? = null,
        val pageSize: Int = 10,
        val currentPage: Int = 1,
        val sortBy: String = "name",
        val sortOrder: String = "ASC",
        val isRefreshing: Boolean = false
    ) : ProductsUiState()

    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : ProductsUiState()

    object Empty : ProductsUiState()
}

/**
 * Estado de diálogos de productos
 */
data class ProductDialogState(
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedProduct: Product? = null,
    val isSubmitting: Boolean = false
)

