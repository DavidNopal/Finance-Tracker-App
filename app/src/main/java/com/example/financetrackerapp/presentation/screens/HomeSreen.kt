package com.example.financetrackerapp.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financetrackerapp.data.local.CategoriesViewModel
import com.example.financetrackerapp.data.local.TransactionsViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    transactionsViewModel: TransactionsViewModel,
    categoriesViewModel: CategoriesViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val transactions by transactionsViewModel.transactions.collectAsState()
    val categories by categoriesViewModel.categories.collectAsState(initial = emptyList())


    val incomeTotal = remember(transactions) {
        transactions.filter { !it.isExpense }.sumOf { abs(it.amount) }
    }
    val expenseTotal = remember(transactions) {
        transactions.filter { it.isExpense }.sumOf { abs(it.amount) }
    }
    val balance = incomeTotal - expenseTotal


    val expenseByCategory: List<Pair<String, Double>> = remember(transactions, categories) {
        val nameMap = categories.associate { it.id to it.name }
        transactions
            .filter { it.isExpense }
            .groupBy { it.categoryId }
            .map { (catId, list) ->
                val name = nameMap[catId] ?: "Uncategorized"
                val total = list.sumOf { abs(it.amount) }
                name to total
            }
            .sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Dashboard") },
                actions = {
                    TextButton(onClick = onNavigateToProfile) { Text("Profile") }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SummaryRow(
                    balance = balance,
                    income = incomeTotal,
                    expenses = expenseTotal
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Spending by Category", fontWeight = FontWeight.SemiBold)

                        if (expenseByCategory.isEmpty()) {
                            Text(
                                "No expenses yet. Add an expense to see the chart.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            PieChartWithLegend(
                                data = expenseByCategory,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.weight(1f)
                    ) { Text("Transactions") }

                    OutlinedButton(
                        onClick = onNavigateToCategories,
                        modifier = Modifier.weight(1f)
                    ) { Text("Categories") }
                }
            }

            item {
                Text("Recent Transactions", fontWeight = FontWeight.SemiBold)
            }

            val recent = transactions.take(5)
            if (recent.isEmpty()) {
                item { Text("Nothing here yet — add your first transaction.") }
            } else {
                items(recent) { tx ->
                    RecentTransactionRow(
                        title = tx.title,
                        amount = tx.amount,
                        isExpense = tx.isExpense
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(balance: Double, income: Double, expenses: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Balance",
            value = money(balance),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Income",
            value = money(income),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Expenses",
            value = money(expenses),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RecentTransactionRow(title: String, amount: Double, isExpense: Boolean) {
    val absAmt = abs(amount)
    val text = (if (isExpense) "-$" else "+$") + String.format("%.2f", absAmt)
    val color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title)
            Text(text, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PieChartWithLegend(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {

    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )

    val total = data.sumOf { it.second }.coerceAtLeast(0.0001)

    val slices = data.mapIndexed { index, (label, value) ->
        Slice(
            label = label,
            value = value,
            color = palette[index % palette.size]
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier.size(140.dp)
        ) {
            var startAngle = -90f
            slices.forEach { s ->
                val sweep = (s.value / total * 360.0).toFloat()
                drawArc(
                    color = s.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                startAngle += sweep
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slices.take(6).forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawArc(s.color, 0f, 360f, true)
                    }
                    Spacer(Modifier.width(8.dp))
                    val pct = (s.value / total * 100.0)
                    Text("${s.label} (${pct.toInt()}%)", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private data class Slice(val label: String, val value: Double, val color: Color)

private fun money(value: Double): String {
    val sign = if (value < 0) "-" else ""
    return sign + "$" + String.format("%.2f", abs(value))
}
