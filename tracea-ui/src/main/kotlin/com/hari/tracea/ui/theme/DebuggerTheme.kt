package com.hari.tracea.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun DebuggerTheme(content: @Composable () -> Unit) {
    val colors = DebuggerColorScheme()
    val materialColors = darkColorScheme(
        background = colors.background,
        surface = colors.background,
        surfaceVariant = colors.surfaceVariant,
        onBackground = colors.onBackground,
        onSurface = colors.onBackground,
        onSurfaceVariant = colors.onSurface,
        primary = colors.primary,
        outline = colors.divider
    )

    CompositionLocalProvider(LocalDebuggerColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
