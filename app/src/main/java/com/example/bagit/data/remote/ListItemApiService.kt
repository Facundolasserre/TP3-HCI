package com.example.bagit.data.remote

import com.example.bagit.data.model.*
import retrofit2.http.*

interface ListItemApiService {

    @POST("api/shopping-lists/{id}/items")
    suspend fun addListItem(
        @Path("id") listId: Long,
        @Body request: ListItemRequest
    ): ListItem

    @GET("api/shopping-lists/{id}/items")
    suspend fun getListItems(
        @Path("id") listId: Long,
        @Query("purchased") purchased: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String = "createdAt",
        @Query("order") order: String = "DESC",
        @Query("pantry_id") pantryId: Long? = null,
        @Query("category_id") categoryId: Long? = null,
        @Query("search") search: String? = null
    ): PaginatedResponse<ListItem>

    @PUT("api/shopping-lists/{id}/items/{item_id}")
    suspend fun updateListItem(
        @Path("id") listId: Long,
        @Path("item_id") itemId: Long,
        @Body request: ListItemUpdateRequest
    ): ListItem

    @PATCH("api/shopping-lists/{id}/items/{item_id}")
    suspend fun toggleListItemPurchased(
        @Path("id") listId: Long,
        @Path("item_id") itemId: Long,
        @Body request: TogglePurchasedRequest = TogglePurchasedRequest()
    ): ListItem

    @DELETE("api/shopping-lists/{id}/items/{item_id}")
    suspend fun deleteListItem(
        @Path("id") listId: Long,
        @Path("item_id") itemId: Long
    )
}

