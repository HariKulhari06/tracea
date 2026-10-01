package com.hari.tracea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.ui.theme.LocalDebuggerColors

@Composable
fun StatusBadge(
    statusCode: Int?,
    statusMessage: String? = null,
    showMessage: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val text = if (statusCode == null) "---" else if (showMessage && statusMessage != null) "$statusCode $statusMessage" else statusCode.toString()
    val containerColor = if (statusCode == null) colors.onSurfaceVariant else colors.statusColor(statusCode)

    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF0F111A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
