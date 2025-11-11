package com.example.bagit.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.isTablet

sealed class BottomDest(
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val contentDescription: String
) {
    data object Favorites : BottomDest(
        icon = Icons.Outlined.StarBorder,
        selectedIcon = Icons.Filled.Star,
        contentDescription = "Favorites"
    )

    data object Home : BottomDest(
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        contentDescription = "Home"
    )

    data object Profile : BottomDest(
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Filled.Person,
        contentDescription = "Profile"
    )
}

@Composable
fun BottomNavBar(
    selected: BottomDest,
    onSelect: (BottomDest) -> Unit = {}
) {
    val destinations = listOf(
        BottomDest.Favorites,
        BottomDest.Home,
        BottomDest.Profile
    )

    // Usar responsive design
    val showLabels = isTablet()
    val navBarHeight = if (showLabels) 72.dp else 64.dp

    NavigationBar(
        modifier = Modifier.heightIn(min = navBarHeight),
        containerColor = Color(0xFF1F2330), // Slightly lighter than DarkNavy for elevation
        contentColor = OnDark
    ) {
        destinations.forEach { dest ->
            NavigationBarItem(
                selected = dest == selected,
                onClick = { onSelect(dest) },
                icon = {
                    Icon(
                        imageVector = if (dest == selected) dest.selectedIcon else dest.icon,
                        contentDescription = dest.contentDescription,
                        tint = if (dest == selected) OnDark else OnDark.copy(alpha = 0.65f)
                    )
                },
                label = if (showLabels) {
                    {
                        Text(
                            text = dest.contentDescription,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else null,
                alwaysShowLabel = showLabels,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF2A2D3E),
                    selectedIconColor = OnDark,
                    unselectedIconColor = OnDark.copy(alpha = 0.65f)
                )
            )
        }
    }
}

