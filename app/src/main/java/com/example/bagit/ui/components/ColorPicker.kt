package com.example.bagit.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ColorOption(
    val name: String,
    val hex: String,
    val color: Color
)

val availableColors = listOf(
    ColorOption("Purple", "#5249B6", Color(0xFF5249B6)),
    ColorOption("Blue", "#4A90E2", Color(0xFF4A90E2)),
    ColorOption("Green", "#50C878", Color(0xFF50C878)),
    ColorOption("Orange", "#FF8C42", Color(0xFFFF8C42)),
    ColorOption("Pink", "#E91E63", Color(0xFFE91E63)),
    ColorOption("Teal", "#26A69A", Color(0xFF26A69A))
)

@Composable
fun ColorSwatch(
    color: ColorOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = color.color
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ColorPicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Color",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            availableColors.forEach { colorOption ->
                ColorSwatch(
                    color = colorOption,
                    selected = colorOption.hex == selectedColorHex,
                    onClick = { onColorSelected(colorOption.hex) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

