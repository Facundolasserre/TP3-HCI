package com.example.bagit.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bagit.data.model.MemberRole
import com.example.bagit.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
            // Card principal del diálogo con sombra y bordes redondeados
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 420.dp)
                    .wrapContentHeight()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LightPurple
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 28.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Título: Share List
                    Text(
                        text = "Share List",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnDark,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // ========== SECCIÓN EMAIL ==========
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Email Address",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnDark
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { 
                                Text(
                                    "Email address", 
                                    color = OnDark.copy(alpha = 0.6f)
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = OnDark.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF3D3566),
                                unfocusedContainerColor = Color(0xFF3D3566),
                                focusedTextColor = OnDark,
                                unfocusedTextColor = OnDark,
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = Color(0xFF5A4E8E),
                                cursorColor = AccentPurple
                            ),
                            singleLine = true
                        )
                    }

                    // ========== SECCIÓN ROLE ==========
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Role",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnDark
                        )
                        
                        // Dropdown para seleccionar rol
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = showRoleDropdown,
                                onExpandedChange = { showRoleDropdown = !showRoleDropdown }
                            ) {
                                OutlinedTextField(
                                    value = selectedRole.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = showRoleDropdown
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF3D3566),
                                        unfocusedContainerColor = Color(0xFF3D3566),
                                        focusedTextColor = OnDark,
                                        unfocusedTextColor = OnDark,
                                        focusedBorderColor = AccentPurple,
                                        unfocusedBorderColor = Color(0xFF5A4E8E),
                                        disabledTextColor = OnDark,
                                        disabledBorderColor = Color(0xFF5A4E8E).copy(alpha = 0.5f)
                                    )
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showRoleDropdown,
                                    onDismissRequest = { showRoleDropdown = false },
                                    modifier = Modifier.background(Color(0xFF3D3566))
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
                        
                        // Leyenda descriptiva del rol
                        Text(
                            text = "Members can view and edit the list.",
                            fontSize = 12.sp,
                            color = OnDark.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // ========== SECCIÓN MESSAGE ==========
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Message",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnDark
                        )
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            placeholder = { 
                                Text(
                                    "Add a message (optional)", 
                                    color = OnDark.copy(alpha = 0.6f)
                                ) 
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF3D3566),
                                unfocusedContainerColor = Color(0xFF3D3566),
                                focusedTextColor = OnDark,
                                unfocusedTextColor = OnDark,
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = Color(0xFF5A4E8E),
                                cursorColor = AccentPurple
                            ),
                            minLines = 4
                        )
                    }
                    
                    // Mensaje de error
                    if (error != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF5F2C2C)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = error,
                                fontSize = 12.sp,
                                color = Color(0xFFFFB3B3),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ========== BOTONES INFERIORES ==========
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Cancel (outlined)
                        OutlinedButton(
                            onClick = onClose,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OnDark
                            )
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Botón Send (primary con ícono)
                        Button(
                            onClick = {
                                if (email.isNotBlank()) {
                                    onSend(email, message, selectedRole)
                                }
                            },
                            enabled = !isLoading && email.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPurple,
                                disabledContainerColor = Color(0xFF5A5080),
                                contentColor = OnDark,
                                disabledContentColor = OnDark.copy(alpha = 0.6f)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = OnDark,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    modifier = Modifier.size(18.dp),
                                    tint = OnDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = if (isLoading) "Enviando..." else "Send",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

