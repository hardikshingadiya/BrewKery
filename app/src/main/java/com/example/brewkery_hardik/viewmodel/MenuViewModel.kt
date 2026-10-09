package com.example.brewkery_hardik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewkery_hardik.data.model.Category
import com.example.brewkery_hardik.data.model.MenuItem
import com.example.brewkery_hardik.data.model.StoreMetadata
import com.example.brewkery_hardik.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MenuUiState(
    val isLoading: Boolean = false,
    val items: List<MenuItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val metadata: StoreMetadata? = null,
    val errorMessage: String? = null
)

class MenuViewModel(
    private val repository: MenuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private var allItems: List<MenuItem> = emptyList()
    private var selectedCategoryId: String? = null
    private var searchQuery: String = ""

    fun loadMenu() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.getMenu()
                .onSuccess { response ->
                    allItems = response.items

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        items = allItems,
                        categories = response.categories,
                        metadata = response.meta,
                        errorMessage = null
                    )

                    applyFilters()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                            ?: "Unable to load menu. Please try again."
                    )
                }
        }
    }

    fun selectCategory(categoryId: String?) {
        selectedCategoryId = categoryId
        applyFilters()
    }

    fun searchItems(query: String) {
        searchQuery = query.trim()
        applyFilters()
    }

    private fun applyFilters() {
        val query = searchQuery.lowercase()

        val filteredItems = allItems.filter { item ->
            val matchesCategory =
                selectedCategoryId == null ||
                        item.categoryId == selectedCategoryId

            val matchesSearch =
                item.name.lowercase().contains(query) ||
                        item.tagline.lowercase().contains(query) ||
                        item.description.lowercase().contains(query)

            matchesCategory && matchesSearch
        }

        _uiState.value = _uiState.value.copy(
            items = filteredItems
        )
    }
}
