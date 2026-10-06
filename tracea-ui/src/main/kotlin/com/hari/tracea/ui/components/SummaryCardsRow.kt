package com.hari.tracea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.util.DurationFormatter
import com.hari.tracea.core.util.SizeFormatter
import com.hari.tracea.ui.theme.LocalDebuggerColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SummaryCardsRow(
    event: NetworkEvent,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val totalMs = event.timing.totalMs ?: 0L
    val totalSize = event.requestSize + event.responseSize
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(event.timestamp))

    val statusText = when {
        event.statusCode != null -> {
            val msg = event.statusMessage
            if (!msg.isNullOrBlank()) "${event.statusCode} $msg" else "${event.statusCode}"
        }
        event.error != null -> "Failed"
        else -> "Pending"
    }

    val statusColor = colors.statusColor(event.statusCode ?: 0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: STATUS & DURATION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                icon = Icons.Default.Shield,
                label = "STATUS",
                value = statusText,
                color = statusColor,
                valueColor = statusColor,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                icon = Icons.Default.Timer,
                label = "DURATION",
                value = DurationFormatter.format(totalMs),
                color = colors.primary,
                valueColor = colors.primary,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: TOTAL SIZE & TIME
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                icon = Icons.Default.SwapVert,
                label = "TOTAL SIZE",
                value = SizeFormatter.format(totalSize),
                color = Color(0xFF569CD6),
                valueColor = colors.onBackground,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                icon = Icons.Default.Schedule,
                label = "TIME",
                value = formattedTime,
                color = colors.onSurfaceVariant,
                valueColor = colors.onBackground,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryCardsRow(
    status: String,
    duration: String,
    size: String,
    time: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                icon = Icons.Default.Shield,
                label = "STATUS",
                value = status,
                color = colors.primary,
                valueColor = colors.primary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                icon = Icons.Default.Timer,
                label = "DURATION",
                value = duration,
                color = colors.primary,
                valueColor = colors.primary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                icon = Icons.Default.SwapVert,
                label = "TOTAL SIZE",
                value = size,
                color = Color(0xFF569CD6),
                valueColor = colors.onBackground,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                icon = Icons.Default.Schedule,
                label = "TIME",
                value = time,
                color = colors.onSurfaceVariant,
                valueColor = colors.onBackground,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surface)
            .border(1.dp, colors.divider, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = colors.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
