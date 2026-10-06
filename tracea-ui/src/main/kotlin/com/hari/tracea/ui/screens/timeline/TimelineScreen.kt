package com.hari.tracea.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.ui.components.EmptyState
import com.hari.tracea.ui.components.MethodBadge
import com.hari.tracea.ui.components.SearchBar
import com.hari.tracea.ui.components.StatusBadge
import com.hari.tracea.ui.theme.LocalDebuggerColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onEventClick: (String) -> Unit = {},
    viewModel: TimelineViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val events by viewModel.events.collectAsState()
    val stats by viewModel.timelineStats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Timeline",
                        color = colors.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAll() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear all",
                            tint = colors.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
                windowInsets = WindowInsets.statusBars
            )
        },
        containerColor = colors.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Metrics Grid: Balanced 3-column layout matching iOS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceVariant.copy(alpha = 0.35f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "REQUESTS",
                        value = "${stats.totalRequests}",
                        color = colors.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "ERRORS",
                        value = "${stats.errorCount}",
                        color = if (stats.errorCount > 0) colors.statusError else colors.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "DATA",
                        value = stats.totalDataTransfer,
                        color = Color(0xFF569CD6),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "DURATION",
                        value = stats.formattedDuration,
                        color = colors.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "SLOWEST",
                        value = stats.slowestFormatted,
                        color = Color(0xFFCE9178),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "STATUS",
                        value = if (stats.totalRequests > 0) "Active" else "Idle",
                        color = Color(0xFF4EC9B0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                prompt = "Search URLs, paths...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = "No Timeline Data",
                        subtitle = "Network requests will appear here chronologically as they are executed."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        TimelineRow(
                            event = event,
                            onClick = { onEventClick(event.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.divider, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            color = colors.onSurfaceVariant,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TimelineRow(
    event: NetworkEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(event.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Timestamp
        Text(
            text = formattedTime,
            color = colors.onSurface,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier
                .width(82.dp)
                .padding(top = 2.dp)
        )

        // Timeline connector
        Box(
            modifier = Modifier
                .width(10.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Vertical line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(colors.divider)
            )
            // Dot
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.methodColor(event.method))
            )
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MethodBadge(method = event.method)

                val displayPath = if (event.path.isEmpty()) "/" else event.path
                Text(
                    text = displayPath,
                    color = colors.onBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                event.statusCode?.let { code ->
                    StatusBadge(statusCode = code)
                }
            }

            // Mini waterfall timing bar
            val totalMs = event.timing.totalMs
            if (totalMs != null && totalMs > 0L) {
                val total = maxOf(totalMs, 1L).toFloat()
                val dnsConnMs = ((event.timing.dnsMs ?: 0L) + (event.timing.connectMs ?: 0L)).toFloat()
                val ttfbMs = ((event.timing.tlsMs ?: 0L) + (event.timing.waitingMs ?: 0L)).toFloat()
                val dlMs = (event.timing.downloadMs ?: 0L).toFloat()

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(colors.surfaceVariant)
                ) {
                    val totalWidth = maxWidth
                    val dnsConnW = (dnsConnMs / total * totalWidth.value).dp.coerceAtLeast(if (dnsConnMs > 0) 2.dp else 0.dp)
                    val ttfbW = (ttfbMs / total * totalWidth.value).dp.coerceAtLeast(if (ttfbMs > 0) 2.dp else 0.dp)
                    val dlW = (dlMs / total * totalWidth.value).dp.coerceAtLeast(if (dlMs > 0) 2.dp else 0.dp)

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        if (dnsConnW > 0.dp) {
                            Box(
                                modifier = Modifier
                                    .width(dnsConnW)
                                    .fillMaxHeight()
                                    .background(Color(0xFFFFCC00))
                            )
                        }
                        if (ttfbW > 0.dp) {
                            Box(
                                modifier = Modifier
                                    .width(ttfbW)
                                    .fillMaxHeight()
                                    .background(Color(0xFF4CD964))
                            )
                        }
                        if (dlW > 0.dp) {
                            Box(
                                modifier = Modifier
                                    .width(dlW)
                                    .fillMaxHeight()
                                    .background(Color(0xFF007AFF))
                            )
                        }
                    }
                }
            }
        }
    }
}
