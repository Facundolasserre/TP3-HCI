package com.example.bagit.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Long,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("surname") val surname: String,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("surname") val surname: String,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

data class RegisterResponse(
    @SerializedName("user") val user: User,
    @SerializedName("verificationToken") val verificationToken: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String
)

data class VerifyAccountRequest(
    @SerializedName("code") val code: String
)

data class ResendVerificationCodeRequest(
    @SerializedName("email") val email: String
)

data class UpdateUserProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("surname") val surname: String,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class ResetPasswordRequest(
    @SerializedName("code") val code: String,
    @SerializedName("password") val password: String
)

