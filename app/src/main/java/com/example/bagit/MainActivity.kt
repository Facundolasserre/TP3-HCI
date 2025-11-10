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
import com.example.bagit.auth.ui.VerifyAccountScreen
import com.example.bagit.ui.screens.HomeScreen
import com.example.bagit.ui.theme.BagItTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                                onRegisterSuccess = { email, password ->
                                    // Navegar a verificación de cuenta con email y password
                                    navController.navigate("verify_account/$email/$password") {
                                        popUpTo("new_user") { inclusive = true }
                                    }
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ---------- VERIFY ACCOUNT ----------
                        composable("verify_account/{email}/{password}") { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            val password = backStackEntry.arguments?.getString("password") ?: ""
                            VerifyAccountScreen(
                                email = email,
                                password = password,
                                onVerifySuccess = {
                                    // Después de verificar e iniciar sesión, ir a Home
                                    navController.navigate("home") {
                                        popUpTo("verify_account/{email}/{password}") { inclusive = true }
                                    }
                                },
                                onBackToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("verify_account/{email}/{password}") { inclusive = true }
                                    }
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
                            HomeScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}