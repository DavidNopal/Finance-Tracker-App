package com.example.financetrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.financetrackerapp.auth.AuthViewModel
import com.example.financetrackerapp.data.local.CategoriesViewModel
import com.example.financetrackerapp.data.local.CategoriesViewModelFactory
import com.example.financetrackerapp.data.local.FinanceDatabase
import com.example.financetrackerapp.data.local.FinanceRepository
import com.example.financetrackerapp.data.local.TransactionsViewModel
import com.example.financetrackerapp.data.local.TransactionsViewModelFactory
import com.example.financetrackerapp.navigation.Screen
import com.example.financetrackerapp.presentation.screens.AddTransactionScreen
import com.example.financetrackerapp.presentation.screens.CategoriesScreen
import com.example.financetrackerapp.presentation.screens.EditTransactionScreen
import com.example.financetrackerapp.presentation.screens.HomeScreen
import com.example.financetrackerapp.presentation.screens.LoginScreen
import com.example.financetrackerapp.presentation.screens.ProfileScreen
import com.example.financetrackerapp.presentation.screens.SignUpScreen
import com.example.financetrackerapp.presentation.screens.TransactionsListScreen
import com.example.financetrackerapp.ui.theme.FinanceTrackerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FinanceTrackerAppTheme { AppRoot() } }
    }
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val db = remember { FinanceDatabase.getInstance(context) }
    val repo = remember { FinanceRepository(db.categoryDao(), db.transactionDao()) }

    val transactionsVm: TransactionsViewModel =
        viewModel(factory = TransactionsViewModelFactory(repo))

    val categoriesVm: CategoriesViewModel =
        viewModel(factory = CategoriesViewModelFactory(repo))

    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        composable(Screen.Login.route) {
            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                state = authState,
                onLoginClick = { email, pass -> authViewModel.login(email, pass) },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
            )
        }

        composable(Screen.SignUp.route) {
            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            SignUpScreen(
                state = authState,
                onSignUpClick = { name, email, pass -> authViewModel.signUp(name, email, pass) },
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                transactionsViewModel = transactionsVm,
                categoriesViewModel = categoriesVm,
                onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }


        composable(Screen.Transactions.route) {
            TransactionsListScreen(
                transactionsViewModel = transactionsVm,
                categoriesViewModel = categoriesVm,
                onNavigateToAdd = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToEdit = { id -> navController.navigate(Screen.EditTransaction.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.AddTransaction.route) {
            AddTransactionScreen(
                transactionsViewModel = transactionsVm,
                categoriesViewModel = categoriesVm,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) }
            )
        }

        composable(Screen.EditTransaction.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull()

            EditTransactionScreen(
                transactionId = id,
                viewModel = transactionsVm,
                categoriesViewModel = categoriesVm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(
                viewModel = categoriesVm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                userEmail = authState.currentUserEmail,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
