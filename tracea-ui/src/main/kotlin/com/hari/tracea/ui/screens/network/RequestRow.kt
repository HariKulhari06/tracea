package com.hari.tracea.ui.screens.network

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.model.isMocked
import com.hari.tracea.core.util.DurationFormatter
import com.hari.tracea.core.util.SizeFormatter
import com.hari.tracea.ui.components.MethodBadge
import com.hari.tracea.ui.components.StatusBadge
import com.hari.tracea.ui.theme.LocalDebuggerColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

@Composable
fun RequestRow(
    event: NetworkEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(event.timestamp))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(colors.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            // Top Row: Method, Path, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MethodBadge(method = event.method)
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.path,
                        color = colors.onSurface,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (event.queryParameters.isNotEmpty()) {
                        Text(
                            text = "?...",
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (event.isMocked) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFC586C0), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "MOCK",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    StatusBadge(statusCode = event.statusCode)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Middle Row: URL/Host
            val displayUrl = if (event.url.length > 50) {
                event.url.take(25) + "..." + event.url.takeLast(20)
            } else {
                event.url
            }

            Text(
                text = displayUrl,
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Row: Time, Size, Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp).height(12.dp).width(12.dp)
                    )
                    Text(
                        text = formattedTime,
                        color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val totalSize = event.requestSize + event.responseSize
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp).height(12.dp).width(12.dp)
                        )
                        InfoChip(text = SizeFormatter.format(totalSize))
                    }
                    
                    event.timing.totalMs?.let { duration ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp).height(12.dp).width(12.dp)
                            )
                            InfoChip(text = DurationFormatter.format(duration))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    val colors = LocalDebuggerColors.current
    Text(
        text = text,
        color = colors.onSurfaceVariant,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium
    )
}
