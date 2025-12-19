package com.example.financetrackerapp.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financetrackerapp.data.local.CategoriesViewModel
import com.example.financetrackerapp.data.local.TransactionsViewModel
import kotlin.math.abs

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transactionId: Long?,
    viewModel: TransactionsViewModel,
    categoriesViewModel: CategoriesViewModel,
    onNavigateBack: () -> Unit
) {
    if (transactionId == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Invalid transaction id")
            Spacer(Modifier.height(12.dp))
            Button(onClick = onNavigateBack) { Text("Back") }
        }
        return
    }

    LaunchedEffect(transactionId) {
        viewModel.selectTransaction(transactionId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSelected() }
    }


    val tx by viewModel.selectedTransaction.collectAsState()
    val categories by categoriesViewModel.categories.collectAsState(initial = emptyList())
    val categoryNameMap = remember(categories) { categories.associate { it.id to it.name } }

    // UI state (reset whenever tx changes)
    var title by remember(tx) { mutableStateOf(tx?.title.orEmpty()) }
    var amountText by remember(tx) { mutableStateOf(tx?.let { abs(it.amount).toString() }.orEmpty()) }
    var isExpense by remember(tx) { mutableStateOf(tx?.isExpense ?: true) }

    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        if (tx == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val categoryName = categoryNameMap[tx!!.categoryId] ?: "Uncategorized"

        val absAmount = abs(tx!!.amount)
        val formattedAmount = (if (isExpense) "-$" else "+$") + String.format("%.2f", absAmount)
        val amountColor =
            if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category name
            Text(
                text = "Category: $categoryName",
                style = MaterialTheme.typography.bodyMedium
            )

            // Current amount preview (formatted)
            Text(
                text = "Current: $formattedAmount",
                color = amountColor,
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; error = null },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Expense / Income toggle
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = isExpense,
                    onClick = { isExpense = true; error = null },
                    label = { Text("Expense") }
                )
                FilterChip(
                    selected = !isExpense,
                    onClick = { isExpense = false; error = null },
                    label = { Text("Income") }
                )
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it; error = null },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val trimmed = title.trim()
                    val amt = amountText.toDoubleOrNull()

                    if (trimmed.isEmpty()) {
                        error = "Title can't be empty"
                        return@Button
                    }
                    if (amt == null || amt == 0.0) {
                        error = "Enter a valid amount"
                        return@Button
                    }

                    saving = true

                    val signedAmount = if (isExpense) -abs(amt) else abs(amt)
                    val updated = tx!!.copy(
                        title = trimmed,
                        amount = signedAmount,
                        isExpense = isExpense
                    )

                    viewModel.saveTransaction(updated)

                    saving = false
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving
            ) {
                Text(if (saving) "Saving..." else "Save Changes")
            }
        }
    }
}

private fun TransactionsViewModel.loadTransaction(
    transactionId: Long
) {
}
