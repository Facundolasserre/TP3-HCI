package com.example.bagit.auth.ui


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.R
import com.example.bagit.data.repository.Result
import com.example.bagit.ui.theme.*
import com.example.bagit.ui.viewmodel.AuthViewModel

private const val TAG = "VerifyAccountScreen"

@Composable
fun VerifyAccountScreen(
    email: String,
    password: String,
    onVerifySuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var code by rememberSaveable { mutableStateOf("") }
    var isVerified by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val verifyState by viewModel.userState
    val loginState by viewModel.loginState
    val resendCodeState by viewModel.resendCodeState

    Log.d(TAG, "VerifyAccountScreen renderizado - email=$email")

    // Observar el estado de verificación
    LaunchedEffect(verifyState) {
        when (verifyState) {
            is Result.Success -> {
                if (!isVerified) {
                    Log.d(TAG, "Verificación exitosa, iniciando login automático")
                    isVerified = true
                    snackbarHostState.showSnackbar("¡Cuenta verificada! Iniciando sesión...")
                    kotlinx.coroutines.delay(800)
                    // Hacer login automáticamente después de verificar
                    viewModel.login(email, password)
                }
            }
            is Result.Error -> {
                val errorMsg = (verifyState as Result.Error).message ?: "Código incorrecto. Verifica el código enviado a tu email."
                Log.e(TAG, "Error en verificación: $errorMsg")
                snackbarHostState.showSnackbar(errorMsg)
                isVerified = false
            }
            else -> {}
        }
    }

    // Observar el estado de login después de verificar
    LaunchedEffect(loginState) {
        if (isVerified) {
            when (loginState) {
                is Result.Success -> {
                    Log.d(TAG, "Login automático exitoso, navegando a Home")
                    kotlinx.coroutines.delay(500)
                    onVerifySuccess()
                }
                is Result.Error -> {
                    val errorMsg = "Cuenta verificada pero error al iniciar sesión. Por favor inicia sesión manualmente."
                    Log.e(TAG, "Error en login automático: $errorMsg")
                    snackbarHostState.showSnackbar(errorMsg)
                    kotlinx.coroutines.delay(2000)
                    onBackToLogin()
                }
                else -> {}
            }
        }
    }

    // Observar el estado de resend
    LaunchedEffect(resendCodeState) {
        when (resendCodeState) {
            is Result.Success -> {
                Log.d(TAG, "Código reenviado exitosamente")
                snackbarHostState.showSnackbar("✓ Código reenviado correctamente a $email")
            }
            is Result.Error -> {
                val errorMsg = (resendCodeState as Result.Error).message ?: "Error al reenviar código"
                Log.e(TAG, "Error en resend: $errorMsg")
                snackbarHostState.showSnackbar("Error: $errorMsg")
            }
            else -> {}
        }
    }

    // Máscara del email
    val emailMasked = remember(email) {
        val parts = email.split("@")
        if (parts.size == 2) {
            val username = parts[0]
            val domain = parts[1]
            val maskedUsername = if (username.length > 3) {
                username.take(3) + "•••"
            } else {
                username
            }
            "$maskedUsername@$domain"
        } else {
            email
        }
    }

    // Normaliza a minúsculas (el backend genera tokens en hex minúsculas) y limita a 16 alfanuméricos
    fun normalize(input: String): String {
        val filtered = input
            .lowercase()
            .filter { it.isLetterOrDigit() }
        return filtered.take(16)
    }

    val isReady = code.length == 16

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyBlue)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = LightPurple),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono en círculo
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_hci),
                            contentDescription = "App icon",
                            modifier = Modifier.fillMaxSize(0.7f)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Verificar Cuenta",
                        color = White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Ingresa el código de verificación enviado a $emailMasked",
                        color = White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(18.dp))

                    // Campo de código (estilo línea)
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = normalize(it) },
                        placeholder = { Text("Ingresa el código de 16 caracteres", color = Gray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (isReady) {
                                    Log.d(TAG, "Presionado Done en teclado - verificando código")
                                    viewModel.verifyAccount(email, code)
                                }
                            }
                        ),
                        supportingText = {
                            Text(
                                "${code.length}/16",
                                color = White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = White,
                            unfocusedIndicatorColor = White.copy(alpha = 0.6f),
                            cursorColor = White,
                            focusedPlaceholderColor = Gray,
                            unfocusedPlaceholderColor = Gray,
                            focusedTextColor = White,
                            unfocusedTextColor = White
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    // Botón "Verificar"
                    Button(
                        onClick = {
                            if (isReady) {
                                Log.d(TAG, "Click en Verificar - email=$email, code=${code.take(4)}...")
                                viewModel.verifyAccount(email, code)
                            }
                        },
                        enabled = isReady && verifyState !is Result.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = White,
                            contentColor = Color.Black,
                            disabledContainerColor = White.copy(alpha = 0.5f),
                            disabledContentColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (verifyState is Result.Loading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Verificar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Links secundarios
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                Log.d(TAG, "Click en Reenviar código - email=$email")
                                viewModel.resendVerificationCode(email)
                            },
                            enabled = resendCodeState !is Result.Loading
                        ) {
                            if (resendCodeState is Result.Loading) {
                                CircularProgressIndicator(
                                    color = White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                "Reenviar código",
                                color = White,
                                fontSize = 12.sp,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                        TextButton(onClick = {
                            Log.d(TAG, "Click en Volver al login")
                            onBackToLogin()
                        }) {
                            Text(
                                "Volver al login",
                                color = White,
                                fontSize = 12.sp,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VerifyAccountPreview() {
    BagItTheme {
        VerifyAccountScreen(
            email = "usuario@bagit.com",
            password = "123456",
            onVerifySuccess = {},
            onBackToLogin = {}
        )
    }
}