package com.example.bagit.data.model

import com.google.gson.annotations.SerializedName

data class Pantry(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("owner") val owner: User,
    @SerializedName("sharedWith") val sharedWith: List<User>? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class PantryRequest(
    @SerializedName("name") val name: String,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

data class PantryItem(
    @SerializedName("id") val id: Long,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unit") val unit: String,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("product") val product: Product,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class PantryItemRequest(
    @SerializedName("product") val product: ProductId,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unit") val unit: String,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

data class PantryItemUpdateRequest(
    @SerializedName("quantity") val quantity: Double? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

