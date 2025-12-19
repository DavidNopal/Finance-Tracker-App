package com.example.financetrackerapp.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Check if user is already logged in
        val email = repository.getCurrentUserEmail()
        if (email != null) {
            _uiState.value = _uiState.value.copy(
                isAuthenticated = true,
                currentUserEmail = email
            )
        }
    }

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()

        if (!isValidEmail(trimmedEmail)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid email address.")
            return
        }
        if (!isValidPassword(password)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        repository.login(trimmedEmail, password) { result ->
            if (result.isSuccess) {
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    currentUserEmail = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Login failed"
                )
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your name.")
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid email address.")
            return
        }
        if (!isValidPassword(password)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        repository.signUp(trimmedName, trimmedEmail, password) { result ->
            if (result.isSuccess) {
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    currentUserEmail = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Sign up failed"
                )
            }
        }
    }


    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

}
