package com.example.bagit.data.repository

import com.example.bagit.data.model.*
import com.example.bagit.data.remote.PantryApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryRepository @Inject constructor(
    private val pantryApiService: PantryApiService
) {

    suspend fun createPantry(request: PantryRequest): Flow<Result<Pantry>> = flow {
        emit(Result.Loading)
        try {
            val pantry = pantryApiService.createPantry(request)
            emit(Result.Success(pantry))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getPantries(
        owner: Boolean? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "createdAt",
        order: String = "ASC"
    ): Flow<Result<PaginatedResponse<Pantry>>> = flow {
        emit(Result.Loading)
        try {
            val response = pantryApiService.getPantries(owner, page, perPage, sortBy, order)
            emit(Result.Success(response))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getPantryById(id: Long): Flow<Result<Pantry>> = flow {
        emit(Result.Loading)
        try {
            val pantry = pantryApiService.getPantryById(id)
            emit(Result.Success(pantry))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun updatePantry(id: Long, request: PantryRequest): Flow<Result<Pantry>> = flow {
        emit(Result.Loading)
        try {
            val pantry = pantryApiService.updatePantry(id, request)
            emit(Result.Success(pantry))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun deletePantry(id: Long): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            pantryApiService.deletePantry(id)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun sharePantry(id: Long, email: String): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val user = pantryApiService.sharePantry(id, ShareRequest(email))
            emit(Result.Success(user))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getSharedUsers(id: Long): Flow<Result<List<User>>> = flow {
        emit(Result.Loading)
        try {
            val users = pantryApiService.getSharedUsers(id)
            emit(Result.Success(users))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun revokeSharePantry(id: Long, userId: Long): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            pantryApiService.revokeSharePantry(id, userId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }
}

