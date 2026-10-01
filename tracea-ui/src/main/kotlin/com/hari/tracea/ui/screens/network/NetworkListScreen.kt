package com.hari.tracea.ui.screens.network

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hari.tracea.ui.components.EmptyState
import com.hari.tracea.ui.components.SearchBar
import com.hari.tracea.ui.components.SessionHeader
import com.hari.tracea.ui.theme.LocalDebuggerColors

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun NetworkListScreen(
    onEventClick: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    onClose: (() -> Unit)? = null,
    viewModel: NetworkListViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val context = LocalContext.current
    val events by viewModel.events.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val activeMethodFilter by viewModel.activeMethodFilter.collectAsState()

    var deleteConfirmationSession by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val handleClose = {
        if (onClose != null) {
            onClose()
        } else {
            context.findActivity()?.finish()
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(colors.background)) {
                // Top Navigation Header: [✕] Network [🗑] [⚙]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .height(52.dp)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { handleClose() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Network",
                        color = colors.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear All",
                            tint = colors.statusError,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Search Bar & Filter Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        prompt = "Search URLs, paths, hosts...",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    NetworkFilterBar(
                        activeStatusFilter = activeFilter,
                        activeMethodFilter = activeMethodFilter,
                        onStatusSelected = { viewModel.setFilter(it) },
                        onMethodSelected = { viewModel.setMethodFilter(it) },
                        onReset = { viewModel.resetFilters() }
                    )
                }

                HorizontalDivider(color = colors.divider, thickness = 1.dp)
            }
        },
        containerColor = colors.background,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (events.isEmpty()) {
                EmptyState(
                    title = if (searchQuery.isNotEmpty()) "No Matching Requests" else "No Events",
                    subtitle = if (searchQuery.isNotEmpty()) "No requests match the selected filters or search query." else "Network requests captured by Tracea will appear here automatically."
                )
            } else {
                val grouped = remember(events) { events.groupBy { it.sessionId } }
                val collapsedSessions = remember { mutableStateMapOf<String, Boolean>() }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    grouped.forEach { (sessionId, sessionEvents) ->
                        val sessionName = sessionEvents.firstOrNull()?.sessionName ?: "Unknown Session"
                        val isCollapsed = collapsedSessions[sessionId] ?: false

                        item(key = sessionId) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                border = BorderStroke(1.dp, colors.divider)
                            ) {
                                Column {
                                    SessionHeader(
                                        name = sessionName,
                                        requestCount = sessionEvents.size,
                                        isCollapsed = isCollapsed,
                                        onToggle = { collapsedSessions[sessionId] = !isCollapsed },
                                        onShareClick = { viewModel.exportSessionHar(context, sessionId, sessionName) },
                                        onDeleteClick = { deleteConfirmationSession = Pair(sessionId, sessionName) }
                                    )

                                    if (!isCollapsed) {
                                        sessionEvents.forEachIndexed { index, event ->
                                            RequestRow(
                                                event = event,
                                                onClick = { onEventClick(event.id) }
                                            )
                                            if (index < sessionEvents.size - 1) {
                                                HorizontalDivider(
                                                    color = colors.divider.copy(alpha = 0.6f),
                                                    thickness = 0.5.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            deleteConfirmationSession?.let { (sessionId, sessionName) ->
                AlertDialog(
                    onDismissRequest = { deleteConfirmationSession = null },
                    title = { Text(text = "Delete Session", color = colors.onSurface) },
                    text = { Text(text = "Are you sure you want to delete '$sessionName'? This will permanently remove all its network logs.", color = colors.onSurfaceVariant) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteSession(sessionId)
                                deleteConfirmationSession = null
                            }
                        ) {
                            Text(text = "DELETE", color = colors.statusError, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteConfirmationSession = null }) {
                            Text(text = "CANCEL", color = colors.onSurfaceVariant)
                        }
                    },
                    containerColor = colors.surfaceVariant
                )
            }

            if (showClearConfirm) {
                AlertDialog(
                    onDismissRequest = { showClearConfirm = false },
                    title = { Text(text = "Clear All Sessions", color = colors.onSurface) },
                    text = { Text(text = "Are you sure you want to delete all captured network sessions and requests? This action cannot be undone.", color = colors.onSurfaceVariant) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.clearAll()
                                showClearConfirm = false
                            }
                        ) {
                            Text(text = "DELETE ALL", color = colors.statusError, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearConfirm = false }) {
                            Text(text = "CANCEL", color = colors.onSurfaceVariant)
                        }
                    },
                    containerColor = colors.surfaceVariant
                )
            }
        }
    }
}
