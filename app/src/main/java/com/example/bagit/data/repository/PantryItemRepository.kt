package com.example.bagit.data.repository

import com.example.bagit.data.model.*
import com.example.bagit.data.remote.PantryItemApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryItemRepository @Inject constructor(
    private val pantryItemApiService: PantryItemApiService
) {

    suspend fun addPantryItem(pantryId: Long, request: PantryItemRequest): Flow<Result<PantryItem>> = flow {
        emit(Result.Loading)
        try {
            val item = pantryItemApiService.addPantryItem(pantryId, request)
            emit(Result.Success(item))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getPantryItems(
        pantryId: Long,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String? = null,
        order: String = "DESC",
        search: String? = null,
        categoryId: Long? = null
    ): Flow<Result<PaginatedResponse<PantryItem>>> = flow {
        emit(Result.Loading)
        try {
            val response = pantryItemApiService.getPantryItems(
                pantryId, page, perPage, sortBy, order, search, categoryId
            )
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun updatePantryItem(
        pantryId: Long,
        itemId: Long,
        request: PantryItemUpdateRequest
    ): Flow<Result<PantryItem>> = flow {
        emit(Result.Loading)
        try {
            val item = pantryItemApiService.updatePantryItem(pantryId, itemId, request)
            emit(Result.Success(item))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun deletePantryItem(pantryId: Long, itemId: Long): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            pantryItemApiService.deletePantryItem(pantryId, itemId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }
}

