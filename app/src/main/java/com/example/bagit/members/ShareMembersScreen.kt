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
import androidx.hilt.navigation.compose.hiltViewModel
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
    LaunchedEffect(listId) {
        viewModel.loadListMembers(listId, listName)
    }

    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            MembersTopBar(
                listName = listName,
                onBack = onBack,
                onAddMember = onAddMember,
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

            // Members List
            val displayedMembers = uiState.getDisplayedMembers()
            if (displayedMembers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
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
                            onEdit = { /* TODO */ },
                            onRemove = { viewModel.removeMember(member) }
                        )
                    }
                }
            }
        }
    }
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
        placeholder = { Text("Search member", color = OnDark.copy(alpha = 0.5f)) },
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

