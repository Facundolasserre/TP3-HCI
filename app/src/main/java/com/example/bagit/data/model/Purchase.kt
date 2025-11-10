package com.example.bagit.data.model

import com.google.gson.annotations.SerializedName

data class Purchase(
    @SerializedName("id") val id: Long,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("owner") val owner: User,
    @SerializedName("list") val list: ShoppingList,
    @SerializedName("listItemArray") val listItemArray: List<ListItem>,
    @SerializedName("createdAt") val createdAt: String
)

