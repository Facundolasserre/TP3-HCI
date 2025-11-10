package com.example.bagit.data.remote

import com.example.bagit.data.model.*
import retrofit2.http.*

interface ShoppingListApiService {

    @POST("api/shopping-lists")
    suspend fun createShoppingList(@Body request: ShoppingListRequest): ShoppingList

    @GET("api/shopping-lists")
    suspend fun getShoppingLists(
        @Query("name") name: String? = null,
        @Query("owner") owner: Boolean? = null,
        @Query("recurring") recurring: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String = "name",
        @Query("order") order: String = "ASC"
    ): PaginatedResponse<ShoppingList>

    @GET("api/shopping-lists/{id}")
    suspend fun getShoppingListById(@Path("id") id: Long): ShoppingList

    @PUT("api/shopping-lists/{id}")
    suspend fun updateShoppingList(
        @Path("id") id: Long,
        @Body request: ShoppingListRequest
    ): ShoppingList

    @DELETE("api/shopping-lists/{id}")
    suspend fun deleteShoppingList(@Path("id") id: Long)

    @POST("api/shopping-lists/{id}/purchase")
    suspend fun purchaseShoppingList(
        @Path("id") id: Long,
        @Body request: PurchaseRequest = PurchaseRequest()
    ): ShoppingList

    @POST("api/shopping-lists/{id}/reset")
    suspend fun resetShoppingList(@Path("id") id: Long): List<ListItem>

    @POST("api/shopping-lists/{id}/move-to-pantry")
    suspend fun moveToPantry(@Path("id") id: Long)

    @POST("api/shopping-lists/{id}/share")
    suspend fun shareShoppingList(
        @Path("id") id: Long,
        @Body request: ShareRequest
    )

    @GET("api/shopping-lists/{id}/shared-users")
    suspend fun getSharedUsers(@Path("id") id: Long): List<User>

    @DELETE("api/shopping-lists/{id}/share/{user_id}")
    suspend fun revokeShareShoppingList(
        @Path("id") id: Long,
        @Path("user_id") userId: Long
    )
}

