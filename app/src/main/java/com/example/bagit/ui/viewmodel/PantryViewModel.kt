package com.example.bagit.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.*
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.PantryRepository
import com.example.bagit.data.repository.PantryItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val pantryRepository: PantryRepository,
    private val pantryItemRepository: PantryItemRepository
) : ViewModel() {

    private val _pantriesState = mutableStateOf<Result<PaginatedResponse<Pantry>>?>(null)
    val pantriesState: State<Result<PaginatedResponse<Pantry>>?> = _pantriesState

    private val _currentPantryState = mutableStateOf<Result<Pantry>?>(null)
    val currentPantryState: State<Result<Pantry>?> = _currentPantryState

    private val _pantryItemsState = mutableStateOf<Result<PaginatedResponse<PantryItem>>?>(null)
    val pantryItemsState: State<Result<PaginatedResponse<PantryItem>>?> = _pantryItemsState

    fun getPantries(owner: Boolean? = null, page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch {
            pantryRepository.getPantries(owner, page, perPage).collect { result ->
                _pantriesState.value = result
            }
        }
    }

    fun getPantryById(id: Long) {
        viewModelScope.launch {
            pantryRepository.getPantryById(id).collect { result ->
                _currentPantryState.value = result
            }
        }
    }

    fun createPantry(name: String, metadata: Map<String, Any>? = null) {
        viewModelScope.launch {
            pantryRepository.createPantry(PantryRequest(name, metadata)).collect { result ->
                if (result is Result.Success) {
                    getPantries()
                }
            }
        }
    }

    fun updatePantry(id: Long, name: String, metadata: Map<String, Any>? = null) {
        viewModelScope.launch {
            pantryRepository.updatePantry(id, PantryRequest(name, metadata)).collect { result ->
                _currentPantryState.value = result
            }
        }
    }

    fun deletePantry(id: Long) {
        viewModelScope.launch {
            pantryRepository.deletePantry(id).collect { result ->
                if (result is Result.Success) {
                    getPantries()
                }
            }
        }
    }

    fun sharePantry(id: Long, email: String) {
        viewModelScope.launch {
            pantryRepository.sharePantry(id, email).collect { /* handle result */ }
        }
    }

    fun revokeShare(id: Long, userId: Long) {
        viewModelScope.launch {
            pantryRepository.revokeSharePantry(id, userId).collect { /* handle result */ }
        }
    }

    // Pantry Items
    fun getPantryItems(pantryId: Long, page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch {
            pantryItemRepository.getPantryItems(pantryId, page, perPage).collect { result ->
                _pantryItemsState.value = result
            }
        }
    }

    fun addPantryItem(
        pantryId: Long,
        productId: Long,
        quantity: Double,
        unit: String,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            pantryItemRepository.addPantryItem(
                pantryId,
                PantryItemRequest(ProductId(productId), quantity, unit, metadata)
            ).collect { result ->
                if (result is Result.Success) {
                    getPantryItems(pantryId)
                }
            }
        }
    }

    fun updatePantryItem(
        pantryId: Long,
        itemId: Long,
        quantity: Double? = null,
        unit: String? = null,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            pantryItemRepository.updatePantryItem(
                pantryId,
                itemId,
                PantryItemUpdateRequest(quantity, unit, metadata)
            ).collect { result ->
                if (result is Result.Success) {
                    getPantryItems(pantryId)
                }
            }
        }
    }

    fun deletePantryItem(pantryId: Long, itemId: Long) {
        viewModelScope.launch {
            pantryItemRepository.deletePantryItem(pantryId, itemId).collect { result ->
                if (result is Result.Success) {
                    getPantryItems(pantryId)
                }
            }
        }
    }
}

