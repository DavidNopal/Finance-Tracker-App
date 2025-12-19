package com.example.financetrackerapp.data.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val repo: FinanceRepository
) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> =
        repo.getAllTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val selectedId = MutableStateFlow<Long?>(null)


    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTransaction: StateFlow<TransactionEntity?> =
        selectedId
            .filterNotNull()
            .flatMapLatest { id -> repo.observeTransactionById(id) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectTransaction(id: Long) {
        selectedId.value = id
    }

    fun clearSelected() {
        selectedId.value = null
    }

    fun saveTransaction(tx: TransactionEntity) {
        viewModelScope.launch { repo.updateTransaction(tx) }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch { repo.deleteTransaction(tx) }
    }

    fun addTransaction(title: String, amount: Double, categoryId: Long, isExpense: Boolean) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repo.insertTransaction(
                TransactionEntity(
                    title = trimmed,
                    amount = amount,
                    categoryId = categoryId
                )
            )
        }
    }
}

class TransactionsViewModelFactory(
    private val repo: FinanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionsViewModel(repo) as T
    }
}
