package com.hari.tracea.ui.screens.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.util.DurationFormatter
import com.hari.tracea.ui.components.MethodBadge
import com.hari.tracea.ui.components.StatusBadge
import com.hari.tracea.ui.theme.LocalDebuggerColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders an individual timeline network item, complete with a relative waterfall timing chart.
 */
@Composable
fun TimelineItem(
    event: NetworkEvent,
    sessionStart: Long,
    sessionDuration: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(event.timestamp))

    val dotColor = colors.methodColor(event.method)
    val outlineColor = colors.outline.copy(alpha = 0.5f)

    // Calculate waterfall timeline ratios
    val totalMs = event.timing.totalMs ?: 0L
    val startOffsetPercent = if (sessionDuration > 0L) {
        ((event.timestamp - sessionStart).toFloat() / sessionDuration).coerceIn(0f, 1f)
    } else 0f

    val maxDur = (1f - startOffsetPercent).coerceAtLeast(0.01f)
    val minDur = 0.01f.coerceAtMost(maxDur)
    val durationPercent = if (sessionDuration > 0L) {
        (totalMs.toFloat() / sessionDuration).coerceIn(minDur, maxDur)
    } else 0.05f

    val remainingPercent = (1f - startOffsetPercent - durationPercent).coerceIn(0f, 1f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp) // increased height to fit waterfall bar comfortably
            .clickable(onClick = onClick)
            .background(colors.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Timestamp
        Text(
            text = formattedTime,
            color = colors.onSurface,
            fontSize = 11.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(82.dp)
        )

        // Center Column: Connecting Line + Circle Dot
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                // Vertical line
                drawLine(
                    color = outlineColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                // Background for dot to create a gap in the line
                drawCircle(
                    color = colors.surface,
                    radius = 6.dp.toPx(),
                    center = Offset(centerX, size.height / 2)
                )
                // Status dot
                drawCircle(
                    color = dotColor,
                    radius = 4.dp.toPx(),
                    center = Offset(centerX, size.height / 2)
                )
            }
        }

        // Right Column: Request Details & Waterfall timing chart
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MethodBadge(method = event.method)
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Text(
                    text = event.path,
                    color = colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))
                
                event.statusCode?.let { code ->
                    StatusBadge(statusCode = code)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.host,
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                event.timing.totalMs?.let { duration ->
                    Text(
                        text = DurationFormatter.format(duration),
                        color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // DevTools-style Waterfall Timing Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(colors.outline.copy(alpha = 0.15f), shape = CircleShape)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (startOffsetPercent > 0f) {
                        Spacer(modifier = Modifier.weight(startOffsetPercent))
                    }

                    if (durationPercent > 0f) {
                        Row(
                            modifier = Modifier
                                .weight(durationPercent)
                                .fillMaxHeight()
                                .clip(CircleShape)
                        ) {
                            val dnsConnectMs = (event.timing.dnsMs ?: 0L) + (event.timing.connectMs ?: 0L) + (event.timing.tlsMs ?: 0L)
                            val waitingMs = event.timing.waitingMs ?: 0L
                            val downloadMs = event.timing.downloadMs ?: 0L
                            val sumMs = dnsConnectMs + waitingMs + downloadMs

                            if (sumMs > 0L) {
                                val dnsWeight = dnsConnectMs.toFloat() / sumMs
                                val waitWeight = waitingMs.toFloat() / sumMs
                                val downloadWeight = downloadMs.toFloat() / sumMs

                                if (dnsWeight > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .weight(dnsWeight)
                                            .fillMaxHeight()
                                            .background(androidx.compose.ui.graphics.Color(0xFFFFCC00)) // Yellow: Connect
                                    )
                                }
                                if (waitWeight > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .weight(waitWeight)
                                            .fillMaxHeight()
                                            .background(androidx.compose.ui.graphics.Color(0xFF4EC9B0)) // Green: TTFB/Wait
                                    )
                                }
                                if (downloadWeight > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .weight(downloadWeight)
                                            .fillMaxHeight()
                                            .background(androidx.compose.ui.graphics.Color(0xFF569CD6)) // Blue: Download
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colors.primary) // Fallback entire duration
                                )
                            }
                        }
                    }

                    if (remainingPercent > 0f) {
                        Spacer(modifier = Modifier.weight(remainingPercent))
                    }
                }
            }
        }
    }
}
