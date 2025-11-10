package com.example.bagit.data.remote

import com.example.bagit.data.model.*
import retrofit2.http.*

interface ProductApiService {

    @POST("api/products")
    suspend fun createProduct(@Body request: ProductRequest): Product

    @GET("api/products")
    suspend fun getProducts(
        @Query("name") name: String? = null,
        @Query("category_id") categoryId: Long? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String = "name",
        @Query("order") order: String = "ASC"
    ): PaginatedResponse<Product>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Product

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body request: ProductRequest
    ): Product

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long)
}

