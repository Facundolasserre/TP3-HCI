package com.example.bagit.data.repository

import com.example.bagit.data.model.*
import com.example.bagit.data.remote.ShoppingListApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    private val shoppingListApiService: ShoppingListApiService
) {

    suspend fun createShoppingList(request: ShoppingListRequest): Flow<Result<ShoppingList>> = flow {
        emit(Result.Loading)
        try {
            val list = shoppingListApiService.createShoppingList(request)
            emit(Result.Success(list))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getShoppingLists(
        name: String? = null,
        owner: Boolean? = null,
        recurring: Boolean? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "name",
        order: String = "ASC"
    ): Flow<Result<PaginatedResponse<ShoppingList>>> = flow {
        emit(Result.Loading)
        try {
            val response = shoppingListApiService.getShoppingLists(name, owner, recurring, page, perPage, sortBy, order)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getShoppingListById(id: Long): Flow<Result<ShoppingList>> = flow {
        emit(Result.Loading)
        try {
            val list = shoppingListApiService.getShoppingListById(id)
            emit(Result.Success(list))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun updateShoppingList(id: Long, request: ShoppingListRequest): Flow<Result<ShoppingList>> = flow {
        emit(Result.Loading)
        try {
            val list = shoppingListApiService.updateShoppingList(id, request)
            emit(Result.Success(list))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun deleteShoppingList(id: Long): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            shoppingListApiService.deleteShoppingList(id)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun purchaseShoppingList(id: Long, metadata: Map<String, Any>? = null): Flow<Result<ShoppingList>> = flow {
        emit(Result.Loading)
        try {
            val list = shoppingListApiService.purchaseShoppingList(id, PurchaseRequest(metadata))
            emit(Result.Success(list))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun resetShoppingList(id: Long): Flow<Result<List<ListItem>>> = flow {
        emit(Result.Loading)
        try {
            val items = shoppingListApiService.resetShoppingList(id)
            emit(Result.Success(items))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun moveToPantry(id: Long): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            shoppingListApiService.moveToPantry(id)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun shareShoppingList(id: Long, email: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            shoppingListApiService.shareShoppingList(id, ShareRequest(email))
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getSharedUsers(id: Long): Flow<Result<List<User>>> = flow {
        emit(Result.Loading)
        try {
            val users = shoppingListApiService.getSharedUsers(id)
            emit(Result.Success(users))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun revokeShareShoppingList(id: Long, userId: Long): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            shoppingListApiService.revokeShareShoppingList(id, userId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }
}

