package com.example.bagit.data.remote

import com.example.bagit.data.model.*
import retrofit2.http.*

interface CategoryApiService {

    @POST("api/categories")
    suspend fun createCategory(@Body request: CategoryRequest): Category

    @GET("api/categories")
    suspend fun getCategories(
        @Query("name") name: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("order") order: String = "ASC",
        @Query("sort_by") sortBy: String = "createdAt"
    ): PaginatedResponse<Category>

    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Long): Category

    @PUT("api/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body request: CategoryRequest
    ): Category

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long)
}

