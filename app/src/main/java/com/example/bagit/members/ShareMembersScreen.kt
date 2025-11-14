package com.example.bagit.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import com.example.bagit.R
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.viewmodel.ShareMembersViewModel

@Composable
fun ShareMembersScreen(
    listId: Long,
    listName: String,
    onBack: () -> Unit = {},
    onAddMember: () -> Unit = {},
    onRenameList: () -> Unit = {},
    onShareList: () -> Unit = {},
    viewModel: ShareMembersViewModel = hiltViewModel()
) {
    Log.d("ShareMembersScreen", "Screen initialized: listId=$listId, listName=$listName")

    LaunchedEffect(listId) {
        Log.d("ShareMembersScreen", "LaunchedEffect triggered with listId=$listId")
        if (listId <= 0) {
            Log.e("ShareMembersScreen", "ERROR: Invalid listId=$listId (must be > 0)")
        } else {
            viewModel.loadListMembers(listId, listName)
        }
    }

    val uiState by viewModel.uiState
    var showShareDialog by remember { mutableStateOf(false) }
    var previousIsLoading by remember { mutableStateOf(false) }

    // Cerrar el diálogo automáticamente cuando addMember tiene éxito
    LaunchedEffect(uiState.isLoading, uiState.error) {
        // Si estaba cargando y ahora no está cargando y no hay error, significa éxito
        if (previousIsLoading && !uiState.isLoading && uiState.error == null && showShareDialog) {
            Log.d("ShareMembersScreen", "addMember success, closing dialog")
            showShareDialog = false
        }
        previousIsLoading = uiState.isLoading
    }

    Scaffold(
        topBar = {
            MembersTopBar(
                listName = listName,
                onBack = onBack,
                onAddMember = { showShareDialog = true },
                onRenameList = onRenameList,
                onShareList = onShareList
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues)
                .padding(vertical = 16.dp)
        ) {
            // Search Bar
            SearchBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tabs
            TabsSegmented(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error message if any
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF5F2C2C)
                    )
                ) {
                    Text(
                        text = uiState.error!!,
                        fontSize = 14.sp,
                        color = Color(0xFFFFB3B3),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Members List
            val displayedMembers = uiState.getDisplayedMembers()

            if (uiState.isLoading && displayedMembers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF7B68EE))
                }
            } else if (displayedMembers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No members found",
                        fontSize = 16.sp,
                        color = OnDark.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedMembers, key = { it.id }) { member ->
                        MemberRow(
                            member = member,
                            isCurrentUserOwner = uiState.isCurrentUserOwner,
                            onEdit = { /* TODO */ },
                            onRemove = { viewModel.removeMember(member) },
                            onChangeRole = { member, newRole ->
                                viewModel.updateMemberRole(member, newRole)
                            }
                        )
                    }
                }
            }
        }
    }

    // Share Member Dialog
    ShareMemberDialog(
        listName = listName,
        isVisible = showShareDialog,
        isLoading = uiState.isLoading,
        error = if (showShareDialog) uiState.error else null,
        onClose = { 
            showShareDialog = false
            // Limpiar error cuando se cierra el diálogo
            if (uiState.error != null) {
                viewModel.uiState.value = viewModel.uiState.value.copy(error = null)
            }
        },
        onSend = { email, message, role ->
            Log.d("ShareMembersScreen", "ShareMemberDialog onSend: email=$email, message=$message, role=$role")
            viewModel.addMember(email, message, role)
        },
        onCopyLink = {
            // TODO: Implement copy link functionality
        }
    )
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        placeholder = { Text(stringResource(R.string.share_members_search_placeholder), color = OnDark.copy(alpha = 0.5f)) },
        shape = RoundedCornerShape(12.dp),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.share_members_search_icon),
                tint = OnDark.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF2A2D3E),
            unfocusedContainerColor = Color(0xFF2A2D3E),
            focusedTextColor = OnDark,
            unfocusedTextColor = OnDark,
            focusedBorderColor = Color(0xFF5249B6),
            unfocusedBorderColor = Color(0xFF3D3F54),
            cursorColor = Color(0xFF5249B6)
        ),
        singleLine = true
    )
}

@Composable
fun TabsSegmented(
    selectedTab: MembersTab,
    onTabSelected: (MembersTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabButton(
            label = "All",
            isSelected = selectedTab == MembersTab.ALL,
            onClick = { onTabSelected(MembersTab.ALL) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            label = "Pending",
            isSelected = selectedTab == MembersTab.PENDING,
            onClick = { onTabSelected(MembersTab.PENDING) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            label = "Blocked",
            isSelected = selectedTab == MembersTab.BLOCKED,
            onClick = { onTabSelected(MembersTab.BLOCKED) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF322D59) else Color(0xFF1C1C30),
            contentColor = Color.White
        )
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF171A26)
@Composable
fun ShareMembersScreenPreview() {
    BagItTheme {
        ShareMembersScreen(
            listId = 1,
            listName = "Grocery Shopping"
        )
    }
}

