package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.myapplication.data.db.DBService
import com.example.myapplication.model.User

@Composable
fun RegisterScreen(onSuccess: () -> Unit) {

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column {

        OutlinedTextField(firstName, { firstName = it }, label = { Text("Nombre") })
        OutlinedTextField(lastName, { lastName = it }, label = { Text("Apellido") })
        OutlinedTextField(email, { email = it }, label = { Text("Correo") })
        OutlinedTextField(
            password,
            { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )

        Button(onClick = {
            DBService.createUser(
                User(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password
                )
            )
            onSuccess()
        }) {
            Text("Registrarse")
        }
    }
}
