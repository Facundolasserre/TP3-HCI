package com.example.bagit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    onPrimary = White,
    background = DarkNavyBlue,
    onBackground = White,
    surface = LightPurple,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = AccentPurple,
    onPrimary = White,
    background = LightPurple,
    onBackground = Black,
    surface = White,
    onSurface = DarkNavyBlue
)

@Composable
fun BagItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}