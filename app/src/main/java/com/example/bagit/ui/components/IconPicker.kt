package com.example.bagit.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class IconOption(
    val key: String,
    val icon: ImageVector,
    val label: String
)

val availableIcons = listOf(
    IconOption("ShoppingCart", Icons.Default.ShoppingCart, "Cart"),
    IconOption("Home", Icons.Default.Home, "Home"),
    IconOption("Star", Icons.Default.Star, "Star"),
    IconOption("Favorite", Icons.Default.Favorite, "Heart"),
    IconOption("Work", Icons.Default.Work, "Work"),
    IconOption("List", Icons.Default.List, "List")
)

@Composable
fun IconPickerItem(
    iconOption: IconOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .then(
                if (selected) {
                    Modifier.border(2.dp, Color(0xFF5249B6), CircleShape)
                } else {
                    Modifier
                }
            ),
        shape = CircleShape,
        color = if (selected) Color(0xFF5249B6).copy(alpha = 0.2f) else Color(0xFF2A2D3E)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = iconOption.icon,
                contentDescription = iconOption.label,
                tint = if (selected) Color(0xFF5249B6) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun IconPicker(
    selectedIconKey: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Icon",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableIcons) { iconOption ->
                IconPickerItem(
                    iconOption = iconOption,
                    selected = iconOption.key == selectedIconKey,
                    onClick = { onIconSelected(iconOption.key) }
                )
            }
        }
    }
}

