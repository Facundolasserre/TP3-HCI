package com.example.bagit.ui.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Screen size categories based on Material Design breakpoints
 */
enum class ScreenSize {
    Small,      // < 600dp (phones in portrait)
    Medium,     // 600dp - 840dp (tablets in portrait, phones in landscape)
    Large       // > 840dp (tablets in landscape)
}

/**
 * Detects the current screen size category
 */
@Composable
fun getScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    return when {
        screenWidth < 600 -> ScreenSize.Small
        screenWidth < 840 -> ScreenSize.Medium
        else -> ScreenSize.Large
    }
}

/**
 * Checks if the device is in landscape orientation
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

/**
 * Checks if the device is a tablet (width >= 600dp)
 */
@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}

/**
 * Gets the current screen width in dp
 */
@Composable
fun getScreenWidthDp(): Int {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp
}

/**
 * Gets the current screen height in dp
 */
@Composable
fun getScreenHeightDp(): Int {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp
}

/**
 * Calculates responsive padding based on screen size
 */
@Composable
fun getResponsivePadding(): Dp {
    val screenSize = getScreenSize()
    return when (screenSize) {
        ScreenSize.Small -> 16.dp
        ScreenSize.Medium -> 24.dp
        ScreenSize.Large -> 32.dp
    }
}

/**
 * Calculates responsive horizontal padding for content
 */
@Composable
fun getContentPadding(): Dp {
    val screenSize = getScreenSize()
    return when (screenSize) {
        ScreenSize.Small -> 16.dp
        ScreenSize.Medium -> 32.dp
        ScreenSize.Large -> 48.dp
    }
}

/**
 * Calculates maximum content width for better readability on large screens
 */
@Composable
fun getMaxContentWidth(): Dp {
    val screenSize = getScreenSize()
    return when (screenSize) {
        ScreenSize.Small -> Dp.Unspecified
        ScreenSize.Medium -> 600.dp
        ScreenSize.Large -> 840.dp
    }
}

/**
 * Calculates responsive font size based on screen size
 */
@Composable
fun getResponsiveFontSize(small: Int, medium: Int, large: Int): Int {
    val screenSize = getScreenSize()
    return when (screenSize) {
        ScreenSize.Small -> small
        ScreenSize.Medium -> medium
        ScreenSize.Large -> large
    }
}

/**
 * Calculates responsive spacing multiplier for different screen sizes
 */
@Composable
fun getSpacingMultiplier(): Float {
    val screenSize = getScreenSize()
    return when (screenSize) {
        ScreenSize.Small -> 1f
        ScreenSize.Medium -> 1.2f
        ScreenSize.Large -> 1.5f
    }
}

/**
 * Gets responsive button height
 */
@Composable
fun getResponsiveButtonHeight(): Dp {
    val screenSize = getScreenSize()
    return when (screenSize) {
        ScreenSize.Small -> 48.dp
        ScreenSize.Medium -> 52.dp
        ScreenSize.Large -> 56.dp
    }
}

