package com.example.bagit.ui.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.Instant

// TODO: Conectar a API real cuando esté permitido (no modificar /api en este PR)

/**
 * ProductsViewModel
 *
 * Maneja el estado de la pantalla de productos.
 * Actualmente usa datos mock locales.
 */
class ProductsViewModel : ViewModel() {

    var uiState by mutableStateOf(ProductsUiState())
        private set

    init {
        loadMockProducts()
    }

    private fun loadMockProducts() {
        val mockProducts = listOf(
            ProductUi(
                id = "1",
                name = "Agua",
                category = "LIQUIDO",
                updatedAt = Instant.parse("2025-10-13T01:47:00Z")
            ),
            ProductUi(
                id = "2",
                name = "Gatorade",
                category = "LIQUIDO",
                updatedAt = Instant.parse("2025-10-13T01:47:00Z")
            ),
            ProductUi(
                id = "3",
                name = "Coca Cola",
                category = "LIQUIDO",
                updatedAt = Instant.parse("2025-10-12T15:30:00Z")
            ),
            ProductUi(
                id = "4",
                name = "Pan Integral",
                category = "PANADERIA",
                updatedAt = Instant.parse("2025-10-11T08:20:00Z")
            ),
            ProductUi(
                id = "5",
                name = "Leche Entera",
                category = "LACTEO",
                updatedAt = Instant.parse("2025-10-10T12:15:00Z")
            ),
            ProductUi(
                id = "6",
                name = "Manzanas",
                category = "FRUTA",
                updatedAt = Instant.parse("2025-10-09T18:45:00Z")
            ),
            ProductUi(
                id = "7",
                name = "Arroz",
                category = "CEREAL",
                updatedAt = Instant.parse("2025-10-08T10:30:00Z")
            ),
            ProductUi(
                id = "8",
                name = "Jabón",
                category = "LIMPIEZA",
                updatedAt = Instant.parse("2025-10-07T14:20:00Z")
            )
        )

        uiState = uiState.copy(allProducts = mockProducts)
    }

    fun onSearchChange(query: String) {
        uiState = uiState.copy(search = query)
    }

    fun onCategorySelect(category: String) {
        uiState = uiState.copy(selectedCategory = category)
    }

    fun onPageSizeChange(size: Int) {
        uiState = uiState.copy(pageSize = size)
    }

    fun onPageChange(page: Int) {
        uiState = uiState.copy(page = page)
    }

    fun getFilteredProducts(): List<ProductUi> {
        var filtered = uiState.allProducts

        // Filter by search
        if (uiState.search.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(uiState.search, ignoreCase = true)
            }
        }

        // Filter by category
        if (uiState.selectedCategory != "Todas las categorías") {
            filtered = filtered.filter {
                it.category.equals(uiState.selectedCategory, ignoreCase = true)
            }
        }

        return filtered
    }

    fun onEditProduct(productId: String) {
        // TODO: Navigate to edit screen when implemented
    }

    fun onDeleteProduct(productId: String) {
        // TODO: Show confirmation dialog and delete when API is connected
    }
}

data class ProductsUiState(
    val search: String = "",
    val selectedCategory: String = "Todas las categorías",
    val page: Int = 1,
    val pageSize: Int = 10,
    val allProducts: List<ProductUi> = emptyList()
)

data class ProductUi(
    val id: String,
    val name: String,
    val category: String,
    val updatedAt: Instant
)

