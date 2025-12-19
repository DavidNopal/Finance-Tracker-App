package com.example.financetrackerapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financetrackerapp.data.local.CategoriesViewModel
import com.example.financetrackerapp.data.local.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    transactionsViewModel: TransactionsViewModel,
    categoriesViewModel: CategoriesViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val transactions by transactionsViewModel.transactions.collectAsState()
    val categories by categoriesViewModel.categories.collectAsState(initial = emptyList())

    // Map categoryId -> categoryName
    val categoryNameMap = remember(categories) {
        categories.associate { it.id to it.name }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            transactions.forEach { tx ->
                val categoryName = categoryNameMap[tx.categoryId] ?: "Uncategorized"

                val absAmount = kotlin.math.abs(tx.amount)
                val amountText = (if (tx.isExpense) "-$" else "+$") + String.format("%.2f", absAmount)
                val amountColor = if (tx.isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    onClick = { onNavigateToEdit(tx.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(tx.title, style = MaterialTheme.typography.titleMedium)
                            Text(categoryName, style = MaterialTheme.typography.bodyMedium)

                            Text(
                                text = amountText,
                                color = amountColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        TextButton(onClick = { transactionsViewModel.deleteTransaction(tx) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
