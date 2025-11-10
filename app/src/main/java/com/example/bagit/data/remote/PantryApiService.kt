package com.example.bagit.data.remote

import com.example.bagit.data.model.*
import retrofit2.http.*

interface PantryApiService {

    @POST("api/pantries")
    suspend fun createPantry(@Body request: PantryRequest): Pantry

    @GET("api/pantries")
    suspend fun getPantries(
        @Query("owner") owner: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String = "createdAt",
        @Query("order") order: String = "ASC"
    ): PaginatedResponse<Pantry>

    @GET("api/pantries/{id}")
    suspend fun getPantryById(@Path("id") id: Long): Pantry

    @PUT("api/pantries/{id}")
    suspend fun updatePantry(
        @Path("id") id: Long,
        @Body request: PantryRequest
    ): Pantry

    @DELETE("api/pantries/{id}")
    suspend fun deletePantry(@Path("id") id: Long)

    @POST("api/pantries/{id}/share")
    suspend fun sharePantry(
        @Path("id") id: Long,
        @Body request: ShareRequest
    ): User

    @GET("api/pantries/{id}/shared-users")
    suspend fun getSharedUsers(@Path("id") id: Long): List<User>

    @DELETE("api/pantries/{id}/share/{user_id}")
    suspend fun revokeSharePantry(
        @Path("id") id: Long,
        @Path("user_id") userId: Long
    )
}

