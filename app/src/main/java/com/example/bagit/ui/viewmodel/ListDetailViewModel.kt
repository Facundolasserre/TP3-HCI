package com.example.bagit.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.*
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.ShoppingListRepository
import com.example.bagit.data.repository.ListItemRepository
import com.example.bagit.data.repository.ProductRepository
import com.example.bagit.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val listItemRepository: ListItemRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _currentListState = mutableStateOf<Result<ShoppingList>?>(null)
    val currentListState: State<Result<ShoppingList>?> = _currentListState

    private val _listItemsState = mutableStateOf<Result<PaginatedResponse<ListItem>>?>(null)
    val listItemsState: State<Result<PaginatedResponse<ListItem>>?> = _listItemsState

    private val _productsState = mutableStateOf<Result<PaginatedResponse<Product>>?>(null)
    val productsState: State<Result<PaginatedResponse<Product>>?> = _productsState

    private val _categoriesState = mutableStateOf<Result<PaginatedResponse<Category>>?>(null)
    val categoriesState: State<Result<PaginatedResponse<Category>>?> = _categoriesState

    private val _createProductState = mutableStateOf<Result<Product>?>(null)
    val createProductState: State<Result<Product>?> = _createProductState

    private val _isCreatingProduct = mutableStateOf(false)
    val isCreatingProduct: State<Boolean> = _isCreatingProduct

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // Mantener el estado de búsqueda actual para reloads
    private var currentListId: Long? = null
    private var currentSearchQuery: String? = null

    private val _searchFallbackActive = mutableStateOf(false)
    val searchFallbackActive: State<Boolean> = _searchFallbackActive

    fun loadList(listId: Long) {
        viewModelScope.launch {
            shoppingListRepository.getShoppingListById(listId).collect { result ->
                _currentListState.value = result
            }
        }
    }

    fun loadListItems(listId: Long, purchased: Boolean? = null, search: String? = null) {
        // Guardar el estado actual para reloads
        currentListId = listId
        currentSearchQuery = search
        _searchFallbackActive.value = false // reset flag on new attempt
        viewModelScope.launch {
            listItemRepository.getListItems(
                listId = listId,
                purchased = purchased,
                page = 1,
                perPage = 100, // Load all items for now
                search = search
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _listItemsState.value = result
                    }
                    is Result.Error -> {
                        // Si falla y había búsqueda, intentar fallback sin search una sola vez
                        if (search != null && !_searchFallbackActive.value) {
                            _searchFallbackActive.value = true
                            listItemRepository.getListItems(
                                listId = listId,
                                purchased = purchased,
                                page = 1,
                                perPage = 100,
                                search = null
                            ).collect { fallbackResult ->
                                _listItemsState.value = fallbackResult
                            }
                        } else {
                            _listItemsState.value = result
                        }
                    }
                    is Result.Loading -> {
                        _listItemsState.value = result
                    }
                }
            }
        }
    }

    // Método privado para recargar manteniendo la búsqueda actual
    private fun reloadCurrentList() {
        currentListId?.let { listId ->
            loadListItems(listId, search = currentSearchQuery)
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            productRepository.getProducts(
                name = query,
                categoryId = null,
                page = 1,
                perPage = 20
            ).collect { result ->
                _productsState.value = result
            }
        }
    }

    fun addListItem(
        listId: Long,
        productId: Long,
        quantity: Double,
        unit: String,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            listItemRepository.addListItem(
                listId = listId,
                request = ListItemRequest(
                    product = ProductId(productId),
                    quantity = quantity,
                    unit = unit,
                    metadata = metadata
                )
            ).collect { result ->
                if (result is Result.Success) {
                    // Reload list items manteniendo la búsqueda
                    reloadCurrentList()
                }
            }
        }
    }

    fun toggleItemPurchased(listId: Long, itemId: Long, purchased: Boolean) {
        viewModelScope.launch {
            listItemRepository.toggleListItemPurchased(
                listId = listId,
                itemId = itemId,
                purchased = purchased
            ).collect { result ->
                if (result is Result.Success) {
                    // Reload list items manteniendo la búsqueda
                    reloadCurrentList()
                }
            }
        }
    }

    fun deleteListItem(listId: Long, itemId: Long) {
        viewModelScope.launch {
            listItemRepository.deleteListItem(listId, itemId).collect { result ->
                if (result is Result.Success) {
                    // Reload list items manteniendo la búsqueda
                    reloadCurrentList()
                }
            }
        }
    }

    fun updateListItem(
        listId: Long,
        itemId: Long,
        quantity: Double? = null,
        unit: String? = null,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            listItemRepository.updateListItem(
                listId = listId,
                itemId = itemId,
                request = ListItemUpdateRequest(
                    quantity = quantity,
                    unit = unit,
                    metadata = metadata
                )
            ).collect { result ->
                if (result is Result.Success) {
                    // Reload list items manteniendo la búsqueda
                    reloadCurrentList()
                }
            }
        }
    }

    /**
     * Carga las categorías disponibles para el diálogo de creación de producto
     */
    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories(
                page = 1,
                perPage = 100,
                sortBy = "name",
                order = "ASC"
            ).collect { result ->
                _categoriesState.value = result
            }
        }
    }

    /**
     * Crea un nuevo producto sin agregarlo a ninguna lista.
     * Maneja correctamente el caso 409 (producto ya existe) buscando el producto existente.
     * Previene múltiples llamadas simultáneas.
     *
     * Útil para el flujo donde el usuario primero crea el producto y luego
     * ingresa cantidad/unidad antes de agregarlo.
     *
     * @param name Nombre del producto
     * @param categoryId ID de la categoría (opcional)
     * @param metadata Metadata adicional (opcional)
     */
    fun createProduct(
        name: String,
        categoryId: Long?,
        metadata: Map<String, Any>? = null
    ) {
        // Prevenir múltiples llamadas simultáneas
        if (_isCreatingProduct.value) {
            Log.d("ListDetailViewModel", "⚠️ Ya hay una creación en progreso, ignorando")
            return
        }

        viewModelScope.launch {
            Log.d("ListDetailViewModel", "🟢 INICIO: createProduct - name='$name'")
            _isCreatingProduct.value = true
            _errorMessage.value = null
            _createProductState.value = Result.Loading

            // Crear el producto o resolver el existente si hay 409
            val request = ProductRequest(
                name = name,
                category = categoryId?.let { CategoryId(it) },
                metadata = metadata
            )

            var createdProduct: Product? = null

            try {
                // Intentar crear el producto
                productRepository.createProduct(request).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            Log.d("ListDetailViewModel", "✅ Producto creado: ${result.data.name} (id=${result.data.id})")
                            createdProduct = result.data
                            _isCreatingProduct.value = false
                            _createProductState.value = Result.Success(result.data)
                        }
                        is Result.Error -> {
                            if (result.isConflict) {
                                // Producto ya existe (409) - buscar el existente
                                Log.d("ListDetailViewModel", "⚠️ Producto ya existe (409), buscando...")

                                productRepository.findProductByName(name).collect { findResult ->
                                    when (findResult) {
                                        is Result.Success -> {
                                            if (findResult.data != null) {
                                                Log.d("ListDetailViewModel", "✅ Producto existente encontrado: ${findResult.data.name} (id=${findResult.data.id})")
                                                createdProduct = findResult.data
                                                _isCreatingProduct.value = false
                                                _createProductState.value = Result.Success(findResult.data)
                                            } else {
                                                Log.e("ListDetailViewModel", "❌ No se pudo encontrar el producto existente")
                                                _isCreatingProduct.value = false
                                                _errorMessage.value = "El producto existe pero no se pudo encontrar"
                                                _createProductState.value = null
                                            }
                                        }
                                        is Result.Error -> {
                                            Log.e("ListDetailViewModel", "❌ Error buscando producto: ${findResult.message}")
                                            _isCreatingProduct.value = false
                                            _errorMessage.value = "Error buscando el producto existente"
                                            _createProductState.value = null
                                        }
                                        is Result.Loading -> {
                                            // Esperar
                                        }
                                    }
                                }
                            } else {
                                // Otro tipo de error
                                Log.e("ListDetailViewModel", "❌ Error creando producto: ${result.message}")
                                _isCreatingProduct.value = false
                                _errorMessage.value = result.message ?: "Error al crear el producto"
                                _createProductState.value = null
                            }
                        }
                        is Result.Loading -> {
                            // Esperar
                        }
                    }
                }
            } catch (e: CancellationException) {
                Log.d("ListDetailViewModel", "⚠️ Operación cancelada")
                throw e
            } catch (e: Exception) {
                Log.e("ListDetailViewModel", "❌ Excepción inesperada: ${e.message}", e)
                _isCreatingProduct.value = false
                _errorMessage.value = e.message ?: "Error inesperado"
                _createProductState.value = null
            }

            if (createdProduct != null) {
                Log.d("ListDetailViewModel", "🟢 COMPLETADO: Producto creado/encontrado exitosamente")
            }
        }
    }

    /**
     * Crea un nuevo producto y lo agrega automáticamente a la lista.
     * Maneja correctamente el caso 409 (producto ya existe) buscando el producto existente.
     * Previene múltiples llamadas simultáneas.
     *
     * @param listId ID de la lista a la que se agregará el producto
     * @param name Nombre del producto
     * @param categoryId ID de la categoría (opcional)
     * @param quantity Cantidad del producto
     * @param unit Unidad del producto
     * @param metadata Metadata adicional (opcional)
     */
    fun createProductAndAddToList(
        listId: Long,
        name: String,
        categoryId: Long?,
        quantity: Double,
        unit: String,
        metadata: Map<String, Any>? = null
    ) {
        // Prevenir múltiples llamadas simultáneas
        if (_isCreatingProduct.value) {
            Log.d("ListDetailViewModel", "⚠️ Ya hay una creación en progreso, ignorando")
            return
        }

        viewModelScope.launch {
            Log.d("ListDetailViewModel", "🟢 INICIO: createProductAndAddToList - name='$name', listId=$listId")
            _isCreatingProduct.value = true
            _errorMessage.value = null
            _createProductState.value = Result.Loading

            // Paso 1: Crear el producto o resolver el existente si hay 409
            val request = ProductRequest(
                name = name,
                category = categoryId?.let { CategoryId(it) },
                metadata = null
            )

            var productToAdd: Product? = null

            try {
                // Intentar crear el producto
                productRepository.createProduct(request).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            Log.d("ListDetailViewModel", "✅ Producto creado: ${result.data.name} (id=${result.data.id})")
                            productToAdd = result.data
                        }
                        is Result.Error -> {
                            if (result.isConflict) {
                                // Producto ya existe (409) - buscar el existente
                                Log.d("ListDetailViewModel", "⚠️ Producto ya existe (409), buscando...")

                                productRepository.findProductByName(name).collect { findResult ->
                                    when (findResult) {
                                        is Result.Success -> {
                                            if (findResult.data != null) {
                                                Log.d("ListDetailViewModel", "✅ Producto existente encontrado: ${findResult.data.name} (id=${findResult.data.id})")
                                                productToAdd = findResult.data
                                            } else {
                                                Log.e("ListDetailViewModel", "❌ No se pudo encontrar el producto existente")
                                                _isCreatingProduct.value = false
                                                _errorMessage.value = "El producto existe pero no se pudo encontrar"
                                                _createProductState.value = null
                                            }
                                        }
                                        is Result.Error -> {
                                            Log.e("ListDetailViewModel", "❌ Error buscando producto: ${findResult.message}")
                                            _isCreatingProduct.value = false
                                            _errorMessage.value = "Error buscando el producto existente"
                                            _createProductState.value = null
                                        }
                                        is Result.Loading -> {
                                            // Esperar
                                        }
                                    }
                                }
                            } else {
                                // Otro tipo de error
                                Log.e("ListDetailViewModel", "❌ Error creando producto: ${result.message}")
                                _isCreatingProduct.value = false
                                _errorMessage.value = result.message ?: "Error al crear el producto"
                                _createProductState.value = null
                            }
                        }
                        is Result.Loading -> {
                            // Esperar
                        }
                    }
                }
            } catch (e: CancellationException) {
                Log.d("ListDetailViewModel", "⚠️ Operación cancelada")
                throw e
            } catch (e: Exception) {
                Log.e("ListDetailViewModel", "❌ Excepción inesperada: ${e.message}", e)
                _isCreatingProduct.value = false
                _errorMessage.value = e.message ?: "Error inesperado"
                _createProductState.value = null
                return@launch
            }

            // Verificar que tengamos un producto
            if (productToAdd == null) {
                Log.d("ListDetailViewModel", "🛑 No se pudo obtener el producto, abortando")
                return@launch
            }

            Log.d("ListDetailViewModel", "➡️ Agregando producto ${productToAdd!!.id} a lista $listId")

            // Paso 2: Agregar el producto a la lista
            try {
                listItemRepository.addListItem(
                    listId = listId,
                    request = ListItemRequest(
                        product = ProductId(productToAdd!!.id),
                        quantity = quantity,
                        unit = unit,
                        metadata = metadata
                    )
                ).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            Log.d("ListDetailViewModel", "✅ Item agregado a la lista exitosamente")
                            _isCreatingProduct.value = false
                            _createProductState.value = Result.Success(productToAdd!!)

                            // Recargar la lista
                            Log.d("ListDetailViewModel", "🔄 Recargando lista de items...")
                            reloadCurrentList()

                            Log.d("ListDetailViewModel", "🟢 COMPLETADO: Flujo exitoso")
                        }
                        is Result.Error -> {
                            Log.e("ListDetailViewModel", "❌ Error agregando a lista: ${result.message}")
                            _isCreatingProduct.value = false
                            _errorMessage.value = result.message ?: "Error al agregar el producto a la lista"
                            _createProductState.value = null
                        }
                        is Result.Loading -> {
                            // Esperar
                        }
                    }
                }
            } catch (e: CancellationException) {
                Log.d("ListDetailViewModel", "⚠️ Operación cancelada")
                throw e
            } catch (e: Exception) {
                Log.e("ListDetailViewModel", "❌ Excepción al agregar item: ${e.message}", e)
                _isCreatingProduct.value = false
                _errorMessage.value = e.message ?: "Error inesperado"
                _createProductState.value = null
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Resetea el estado de creación de producto.
     * Útil cuando el usuario cierra el diálogo o cancela la operación.
     */
    fun resetCreateProductState() {
        _createProductState.value = null
        _isCreatingProduct.value = false
        _errorMessage.value = null
    }

    /**
     * Actualiza el nombre y otros datos de la lista.
     */
    fun updateList(
        listId: Long,
        name: String,
        description: String? = null,
        recurring: Boolean = false,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            shoppingListRepository.updateShoppingList(
                listId,
                ShoppingListRequest(
                    name = name,
                    description = description ?: "",
                    recurring = recurring,
                    metadata = metadata
                )
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Recargar la lista actualizada
                        loadList(listId)
                    }
                    is Result.Error -> {
                        // El error se puede manejar si es necesario
                    }
                    is Result.Loading -> {
                        // Estado de carga
                    }
                }
            }
        }
    }
}
