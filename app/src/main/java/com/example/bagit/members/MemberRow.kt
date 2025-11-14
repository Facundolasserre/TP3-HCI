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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.bagit.R
import com.example.bagit.data.model.Member
import com.example.bagit.data.model.MemberRole
import com.example.bagit.ui.theme.OnDark

@Composable
fun MemberRow(
    member: Member,
    isCurrentUserOwner: Boolean = false,
    onEdit: (Member) -> Unit = {},
    onRemove: (Member) -> Unit = {},
    onChangeRole: (Member, MemberRole) -> Unit = { _, _ -> }
) {
    var showMenuState by remember { mutableStateOf(false) }
    
    val avatarColor = remember(member.avatarColor) {
        try {
            Color(android.graphics.Color.parseColor(member.avatarColor))
        } catch (_: Exception) {
            Color(0xFF5249B6)
        }
    }
    
    // Get initials for avatar
    val initials = remember(member.name) {
        member.name.split(" ")
            .take(2)
            .joinToString("") { it.firstOrNull()?.toString() ?: "" }
            .uppercase()
            .take(2)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
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
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with initials
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = avatarColor,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (initials.isNotEmpty()) {
                            Text(
                                text = initials,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = member.name,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
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
                        color = OnDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = member.email,
                        fontSize = 13.sp,
                        color = OnDark.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Role Badge
                Surface(
                    modifier = Modifier.padding(start = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = when (member.role) {
                        MemberRole.OWNER -> Color(0xFFFFC107)
                        MemberRole.MEMBER -> Color(0xFF3A3A4A)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (member.role) {
                                MemberRole.OWNER -> Icons.Default.Star
                                MemberRole.MEMBER -> Icons.Default.Person
                            },
                            contentDescription = null,
                            tint = when (member.role) {
                                MemberRole.OWNER -> Color(0xFF2E2A3A)
                                MemberRole.MEMBER -> Color(0xFFA594FF)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when (member.role) {
                                MemberRole.OWNER -> "Owner"
                                MemberRole.MEMBER -> "Member"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (member.role) {
                                MemberRole.OWNER -> Color(0xFF2E2A3A)
                                MemberRole.MEMBER -> Color(0xFFA594FF)
                            }
                        )
                    }
                }
            }

            // More Menu - Solo visible si el usuario actual es el owner
            if (isCurrentUserOwner) {
                Box {
                    IconButton(
                        onClick = { showMenuState = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Member options",
                            tint = OnDark.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenuState,
                        onDismissRequest = { showMenuState = false },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = OnDark
                                    )
                                    Text(stringResource(R.string.share_members_edit))
                                }
                            },
                            onClick = {
                                onEdit(member)
                                showMenuState = false
                            }
                        )

                        // Change role options - Solo para miembros que no son owner
                        if (member.role != MemberRole.OWNER) {
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = OnDark
                                        )
                                        Text(stringResource(R.string.share_members_make_owner))
                                    }
                                },
                                onClick = {
                                    onChangeRole(member, MemberRole.OWNER)
                                    showMenuState = false
                                }
                            )
                        }
                        if (member.role != MemberRole.MEMBER) {
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = OnDark
                                        )
                                        Text(stringResource(R.string.share_members_make_member))
                                    }
                                },
                                onClick = {
                                    onChangeRole(member, MemberRole.MEMBER)
                                    showMenuState = false
                                }
                            )
                        }

                        // Remove - No permitir quitar al owner
                        if (member.role != MemberRole.OWNER) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = Color(0xFFFF5252)
                                        )
                                        Text(
                                            "Remove",
                                            color = Color(0xFFFF5252)
                                        )
                                    }
                                },
                                onClick = {
                                    onRemove(member)
                                    showMenuState = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

