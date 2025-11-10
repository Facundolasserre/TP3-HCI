package com.example.bagit.data.remote

import com.example.bagit.data.model.*
import retrofit2.http.*

interface PurchaseApiService {

    @GET("api/purchases")
    suspend fun getPurchases(
        @Query("list_id") listId: Long? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String = "createdAt",
        @Query("order") order: String = "DESC"
    ): PaginatedResponse<Purchase>

    @GET("api/purchases/{id}")
    suspend fun getPurchaseById(@Path("id") id: Long): Purchase

    @POST("api/purchases/{id}/restore")
    suspend fun restorePurchase(@Path("id") id: Long): ShoppingList
}

