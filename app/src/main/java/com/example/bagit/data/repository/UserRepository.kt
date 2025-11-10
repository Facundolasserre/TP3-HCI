package com.example.bagit.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bagit.data.model.*
import com.example.bagit.data.remote.UserApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    suspend fun register(request: RegisterRequest): Flow<Result<RegisterResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = userApiService.register(request)
            emit(Result.Success(response))
        } catch (e: Exception) {
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
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun updateProfile(request: UpdateUserProfileRequest): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val user = userApiService.updateProfile(request)
            emit(Result.Success(user))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun verifyAccount(request: VerifyAccountRequest): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val user = userApiService.verifyAccount(request)
            emit(Result.Success(user))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun sendVerificationCode(email: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val response = userApiService.sendVerificationCode(email)
            emit(Result.Success(response["code"] ?: ""))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun forgotPassword(email: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            userApiService.forgotPassword(email)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            userApiService.resetPassword(request)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    suspend fun changePassword(request: ChangePasswordRequest): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            userApiService.changePassword(request)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
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

