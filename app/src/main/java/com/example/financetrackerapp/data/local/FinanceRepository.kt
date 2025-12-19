package com.example.financetrackerapp.data.local

import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {

    // CATEGORIES


    fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): CategoryEntity? =
        categoryDao.getCategoryById(id)

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insert(category)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.update(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.delete(category)


    // TRANSACTIONS

    fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    fun observeTransactionById(id: Long): Flow<TransactionEntity?> =
        transactionDao.observeTransactionById(id)

    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCategory(categoryId)


    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(limit)


    suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionDao.getTransactionById(id)

    suspend fun insertTransaction(tx: TransactionEntity): Long =
        transactionDao.insert(tx)

    suspend fun updateTransaction(tx: TransactionEntity) =
        transactionDao.update(tx)

    suspend fun deleteTransaction(tx: TransactionEntity) =
        transactionDao.delete(tx)
}
