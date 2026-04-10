package com.example.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authViewModel: AuthViewModel, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val recoveryInteractionSource = remember { MutableInteractionSource() }
    val registerInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Image(
                painter = painterResource(id = R.drawable.img_logo_app),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "¡Bienvenido a AgroSens! Inicie sesión para continuar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF1E232C),
                //textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(30.dp))

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Ingrese su correo electrónico",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Ingrese su contraseña",
                isPassword = true,
                passwordVisible = passwordVisible,
                onVisibilityChange = { passwordVisible = !passwordVisible },
                keyboardType = KeyboardType.Password
            )

            TextButton(
                onClick = { },
                modifier = Modifier.align(Alignment.End),
                interactionSource = recoveryInteractionSource
            ) {
                Text(
                    text = "¿Olvidó su contraseña?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6A707C),
                    fontSize = 14.sp,
                    modifier = Modifier.indication(
                        interactionSource = recoveryInteractionSource,
                        indication = ripple(color = Color(0xFFE8ECF4))
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386641))
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(35.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE8ECF4))
                Text(
                    text = "O inicie sesión con",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = Color(0xFF6A707C),
                    fontSize = 14.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE8ECF4))
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SocialLoginButton(iconRes = R.drawable.ic_facebook, modifier = Modifier.weight(1f))
                SocialLoginButton(iconRes = R.drawable.ic_google, modifier = Modifier.weight(1f))
                SocialLoginButton(
                    iconRes = R.drawable.ic_apple,
                    modifier = Modifier.weight(1f),
                    iconSize = 42.dp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes una cuenta?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6A707C),
                    fontSize = 15.sp
                )

                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(start = 4.dp),
                    interactionSource = registerInteractionSource
                ) {
                    Text(
                        text = "Regístrate",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF386641),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.indication(
                            interactionSource = registerInteractionSource,
                            indication = ripple(color = Color(0xFFE8ECF4))
                        )
                    )
                }
            }
        }
    }
}