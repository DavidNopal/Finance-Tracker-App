package com.example.financetrackerapp.auth

import com.google.firebase.auth.FirebaseAuth

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun login(
        email: String,
        password: String,
        onResult: (Result<String>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val emailResult = auth.currentUser?.email ?: ""
                    onResult(Result.success(emailResult))
                } else {
                    val error = task.exception ?: Exception("Login failed")
                    onResult(Result.failure(error))
                }
            }
    }

    fun signUp(
        name: String,
        email: String,
        password: String,
        onResult: (Result<String>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val emailResult = auth.currentUser?.email ?: ""
                    onResult(Result.success(emailResult))
                } else {
                    val error = task.exception ?: Exception("Sign up failed")
                    onResult(Result.failure(error))
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
}
