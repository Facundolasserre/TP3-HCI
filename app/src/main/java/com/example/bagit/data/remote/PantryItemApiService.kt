package com.example.bagit.data.remote

import com.example.bagit.data.model.*
import retrofit2.http.*

interface PantryItemApiService {

    @POST("api/pantries/{id}/items")
    suspend fun addPantryItem(
        @Path("id") pantryId: Long,
        @Body request: PantryItemRequest
    ): PantryItem

    @GET("api/pantries/{id}/items")
    suspend fun getPantryItems(
        @Path("id") pantryId: Long,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String? = null,
        @Query("order") order: String = "DESC",
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: Long? = null
    ): PaginatedResponse<PantryItem>

    @PUT("api/pantries/{id}/items/{item_id}")
    suspend fun updatePantryItem(
        @Path("id") pantryId: Long,
        @Path("item_id") itemId: Long,
        @Body request: PantryItemUpdateRequest
    ): PantryItem

    @DELETE("api/pantries/{id}/items/{item_id}")
    suspend fun deletePantryItem(
        @Path("id") pantryId: Long,
        @Path("item_id") itemId: Long
    )
}

