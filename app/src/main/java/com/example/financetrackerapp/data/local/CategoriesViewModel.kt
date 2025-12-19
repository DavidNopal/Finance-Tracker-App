package com.example.financetrackerapp.data.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val repo: FinanceRepository
) : ViewModel() {

    // UI reads this
    val categories: StateFlow<List<CategoryEntity>> =
        repo.getAllCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            repo.insertCategory(
                CategoryEntity(
                    name = trimmed,
                    color = "#4CAF50" //
                )
            )

        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repo.deleteCategory(category)
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repo.updateCategory(category)
        }
    }
}

class CategoriesViewModelFactory(
    private val repo: FinanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoriesViewModel(repo) as T
    }
}
