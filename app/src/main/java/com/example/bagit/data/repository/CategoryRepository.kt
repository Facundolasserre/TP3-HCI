package com.example.bagit.data.repository

import com.example.bagit.data.model.*
import com.example.bagit.data.remote.CategoryApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryApiService: CategoryApiService
) {

    suspend fun createCategory(request: CategoryRequest): Flow<Result<Category>> = flow {
        emit(Result.Loading)
        try {
            val category = categoryApiService.createCategory(request)
            emit(Result.Success(category))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getCategories(
        name: String? = null,
        page: Int = 1,
        perPage: Int = 10,
        order: String = "ASC",
        sortBy: String = "createdAt"
    ): Flow<Result<PaginatedResponse<Category>>> = flow {
        emit(Result.Loading)
        try {
            val response = categoryApiService.getCategories(name, page, perPage, order, sortBy)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getCategoryById(id: Long): Flow<Result<Category>> = flow {
        emit(Result.Loading)
        try {
            val category = categoryApiService.getCategoryById(id)
            emit(Result.Success(category))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun updateCategory(id: Long, request: CategoryRequest): Flow<Result<Category>> = flow {
        emit(Result.Loading)
        try {
            val category = categoryApiService.updateCategory(id, request)
            emit(Result.Success(category))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun deleteCategory(id: Long): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            categoryApiService.deleteCategory(id)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }
}

