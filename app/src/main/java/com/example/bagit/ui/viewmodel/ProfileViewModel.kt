package com.example.bagit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.UpdateUserProfileRequest
import com.example.bagit.data.model.User
import com.example.bagit.data.repository.PantryRepository
import com.example.bagit.data.repository.ProductRepository
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.ShoppingListRepository
import com.example.bagit.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val EMAIL_NOTIFICATIONS_KEY = "email_notifications"
private const val PUSH_NOTIFICATIONS_KEY = "push_notifications"
private const val PRICE_ALERTS_KEY = "price_alerts"
private const val FAVORITE_STORES_KEY = "favorite_stores"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val pantryRepository: PantryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refreshProfile(force = true)
    }

    fun refreshProfile(force: Boolean = false) {
        val shouldSkip = _uiState.value.isLoading && !force
        if (shouldSkip) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val userResult = userRepository.getProfile().first { it !is Result.Loading }
            if (userResult is Result.Error) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = userResult.message ?: userResult.exception.message
                    )
                }
                return@launch
            }

            val user = (userResult as Result.Success).data
            val notifications = extractNotificationPreferences(user.metadata)
            val favoriteStores = extractFavoriteStores(user.metadata)

            val statsDeferred = async { fetchStats() }

            _uiState.update {
                it.copy(
                    user = user,
                    notifications = notifications,
                    favoriteStores = favoriteStores
                )
            }

            val stats = statsDeferred.await()
            _uiState.update {
                it.copy(
                    stats = stats,
                    isLoading = false
                )
            }
        }
    }

    fun onEmailNotificationsChanged(enabled: Boolean) {
        updateNotificationPreference(NotificationPreference.EMAIL, enabled)
    }

    fun onPushNotificationsChanged(enabled: Boolean) {
        updateNotificationPreference(NotificationPreference.PUSH, enabled)
    }

    fun onPriceAlertsChanged(enabled: Boolean) {
        updateNotificationPreference(NotificationPreference.PRICE_ALERTS, enabled)
    }

    private suspend fun fetchStats(): ProfileStats = coroutineScope {
        val listsDeferred = async {
            shoppingListRepository
                .getShoppingLists(page = 1, perPage = 1)
                .first { it !is Result.Loading }
        }

        val pantriesDeferred = async {
            pantryRepository
                .getPantries(page = 1, perPage = 1)
                .first { it !is Result.Loading }
        }

        val productsDeferred = async {
            productRepository
                .getProducts(page = 1, perPage = 1)
                .first { it !is Result.Loading }
        }

        val listsResult = listsDeferred.await()
        val pantriesResult = pantriesDeferred.await()
        val productsResult = productsDeferred.await()

        val listsTotal = (listsResult as? Result.Success)?.data?.pagination?.total ?: 0
        val pantriesTotal = (pantriesResult as? Result.Success)?.data?.pagination?.total ?: 0
        val productsTotal = (productsResult as? Result.Success)?.data?.pagination?.total ?: 0

        val errorMessage = listOfNotNull(
            (listsResult as? Result.Error)?.message ?: (listsResult as? Result.Error)?.exception?.message,
            (pantriesResult as? Result.Error)?.message ?: (pantriesResult as? Result.Error)?.exception?.message,
            (productsResult as? Result.Error)?.message ?: (productsResult as? Result.Error)?.exception?.message
        ).firstOrNull()

        if (errorMessage != null) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
        }

        ProfileStats(
            activeLists = listsTotal,
            pantries = pantriesTotal,
            products = productsTotal
        )
    }

    private fun updateNotificationPreference(type: NotificationPreference, enabled: Boolean) {
        val currentUser = _uiState.value.user ?: return
        val previousPreferences = _uiState.value.notifications
        val newPreferences = when (type) {
            NotificationPreference.EMAIL -> previousPreferences.copy(emailNotificationsEnabled = enabled)
            NotificationPreference.PUSH -> previousPreferences.copy(pushNotificationsEnabled = enabled)
            NotificationPreference.PRICE_ALERTS -> previousPreferences.copy(priceAlertsEnabled = enabled)
        }

        _uiState.update {
            it.copy(
                notifications = newPreferences,
                updatingPreference = type,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val metadata = currentUser.metadata?.toMutableMap() ?: mutableMapOf<String, Any>()
            when (type) {
                NotificationPreference.EMAIL -> metadata[EMAIL_NOTIFICATIONS_KEY] = enabled
                NotificationPreference.PUSH -> metadata[PUSH_NOTIFICATIONS_KEY] = enabled
                NotificationPreference.PRICE_ALERTS -> metadata[PRICE_ALERTS_KEY] = enabled
            }

            val request = UpdateUserProfileRequest(
                name = currentUser.name,
                surname = currentUser.surname,
                metadata = metadata
            )

            when (val result = userRepository.updateProfile(request).first { it !is Result.Loading }) {
                is Result.Success -> {
                    val updatedUser = result.data
                    _uiState.update {
                        it.copy(
                            user = updatedUser,
                            notifications = extractNotificationPreferences(updatedUser.metadata),
                            favoriteStores = extractFavoriteStores(updatedUser.metadata),
                            updatingPreference = null
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            notifications = previousPreferences,
                            updatingPreference = null,
                            errorMessage = result.message ?: result.exception.message
                        )
                    }
                }

                Result.Loading -> Unit
            }
        }
    }

    fun addFavoriteStore(storeName: String) {
        val currentUser = _uiState.value.user ?: return
        val trimmedName = storeName.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "El nombre de la tienda no puede estar vacío") }
            return
        }

        val currentStores = _uiState.value.favoriteStores
        if (currentStores.any { it.equals(trimmedName, ignoreCase = true) }) {
            _uiState.update { it.copy(errorMessage = "La tienda ya está en favoritos") }
            return
        }

        viewModelScope.launch {
            val previousState = _uiState.value
            val updatedStores = (previousState.favoriteStores + trimmedName)
                .map(String::trim)
                .filter { it.isNotEmpty() }
                .distinctBy { it.lowercase() }
                .sortedBy { it.lowercase() }

            _uiState.update {
                it.copy(
                    favoriteStores = updatedStores,
                    isFavoriteStoresLoading = true,
                    errorMessage = null
                )
            }

            val metadata = currentUser.metadata?.toMutableMap() ?: mutableMapOf<String, Any>()
            metadata[FAVORITE_STORES_KEY] = updatedStores

            val request = UpdateUserProfileRequest(
                name = currentUser.name,
                surname = currentUser.surname,
                metadata = metadata
            )

            when (val result = userRepository.updateProfile(request).first { it !is Result.Loading }) {
                is Result.Success -> {
                    val updatedUser = result.data
                    _uiState.update {
                        it.copy(
                            user = updatedUser,
                            favoriteStores = extractFavoriteStores(updatedUser.metadata),
                            notifications = extractNotificationPreferences(updatedUser.metadata),
                            isFavoriteStoresLoading = false
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            favoriteStores = previousState.favoriteStores,
                            isFavoriteStoresLoading = false,
                            errorMessage = result.message ?: result.exception.message
                        )
                    }
                }

                Result.Loading -> Unit
            }
        }
    }

    fun removeFavoriteStore(storeName: String) {
        val currentUser = _uiState.value.user ?: return
        val currentStores = _uiState.value.favoriteStores
        if (currentStores.isEmpty()) return

        val normalizedTarget = storeName.trim()
        if (normalizedTarget.isEmpty()) return

        if (currentStores.none { it.equals(normalizedTarget, ignoreCase = true) }) return

        viewModelScope.launch {
            val previousState = _uiState.value
            val updatedStores = currentStores.filterNot { it.equals(normalizedTarget, ignoreCase = true) }

            _uiState.update {
                it.copy(
                    favoriteStores = updatedStores,
                    isFavoriteStoresLoading = true,
                    errorMessage = null
                )
            }

            val metadata = currentUser.metadata?.toMutableMap() ?: mutableMapOf<String, Any>()
            metadata[FAVORITE_STORES_KEY] = updatedStores

            val request = UpdateUserProfileRequest(
                name = currentUser.name,
                surname = currentUser.surname,
                metadata = metadata
            )

            when (val result = userRepository.updateProfile(request).first { it !is Result.Loading }) {
                is Result.Success -> {
                    val updatedUser = result.data
                    _uiState.update {
                        it.copy(
                            user = updatedUser,
                            favoriteStores = extractFavoriteStores(updatedUser.metadata),
                            notifications = extractNotificationPreferences(updatedUser.metadata),
                            isFavoriteStoresLoading = false
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            favoriteStores = previousState.favoriteStores,
                            isFavoriteStoresLoading = false,
                            errorMessage = result.message ?: result.exception.message
                        )
                    }
                }

                Result.Loading -> Unit
            }
        }
    }

    private fun extractNotificationPreferences(metadata: Map<String, Any>?): NotificationPreferences {
        val prefs: Map<String, Any> = metadata ?: emptyMap()
        return NotificationPreferences(
            emailNotificationsEnabled = prefs.getBoolean(EMAIL_NOTIFICATIONS_KEY, defaultValue = true),
            pushNotificationsEnabled = prefs.getBoolean(PUSH_NOTIFICATIONS_KEY, defaultValue = true),
            priceAlertsEnabled = prefs.getBoolean(PRICE_ALERTS_KEY, defaultValue = false)
        )
    }

    private fun extractFavoriteStores(metadata: Map<String, Any>?): List<String> {
        val prefs: Map<String, Any> = metadata ?: emptyMap()
        val raw = prefs[FAVORITE_STORES_KEY] ?: return emptyList()
        val stores = when (raw) {
            is List<*> -> raw.mapNotNull { it?.toString()?.trim() }
            is Array<*> -> raw.mapNotNull { it?.toString()?.trim() }
            is String -> raw.split(",").map { it.trim() }
            else -> emptyList()
        }
        return stores
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val stats: ProfileStats = ProfileStats(),
    val notifications: NotificationPreferences = NotificationPreferences(),
    val favoriteStores: List<String> = emptyList(),
    val isFavoriteStoresLoading: Boolean = false,
    val updatingPreference: NotificationPreference? = null,
    val errorMessage: String? = null
)

data class ProfileStats(
    val activeLists: Int = 0,
    val pantries: Int = 0,
    val products: Int = 0
)

data class NotificationPreferences(
    val emailNotificationsEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val priceAlertsEnabled: Boolean = false
)

enum class NotificationPreference {
    EMAIL,
    PUSH,
    PRICE_ALERTS
}

private fun Map<String, Any>.getBoolean(key: String, defaultValue: Boolean): Boolean {
    val value = this[key] ?: return defaultValue
    return when (value) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> value.equals("true", ignoreCase = true) || value == "1"
        else -> defaultValue
    }
}

