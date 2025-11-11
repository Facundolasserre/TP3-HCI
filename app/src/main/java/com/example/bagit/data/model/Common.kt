package com.example.bagit.data.model

import com.google.gson.annotations.SerializedName

data class PaginatedResponse<T>(
    @SerializedName("data") val data: List<T>,
    @SerializedName("pagination") val pagination: Pagination
)

data class Pagination(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("has_prev") val hasPrev: Boolean
)

data class ApiError(
    @SerializedName("message") val message: String
)

