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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
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
     * Crea un nuevo producto y lo agrega automáticamente a la lista
     * 
     * Manejo seguro de Flow sin violar la transparencia de excepciones:
     * - Usa collect con flag para detener después del primer resultado no-Loading
     * - NO usa take() que puede causar cancelaciones prematuras y AbortFlowException
     * - catch maneja excepciones sin re-emitir (solo actualiza estado)
     * - Actualiza el estado inmediatamente cuando hay éxito
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
        viewModelScope.launch {
            Log.d("ListDetailViewModel", "🟢 INICIO: createProductAndAddToList - name=$name, listId=$listId")
            _isCreatingProduct.value = true
            _errorMessage.value = null
            _createProductState.value = Result.Loading

            // Paso 1: Crear el producto
            val request = ProductRequest(
                name = name,
                category = categoryId?.let { CategoryId(it) },
                metadata = null
            )

            // CRÍTICO: Usamos first() con filtro para obtener el primer resultado no-Loading
            // first() puede lanzar AbortFlowException cuando encuentra el resultado, pero eso es normal
            // El catch maneja excepciones reales sin re-emitir
            var createResult: Result<Product>? = null
            
            try {
                // Usar first() con filtro para obtener el primer resultado que no sea Loading
                // Esto es más eficiente que collect y evita problemas de cancelación
                createResult = productRepository.createProduct(request)
                    .catch { throwable ->
                        // CRÍTICO: catch NO debe re-emitir, solo manejar la excepción
                        // Si es CancellationException (incluye AbortFlowException de first()), re-lanzar
                        if (throwable is CancellationException) {
                            Log.d("ListDetailViewModel", "⚠️ Operación cancelada (puede ser AbortFlowException normal de first())")
                            throw throwable // Re-lanzar para cancelación correcta
                        }
                        
                        // Solo manejar excepciones reales, actualizar estado sin re-emitir
                        Log.e("ListDetailViewModel", "❌ Error en creación de producto: ${throwable.message}")
                        _isCreatingProduct.value = false
                        val exception = throwable as? Exception ?: Exception(throwable.message, throwable)
                        _errorMessage.value = exception.message ?: "Error al crear el producto"
                        _createProductState.value = null
                        // NO re-emitir, solo actualizar estado local
                    }
                    .first { it !is Result.Loading } // Obtener primer resultado no-Loading
                
                // Procesar el resultado obtenido
                when (createResult) {
                    is Result.Success -> {
                        Log.d("ListDetailViewModel", "✅ Producto creado exitosamente: ${createResult.data.name} (id=${createResult.data.id})")
                        _createProductState.value = createResult
                    }
                    is Result.Error -> {
                        Log.e("ListDetailViewModel", "❌ Error en resultado: ${createResult.message}")
                        _isCreatingProduct.value = false
                        _errorMessage.value = createResult.message ?: "Error al crear el producto"
                        _createProductState.value = null
                    }
                    is Result.Loading -> {
                        // No debería llegar aquí por el filtro first()
                        Log.w("ListDetailViewModel", "⚠️ Resultado inesperado: Loading")
                    }
                }
            } catch (e: CancellationException) {
                // CancellationException incluye AbortFlowException (que es interna)
                // AbortFlowException es normal cuando first() encuentra el resultado
                // No es un error, solo significa que first() completó su trabajo
                // Verificamos si tenemos un resultado antes de considerar esto un error
                if (createResult != null) {
                    Log.d("ListDetailViewModel", "ℹ️ CancellationException capturada (normal, probablemente AbortFlowException de first())")
                    // Continuar con el resultado que ya tenemos
                } else {
                    // Si no hay resultado, es una cancelación real
                    Log.d("ListDetailViewModel", "⚠️ Operación cancelada sin resultado")
                    throw e // Re-lanzar para cancelación correcta
                }
            } catch (e: Exception) {
                Log.e("ListDetailViewModel", "❌ Excepción inesperada: ${e.message}")
                _isCreatingProduct.value = false
                _errorMessage.value = e.message ?: "Error inesperado"
                _createProductState.value = null
                return@launch
            }

            // Si hubo error en la creación, no continuar
            if (createResult !is Result.Success) {
                Log.d("ListDetailViewModel", "🛑 Deteniendo: error en creación de producto")
                return@launch
            }

            val createdProduct = (createResult as Result.Success<Product>).data
            Log.d("ListDetailViewModel", "➡️ Continuando: agregando producto ${createdProduct.id} a lista $listId")

            // Paso 2: Agregar el producto a la lista
            // Mismo patrón seguro: first() con filtro
            var addResult: Result<ListItem>? = null
            
            try {
                addResult = listItemRepository.addListItem(
                    listId = listId,
                    request = ListItemRequest(
                        product = ProductId(createdProduct.id),
                        quantity = quantity,
                        unit = unit,
                        metadata = metadata
                    )
                )
                    .catch { throwable ->
                        // CRÍTICO: catch NO debe re-emitir, solo manejar la excepción
                        // Si es CancellationException (incluye AbortFlowException de first()), re-lanzar
                        if (throwable is CancellationException) {
                            Log.d("ListDetailViewModel", "⚠️ Operación cancelada (puede ser AbortFlowException normal de first())")
                            throw throwable
                        }
                        
                        Log.e("ListDetailViewModel", "❌ Error al agregar a lista: ${throwable.message}")
                        _isCreatingProduct.value = false
                        val exception = throwable as? Exception ?: Exception(throwable.message, throwable)
                        _errorMessage.value = exception.message ?: "Error al agregar el producto a la lista"
                        _createProductState.value = null
                        // NO re-emitir, solo actualizar estado local
                    }
                    .first { it !is Result.Loading } // Obtener primer resultado no-Loading
                
                // Procesar el resultado obtenido
                when (addResult) {
                    is Result.Success -> {
                        Log.d("ListDetailViewModel", "✅ Producto agregado a lista exitosamente")
                        _isCreatingProduct.value = false
                        // ACTUALIZACIÓN INMEDIATA: recargar la lista de items
                        Log.d("ListDetailViewModel", "🔄 Recargando lista de items...")
                        reloadCurrentList()
                        // Mantener éxito para que la UI sepa que se completó
                        _createProductState.value = Result.Success(createdProduct)
                        Log.d("ListDetailViewModel", "🟢 COMPLETADO: Producto creado y agregado exitosamente")
                    }
                    is Result.Error -> {
                        Log.e("ListDetailViewModel", "❌ Error en resultado de agregar: ${addResult.message}")
                        _isCreatingProduct.value = false
                        _errorMessage.value = addResult.message ?: "Error al agregar el producto a la lista"
                        _createProductState.value = null
                    }
                    is Result.Loading -> {
                        // No debería llegar aquí por el filtro first()
                        Log.w("ListDetailViewModel", "⚠️ Resultado inesperado: Loading")
                    }
                }
            } catch (e: CancellationException) {
                // CancellationException incluye AbortFlowException (que es interna)
                // AbortFlowException es normal cuando first() encuentra el resultado
                // Verificamos si tenemos un resultado antes de considerar esto un error
                if (addResult != null) {
                    Log.d("ListDetailViewModel", "ℹ️ CancellationException capturada (normal, probablemente AbortFlowException de first())")
                    // Continuar con el resultado que ya tenemos
                    if (addResult is Result.Success) {
                        _isCreatingProduct.value = false
                        Log.d("ListDetailViewModel", "🔄 Recargando lista de items...")
                        reloadCurrentList()
                        _createProductState.value = Result.Success(createdProduct)
                        Log.d("ListDetailViewModel", "🟢 COMPLETADO: Producto creado y agregado exitosamente")
                    }
                } else {
                    // Si no hay resultado, es una cancelación real
                    Log.d("ListDetailViewModel", "⚠️ Operación cancelada sin resultado")
                    throw e // Re-lanzar para cancelación correcta
                }
            } catch (e: Exception) {
                Log.e("ListDetailViewModel", "❌ Excepción inesperada al agregar: ${e.message}")
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
}
