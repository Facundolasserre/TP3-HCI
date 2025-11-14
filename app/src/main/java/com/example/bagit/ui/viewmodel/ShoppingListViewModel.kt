package com.example.bagit.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.*
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.ShoppingListRepository
import com.example.bagit.data.repository.ListItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val listItemRepository: ListItemRepository
) : ViewModel() {

    private val _listsState = mutableStateOf<Result<PaginatedResponse<ShoppingList>>?>(null)
    val listsState: State<Result<PaginatedResponse<ShoppingList>>?> = _listsState

    private val _currentListState = mutableStateOf<Result<ShoppingList>?>(null)
    val currentListState: State<Result<ShoppingList>?> = _currentListState

    private val _listItemsState = mutableStateOf<Result<PaginatedResponse<ListItem>>?>(null)
    val listItemsState: State<Result<PaginatedResponse<ListItem>>?> = _listItemsState

    fun getShoppingLists(
        name: String? = null,
        owner: Boolean? = null,
        recurring: Boolean? = null,
        page: Int = 1,
        perPage: Int = 10
    ) {
        viewModelScope.launch {
            shoppingListRepository.getShoppingLists(name, owner, recurring, page, perPage)
                .collect { result ->
                    _listsState.value = result
                }
        }
    }

    fun getShoppingListById(id: Long) {
        viewModelScope.launch {
            shoppingListRepository.getShoppingListById(id).collect { result ->
                _currentListState.value = result
            }
        }
    }

    fun createShoppingList(
        name: String,
        description: String? = null,
        recurring: Boolean = false,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            shoppingListRepository.createShoppingList(
                ShoppingListRequest(name, description ?: "", recurring, metadata)
            ).collect { result ->
                if (result is Result.Success) {
                    // Refresh lists
                    getShoppingLists()
                }
            }
        }
    }

    fun updateShoppingList(
        id: Long,
        name: String,
        description: String? = null,
        recurring: Boolean = false,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            shoppingListRepository.updateShoppingList(
                id,
                ShoppingListRequest(name, description ?: "", recurring, metadata)
            ).collect { result ->
                _currentListState.value = result
            }
        }
    }

    fun deleteShoppingList(id: Long) {
        viewModelScope.launch {
            shoppingListRepository.deleteShoppingList(id).collect { result ->
                if (result is Result.Success) {
                    getShoppingLists()
                }
            }
        }
    }

    fun purchaseShoppingList(id: Long, metadata: Map<String, Any>? = null) {
        viewModelScope.launch {
            shoppingListRepository.purchaseShoppingList(id, metadata).collect { result ->
                _currentListState.value = result
            }
        }
    }

    fun resetShoppingList(id: Long) {
        viewModelScope.launch {
            shoppingListRepository.resetShoppingList(id).collect { /* handle result */ }
        }
    }

    fun moveToPantry(id: Long) {
        viewModelScope.launch {
            shoppingListRepository.moveToPantry(id).collect { /* handle result */ }
        }
    }

    fun shareShoppingList(id: Long, email: String) {
        viewModelScope.launch {
            shoppingListRepository.shareShoppingList(id, email).collect { /* handle result */ }
        }
    }

    fun revokeShare(id: Long, userId: Long) {
        viewModelScope.launch {
            shoppingListRepository.revokeShareShoppingList(id, userId).collect { /* handle result */ }
        }
    }

    // List Items
    fun getListItems(
        listId: Long,
        purchased: Boolean? = null,
        page: Int = 1,
        perPage: Int = 10
    ) {
        viewModelScope.launch {
            listItemRepository.getListItems(listId, purchased, page, perPage)
                .collect { result ->
                    _listItemsState.value = result
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
                listId,
                ListItemRequest(ProductId(productId), quantity, unit, metadata)
            ).collect { result ->
                if (result is Result.Success) {
                    getListItems(listId)
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
                listId,
                itemId,
                ListItemUpdateRequest(quantity, unit, metadata)
            ).collect { result ->
                if (result is Result.Success) {
                    getListItems(listId)
                }
            }
        }
    }

    fun toggleItemPurchased(listId: Long, itemId: Long, purchased: Boolean? = null) {
        viewModelScope.launch {
            listItemRepository.toggleListItemPurchased(listId, itemId, purchased)
                .collect { result ->
                    if (result is Result.Success) {
                        getListItems(listId)
                    }
                }
        }
    }

    fun deleteListItem(listId: Long, itemId: Long) {
        viewModelScope.launch {
            listItemRepository.deleteListItem(listId, itemId).collect { result ->
                if (result is Result.Success) {
                    getListItems(listId)
                }
            }
        }
    }

    // Favorites
    fun toggleFavorite(listId: Long, currentIsFavorite: Boolean) {
        viewModelScope.launch {
            // Actualización optimista: actualizar el estado local primero
            val currentLists = _listsState.value
            if (currentLists is Result.Success) {
                val updatedLists = currentLists.data.data.map { list ->
                    if (list.id == listId) {
                        val updatedMetadata = list.metadata?.toMutableMap() ?: mutableMapOf()
                        updatedMetadata["favorite"] = !currentIsFavorite
                        list.copy(metadata = updatedMetadata)
                    } else {
                        list
                    }
                }
                _listsState.value = Result.Success(
                    currentLists.data.copy(data = updatedLists)
                )
            }

            // Llamar a la API
            shoppingListRepository.setFavorite(listId, !currentIsFavorite).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Actualizar con la respuesta del servidor
                        val currentListsState = _listsState.value
                        if (currentListsState is Result.Success) {
                            val updatedLists = currentListsState.data.data.map { list ->
                                if (list.id == listId) result.data else list
                            }
                            _listsState.value = Result.Success(
                                currentListsState.data.copy(data = updatedLists)
                            )
                        }
                    }
                    is Result.Error -> {
                        // Revertir cambio optimista en caso de error
                        val currentListsState = _listsState.value
                        if (currentListsState is Result.Success) {
                            val revertedLists = currentListsState.data.data.map { list ->
                                if (list.id == listId) {
                                    val revertedMetadata = list.metadata?.toMutableMap() ?: mutableMapOf()
                                    revertedMetadata["favorite"] = currentIsFavorite
                                    list.copy(metadata = revertedMetadata)
                                } else {
                                    list
                                }
                            }
                            _listsState.value = Result.Success(
                                currentListsState.data.copy(data = revertedLists)
                            )
                        }
                        // TODO: Mostrar mensaje de error al usuario (snackbar)
                    }
                    is Result.Loading -> {
                        // Ya manejado con actualización optimista
                    }
                }
            }
        }
    }

    fun getFavoriteLists() {
        viewModelScope.launch {
            shoppingListRepository.getFavoriteLists(page = 1, perPage = 100)
                .collect { result ->
                    _listsState.value = result
                }
        }
    }

    // Helper function to check if a list is favorite
    fun isFavorite(list: ShoppingList): Boolean {
        val metadata = list.metadata
        val favoriteValue = metadata?.get("favorite")
        return when (favoriteValue) {
            is Boolean -> favoriteValue
            is String -> favoriteValue.equals("true", ignoreCase = true)
            is Number -> favoriteValue.toInt() != 0
            else -> false
        }
    }
}

