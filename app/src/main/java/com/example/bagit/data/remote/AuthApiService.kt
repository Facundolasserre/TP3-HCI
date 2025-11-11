package com.example.bagit.data.remote

import com.example.bagit.data.model.LoginRequest
import com.example.bagit.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaz Retrofit para los endpoints de autenticación.
 * Define las operaciones disponibles en la API de autenticación.
 */
interface AuthApiService {

    /**
     * Realiza el login del usuario con email y contraseña.
     *
     * @param request Objeto con email y password
     * @return LoginResponse con el token JWT
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

