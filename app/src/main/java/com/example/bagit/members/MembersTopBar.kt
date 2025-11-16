package com.example.bagit.members

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.bagit.R
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
                    contentDescription = stringResource(R.string.share_members_back),
                    tint = OnDark
                )
            }
        },
        actions = {
            // Add Member Button
            IconButton(onClick = onAddMember) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = stringResource(R.string.share_members_add_member),
                    tint = OnDark,
                    modifier = Modifier.size(24.dp)
                )
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

