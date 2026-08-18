package com.system.debugger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF007AFF),
    secondary = Color(0xFF5AC8FA),
    tertiary = Color(0xFFAF52DE),
    background = Color(0xFF0B0F19),
    surface = Color(0xFF151B2E),
    surfaceVariant = Color(0xFF1C2338),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color(0xFFE3E8F0),
    onSurfaceVariant = Color(0xFF9CA3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007AFF),
    secondary = Color(0xFF5AC8FA),
    tertiary = Color(0xFFAF52DE)
)

@Composable
fun SystemDebuggerTheme(
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

val CardColors = mapOf(
    "freezer" to Color(0xFF1E2A50),
    "privacy" to Color(0xFF0E3A3D),
    "file" to Color(0xFF2A1A4A),
    "tuner" to Color(0xFF0E3D2E),
    "audit" to Color(0xFF3D2B0E),
    "automation" to Color(0xFF3D1A2C)
)

fun getCardColor(id: String): Color = CardColors[id] ?: Color(0xFF1E2A50)