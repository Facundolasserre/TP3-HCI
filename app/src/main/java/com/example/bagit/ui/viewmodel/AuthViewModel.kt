package com.example.bagit.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.*
import com.example.bagit.data.repository.AuthRepository
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para manejar la lógica de autenticación.
 *
 * Responsabilidades:
 * - Orquestar las llamadas al repositorio de autenticación
 * - Mantener el estado de login/logout
 * - Exponer estados observables a la UI
 * - Manejar corrutinas de forma segura dentro del ciclo de vida
 *
 * Sigue el patrón MVVM + UDF (Unidirectional Data Flow)
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Estado del login
    private val _loginState = mutableStateOf<Result<LoginResponse>?>(null)
    val loginState: State<Result<LoginResponse>?> = _loginState

    // Estado del registro
    private val _registerState = mutableStateOf<Result<RegisterResponse>?>(null)
    val registerState: State<Result<RegisterResponse>?> = _registerState

    // Estado del usuario (perfil)
    private val _userState = mutableStateOf<Result<User>?>(null)
    val userState: State<Result<User>?> = _userState

    // Indicador de sesión iniciada
    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    init {
        checkLoginStatus()
    }

    /**
     * Verifica si existe una sesión válida al iniciar.
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = authRepository.isLoggedIn()
        }
    }

    /**
     * Realiza el login del usuario con email y contraseña.
     *
     * Emite estados:
     * - Loading: mientras se procesa la solicitud
     * - Success: cuando la API retorna token JWT
     * - Error: si hay errores de red, validación o credenciales
     *
     * @param email Email del usuario
     * @param password Contraseña del usuario
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepository.login(email, password).collect { result ->
                _loginState.value = result
                if (result is Result.Success) {
                    _isLoggedIn.value = true
                }
            }
        }
    }

    /**
     * Registra un nuevo usuario.
     *
     * @param name Nombre del usuario
     * @param surname Apellido del usuario
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param metadata Datos adicionales opcionales
     */
    fun register(
        name: String,
        surname: String,
        email: String,
        password: String,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            userRepository.register(
                RegisterRequest(email, password, name, surname, metadata)
            ).collect { result ->
                _registerState.value = result
            }
        }
    }

    /**
     * Cierra la sesión del usuario.
     *
     * - Llama al endpoint de logout de la API
     * - Borra el token JWT del almacenamiento local
     * - Limpia el estado de usuario
     * - Redirige el flujo a la pantalla de login
     */
    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.logout().collect { result ->
                    if (result is Result.Success || result is Result.Error) {
                        // Incluso si falla, limpiamos los datos locales
                        authRepository.clearAuthToken()
                        _isLoggedIn.value = false
                        _userState.value = null
                        _loginState.value = null
                    }
                }
            } catch (e: Exception) {
                // Si algo falla, limpiar datos locales de todas formas
                viewModelScope.launch {
                    authRepository.clearAuthToken()
                    _isLoggedIn.value = false
                    _userState.value = null
                }
            }
        }
    }

    /**
     * Obtiene el perfil del usuario actual.
     */
    fun getProfile() {
        viewModelScope.launch {
            userRepository.getProfile().collect { result ->
                _userState.value = result
            }
        }
    }

    /**
     * Actualiza el perfil del usuario.
     *
     * @param name Nuevo nombre
     * @param surname Nuevo apellido
     * @param metadata Datos adicionales
     */
    fun updateProfile(
        name: String,
        surname: String,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            userRepository.updateProfile(
                UpdateUserProfileRequest(name, surname, metadata)
            ).collect { result ->
                _userState.value = result
            }
        }
    }

    /**
     * Verifica la cuenta del usuario con un código.
     *
     * @param code Código de verificación
     */
    fun verifyAccount(code: String) {
        viewModelScope.launch {
            userRepository.verifyAccount(VerifyAccountRequest(code)).collect { result ->
                _userState.value = result
            }
        }
    }

    /**
     * Solicita el reseteo de contraseña.
     *
     * @param email Email del usuario
     */
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            userRepository.forgotPassword(email).collect { /* handle result */ }
        }
    }

    /**
     * Resetea la contraseña con el código enviado por email.
     *
     * @param code Código de reseteo
     * @param password Nueva contraseña
     */
    fun resetPassword(code: String, password: String) {
        viewModelScope.launch {
            userRepository.resetPassword(ResetPasswordRequest(code, password)).collect { /* handle result */ }
        }
    }

    /**
     * Cambia la contraseña del usuario.
     *
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            userRepository.changePassword(
                ChangePasswordRequest(currentPassword, newPassword)
            ).collect { /* handle result */ }
        }
    }
}

