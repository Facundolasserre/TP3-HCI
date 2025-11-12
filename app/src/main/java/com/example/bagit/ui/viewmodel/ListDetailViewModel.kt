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

    fun loadList(listId: Long) {
        viewModelScope.launch {
            shoppingListRepository.getShoppingListById(listId).collect { result ->
                _currentListState.value = result
            }
        }
    }

    fun loadListItems(listId: Long, purchased: Boolean? = null) {
        viewModelScope.launch {
            listItemRepository.getListItems(
                listId = listId,
                purchased = purchased,
                page = 1,
                perPage = 100 // Load all items for now
            ).collect { result ->
                _listItemsState.value = result
            }
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
                    // Reload list items
                    loadListItems(listId)
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
                    // Reload list items
                    loadListItems(listId)
                }
            }
        }
    }

    fun deleteListItem(listId: Long, itemId: Long) {
        viewModelScope.launch {
            listItemRepository.deleteListItem(listId, itemId).collect { result ->
                if (result is Result.Success) {
                    // Reload list items
                    loadListItems(listId)
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
                    // Reload list items
                    loadListItems(listId)
                }
            }
        }
    }
}

