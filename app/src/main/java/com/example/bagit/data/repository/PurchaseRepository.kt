package com.example.bagit.data.repository

import com.example.bagit.data.model.*
import com.example.bagit.data.remote.PurchaseApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val purchaseApiService: PurchaseApiService
) {

    suspend fun getPurchases(
        listId: Long? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "createdAt",
        order: String = "DESC"
    ): Flow<Result<PaginatedResponse<Purchase>>> = flow {
        emit(Result.Loading)
        try {
            val response = purchaseApiService.getPurchases(listId, page, perPage, sortBy, order)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getPurchaseById(id: Long): Flow<Result<Purchase>> = flow {
        emit(Result.Loading)
        try {
            val purchase = purchaseApiService.getPurchaseById(id)
            emit(Result.Success(purchase))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun restorePurchase(id: Long): Flow<Result<ShoppingList>> = flow {
        emit(Result.Loading)
        try {
            val list = purchaseApiService.restorePurchase(id)
            emit(Result.Success(list))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }
}

