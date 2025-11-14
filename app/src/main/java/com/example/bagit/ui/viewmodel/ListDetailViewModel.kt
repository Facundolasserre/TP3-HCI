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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val listItemRepository: ListItemRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _currentListState = mutableStateOf<Result<ShoppingList>?>(null)
    val currentListState: State<Result<ShoppingList>?> = _currentListState

    private val _listItemsState = mutableStateOf<Result<PaginatedResponse<ListItem>>?>(null)
    val listItemsState: State<Result<PaginatedResponse<ListItem>>?> = _listItemsState

    private val _productsState = mutableStateOf<Result<PaginatedResponse<Product>>?>(null)
    val productsState: State<Result<PaginatedResponse<Product>>?> = _productsState

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
}
