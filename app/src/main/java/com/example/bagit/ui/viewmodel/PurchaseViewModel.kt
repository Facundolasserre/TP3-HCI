package com.example.bagit.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.*
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _purchasesState = mutableStateOf<Result<PaginatedResponse<Purchase>>?>(null)
    val purchasesState: State<Result<PaginatedResponse<Purchase>>?> = _purchasesState

    private val _currentPurchaseState = mutableStateOf<Result<Purchase>?>(null)
    val currentPurchaseState: State<Result<Purchase>?> = _currentPurchaseState

    fun getPurchases(listId: Long? = null, page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch {
            purchaseRepository.getPurchases(listId, page, perPage).collect { result ->
                _purchasesState.value = result
            }
        }
    }

    fun getPurchaseById(id: Long) {
        viewModelScope.launch {
            purchaseRepository.getPurchaseById(id).collect { result ->
                _currentPurchaseState.value = result
            }
        }
    }

    fun restorePurchase(id: Long) {
        viewModelScope.launch {
            purchaseRepository.restorePurchase(id).collect { /* handle result */ }
        }
    }
}

