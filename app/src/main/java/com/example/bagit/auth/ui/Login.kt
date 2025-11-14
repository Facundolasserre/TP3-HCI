package com.example.bagit.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.R
import com.example.bagit.data.repository.Result
import com.example.bagit.ui.theme.*
import com.example.bagit.ui.utils.*
import com.example.bagit.ui.viewmodel.AuthViewModel
import androidx.compose.runtime.saveable.rememberSaveable
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

    val loginState by viewModel.loginState

    // Manejar cambios en el estado de login
    LaunchedEffect(loginState) {
        when (loginState) {
            is Result.Success -> onLoginSuccess()
            is Result.Error -> {
                errorMessage = (loginState as Result.Error).message ?: "Error desconocido"
                snackbarHostState.showSnackbar(errorMessage)
            }
            else -> Unit
        }
    }

    val isLandscape = isLandscape()
    val isTablet = isTablet()
    val contentPadding = getContentPadding()
    val maxContentWidth = getMaxContentWidth()

    val logoSize = when {
        isTablet && isLandscape -> 100.dp
        isTablet -> 120.dp
        else -> 100.dp
    }

    val cardPadding = when {
        isTablet && isLandscape -> 32.dp
        isTablet -> 32.dp
        else -> 24.dp
    }

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
        // Para tablets en horizontal → dos columnas
        if (isTablet && isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = contentPadding),
                horizontalArrangement = Arrangement.Center
            ) {
                // Lado izquierdo: logo
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_hci),
                        contentDescription = stringResource(R.string.login_logo_description),
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

                // Lado derecho: formulario
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
            // Celulares o portrait
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
                            contentDescription = stringResource(R.string.login_logo_description),
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
                            verticalSpacing = verticalSpacing,
                            onLoginAction = { viewModel.login(username, password) }
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
                                text = if (loginState is Result.Loading) stringResource(R.string.login_loading) else stringResource(R.string.login_button),
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
                        text = stringResource(R.string.login_no_account),
                        color = White,
                        fontSize = if (isTablet) 16.sp else 14.sp
                    )
                    Text(
                        text = stringResource(R.string.login_sign_up),
                        color = AccentPurple,
                        textDecoration = TextDecoration.Underline,
                        fontSize = if (isTablet) 16.sp else 14.sp,
                        modifier = Modifier.clickable(onClick = onCreateAccount)
                    )
                }
            }
        }

        // Snackbar para errores
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

    val verticalSpacing = when {
        isTablet && isLandscape -> 16.dp
        else -> 24.dp
    }

    Column(
        modifier = Modifier.padding(cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_welcome),
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
            verticalSpacing = verticalSpacing,
            onLoginAction = onLoginClick
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
                text = if (isLoading) stringResource(R.string.login_loading) else stringResource(R.string.login_button),
                color = Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text(
                text = stringResource(R.string.login_no_account),
                color = White,
                fontSize = 14.sp
            )
            Text(
                text = stringResource(R.string.login_sign_up),
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
    verticalSpacing: Dp,
    onLoginAction: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    // ===== EMAIL =====
    OutlinedTextField(
        value = username,
        onValueChange = onUsernameChange,
        label = { Text(stringResource(R.string.login_email_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onLoginAction() }
        ),
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
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(stringResource(R.string.login_password_label)) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onLoginAction() }
        ),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordVisible) stringResource(R.string.login_hide_password) else stringResource(R.string.login_show_password),
                    tint = Gray
                )
            }
        },
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
        text = stringResource(R.string.login_forgot_password),
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