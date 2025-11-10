package com.example.bagit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// ========= AUTH UI ========
import com.example.bagit.auth.ui.LoginScreen
import com.example.bagit.auth.ui.NewUserScreen
import com.example.bagit.auth.ui.ResetPasswordScreen
import com.example.bagit.ui.theme.BagItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BagItTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // ---------- LOGIN ----------
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onCreateAccount = {
                                    navController.navigate("new_user")
                                },
                                onForgotPassword = {
                                    navController.navigate("reset_password")
                                }
                            )
                        }

                        // ---------- REGISTER ----------
                        composable("new_user") {
                            NewUserScreen(
                                onRegisterSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ---------- RESET PASSWORD ----------
                        composable("reset_password") {
                            ResetPasswordScreen(
                                onPasswordReset = {
                                    navController.navigate("login") {
                                        popUpTo("reset_password") { inclusive = true }
                                    }
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ---------- HOME ----------
                        composable("home") {
                            /*
                            HomeScreen(
                                onMenuClick = { /* TODO: abrir drawer lateral */ },
                                onSearchClick = { /* TODO: ir a pantalla de búsqueda */ },
                                onItemClick = { list ->
                                    // TODO: navegar a detalle de lista
                                    // navController.navigate("list_detail/${list.id}") -> Crear ruta
                                },
                                onToggleFavorite = { list ->
                                    // TODO: actualizar favorito en ViewModel o estado global
                                },
                                onFabClick = {
                                    // TODO: crear nueva lista
                                },
                                onBottomNavSelected = { dest ->
                                    // TODO: manejar navegación inferior (por ejemplo, a perfil)
                                    // navController.navigate(dest)
                                }
                            )*/
                        }
                    }
                }
            }
        }
    }
}