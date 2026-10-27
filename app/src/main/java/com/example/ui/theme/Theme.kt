package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TerminalDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = TerminalBg,
    primaryContainer = TerminalSurfaceBorder,
    onPrimaryContainer = NeonCyan,
    secondary = NeonGold,
    onSecondary = TerminalBg,
    secondaryContainer = TerminalSurfaceElevated,
    onSecondaryContainer = NeonGold,
    tertiary = CyberPurple,
    onTertiary = TextPrimary,
    background = TerminalBg,
    onBackground = TextPrimary,
    surface = TerminalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TerminalSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = TerminalSurfaceBorder,
    error = BearRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Trading terminal defaults to dark mode
    dynamicColor: Boolean = false, // Keep high-contrast terminal theme
    content: @Composable () -> Unit
) {
    val colorScheme = TerminalDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = TerminalBg.toArgb()
                window.navigationBarColor = TerminalBg.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
