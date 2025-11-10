package com.example.bagit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.data.repository.Result
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.DarkNavyBlue
import com.example.bagit.ui.theme.White
import com.example.bagit.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val userState by viewModel.userState
    var userName by remember { mutableStateOf("Usuario") }

    // Obtener información del usuario
    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }

    // Observar el estado del usuario
    LaunchedEffect(userState) {
        when (val state = userState) {
            is Result.Success -> {
                userName = state.data.name
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BagIt - Home",
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavyBlue
                ),
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Logout",
                            tint = White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyBlue)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Shopping Cart",
                    modifier = Modifier.size(120.dp),
                    tint = White
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "¡Bienvenido, $userName!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Tu cuenta ha sido verificada exitosamente.\nEsta es tu pantalla principal.",
                    fontSize = 16.sp,
                    color = White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(48.dp),
                            tint = White
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Pantalla Home",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Aquí irá el contenido principal de tu aplicación.",
                            fontSize = 14.sp,
                            color = White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = White
                    )
                ) {
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BagItTheme {
        HomeScreen()
    }
}

