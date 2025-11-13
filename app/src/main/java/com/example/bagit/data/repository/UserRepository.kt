package com.example.bagit.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bagit.data.model.*
import com.example.bagit.data.remote.UserApiService
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserRepository"

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }
    
    /**
     * Extrae el mensaje de error del cuerpo de respuesta HTTP
     */
    private fun extractErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return "Error desconocido"
        }
        return try {
            val apiError = gson.fromJson(errorBody, ApiError::class.java)
            val apiMessage = apiError.message
            if (!apiMessage.isNullOrBlank()) {
                apiMessage
            } else {
                errorBody
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo parsear el error como JSON: $errorBody", e)
            errorBody
        }
    }

    suspend fun register(request: RegisterRequest): Flow<Result<RegisterResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = userApiService.register(request)
            emit(Result.Success(response))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun login(request: LoginRequest): Flow<Result<LoginResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = userApiService.login(request)
            // Guardar token
            saveAuthToken(response.token)
            emit(Result.Success(response))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun logout(): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            userApiService.logout()
            clearAuthToken()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // Incluso si falla, limpiamos el token local
            clearAuthToken()
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun getProfile(): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val user = userApiService.getProfile()
            emit(Result.Success(user))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun updateProfile(request: UpdateUserProfileRequest): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val user = userApiService.updateProfile(request)
            emit(Result.Success(user))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun verifyAccount(request: VerifyAccountRequest): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            // Validar que el código no esté vacío
            val trimmedCode = request.code.trim()
            if (trimmedCode.isEmpty()) {
                val errorMsg = "El código de verificación no puede estar vacío"
                Log.e(TAG, "verifyAccount() - Error de validación: $errorMsg")
                emit(Result.Error(IllegalArgumentException(errorMsg), errorMsg))
                return@flow
            }
            
            // Crear request con código validado
            val validatedRequest = VerifyAccountRequest(trimmedCode)
            
            Log.d(TAG, "verifyAccount() - Enviando código (longitud: ${trimmedCode.length}): ${trimmedCode.take(4)}...")
            Log.d(TAG, "verifyAccount() - Request JSON: ${gson.toJson(validatedRequest)}")
            
            val user = userApiService.verifyAccount(validatedRequest)
            Log.d(TAG, "verifyAccount() - Éxito: usuario verificado ${user.email}")
            emit(Result.Success(user))
        } catch (e: HttpException) {
            val errorBodyString = try {
                e.response()?.errorBody()?.string()
            } catch (ioe: IOException) {
                Log.e(TAG, "Error leyendo error body", ioe)
                null
            }
            
            val errorMessage = extractErrorMessage(errorBodyString)
            Log.e(TAG, "verifyAccount() - Error HTTP ${e.code()}: $errorMessage")
            Log.e(TAG, "verifyAccount() - Error body raw: $errorBodyString")
            emit(Result.Error(e, errorMessage))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "verifyAccount() - Error: ${e.message}", e)
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun sendVerificationCode(email: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val response = userApiService.sendVerificationCode(email)
            emit(Result.Success(response["code"].orEmpty()))
        } catch (e: HttpException) {
            val errorBodyString = try {
                e.response()?.errorBody()?.string()
            } catch (ioe: IOException) {
                Log.e(TAG, "Error leyendo error body", ioe)
                null
            }
            val errorMessage = extractErrorMessage(errorBodyString)
            Log.e(TAG, "sendVerificationCode() - Error HTTP ${e.code()}: $errorMessage")
            emit(Result.Error(e, errorMessage))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "sendVerificationCode() - Error: ${e.message}", e)
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun resendVerificationCode(email: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            Log.d(TAG, "resendVerificationCode() - Reenviando código a: $email")
            val response = userApiService.sendVerificationCode(email)
            Log.d(TAG, "resendVerificationCode() - Éxito: código reenviado a $email")
            emit(Result.Success(response["code"].orEmpty()))
        } catch (e: HttpException) {
            val errorBodyString = try {
                e.response()?.errorBody()?.string()
            } catch (ioe: IOException) {
                Log.e(TAG, "Error leyendo error body", ioe)
                null
            }
            val errorMessage = extractErrorMessage(errorBodyString)
            Log.e(TAG, "resendVerificationCode() - Error HTTP ${e.code()}: $errorMessage")
            emit(Result.Error(e, errorMessage))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "resendVerificationCode() - Error: ${e.message}", e)
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun forgotPassword(email: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            userApiService.forgotPassword(email)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            userApiService.resetPassword(request)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun changePassword(request: ChangePasswordRequest): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            userApiService.changePassword(request)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun deleteAccount(): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            userApiService.deleteAccount()
            clearAuthToken()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return dataStore.data.first()[AUTH_TOKEN_KEY] != null
    }

    suspend fun getAuthToken(): String? {
        return dataStore.data.first()[AUTH_TOKEN_KEY]
    }

    private suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    private suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }
}

