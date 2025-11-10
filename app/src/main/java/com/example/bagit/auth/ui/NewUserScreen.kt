package com.example.bagit.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.R
import com.example.bagit.data.repository.Result
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.DarkNavyBlue
import com.example.bagit.ui.theme.Gray
import com.example.bagit.ui.theme.White
import com.example.bagit.ui.theme.LightPurple
import com.example.bagit.ui.viewmodel.AuthViewModel

@Composable
fun NewUserScreen(
    onRegisterSuccess: (String, String) -> Unit, // Ahora recibe email Y password
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by rememberSaveable { mutableStateOf("") }
    var surname by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var rewriteEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val registerState by viewModel.registerState

    // Observar el estado de registro
    LaunchedEffect(registerState) {
        when (registerState) {
            is Result.Success -> {
                // Registro exitoso, navegar a verificación pasando email Y password
                onRegisterSuccess(email, password)
            }
            is Result.Error -> {
                val error = registerState as Result.Error
                // Mejorar el mensaje de error según el tipo
                errorMessage = when {
                    error.message?.contains("Failed to connect") == true ||
                    error.message?.contains("Unable to resolve host") == true ||
                    error.message?.contains("timeout") == true ->
                        "No se puede conectar al servidor. Verifica que la API esté corriendo."

                    error.message?.contains("already exists") == true ||
                    error.message?.contains("duplicate") == true ||
                    error.message?.contains("409") == true ->
                        "Este email ya está registrado. Intenta con otro email."

                    error.message?.contains("400") == true ->
                        "Datos inválidos. Verifica que todos los campos sean correctos."

                    error.message?.contains("500") == true ->
                        "Error en el servidor. Intenta de nuevo más tarde."

                    else -> "Error al registrar: ${error.message ?: "Desconocido"}"
                }
            }
            else -> {}
        }
    }

    val tfColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Gray,
        unfocusedIndicatorColor = Gray.copy(alpha = 0.7f),
        disabledIndicatorColor = Gray.copy(alpha = 0.4f),
        focusedTextColor = White,
        unfocusedTextColor = White,
        cursorColor = White,
        focusedPlaceholderColor = Gray,
        unfocusedPlaceholderColor = Gray
    )

    Scaffold { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyBlue)
                .padding(inner),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightPurple),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_hci),
                        contentDescription = "BagIt Logo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Welcome to BagIt",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )

                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Nombre") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it },
                        placeholder = { Text("Apellido") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth(),
                        isError = email.isNotEmpty() && !email.contains("@")
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = rewriteEmail,
                        onValueChange = { rewriteEmail = it },
                        placeholder = { Text("Confirmar email") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth(),
                        isError = rewriteEmail.isNotEmpty() && rewriteEmail != email
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Gray
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth(),
                        isError = password.isNotEmpty() && password.length < 6
                    )

                    // Mostrar mensaje de error si existe
                    if (errorMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            errorMessage = ""
                            when {
                                name.isBlank() -> errorMessage = "El nombre es requerido"
                                surname.isBlank() -> errorMessage = "El apellido es requerido"
                                !email.contains("@") -> errorMessage = "Email inválido"
                                email != rewriteEmail -> errorMessage = "Los emails no coinciden"
                                password.length < 6 -> errorMessage = "La contraseña debe tener al menos 6 caracteres"
                                else -> {
                                    // Registrar usuario
                                    viewModel.register(name, surname, email, password)
                                }
                            }
                        },
                        enabled = registerState !is Result.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = White,
                            contentColor = Color.Black
                        )
                    ) {
                        if (registerState is Result.Loading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(text = "Registrarse", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewUserScreenPreview() {
    BagItTheme {
        NewUserScreen(
            onRegisterSuccess = { _, _ -> }, // Recibe email y password
            onBack = {}
        )
    }
}