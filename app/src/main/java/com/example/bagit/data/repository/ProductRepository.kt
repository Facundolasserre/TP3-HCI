package com.example.bagit.data.repository

import com.example.bagit.data.model.*
import com.example.bagit.data.remote.ProductApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productApiService: ProductApiService
) {

    fun createProduct(request: ProductRequest): Flow<Result<Product>> = flow {
        emit(Result.Loading)
        try {
            val product = productApiService.createProduct(request)
            emit(Result.Success(product))
        } catch (e: HttpException) {
            if (e.code() == 409) {
                // Producto ya existe - intentar encontrarlo
                emit(Result.Error(exception = e, message = "El producto ya existe", isConflict = true))
            } else {
                emit(Result.Error(exception = e, message = "Error ${e.code()}: ${e.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(exception = e, message = e.message))
        }
    }

    /**
     * Busca un producto por nombre exacto.
     * Útil para resolver conflictos 409.
     */
    fun findProductByName(name: String): Flow<Result<Product?>> = flow {
        emit(Result.Loading)
        try {
            val response = productApiService.getProducts(
                name = name,
                categoryId = null,
                page = 1,
                perPage = 100
            )
            // Buscar coincidencia exacta (case-insensitive)
            val exactMatch = response.data.find {
                it.name.equals(name, ignoreCase = true)
            }
            emit(Result.Success(exactMatch))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(exception = e, message = e.message))
        }
    }

    fun getProducts(
        name: String? = null,
        categoryId: Long? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "name",
        order: String = "ASC"
    ): Flow<Result<PaginatedResponse<Product>>> = flow {
        emit(Result.Loading)
        try {
            val response = productApiService.getProducts(name, categoryId, page, perPage, sortBy, order)
            emit(Result.Success(response))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(exception = e, message = e.message))
        }
    }

    fun getProductById(id: Long): Flow<Result<Product>> = flow {
        emit(Result.Loading)
        try {
            val product = productApiService.getProductById(id)
            emit(Result.Success(product))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(exception = e, message = e.message))
        }
    }

    fun updateProduct(id: Long, request: ProductRequest): Flow<Result<Product>> = flow {
        emit(Result.Loading)
        try {
            val product = productApiService.updateProduct(id, request)
            emit(Result.Success(product))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(exception = e, message = e.message))
        }
    }

    fun deleteProduct(id: Long): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            productApiService.deleteProduct(id)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(exception = e, message = e.message))
        }
    }
}

