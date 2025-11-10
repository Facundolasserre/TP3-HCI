package com.example.bagit.data.remote

import com.example.bagit.data.model.*
import retrofit2.http.*

interface UserApiService {

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/users/logout")
    suspend fun logout()

    @GET("api/users/profile")
    suspend fun getProfile(): User

    @PUT("api/users/profile")
    suspend fun updateProfile(@Body request: UpdateUserProfileRequest): User

    @POST("api/users/verify-account")
    suspend fun verifyAccount(@Body request: VerifyAccountRequest): User

    @POST("api/users/send-verification")
    suspend fun sendVerificationCode(@Query("email") email: String): Map<String, String>

    @POST("api/users/forgot-password")
    suspend fun forgotPassword(@Query("email") email: String)

    @POST("api/users/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest)

    @POST("api/users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest)
}

