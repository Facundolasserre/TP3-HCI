package com.example.bagit.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
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
    onRegisterSuccess: (String, String) -> Unit, // email y password
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by rememberSaveable { mutableStateOf("") }
    var surname by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val registerState by viewModel.registerState

    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Observar el estado de registro
    LaunchedEffect(registerState) {
        when (registerState) {
            is Result.Success -> onRegisterSuccess(email, password)
            is Result.Error -> {
                val error = registerState as Result.Error
                errorMessage = when {
                    error.message?.contains("Failed to connect") == true ||
                            error.message?.contains("Unable to resolve host") == true ||
                            error.message?.contains("timeout") == true ->
                        context.getString(R.string.new_user_error_connection)
                    error.message?.contains("already exists") == true ||
                            error.message?.contains("duplicate") == true ||
                            error.message?.contains("409") == true ->
                        context.getString(R.string.new_user_error_email_exists)
                    error.message?.contains("400") == true ->
                        context.getString(R.string.new_user_error_invalid_data)
                    error.message?.contains("500") == true ->
                        context.getString(R.string.new_user_error_server)
                    else -> context.getString(R.string.new_user_error_register, error.message ?: context.getString(R.string.common_error))
                }
            }
            else -> Unit
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
                .padding(inner)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.new_user_back),
                    tint = White
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = LightPurple),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.Center)
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
                        contentDescription = stringResource(R.string.new_user_logo),
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = stringResource(R.string.new_user_welcome),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )

                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text(stringResource(R.string.new_user_name_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it },
                        placeholder = { Text(stringResource(R.string.new_user_surname_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text(stringResource(R.string.new_user_email_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth(),
                        isError = email.isNotEmpty() && !email.contains("@")
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text(stringResource(R.string.new_user_password_placeholder)) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) stringResource(R.string.new_user_hide_password) else stringResource(R.string.new_user_show_password),
                                    tint = Gray
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth(),
                        isError = password.isNotEmpty() && password.length < 6
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text(stringResource(R.string.new_user_repeat_password_placeholder)) },
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) stringResource(R.string.new_user_hide_password) else stringResource(R.string.new_user_show_password),
                                    tint = Gray
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth(),
                        isError = confirmPassword.isNotEmpty() && confirmPassword != password
                    )

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
                                name.isBlank() -> errorMessage = context.getString(R.string.new_user_error_name_required)
                                surname.isBlank() -> errorMessage = context.getString(R.string.new_user_error_surname_required)
                                !email.contains("@") -> errorMessage = context.getString(R.string.new_user_error_invalid_email)
                                password.length < 6 -> errorMessage = context.getString(R.string.new_user_error_password_length)
                                confirmPassword != password -> errorMessage = context.getString(R.string.new_user_error_passwords_dont_match)
                                else -> viewModel.register(name, surname, email, password)
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
                            Text(text = stringResource(R.string.new_user_register_button), fontWeight = FontWeight.Bold)
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
            onRegisterSuccess = { _, _ -> },
            onBack = {}
        )
    }
}