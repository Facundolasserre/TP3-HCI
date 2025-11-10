package com.example.bagit.data.repository

import com.example.bagit.data.model.*
import com.example.bagit.data.remote.ListItemApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListItemRepository @Inject constructor(
    private val listItemApiService: ListItemApiService
) {

    suspend fun addListItem(listId: Long, request: ListItemRequest): Flow<Result<ListItem>> = flow {
        emit(Result.Loading)
        try {
            val item = listItemApiService.addListItem(listId, request)
            emit(Result.Success(item))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getListItems(
        listId: Long,
        purchased: Boolean? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "createdAt",
        order: String = "DESC",
        pantryId: Long? = null,
        categoryId: Long? = null,
        search: String? = null
    ): Flow<Result<PaginatedResponse<ListItem>>> = flow {
        emit(Result.Loading)
        try {
            val response = listItemApiService.getListItems(
                listId, purchased, page, perPage, sortBy, order, pantryId, categoryId, search
            )
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun updateListItem(
        listId: Long,
        itemId: Long,
        request: ListItemUpdateRequest
    ): Flow<Result<ListItem>> = flow {
        emit(Result.Loading)
        try {
            val item = listItemApiService.updateListItem(listId, itemId, request)
            emit(Result.Success(item))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun toggleListItemPurchased(
        listId: Long,
        itemId: Long,
        purchased: Boolean? = null
    ): Flow<Result<ListItem>> = flow {
        emit(Result.Loading)
        try {
            val item = listItemApiService.toggleListItemPurchased(listId, itemId, TogglePurchasedRequest(purchased))
            emit(Result.Success(item))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun deleteListItem(listId: Long, itemId: Long): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            listItemApiService.deleteListItem(listId, itemId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }
}

