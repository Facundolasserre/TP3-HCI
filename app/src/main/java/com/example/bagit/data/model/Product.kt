package com.example.bagit.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("category") val category: Category? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class ProductRequest(
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: CategoryId? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

data class CategoryId(
    @SerializedName("id") val id: Long
)

