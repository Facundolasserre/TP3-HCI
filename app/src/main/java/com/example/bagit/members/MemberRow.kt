package com.example.bagit.members

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bagit.data.model.Member
import com.example.bagit.data.model.MemberRole
import com.example.bagit.ui.theme.OnDark

@Composable
fun MemberRow(
    member: Member,
    onEdit: (Member) -> Unit = {},
    onRemove: (Member) -> Unit = {}
) {
    val showMenuState = remember { mutableStateOf(false) }
    val avatarColor = try {
        Color(android.graphics.Color.parseColor(member.avatarColor))
    } catch (_: Exception) {
        Color(0xFF5249B6)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111126)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = avatarColor
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = member.name,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Member Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = member.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnDark
                    )
                    Text(
                        text = member.email,
                        fontSize = 13.sp,
                        color = OnDark.copy(alpha = 0.6f)
                    )
                }

                // Role Badge
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = when (member.role) {
                        MemberRole.OWNER -> Color(0xFFFFC107)
                        MemberRole.MEMBER -> Color(0xFF424242)
                    }
                ) {
                    Text(
                        text = when (member.role) {
                            MemberRole.OWNER -> "Owner"
                            MemberRole.MEMBER -> "Member"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (member.role) {
                            MemberRole.OWNER -> Color(0xFF2E2A3A)
                            MemberRole.MEMBER -> Color.White
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // More Menu
            Box {
                IconButton(onClick = { showMenuState.value = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Member options",
                        tint = OnDark.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenuState.value,
                    onDismissRequest = { showMenuState.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEdit(member)
                            showMenuState.value = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove") },
                        onClick = {
                            onRemove(member)
                            showMenuState.value = false
                        }
                    )
                }
            }
        }
    }
}

