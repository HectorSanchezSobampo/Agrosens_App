package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.myapplication.data.db.DBService
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authViewModel: AuthViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column {

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )

        Button(onClick = {
            val user = DBService.getUserByEmail(email)

            if (user != null && user.password == password) {
                authViewModel.signIn(user.email)
            }
        }) {
            Text("Iniciar sesión")
        }
    }
}
