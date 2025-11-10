package com.example.bagit.ui.screens.examples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.data.repository.Result
import com.example.bagit.ui.viewmodel.AuthViewModel
import com.example.bagit.ui.viewmodel.ShoppingListViewModel

/**
 * Pantalla de ejemplo que muestra cómo usar los ViewModels
 * Esta es una implementación de referencia que puedes adaptar a tus necesidades
 */
@Composable
fun ExampleScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by authViewModel.loginState
    val isLoggedIn by authViewModel.isLoggedIn
    val listsState by shoppingListViewModel.listsState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Backend Example",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isLoggedIn) {
            // Login Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Login", style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            authViewModel.login(email, password)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login")
                    }

                    // Show login state
                    when (loginState) {
                        is Result.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                        is Result.Success -> {
                            Text(
                                "Login successful!",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        is Result.Error -> {
                            Text(
                                "Error: ${(loginState as Result.Error).message}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        null -> {}
                    }
                }
            }
        } else {
            // Shopping Lists Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Shopping Lists", style = MaterialTheme.typography.titleLarge)

                        Button(
                            onClick = {
                                shoppingListViewModel.getShoppingLists()
                            }
                        ) {
                            Text("Refresh")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show lists
                    when (val state = listsState) {
                        is Result.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        is Result.Success -> {
                            if (state.data.data.isEmpty()) {
                                Text("No lists found")
                            } else {
                                LazyColumn {
                                    items(state.data.data) { list ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                Text(
                                                    text = list.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                list.description?.let {
                                                    Text(
                                                        text = it,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                Text(
                                                    text = "Owner: ${list.owner.name} ${list.owner.surname}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is Result.Error -> {
                            Text(
                                "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        null -> {
                            Text("Click Refresh to load lists")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { authViewModel.logout() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}

/**
 * Ejemplo de uso:
 *
 * En tu MainActivity o Navigation:
 * composable("example") {
 *     ExampleScreen()
 * }
 */

