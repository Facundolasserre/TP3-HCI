package com.example.bagit.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.bagit.R
import com.example.bagit.ui.theme.*

@Composable
fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    onPasswordReset: (email: String) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    val isValid = remember(email) {
        email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBlue),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.reset_password_back),
                tint = White
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
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
                // Ícono en círculo blanco
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_hci),
                        contentDescription = stringResource(R.string.reset_password_app_icon),
                        modifier = Modifier.fillMaxSize(0.75f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.reset_password_title),
                    color = White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // Campo de email (versión compatible con Compose 1.7+)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text(stringResource(R.string.reset_password_email_placeholder), color = Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isValid) onPasswordReset(email.lowercase()) }
                    ),
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

                Spacer(Modifier.height(28.dp))

                // Botón blanco redondeado “Reset”
                Button(
                    onClick = { if (isValid) onPasswordReset(email.lowercase()) },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = Color.Black,
                        disabledContainerColor = White.copy(alpha = 0.5f),
                        disabledContentColor = Color.Black.copy(alpha = 0.6f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(stringResource(R.string.reset_password_button), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetPasswordPreview() {
    BagItTheme {
        ResetPasswordScreen()
    }
}
