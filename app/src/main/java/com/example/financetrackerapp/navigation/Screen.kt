package com.example.financetrackerapp.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: Long): String =
            "edit_transaction/$transactionId"
    }

    object Categories : Screen("categories")
    object Profile : Screen("profile")
}