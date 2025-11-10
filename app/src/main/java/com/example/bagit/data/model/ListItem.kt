package com.example.bagit.data.model

import com.google.gson.annotations.SerializedName

data class ListItem(
    @SerializedName("id") val id: Long,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unit") val unit: String,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("purchased") val purchased: Boolean = false,
    @SerializedName("lastPurchasedAt") val lastPurchasedAt: String? = null,
    @SerializedName("product") val product: Product,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class ListItemRequest(
    @SerializedName("product") val product: ProductId,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unit") val unit: String,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

data class ListItemUpdateRequest(
    @SerializedName("quantity") val quantity: Double? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

data class TogglePurchasedRequest(
    @SerializedName("purchased") val purchased: Boolean? = null
)

data class ProductId(
    @SerializedName("id") val id: Long
)

