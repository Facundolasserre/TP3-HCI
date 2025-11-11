package com.example.bagit.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.*

/**
 * Template for creating responsive screens in BagIt app.
 * 
 * This template demonstrates:
 * - Responsive layout that adapts to different screen sizes
 * - Landscape/portrait orientation handling
 * - Tablet vs phone layouts
 * - Consistent use of BagItTheme colors and typography
 * - Proper padding and spacing based on screen size
 * 
 * Usage:
 * 1. Copy this file and rename it to your screen name
 * 2. Replace "TemplateScreen" with your screen name
 * 3. Customize the content in the ContentSection composable
 * 4. Adjust layout logic based on your needs (single column vs two columns)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveScreenTemplate(
    onBack: () -> Unit = {},
    // Add your screen-specific parameters here
) {
    // 1. Get screen information
    val screenSize = getScreenSize()
    val isLandscape = isLandscape()
    val isTablet = isTablet()
    val contentPadding = getContentPadding()
    val maxContentWidth = getMaxContentWidth()
    
    // 2. Calculate responsive sizes
    val cardPadding = when {
        isTablet && isLandscape -> 32.dp
        isTablet -> 28.dp
        else -> 24.dp
    }
    
    val verticalSpacing = when {
        isTablet && isLandscape -> 16.dp
        else -> 24.dp
    }
    
    // 3. Scaffold with TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Screen Title", // Replace with your title
                        fontWeight = FontWeight.SemiBold,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = OnDark,
                    navigationIconContentColor = OnDark
                )
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        // 4. Main content container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // 5. Choose layout based on screen size and orientation
            if (isTablet && isLandscape) {
                // Two-column layout for landscape tablets
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Left column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        ContentSection(
                            cardPadding = cardPadding,
                            verticalSpacing = verticalSpacing
                        )
                    }
                    
                    // Right column (optional)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Add secondary content here if needed
                    }
                }
            } else {
                // Single-column layout for portrait or phones
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            // Limit max width on large screens for better readability
                            if (maxContentWidth != Dp.Unspecified) {
                                Modifier.widthIn(max = maxContentWidth)
                            } else {
                                Modifier
                            }
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = contentPadding, vertical = contentPadding)
                ) {
                    ContentSection(
                        cardPadding = cardPadding,
                        verticalSpacing = verticalSpacing
                    )
                }
            }
        }
    }
}

/**
 * Main content section - customize this based on your screen needs
 */
@Composable
private fun ContentSection(
    cardPadding: Dp,
    verticalSpacing: Dp
) {
    // Example: Card with content
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(cardPadding)
        ) {
            Text(
                text = "Content Title",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnDark
            )
            
            Spacer(modifier = Modifier.height(verticalSpacing))
            
            Text(
                text = "Your content goes here. Use responsive spacing and padding.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnDark.copy(alpha = 0.8f)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(verticalSpacing))
    
    // Add more content sections as needed
}

@Preview(showBackground = true, backgroundColor = 0xFF171A26)
@Composable
fun ResponsiveScreenTemplatePreview() {
    BagItTheme {
        ResponsiveScreenTemplate()
    }
}

