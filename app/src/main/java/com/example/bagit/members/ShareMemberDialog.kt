package com.example.bagit.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bagit.data.model.MemberRole
import com.example.bagit.ui.theme.OnDark

@Composable
fun ShareMemberDialog(
    listName: String,
    isVisible: Boolean,
    isLoading: Boolean = false,
    error: String? = null,
    onClose: () -> Unit,
    onSend: (email: String, message: String, role: MemberRole) -> Unit,
    onCopyLink: () -> Unit
) {
    if (!isVisible) return

    // State management
    var email by remember(isVisible) { mutableStateOf("") }
    var message by remember(isVisible) { mutableStateOf("") }
    var selectedRole by remember(isVisible) { mutableStateOf(MemberRole.MEMBER) }
    var showRoleDropdown by remember { mutableStateOf(false) }
    
    // Resetear campos cuando el diálogo se cierra exitosamente (no está cargando y no hay error)
    LaunchedEffect(isVisible, isLoading) {
        if (!isVisible && !isLoading && error == null) {
            email = ""
            message = ""
            selectedRole = MemberRole.MEMBER
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .background(
                        color = Color(0xFF4B3F7E),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4B3F7E)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with title and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Share $listName",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnDark
                        )
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = OnDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Email TextField
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        placeholder = { Text("Email", color = OnDark.copy(alpha = 0.6f)) },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = OnDark.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF3D3566),
                            unfocusedContainerColor = Color(0xFF3D3566),
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark,
                            focusedBorderColor = Color(0xFF7B68EE),
                            unfocusedBorderColor = Color(0xFF5A4E8E),
                            cursorColor = Color(0xFF7B68EE)
                        ),
                        singleLine = true
                    )

                    // Message TextField
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        placeholder = { Text("Share list with a message", color = OnDark.copy(alpha = 0.6f)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF3D3566),
                            unfocusedContainerColor = Color(0xFF3D3566),
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark,
                            focusedBorderColor = Color(0xFF7B68EE),
                            unfocusedBorderColor = Color(0xFF5A4E8E),
                            cursorColor = Color(0xFF7B68EE)
                        ),
                        minLines = 4
                    )
                    
                    // Error message
                    if (error != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF5F2C2C)
                            )
                        ) {
                            Text(
                                text = error,
                                fontSize = 12.sp,
                                color = Color(0xFFFFB3B3),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Member Roles Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Member Roles",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnDark
                        )

                        // Dropdown Menu for Roles
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { showRoleDropdown = !showRoleDropdown },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3D3566)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedRole.name,
                                        fontSize = 14.sp,
                                        color = OnDark
                                    )
                                    Icon(
                                        imageVector = if (showRoleDropdown)
                                            Icons.Default.KeyboardArrowUp
                                        else
                                            Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle dropdown",
                                        tint = OnDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Dropdown Menu
                            DropdownMenu(
                                expanded = showRoleDropdown,
                                onDismissRequest = { showRoleDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                MemberRole.entries.forEach { role ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = role.name,
                                                color = OnDark
                                            )
                                        },
                                        onClick = {
                                            selectedRole = role
                                            showRoleDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Copy Link Button
                        OutlinedButton(
                            onClick = onCopyLink,
                            enabled = !isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF3D3566),
                                contentColor = OnDark
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Copy link",
                                tint = OnDark,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(end = 6.dp)
                            )
                            Text(
                                text = "Copy link",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Send Button
                        Button(
                            onClick = {
                                if (email.isNotBlank()) {
                                    onSend(email, message, selectedRole)
                                    // Los campos se resetean cuando el dialog se cierra
                                }
                            },
                            enabled = !isLoading && email.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B68EE),
                                disabledContainerColor = Color(0xFF5A5080)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = OnDark,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isLoading) "Enviando..." else "Send",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnDark
                            )
                        }
                    }
                }
            }
        }
    }
}

