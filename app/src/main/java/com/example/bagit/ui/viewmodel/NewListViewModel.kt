package com.example.bagit.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.ShoppingListRequest
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateListUiState(
    val name: String = "",
    val category: String = "Groceries",
    val colorHex: String = "#5249B6",
    val iconKey: String = "ShoppingCart",
    val isFavorite: Boolean = false,
    val notes: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class NewListViewModel @Inject constructor(
    private val repository: ShoppingListRepository
) : ViewModel() {

    var uiState by mutableStateOf(CreateListUiState())
        private set

    fun updateName(name: String) {
        uiState = uiState.copy(name = name, error = null)
    }

    fun updateCategory(category: String) {
        uiState = uiState.copy(category = category)
    }

    fun updateColor(colorHex: String) {
        uiState = uiState.copy(colorHex = colorHex)
    }

    fun updateIcon(iconKey: String) {
        uiState = uiState.copy(iconKey = iconKey)
    }

    fun toggleFavorite() {
        uiState = uiState.copy(isFavorite = !uiState.isFavorite)
    }

    fun updateNotes(notes: String) {
        uiState = uiState.copy(notes = notes)
    }

    fun createList(onSuccess: () -> Unit) {
        val trimmedName = uiState.name.trim()
        if (trimmedName.isEmpty()) {
            uiState = uiState.copy(error = "List name is required")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, error = null)

            val metadata = mutableMapOf<String, Any>(
                "category" to uiState.category,
                "color" to uiState.colorHex,
                "icon" to uiState.iconKey,
                "favorite" to uiState.isFavorite
            )

            val request = ShoppingListRequest(
                name = trimmedName,
                description = uiState.notes.takeIf { it.isNotBlank() },
                recurring = false,
                metadata = metadata
            )

            repository.createShoppingList(request).collect { result ->
                when (result) {
                    is Result.Success -> {
                        uiState = uiState.copy(isSaving = false, isSuccess = true)
                        onSuccess()
                    }
                    is Result.Error -> {
                        uiState = uiState.copy(
                            isSaving = false,
                            error = result.message ?: "Failed to create list"
                        )
                    }
                    is Result.Loading -> {
                        // Already handled
                    }
                }
            }
        }
    }

    fun resetState() {
        uiState = CreateListUiState()
    }
}

