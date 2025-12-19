package com.example.financetrackerapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financetrackerapp.data.local.CategoriesViewModel
import com.example.financetrackerapp.data.local.CategoryEntity
import com.example.financetrackerapp.data.local.TransactionsViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionsViewModel: TransactionsViewModel,
    categoriesViewModel: CategoriesViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit = {} // optional
) {
    val categories by categoriesViewModel.categories.collectAsState(initial = emptyList())

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }

    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }

    // Default category when list loads
    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    val selectedCategory: CategoryEntity? =
        categories.firstOrNull { it.id == selectedCategoryId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("Back") }
                },
                actions = {
                    TextButton(onClick = onNavigateToCategories) {
                        Text("Categories")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; error = null },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it; error = null },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Income / Expense toggle
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = isExpense,
                    onClick = { isExpense = true },
                    label = { Text("Expense") }
                )
                FilterChip(
                    selected = !isExpense,
                    onClick = { isExpense = false },
                    label = { Text("Income") }
                )
            }

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name
                        ?: if (categories.isEmpty()) "No categories" else "Select category",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategoryId = cat.id
                                categoryMenuExpanded = false
                                error = null
                            }
                        )
                    }
                }
            }

            if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val parsedAmount = amountText.toDoubleOrNull()

                    val titleError = if (trimmedTitle.isEmpty()) "Title can't be empty" else null

                    val amountError = when {
                        parsedAmount == null -> "Enter a valid number"
                        parsedAmount == 0.0 -> "Amount cannot be 0"
                        else -> null
                    }


                    val catId = selectedCategoryId ?: run {
                        error = titleError ?: amountError ?: "Select a category"
                        return@Button
                    }

                    error = titleError ?: amountError
                    if (error != null) return@Button

                    val finalAmount =
                        if (isExpense) -abs(parsedAmount!!) else abs(parsedAmount!!)

                    transactionsViewModel.addTransaction(
                        title = trimmedTitle,
                        amount = finalAmount,
                        categoryId = catId,
                        isExpense = isExpense
                    )

                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = categories.isNotEmpty()
            ) {
                Text("Save")
            }

            if (categories.isEmpty()) {
                Text(
                    text = "Add a category first (Categories screen) to enable saving transactions.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
