package com.example.bagit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.*

@Composable
fun FavoritesScreen() {
    val isTablet = isTablet()
    val contentPadding = getContentPadding()
    
    // Responsive sizes
    val emojiSize = if (isTablet) 80.sp else 64.sp
    val titleSize = if (isTablet) 36.sp else 28.sp
    val subtitleSize = if (isTablet) 18.sp else 16.sp
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⭐",
                fontSize = emojiSize
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Favorites",
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = OnDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "TODO: Implement favorites",
                fontSize = subtitleSize,
                color = OnDark.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF171A26)
@Composable
fun FavoritesScreenPreview() {
    BagItTheme {
        FavoritesScreen()
    }
}

