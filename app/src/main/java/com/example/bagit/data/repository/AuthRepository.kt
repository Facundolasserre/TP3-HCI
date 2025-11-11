package com.example.bagit.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bagit.data.model.LoginRequest
import com.example.bagit.data.model.LoginResponse
import com.example.bagit.data.remote.AuthApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar la autenticación del usuario.
 * Actúa como intermediario entre la UI y la API de autenticación.
 * Sigue el patrón UDF (Unidirectional Data Flow).
 *
 * Responsabilidades:
 * - Llamar a la API de login
 * - Guardar y recuperar tokens JWT en DataStore
 * - Emitir estados (Loading, Success, Error) como Flow
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    /**
     * Realiza el login del usuario con email y contraseña.
     *
     * Flujo:
     * 1. Emite Loading
     * 2. Intenta llamar a la API de login
     * 3. Si tiene éxito, guarda el token y emite Success
     * 4. Si falla, emite Error con el mensaje de excepción
     *
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return Flow<Result<LoginResponse>> con el estado de la operación
     */
    fun login(email: String, password: String): Flow<Result<LoginResponse>> = flow {
        emit(Result.Loading)
        try {
            val request = LoginRequest(email, password)
            val response = authApiService.login(request)

            // Guardar el token JWT en DataStore
            saveAuthToken(response.token)

            emit(Result.Success(response))
        } catch (e: Exception) {
            // Convertir excepciones de red en mensajes amigables
            val errorMessage = when {
                e.message?.contains("401") == true -> "Email o contraseña incorrectos"
                e.message?.contains("500") == true -> "Error en el servidor. Por favor, intenta más tarde"
                e.message?.contains("Connect") == true -> "No se puede conectar con el servidor. Verifica tu conexión de red"
                else -> e.message ?: "Error desconocido durante el login"
            }
            emit(Result.Error(e, errorMessage))
        }
    }

    /**
     * Guarda el token JWT en DataStore para futuras peticiones autenticadas.
     *
     * @param token Token JWT a guardar
     */
    private suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    /**
     * Recupera el token JWT almacenado en DataStore.
     *
     * @return Token JWT o null si no existe
     */
    suspend fun getAuthToken(): String? {
        return dataStore.data.first()[AUTH_TOKEN_KEY]
    }

    /**
     * Borra el token JWT del almacenamiento durante el logout.
     */
    suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }

    /**
     * Verifica si hay un token válido guardado.
     *
     * @return true si existe un token, false en caso contrario
     */
    suspend fun isLoggedIn(): Boolean {
        return dataStore.data.first()[AUTH_TOKEN_KEY] != null
    }
}

