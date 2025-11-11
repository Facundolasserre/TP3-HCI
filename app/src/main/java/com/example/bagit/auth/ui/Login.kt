package com.example.bagit.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.R
import com.example.bagit.data.repository.Result
import com.example.bagit.ui.theme.*
import com.example.bagit.ui.utils.*
import com.example.bagit.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Observar el estado del login desde el ViewModel
    val loginState by viewModel.loginState

    // Manejar cambios en el estado de login
    LaunchedEffect(loginState) {
        when (loginState) {
            is Result.Success -> {
                onLoginSuccess()
            }
            is Result.Error -> {
                errorMessage = (loginState as Result.Error).message ?: "Error desconocido"
                snackbarHostState.showSnackbar(errorMessage)
            }
            is Result.Loading, null -> {
                // No hacer nada
            }
        }
    }

    val isLandscape = isLandscape()
    val isTablet = isTablet()
    val contentPadding = getContentPadding()
    val maxContentWidth = getMaxContentWidth()
    
    // Responsive logo size
    val logoSize = when {
        isTablet && isLandscape -> 100.dp
        isTablet -> 120.dp
        else -> 100.dp
    }
    
    // Responsive card padding
    val cardPadding = when {
        isTablet && isLandscape -> 32.dp
        isTablet -> 32.dp
        else -> 24.dp
    }
    
    // Responsive spacing
    val verticalSpacing = when {
        isTablet && isLandscape -> 16.dp
        else -> 24.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavyBlue),
        contentAlignment = Alignment.Center
    ) {
        // For landscape tablets, use two-column layout
        if (isTablet && isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = contentPadding),
                horizontalArrangement = Arrangement.Center
            ) {
                // Left side: Logo and branding
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_hci),
                        contentDescription = "Logo BagIt",
                        modifier = Modifier.size(logoSize)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "BagIt",
                        color = White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Right side: Login form
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightPurple),
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (maxContentWidth != Dp.Unspecified) {
                                Modifier.widthIn(max = maxContentWidth)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    LoginFormContent(
                        username = username,
                        password = password,
                        onUsernameChange = { username = it },
                        onPasswordChange = { password = it },
                        onLoginClick = { viewModel.login(username, password) },
                        onForgotPassword = onForgotPassword,
                        onCreateAccount = onCreateAccount,
                        cardPadding = cardPadding,
                        isLoading = loginState is Result.Loading
                    )
                }
            }
        } else {
            // Portrait or phone: single column layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = contentPadding, vertical = contentPadding)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (maxContentWidth != Dp.Unspecified) {
                                Modifier.widthIn(max = maxContentWidth)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(cardPadding)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_hci),
                            contentDescription = "Logo BagIt",
                            modifier = Modifier.size(logoSize)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "BagIt",
                            color = White,
                            fontSize = if (isTablet) 40.sp else 32.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(verticalSpacing))

                        LoginFormFields(
                            username = username,
                            password = password,
                            onUsernameChange = { username = it },
                            onPasswordChange = { password = it },
                            onForgotPassword = onForgotPassword,
                            verticalSpacing = verticalSpacing
                        )

                        Spacer(modifier = Modifier.height(verticalSpacing))

                        Button(
                            onClick = { viewModel.login(username, password) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = White),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = loginState !is Result.Loading
                        ) {
                            if (loginState is Result.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp),
                                    strokeWidth = 2.dp,
                                    color = Black
                                )
                            }
                            Text(
                                text = if (loginState is Result.Loading) "Iniciando sesión..." else "Login",
                                color = Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isTablet) 20.sp else 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    Text(
                        text = "Don't have an account? ",
                        color = White,
                        fontSize = if (isTablet) 16.sp else 14.sp
                    )
                    Text(
                        text = "Sign Up",
                        color = AccentPurple,
                        textDecoration = TextDecoration.Underline,
                        fontSize = if (isTablet) 16.sp else 14.sp,
                        modifier = Modifier.clickable(onClick = onCreateAccount)
                    )
                }
            }
        }

        // Snackbar para mostrar errores
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun LoginFormContent(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit,
    cardPadding: Dp,
    isLoading: Boolean = false
) {
    val isTablet = isTablet()
    val isLandscape = isLandscape()

    // Responsive spacing
    val verticalSpacing = when {
        isTablet && isLandscape -> 16.dp
        else -> 24.dp
    }

    Column(
        modifier = Modifier.padding(cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome Back",
            color = White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(verticalSpacing))
        
        LoginFormFields(
            username = username,
            password = password,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onForgotPassword = onForgotPassword,
            verticalSpacing = verticalSpacing
        )

        Spacer(modifier = Modifier.height(verticalSpacing))

        Button(
            onClick = onLoginClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = White),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp,
                    color = Black
                )
            }
            Text(
                text = if (isLoading) "Iniciando sesión..." else "Login",
                color = Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row {
            Text(
                text = "Don't have an account? ",
                color = White,
                fontSize = 14.sp
            )
            Text(
                text = "Sign Up",
                color = AccentPurple,
                textDecoration = TextDecoration.Underline,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onCreateAccount)
            )
        }
    }
}

@Composable
private fun LoginFormFields(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onForgotPassword: () -> Unit,
    verticalSpacing: Dp
) {
    // ===== USERNAME =====
    OutlinedTextField(
        value = username,
        onValueChange = onUsernameChange,
        label = { Text("Username") },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentPurple,
            unfocusedBorderColor = Gray,
            cursorColor = AccentPurple,
            focusedLabelColor = AccentPurple,
            unfocusedLabelColor = Gray,
            focusedTextColor = White,
            unfocusedTextColor = White
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // ===== PASSWORD =====
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentPurple,
            unfocusedBorderColor = Gray,
            cursorColor = AccentPurple,
            focusedLabelColor = AccentPurple,
            unfocusedLabelColor = Gray,
            focusedTextColor = White,
            unfocusedTextColor = White
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Forgot Password?",
        color = Gray,
        textAlign = TextAlign.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp)
            .clickable(onClick = onForgotPassword)
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen({}, {}, {})
}