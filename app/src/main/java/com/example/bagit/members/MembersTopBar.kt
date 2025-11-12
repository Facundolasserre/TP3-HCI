package com.example.bagit.members

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersTopBar(
    listName: String,
    onBack: () -> Unit = {},
    onAddMember: () -> Unit = {},
    onRenameList: () -> Unit = {},
    onShareList: () -> Unit = {}
) {
    val showMenuState = remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = listName,
                style = MaterialTheme.typography.headlineSmall,
                color = OnDark
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = OnDark
                )
            }
        },
        actions = {
            // Add Member Button
            IconButton(onClick = onAddMember) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add member",
                    tint = OnDark,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Menu Button
            Box {
                IconButton(onClick = { showMenuState.value = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = OnDark
                    )
                }
                DropdownMenu(
                    expanded = showMenuState.value,
                    onDismissRequest = { showMenuState.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Renombrar lista") },
                        onClick = {
                            onRenameList()
                            showMenuState.value = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Compartir lista") },
                        onClick = {
                            onShareList()
                            showMenuState.value = false
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkNavy,
            titleContentColor = OnDark,
            navigationIconContentColor = OnDark
        ),
        modifier = Modifier.statusBarsPadding()
    )
}

