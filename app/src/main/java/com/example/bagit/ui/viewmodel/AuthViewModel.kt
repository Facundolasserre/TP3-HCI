package com.example.bagit.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.*
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = mutableStateOf<Result<LoginResponse>?>(null)
    val loginState: State<Result<LoginResponse>?> = _loginState

    private val _registerState = mutableStateOf<Result<RegisterResponse>?>(null)
    val registerState: State<Result<RegisterResponse>?> = _registerState

    private val _userState = mutableStateOf<Result<User>?>(null)
    val userState: State<Result<User>?> = _userState

    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = userRepository.isLoggedIn()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            userRepository.login(LoginRequest(email, password)).collect { result ->
                _loginState.value = result
                if (result is Result.Success) {
                    _isLoggedIn.value = true
                }
            }
        }
    }

    fun register(name: String, surname: String, email: String, password: String, metadata: Map<String, Any>? = null) {
        viewModelScope.launch {
            userRepository.register(
                RegisterRequest(email, password, name, surname, metadata)
            ).collect { result ->
                _registerState.value = result
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout().collect { result ->
                if (result is Result.Success) {
                    _isLoggedIn.value = false
                    _userState.value = null
                }
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            userRepository.getProfile().collect { result ->
                _userState.value = result
            }
        }
    }

    fun updateProfile(name: String, surname: String, metadata: Map<String, Any>? = null) {
        viewModelScope.launch {
            userRepository.updateProfile(
                UpdateUserProfileRequest(name, surname, metadata)
            ).collect { result ->
                _userState.value = result
            }
        }
    }

    fun verifyAccount(code: String) {
        viewModelScope.launch {
            userRepository.verifyAccount(VerifyAccountRequest(code)).collect { result ->
                _userState.value = result
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            userRepository.forgotPassword(email).collect { /* handle result */ }
        }
    }

    fun resetPassword(code: String, password: String) {
        viewModelScope.launch {
            userRepository.resetPassword(ResetPasswordRequest(code, password)).collect { /* handle result */ }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            userRepository.changePassword(
                ChangePasswordRequest(currentPassword, newPassword)
            ).collect { /* handle result */ }
        }
    }
}

