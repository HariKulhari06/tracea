package com.hari.tracea.ui.screens.detail.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.util.DurationFormatter
import com.hari.tracea.ui.components.EmptyState
import com.hari.tracea.ui.components.KeyValueCard
import com.hari.tracea.ui.components.SectionHeader
import com.hari.tracea.ui.theme.LocalDebuggerColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimingTab(
    event: NetworkEvent,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val scrollState = rememberScrollState()
    val timing = event.timing
    val totalMs = timing.totalMs

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (totalMs != null && totalMs > 0L) {
            val total = maxOf(totalMs, 1L)

            // Timing Overview Card
            SectionHeader(title = "Timing Overview")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.divider, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "TOTAL DURATION",
                            color = colors.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = DurationFormatter.format(totalMs),
                            color = colors.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    timing.waitingMs?.let { waiting ->
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "WAITING (TTFB)",
                                color = colors.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$waiting ms",
                                color = Color(0xFF4EC9B0),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.divider, thickness = 1.dp)

                // Unified Waterfall Visualizer Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "WATERFALL BREAKDOWN",
                        color = colors.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceVariant)
                    ) {
                        val fullW = maxWidth
                        val dnsW = ((timing.dnsMs ?: 0L).toFloat() / total * fullW.value).dp.coerceAtLeast(if ((timing.dnsMs ?: 0L) > 0) 2.dp else 0.dp)
                        val connW = ((timing.connectMs ?: 0L).toFloat() / total * fullW.value).dp.coerceAtLeast(if ((timing.connectMs ?: 0L) > 0) 2.dp else 0.dp)
                        val tlsW = ((timing.tlsMs ?: 0L).toFloat() / total * fullW.value).dp.coerceAtLeast(if ((timing.tlsMs ?: 0L) > 0) 2.dp else 0.dp)
                        val waitW = ((timing.waitingMs ?: 0L).toFloat() / total * fullW.value).dp.coerceAtLeast(if ((timing.waitingMs ?: 0L) > 0) 2.dp else 0.dp)
                        val dlW = ((timing.downloadMs ?: 0L).toFloat() / total * fullW.value).dp.coerceAtLeast(if ((timing.downloadMs ?: 0L) > 0) 2.dp else 0.dp)

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (dnsW > 0.dp) Box(modifier = Modifier.width(dnsW).fillMaxHeight().background(Color.Yellow))
                            if (connW > 0.dp) Box(modifier = Modifier.width(connW).fillMaxHeight().background(Color(0xFFFF9500)))
                            if (tlsW > 0.dp) Box(modifier = Modifier.width(tlsW).fillMaxHeight().background(Color(0xFF9C27B0)))
                            if (waitW > 0.dp) Box(modifier = Modifier.width(waitW).fillMaxHeight().background(Color(0xFF4EC9B0)))
                            if (dlW > 0.dp) Box(modifier = Modifier.width(dlW).fillMaxHeight().background(Color(0xFF569CD6)))
                        }
                    }
                }
            }

            // Detailed Phases Card
            SectionHeader(title = "Timing Phases")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.divider, RoundedCornerShape(10.dp))
            ) {
                TimingPhaseRow(label = "DNS Lookup", ms = timing.dnsMs, totalMs = total, color = Color.Yellow)
                HorizontalDivider(color = colors.divider.copy(alpha = 0.6f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 14.dp))

                TimingPhaseRow(label = "TCP Connect", ms = timing.connectMs, totalMs = total, color = Color(0xFFFF9500))
                HorizontalDivider(color = colors.divider.copy(alpha = 0.6f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 14.dp))

                TimingPhaseRow(label = "TLS Handshake", ms = timing.tlsMs, totalMs = total, color = Color(0xFF9C27B0))
                HorizontalDivider(color = colors.divider.copy(alpha = 0.6f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 14.dp))

                TimingPhaseRow(label = "Waiting (TTFB)", ms = timing.waitingMs, totalMs = total, color = Color(0xFF4EC9B0))
                HorizontalDivider(color = colors.divider.copy(alpha = 0.6f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 14.dp))

                TimingPhaseRow(label = "Content Download", ms = timing.downloadMs, totalMs = total, color = Color(0xFF569CD6))
            }

            // Timestamps Card
            val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
            val timestampItems = mutableListOf(
                "Start Timestamp" to timeFormat.format(Date(timing.startTimestamp))
            )
            timing.endTimestamp?.let { end ->
                timestampItems.add("End Timestamp" to timeFormat.format(Date(end)))
            }
            KeyValueCard(title = "Timestamps", items = timestampItems)
        } else {
            EmptyState(
                title = "No Timing Data",
                subtitle = "High-resolution network timing metrics are not available for this event."
            )
        }
    }
}

@Composable
private fun TimingPhaseRow(
    label: String,
    ms: Long?,
    totalMs: Long,
    color: Color
) {
    val colors = LocalDebuggerColors.current
    val valMs = ms ?: 0L
    val percentage = if (totalMs > 0) (valMs.toDouble() / totalMs.toDouble() * 100.0) else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Text(
                text = label,
                color = colors.onBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "$valMs ms",
                color = colors.onBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = String.format(Locale.US, "(%.1f%%)", percentage),
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(56.dp)
                    .padding(start = 6.dp)
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.surfaceVariant)
        ) {
            val barW = (valMs.toFloat() / maxOf(totalMs, 1L) * maxWidth.value).dp.coerceAtLeast(if (valMs > 0) 2.dp else 0.dp)
            if (barW > 0.dp) {
                Box(
                    modifier = Modifier
                        .width(barW)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(color.copy(alpha = 0.85f))
                )
            }
        }
    }
}

