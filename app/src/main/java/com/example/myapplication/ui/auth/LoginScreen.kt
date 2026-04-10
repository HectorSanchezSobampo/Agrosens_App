package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.db.DBService
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authViewModel: AuthViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column {

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Correo") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )

        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = msg, color = MaterialTheme.colorScheme.error)
        }

        Button(onClick = {
            val trimmed = email.trim()
            val user = DBService.getUserByEmail(trimmed)

            if (user == null) {
                errorMessage = "No hay una cuenta con ese correo. Regístrate primero."
            } else if (user.password != password) {
                errorMessage = "Contraseña incorrecta."
            } else {
                authViewModel.signIn(user.email)
            }
        }) {
            Text("Iniciar sesión")
        }
    }
}
