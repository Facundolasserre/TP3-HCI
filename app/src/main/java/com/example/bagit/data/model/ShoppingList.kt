package com.example.bagit.data.model

import com.google.gson.annotations.SerializedName

data class ShoppingList(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("recurring") val recurring: Boolean = false,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("owner") val owner: User,
    @SerializedName("sharedWith") val sharedWith: List<User>? = null,
    @SerializedName("lastPurchasedAt") val lastPurchasedAt: String? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class ShoppingListRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("recurring") val recurring: Boolean = false,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

data class ShareRequest(
    @SerializedName("email") val email: String
)

data class PurchaseRequest(
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

