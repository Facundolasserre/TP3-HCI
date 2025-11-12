package com.example.bagit.ui.viewmodel

import android.util.Log
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

private const val TAG = "AuthViewModel"

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

    // Estado del reenvío de código
    private val _resendCodeState = mutableStateOf<Result<String>?>(null)
    val resendCodeState: State<Result<String>?> = _resendCodeState

    // Indicador de sesión iniciada
    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    init {
        checkLoginStatus()
    }

    /**
     * Verifica si existe una sesión válida al iniciar.
     * Se ejecuta:
     * - Al inicializar el ViewModel
     * - Al loguear exitosamente
     * - Después de cada operación importante
     */
    fun checkLoginStatus() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            _isLoggedIn.value = isLoggedIn
            Log.d(TAG, "checkLoginStatus() - isLoggedIn: $isLoggedIn")
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
            authRepository.login(email.lowercase(), password).collect { result ->
                _loginState.value = result
                if (result is Result.Success) {
                    _isLoggedIn.value = true
                    Log.d(TAG, "login() - Éxito para email: $email")
                    checkLoginStatus() // Verificar nuevamente para confirmar
                } else if (result is Result.Error) {
                    Log.e(TAG, "login() - Error: ${result.message}")
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
                Log.d(TAG, "logout() - Iniciando logout")
                userRepository.logout().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            Log.d(TAG, "logout() - API logout exitoso")
                        }
                        is Result.Error -> {
                            Log.w(TAG, "logout() - Error en API logout: ${result.message}, limpiando datos locales de todas formas")
                        }
                        else -> {}
                    }
                    // Limpiar SOLO el token, mantener otros datos para re-login
                    authRepository.clearAuthToken()
                    _isLoggedIn.value = false
                    // NO limpiar loginState ni userState, pueden ser útiles para debugging
                    Log.d(TAG, "logout() - Token limpiado, sesión cerrada")
                }
            } catch (e: Exception) {
                Log.e(TAG, "logout() - Excepción: ${e.message}", e)
                // Si algo falla, limpiar datos locales de todas formas
                viewModelScope.launch {
                    authRepository.clearAuthToken()
                    _isLoggedIn.value = false
                    Log.d(TAG, "logout() - Token limpiado por excepción")
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
     * @param email Email del usuario (mantenido para compatibilidad, no se envía al backend)
     * @param code Código de verificación (16 caracteres)
     */
    fun verifyAccount(email: String, code: String) {
        Log.d(TAG, "verifyAccount() - Llamada iniciada para email=$email, code=${code.take(4)}...")
        viewModelScope.launch {
            // El backend solo espera el código, no el email
            userRepository.verifyAccount(VerifyAccountRequest(code.trim())).collect { result ->
                _userState.value = result
                when (result) {
                    is Result.Success -> Log.d(TAG, "verifyAccount() - Éxito")
                    is Result.Error -> Log.e(TAG, "verifyAccount() - Error: ${result.message}")
                    is Result.Loading -> Log.d(TAG, "verifyAccount() - Cargando...")
                }
            }
        }
    }

    /**
     * Reenvía el código de verificación al email del usuario.
     *
     * @param email Email del usuario
     */
    fun resendVerificationCode(email: String) {
        Log.d(TAG, "resendVerificationCode() - Llamada iniciada para email=$email")
        viewModelScope.launch {
            userRepository.resendVerificationCode(email.lowercase()).collect { result ->
                _resendCodeState.value = result
                when (result) {
                    is Result.Success -> Log.d(TAG, "resendVerificationCode() - Éxito: código reenviado")
                    is Result.Error -> Log.e(TAG, "resendVerificationCode() - Error: ${result.message}")
                    is Result.Loading -> Log.d(TAG, "resendVerificationCode() - Cargando...")
                }
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
            userRepository.forgotPassword(email.lowercase()).collect { /* handle result */ }
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

