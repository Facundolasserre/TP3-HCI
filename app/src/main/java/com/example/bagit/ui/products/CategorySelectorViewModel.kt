package com.example.bagit.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.Category
import com.example.bagit.data.model.CategoryRequest
import com.example.bagit.data.repository.CategoryRepository
import com.example.bagit.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Estados del selector de categorías
 */
sealed class CategorySelectorUiState {
    object Loading : CategorySelectorUiState()
    data class Success(
        val categories: List<Category>,
        val searchQuery: String = "",
        val selectedCategory: Category? = null
    ) : CategorySelectorUiState()
    data class Error(val message: String) : CategorySelectorUiState()
    object Empty : CategorySelectorUiState()
}

/**
 * Estados del diálogo de crear categoría
 */
data class CreateCategoryDialogState(
    val isVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel para el selector de categorías.
 * Maneja búsqueda con debounce, selección y creación de nuevas categorías.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class CategorySelectorViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategorySelectorUiState>(CategorySelectorUiState.Loading)
    val uiState: StateFlow<CategorySelectorUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(CreateCategoryDialogState())
    val dialogState: StateFlow<CreateCategoryDialogState> = _dialogState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    // Cache local de categorías para optimistic updates
    private var cachedCategories = listOf<Category>()
    
    // Preservar la categoría seleccionada incluso cuando el estado es Empty
    private var preservedSelectedCategory: Category? = null

    init {
        loadCategories()
        setupSearchDebounce()
    }

    /**
     * Configura el debounce para la búsqueda (400ms)
     */
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(400)
                .collect { query ->
                    searchCategories(query)
                }
        }
    }

    /**
     * Carga todas las categorías desde la API
     */
    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories(
                page = 1,
                perPage = 100, // Cargar todas para búsqueda local
                sortBy = "name",
                order = "ASC"
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        if (_uiState.value !is CategorySelectorUiState.Success) {
                            _uiState.value = CategorySelectorUiState.Loading
                        }
                    }
                    is Result.Success -> {
                        cachedCategories = result.data.data
                        val currentState = _uiState.value
                        // Usar la categoría seleccionada del estado actual o del preservado
                        val selectedCategory = (currentState as? CategorySelectorUiState.Success)?.selectedCategory
                            ?: preservedSelectedCategory

                        if (cachedCategories.isEmpty()) {
                            _uiState.value = CategorySelectorUiState.Empty
                        } else {
                            _uiState.value = CategorySelectorUiState.Success(
                                categories = cachedCategories,
                                selectedCategory = selectedCategory
                            )
                            preservedSelectedCategory = selectedCategory
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = CategorySelectorUiState.Error(
                            message = result.message ?: "Error al cargar categorías"
                        )
                    }
                }
            }
        }
    }

    /**
     * Actualiza la consulta de búsqueda con debounce
     */
    fun onSearchQueryChanged(query: String) {
        searchQueryFlow.value = query

        // Actualizar el estado inmediatamente para la UI
        val currentState = _uiState.value
        if (currentState is CategorySelectorUiState.Success) {
            _uiState.value = currentState.copy(searchQuery = query)
        }
    }

    /**
     * Busca categorías localmente (ya están cargadas)
     */
    private fun searchCategories(query: String) {
        val currentState = _uiState.value
        // Obtener la categoría seleccionada del estado actual o del preservado
        val selectedCategory = (currentState as? CategorySelectorUiState.Success)?.selectedCategory
            ?: preservedSelectedCategory

        if (query.isBlank()) {
            _uiState.value = CategorySelectorUiState.Success(
                categories = cachedCategories,
                searchQuery = "",
                selectedCategory = selectedCategory
            )
            preservedSelectedCategory = selectedCategory
        } else {
            val filtered = cachedCategories.filter {
                it.name.contains(query, ignoreCase = true)
            }

            _uiState.value = if (filtered.isEmpty()) {
                // Preservar la selección incluso cuando no hay resultados
                preservedSelectedCategory = selectedCategory
                CategorySelectorUiState.Empty
            } else {
                CategorySelectorUiState.Success(
                    categories = filtered,
                    searchQuery = query,
                    selectedCategory = selectedCategory
                )
            }
        }
    }

    /**
     * Selecciona una categoría
     */
    fun selectCategory(category: Category) {
        preservedSelectedCategory = category
        val currentState = _uiState.value
        when (currentState) {
            is CategorySelectorUiState.Success -> {
                _uiState.value = currentState.copy(selectedCategory = category)
            }
            is CategorySelectorUiState.Empty -> {
                // Si está vacío pero hay categorías en cache, restaurar estado Success
                if (cachedCategories.isNotEmpty()) {
                    _uiState.value = CategorySelectorUiState.Success(
                        categories = cachedCategories,
                        searchQuery = "",
                        selectedCategory = category
                    )
                }
            }
            else -> {
                // Para otros estados, esperar a que se carguen las categorías
                // La selección se aplicará cuando el estado cambie a Success
            }
        }
    }

    /**
     * Limpia la selección
     */
    fun clearSelection() {
        preservedSelectedCategory = null
        val currentState = _uiState.value
        when (currentState) {
            is CategorySelectorUiState.Success -> {
                _uiState.value = currentState.copy(selectedCategory = null)
            }
            is CategorySelectorUiState.Empty -> {
                // Si está vacío pero hay categorías en cache, restaurar estado Success sin selección
                if (cachedCategories.isNotEmpty()) {
                    _uiState.value = CategorySelectorUiState.Success(
                        categories = cachedCategories,
                        searchQuery = "",
                        selectedCategory = null
                    )
                }
            }
            else -> {
                // Para otros estados, no hacer nada
            }
        }
    }

    /**
     * Muestra el diálogo de crear categoría
     */
    fun showCreateDialog() {
        _dialogState.value = CreateCategoryDialogState(isVisible = true)
    }

    /**
     * Cierra el diálogo de crear categoría
     */
    fun dismissCreateDialog() {
        _dialogState.value = CreateCategoryDialogState(isVisible = false)
    }

    /**
     * Crea una nueva categoría
     */
    fun createCategory(name: String) {
        // Validación local de duplicados (case-insensitive)
        val isDuplicate = cachedCategories.any {
            it.name.equals(name, ignoreCase = true)
        }

        if (isDuplicate) {
            _dialogState.value = _dialogState.value.copy(
                errorMessage = "Ya existe una categoría con ese nombre"
            )
            return
        }

        viewModelScope.launch {
            _dialogState.value = _dialogState.value.copy(isSubmitting = true, errorMessage = null)

            val request = CategoryRequest(name = name.trim())

            categoryRepository.createCategory(request).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Agregar a cache local (optimistic update)
                        cachedCategories = cachedCategories + result.data

                        // Actualizar estado con nueva categoría y seleccionarla
                        val currentState = _uiState.value
                        _uiState.value = CategorySelectorUiState.Success(
                            categories = cachedCategories,
                            searchQuery = (currentState as? CategorySelectorUiState.Success)?.searchQuery ?: "",
                            selectedCategory = result.data
                        )

                        // Cerrar diálogo
                        _dialogState.value = CreateCategoryDialogState(isVisible = false)
                    }
                    is Result.Error -> {
                        // Manejar errores específicos
                        val errorMessage = when {
                            result.exception is HttpException -> {
                                when ((result.exception as HttpException).code()) {
                                    409 -> "Ya existe una categoría con ese nombre"
                                    400 -> "Nombre inválido. Debe tener entre 1 y 50 caracteres"
                                    else -> "Error al crear la categoría: ${result.message}"
                                }
                            }
                            else -> "Error al crear la categoría: ${result.message}"
                        }

                        _dialogState.value = _dialogState.value.copy(
                            isSubmitting = false,
                            errorMessage = errorMessage
                        )
                    }
                    is Result.Loading -> {
                        // Ya está en isSubmitting = true
                    }
                }
            }
        }
    }

    /**
     * Limpia el error del diálogo
     */
    fun clearDialogError() {
        _dialogState.value = _dialogState.value.copy(errorMessage = null)
    }

    /**
     * Reintenta cargar categorías después de un error
     */
    fun retry() {
        loadCategories()
    }
}

