package com.example.myapplication.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.User

class AuthViewModel : ViewModel() {

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    fun signIn(email: String) {
        _user.value = User(firstName = "", lastName = "", email = email, password = "")
    }

    fun signOut() {
        _user.value = null
    }
}
