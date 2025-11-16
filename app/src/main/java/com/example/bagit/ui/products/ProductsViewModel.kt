package com.example.bagit.ui.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.CategoryId
import com.example.bagit.data.model.Product
import com.example.bagit.data.model.ProductRequest
import com.example.bagit.data.repository.CategoryRepository
import com.example.bagit.data.repository.PreferencesRepository
import com.example.bagit.data.repository.ProductRepository
import com.example.bagit.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ProductsViewModel
 *
 * Maneja el estado de la pantalla de productos conectado a la API real.
 * Soporta búsqueda con debounce, filtros, paginación y operaciones CRUD.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var uiState by mutableStateOf<ProductsUiState>(ProductsUiState.Loading)
        private set

    var dialogState by mutableStateOf(ProductDialogState())
        private set

    private val searchQueryFlow = MutableStateFlow("")
    private var searchJob: Job? = null
    
    // Cache de categorías que tienen productos asignados
    private val categoriesWithProducts = mutableSetOf<Long>()

    init {
        loadCategories()
        loadProducts()
        setupSearchDebounce()
        // Cargar todos los productos inicialmente para construir el cache de categorías
        buildCategoryCache()
    }
    
    /**
     * Construye el cache de categorías cargando todos los productos (sin filtros)
     */
    private fun buildCategoryCache() {
        viewModelScope.launch {
            productRepository.getProducts(
                name = null,
                categoryId = null,
                page = 1,
                perPage = 100 // Cargar muchos productos para construir el cache
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Actualizar cache de categorías
                        result.data.data.forEach { product ->
                            product.category?.id?.let { categoryId ->
                                categoriesWithProducts.add(categoryId)
                            }
                        }
                        // Actualizar el estado si existe
                        val currentState = uiState
                        if (currentState is ProductsUiState.Success) {
                            uiState = currentState.copy(availableCategoryIds = categoriesWithProducts.toSet())
                        }
                    }
                    else -> {
                        // Ignorar errores en la construcción del cache
                    }
                }
            }
        }
    }

    /**
     * Configura el debounce para la búsqueda (500ms)
     */
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(500)
                .collect { query ->
                    loadProducts(searchQuery = query)
                }
        }
    }

    /**
     * Carga las categorías desde la API
     */
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories(
                page = 1,
                perPage = 100, // Cargar todas las categorías
                sortBy = "name",
                order = "ASC"
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Actualizar categorías en el estado Success si existe
                        val currentState = uiState
                        if (currentState is ProductsUiState.Success) {
                            uiState = currentState.copy(categories = result.data.data)
                        }
                    }
                    else -> {
                        // Ignorar errores en la carga de categorías
                    }
                }
            }
        }
    }

    /**
     * Carga productos con los filtros actuales
     */
    fun loadProducts(
        searchQuery: String? = null,
        categoryId: Long? = null,
        page: Int? = null,
        pageSize: Int? = null,
        sortBy: String? = null,
        order: String? = null
    ) {
        val currentState = uiState

        // Determinar valores a usar
        val query = searchQuery ?: (currentState as? ProductsUiState.Success)?.searchQuery ?: ""
        val catId = categoryId ?: (currentState as? ProductsUiState.Success)?.selectedCategoryId
        val pg = page ?: (currentState as? ProductsUiState.Success)?.currentPage ?: 1
        val size = pageSize ?: (currentState as? ProductsUiState.Success)?.pageSize ?: 10
        val sort = sortBy ?: (currentState as? ProductsUiState.Success)?.sortBy ?: "name"
        val ord = order ?: (currentState as? ProductsUiState.Success)?.sortOrder ?: "ASC"

        viewModelScope.launch {
            productRepository.getProducts(
                name = query.ifBlank { null },
                categoryId = catId,
                page = pg,
                perPage = size,
                sortBy = sort,
                order = ord
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        if (currentState !is ProductsUiState.Success) {
                            uiState = ProductsUiState.Loading
                        } else {
                            uiState = currentState.copy(isRefreshing = true)
                        }
                    }
                    is Result.Success -> {
                        val categories = (currentState as? ProductsUiState.Success)?.categories ?: emptyList()
                        
                        // Actualizar cache de categorías que tienen productos
                        result.data.data.forEach { product ->
                            product.category?.id?.let { categoryId ->
                                categoriesWithProducts.add(categoryId)
                            }
                        }

                        if (result.data.data.isEmpty() && pg == 1) {
                            uiState = ProductsUiState.Empty
                        } else {
                            uiState = ProductsUiState.Success(
                                products = result.data.data,
                                pagination = result.data.pagination,
                                categories = categories,
                                availableCategoryIds = categoriesWithProducts.toSet(),
                                searchQuery = query,
                                selectedCategoryId = catId,
                                pageSize = size,
                                currentPage = pg,
                                sortBy = sort,
                                sortOrder = ord,
                                isRefreshing = false
                            )
                        }
                    }
                    is Result.Error -> {
                        uiState = ProductsUiState.Error(
                            message = result.message ?: "Error al cargar productos",
                            exception = result.exception
                        )
                    }
                }
            }
        }
    }

    /**
     * Actualiza la búsqueda con debounce
     */
    fun onSearchChange(query: String) {
        searchQueryFlow.value = query

        // Actualizar el estado inmediatamente para la UI
        val currentState = uiState
        if (currentState is ProductsUiState.Success) {
            uiState = currentState.copy(searchQuery = query)
        }
    }

    /**
     * Cambia el filtro de categoría
     */
    fun onCategorySelect(categoryId: Long?) {
        loadProducts(categoryId = categoryId, page = 1)
    }

    /**
     * Cambia el tamaño de página
     */
    fun onPageSizeChange(size: Int) {
        loadProducts(pageSize = size, page = 1)
    }

    /**
     * Cambia de página
     */
    fun onPageChange(page: Int) {
        loadProducts(page = page)
    }

    /**
     * Refresca los productos (pull-to-refresh)
     */
    fun refreshProducts() {
        val currentState = uiState
        if (currentState is ProductsUiState.Success) {
            uiState = currentState.copy(isRefreshing = true)
        }
        loadProducts(page = 1)
    }

    /**
     * Muestra el diálogo de crear producto
     */
    fun showCreateDialog() {
        dialogState = dialogState.copy(
            showCreateDialog = true,
            selectedProduct = null
        )
    }

    /**
     * Muestra el diálogo de editar producto
     */
    fun showEditDialog(product: Product) {
        dialogState = dialogState.copy(
            showEditDialog = true,
            selectedProduct = product
        )
    }

    /**
     * Muestra el diálogo de eliminar producto
     */
    fun showDeleteDialog(product: Product) {
        dialogState = dialogState.copy(
            showDeleteDialog = true,
            selectedProduct = product
        )
    }

    /**
     * Cierra todos los diálogos
     */
    fun dismissDialogs() {
        dialogState = ProductDialogState()
    }

    /**
     * Crea un nuevo producto
     */
    fun createProduct(name: String, categoryId: Long?, metadata: Map<String, Any>? = null) {
        viewModelScope.launch {
            dialogState = dialogState.copy(isSubmitting = true)

            val request = ProductRequest(
                name = name,
                category = categoryId?.let { CategoryId(it) },
                metadata = metadata
            )

            productRepository.createProduct(request).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Actualizar cache si el producto tiene categoría
                        result.data.category?.id?.let { categoryId ->
                            categoriesWithProducts.add(categoryId)
                        }
                        dismissDialogs()
                        refreshProducts()
                    }
                    is Result.Error -> {
                        dialogState = dialogState.copy(isSubmitting = false)
                        // TODO: Mostrar error en diálogo
                    }
                    is Result.Loading -> {
                        // Ya está en isSubmitting = true
                    }
                }
            }
        }
    }

    /**
     * Actualiza un producto existente
     */
    fun updateProduct(productId: Long, name: String, categoryId: Long?, metadata: Map<String, Any>? = null) {
        viewModelScope.launch {
            dialogState = dialogState.copy(isSubmitting = true)

            val request = ProductRequest(
                name = name,
                category = categoryId?.let { CategoryId(it) },
                metadata = metadata
            )

            productRepository.updateProduct(productId, request).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Actualizar cache si el producto tiene categoría
                        result.data.category?.id?.let { categoryId ->
                            categoriesWithProducts.add(categoryId)
                        }
                        dismissDialogs()
                        refreshProducts()
                    }
                    is Result.Error -> {
                        dialogState = dialogState.copy(isSubmitting = false)
                        // TODO: Mostrar error en diálogo
                    }
                    is Result.Loading -> {
                        // Ya está en isSubmitting = true
                    }
                }
            }
        }
    }

    /**
     * Elimina un producto
     */
    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            dialogState = dialogState.copy(isSubmitting = true)

            productRepository.deleteProduct(productId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        dismissDialogs()
                        refreshProducts()
                    }
                    is Result.Error -> {
                        dialogState = dialogState.copy(isSubmitting = false)
                        // TODO: Mostrar error en diálogo
                    }
                    is Result.Loading -> {
                        // Ya está en isSubmitting = true
                    }
                }
            }
        }
    }

    /**
     * Reintenta cargar productos después de un error
     */
    fun retry() {
        loadProducts(page = 1)
    }
}

