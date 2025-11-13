package com.example.bagit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.ChangePasswordRequest
import com.example.bagit.data.model.UpdateUserProfileRequest
import com.example.bagit.data.model.User
import com.example.bagit.data.repository.AuthRepository
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val USERNAME_METADATA_KEY = "username"

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountSettingsUiState())
    val uiState: StateFlow<AccountSettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AccountSettingsEvent>()
    val events: SharedFlow<AccountSettingsEvent> = _events.asSharedFlow()

    private var cachedUser: User? = null

    init {
        refreshUser()
    }

    fun refreshUser(force: Boolean = false) {
        if (_uiState.value.isLoading && !force) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = userRepository.getProfile().first { it !is Result.Loading }) {
                is Result.Success -> {
                    handleUserLoaded(result.data)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: result.exception.message ?: "Error al cargar los datos"
                        )
                    }
                    _events.emit(
                        AccountSettingsEvent.ShowSnackbar(
                            message = result.message ?: result.exception.message ?: "Error al cargar los datos",
                            type = SnackbarType.ERROR
                        )
                    )
                }

                Result.Loading -> Unit
            }
        }
    }

    fun updateProfile(name: String, username: String) {
        val trimmedName = name.trim()
        val trimmedUsername = username.trim()

        if (trimmedName.isEmpty() || trimmedUsername.isEmpty()) {
            _uiState.update {
                it.copy(profileError = "Completá nombre y usuario antes de guardar")
            }
            return
        }

        val currentUser = cachedUser ?: run {
            _uiState.update { it.copy(profileError = "No se pudo cargar la información del usuario") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true, profileError = null, errorMessage = null) }

            val metadata = currentUser.metadata?.toMutableMap() ?: mutableMapOf()
            metadata[USERNAME_METADATA_KEY] = trimmedUsername

            val request = UpdateUserProfileRequest(
                name = trimmedName,
                surname = trimmedUsername,
                metadata = metadata
            )

            when (val result = userRepository.updateProfile(request).first { it !is Result.Loading }) {
                is Result.Success -> {
                    handleUserLoaded(result.data)
                    _uiState.update { it.copy(isSavingProfile = false) }
                    _events.emit(
                        AccountSettingsEvent.ShowSnackbar(
                            message = "Perfil actualizado correctamente",
                            type = SnackbarType.PROFILE_SUCCESS
                        )
                    )
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSavingProfile = false,
                            profileError = result.message ?: result.exception.message ?: "No se pudo actualizar el perfil"
                        )
                    }
                    _events.emit(
                        AccountSettingsEvent.ShowSnackbar(
                            message = result.message ?: result.exception.message ?: "No se pudo actualizar el perfil",
                            type = SnackbarType.ERROR
                        )
                    )
                }

                Result.Loading -> Unit
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, repeatPassword: String) {
        val trimmedCurrent = currentPassword.trim()
        val trimmedNew = newPassword.trim()
        val trimmedRepeat = repeatPassword.trim()

        when {
            trimmedCurrent.isEmpty() -> {
                _uiState.update { it.copy(passwordError = "Ingresá tu contraseña actual") }
                return
            }

            trimmedNew.isEmpty() || trimmedRepeat.isEmpty() -> {
                _uiState.update { it.copy(passwordError = "Ingresá y repetí la nueva contraseña") }
                return
            }

            trimmedNew.length < 6 -> {
                _uiState.update { it.copy(passwordError = "La nueva contraseña debe tener al menos 6 caracteres") }
                return
            }

            trimmedNew != trimmedRepeat -> {
                _uiState.update { it.copy(passwordError = "Las contraseñas nuevas no coinciden") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true, passwordError = null, errorMessage = null) }

            val request = ChangePasswordRequest(
                currentPassword = trimmedCurrent,
                newPassword = trimmedNew
            )

            when (val result = userRepository.changePassword(request).first { it !is Result.Loading }) {
                is Result.Success -> {
                    _uiState.update { it.copy(isChangingPassword = false) }
                    _events.emit(
                        AccountSettingsEvent.ShowSnackbar(
                            message = "Contraseña actualizada correctamente",
                            type = SnackbarType.PASSWORD_SUCCESS
                        )
                    )
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            passwordError = result.message ?: result.exception.message ?: "No se pudo cambiar la contraseña"
                        )
                    }
                    _events.emit(
                        AccountSettingsEvent.ShowSnackbar(
                            message = result.message ?: result.exception.message ?: "No se pudo cambiar la contraseña",
                            type = SnackbarType.ERROR
                        )
                    )
                }

                Result.Loading -> Unit
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, deleteError = null, errorMessage = null) }

            when (val result = userRepository.deleteAccount().first { it !is Result.Loading }) {
                is Result.Success -> {
                    authRepository.clearAuthToken()
                    _uiState.update { it.copy(isDeletingAccount = false) }
                    _events.emit(AccountSettingsEvent.AccountDeleted)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            deleteError = result.message ?: result.exception.message ?: "No se pudo eliminar la cuenta"
                        )
                    }
                    _events.emit(
                        AccountSettingsEvent.ShowSnackbar(
                            message = result.message ?: result.exception.message ?: "No se pudo eliminar la cuenta",
                            type = SnackbarType.ERROR
                        )
                    )
                }

                Result.Loading -> Unit
            }
        }
    }

    fun clearProfileError() {
        _uiState.update { it.copy(profileError = null) }
    }

    fun clearPasswordError() {
        _uiState.update { it.copy(passwordError = null) }
    }

    private fun handleUserLoaded(user: User) {
        cachedUser = user
        val username = extractUsername(user)

        _uiState.update {
            it.copy(
                isLoading = false,
                name = user.name,
                surname = user.surname,
                username = username,
                email = user.email,
                errorMessage = null,
                profileError = null
            )
        }
    }

    private fun extractUsername(user: User): String {
        val metadata = user.metadata ?: return user.surname.ifBlank { user.email }
        val raw = metadata[USERNAME_METADATA_KEY]?.toString()?.trim().orEmpty()
        return when {
            raw.isNotEmpty() -> raw
            user.surname.isNotBlank() -> user.surname
            else -> user.email
        }
    }
}

data class AccountSettingsUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val surname: String = "",
    val username: String = "",
    val email: String = "",
    val isSavingProfile: Boolean = false,
    val isChangingPassword: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val profileError: String? = null,
    val passwordError: String? = null,
    val deleteError: String? = null,
    val errorMessage: String? = null
)

sealed class AccountSettingsEvent {
    data class ShowSnackbar(val message: String, val type: SnackbarType) : AccountSettingsEvent()
    object AccountDeleted : AccountSettingsEvent()
}

enum class SnackbarType {
    SUCCESS,
    ERROR,
    PROFILE_SUCCESS,
    PASSWORD_SUCCESS
}


