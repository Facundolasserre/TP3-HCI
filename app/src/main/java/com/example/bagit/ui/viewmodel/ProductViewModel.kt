package com.example.bagit.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bagit.data.model.*
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.ProductRepository
import com.example.bagit.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _productsState = mutableStateOf<Result<PaginatedResponse<Product>>?>(null)
    val productsState: State<Result<PaginatedResponse<Product>>?> = _productsState

    private val _categoriesState = mutableStateOf<Result<PaginatedResponse<Category>>?>(null)
    val categoriesState: State<Result<PaginatedResponse<Category>>?> = _categoriesState

    private val _currentProductState = mutableStateOf<Result<Product>?>(null)
    val currentProductState: State<Result<Product>?> = _currentProductState

    fun getProducts(
        name: String? = null,
        categoryId: Long? = null,
        page: Int = 1,
        perPage: Int = 10
    ) {
        viewModelScope.launch {
            productRepository.getProducts(name, categoryId, page, perPage).collect { result ->
                _productsState.value = result
            }
        }
    }

    fun getProductById(id: Long) {
        viewModelScope.launch {
            productRepository.getProductById(id).collect { result ->
                _currentProductState.value = result
            }
        }
    }

    fun createProduct(
        name: String,
        categoryId: Long? = null,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            val category = categoryId?.let { CategoryId(it) }
            productRepository.createProduct(
                ProductRequest(name, category, metadata)
            ).collect { result ->
                if (result is Result.Success) {
                    getProducts()
                }
            }
        }
    }

    fun updateProduct(
        id: Long,
        name: String,
        categoryId: Long? = null,
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            val category = categoryId?.let { CategoryId(it) }
            productRepository.updateProduct(
                id,
                ProductRequest(name, category, metadata)
            ).collect { result ->
                _currentProductState.value = result
            }
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            productRepository.deleteProduct(id).collect { result ->
                if (result is Result.Success) {
                    getProducts()
                }
            }
        }
    }

    // Categories
    fun getCategories(name: String? = null, page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch {
            categoryRepository.getCategories(name, page, perPage).collect { result ->
                _categoriesState.value = result
            }
        }
    }

    fun createCategory(name: String, metadata: Map<String, Any>? = null) {
        viewModelScope.launch {
            categoryRepository.createCategory(CategoryRequest(name, metadata)).collect { result ->
                if (result is Result.Success) {
                    getCategories()
                }
            }
        }
    }

    fun updateCategory(id: Long, name: String, metadata: Map<String, Any>? = null) {
        viewModelScope.launch {
            categoryRepository.updateCategory(id, CategoryRequest(name, metadata)).collect { /* handle */ }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(id).collect { result ->
                if (result is Result.Success) {
                    getCategories()
                }
            }
        }
    }
}

