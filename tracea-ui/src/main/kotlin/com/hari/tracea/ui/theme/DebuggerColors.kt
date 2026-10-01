package com.hari.tracea.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.hari.tracea.core.model.HttpMethod

data class DebuggerColorScheme(
    // Surface hierarchy — matches iOS exactly
    val background: Color = Color(0xFF0F111A),     // Deepest dark: scaffold, screen background
    val surface: Color = Color(0xFF1A1D2E),         // Cards, panels, rows, headers
    val surfaceVariant: Color = Color(0xFF242842),   // Sub-panels, button backgrounds, inputs, tags

    // Text hierarchy — matches iOS exactly
    val onBackground: Color = Color(0xFFE0E0E0),    // Primary headings, prominent body text, URLs
    val onSurface: Color = Color(0xFFB0B0B0),        // Secondary labels, timestamps, metadata keys
    val onSurfaceVariant: Color = Color(0xFF808080), // Icons, placeholders, subtle borders, footnotes

    // Accent
    val primary: Color = Color(0xFF7E97FF),          // Accents, links, active icons, selected buttons

    // Borders
    val divider: Color = Color(0xFF2A2D3E),          // Card borders, horizontal separators

    // HTTP Method Colors — VS Code Dark theme (matches iOS)
    val methodGet: Color = Color(0xFF4EC9B0),        // GET — teal
    val methodPost: Color = Color(0xFF7E97FF),       // POST — blue (same as primary)
    val methodPut: Color = Color(0xFFDCDC8B),        // PUT — pale yellow
    val methodDelete: Color = Color(0xFFF44747),     // DELETE — brick red
    val methodPatch: Color = Color(0xFFC586C0),      // PATCH — dusty rose

    // HTTP Status Colors — VS Code Dark theme (matches iOS)
    val status2xx: Color = Color(0xFF4EC9B0),        // Success — teal (same as GET)
    val status3xx: Color = Color(0xFF569CD6),        // Redirect — steel blue
    val status4xx: Color = Color(0xFFCE9178),        // Client error — terracotta
    val status5xx: Color = Color(0xFFF44747),        // Server error — red (same as DELETE)
    val statusError: Color = Color(0xFFF44747)       // Network errors — red
) {
    // Backward-compatible aliases — map old token names to iOS-aligned values
    val outline: Color get() = divider
    val surfaceContainer: Color get() = surfaceVariant
    val sectionHeader: Color get() = onSurfaceVariant
    val primaryContainer: Color get() = surfaceVariant
    val onPrimaryContainer: Color get() = onBackground
    val liveDot: Color get() = status2xx
    val errorDot: Color get() = statusError
    val status2xxContainer: Color get() = surface
    val status3xxContainer: Color get() = surface
    val status4xxContainer: Color get() = surface
    val status5xxContainer: Color get() = surface

    fun statusContainerColor(statusCode: Int): Color = surface

    fun methodColor(method: HttpMethod): Color = when(method) {
        HttpMethod.GET -> methodGet
        HttpMethod.POST -> methodPost
        HttpMethod.PUT -> methodPut
        HttpMethod.DELETE -> methodDelete
        HttpMethod.PATCH -> methodPatch
        else -> onSurfaceVariant
    }

    fun statusColor(statusCode: Int): Color = when(statusCode) {
        in 200..299 -> status2xx
        in 300..399 -> status3xx
        in 400..499 -> status4xx
        in 500..599 -> status5xx
        else -> onSurfaceVariant
    }
}

val LocalDebuggerColors = staticCompositionLocalOf { DebuggerColorScheme() }
