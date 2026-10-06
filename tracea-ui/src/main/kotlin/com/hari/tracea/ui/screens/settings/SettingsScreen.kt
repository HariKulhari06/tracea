package com.hari.tracea.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hari.tracea.ui.theme.LocalDebuggerColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onDomainFilterClick: () -> Unit = {},
    onNavigateToRedaction: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val scrollState = rememberScrollState()

    var showClearDialog by remember { mutableStateOf(false) }

    val enableDebugger by viewModel.enableDebugger.collectAsState()
    val floatingButton by viewModel.floatingButton.collectAsState()
    val captureRequests by viewModel.captureRequests.collectAsState()
    val captureWebSocket by viewModel.captureWebSocket.collectAsState()
    val showRedactedPlaceholder by viewModel.showRedactedPlaceholder.collectAsState()
    val logCurl by viewModel.logCurl.collectAsState()
    val showGetRequestBody by viewModel.showGetRequestBody.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = colors.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        containerColor = colors.surface,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // GENERAL
            SettingsSection(title = "GENERAL") {
                ToggleRow(
                    title = "Enable Debugger",
                    subtitle = "Enable or disable network capture",
                    checked = enableDebugger,
                    onCheckedChange = { viewModel.setEnableDebugger(it) }
                )
                ToggleRow(
                    title = "Floating Button",
                    subtitle = "Show floating button to open debugger",
                    checked = floatingButton,
                    onCheckedChange = { viewModel.setFloatingButton(it) }
                )
            }

            // CAPTURE & STORAGE
            SettingsSection(title = "CAPTURE & STORAGE") {
                ToggleRow(
                    title = "Capture Requests",
                    subtitle = "Automatically capture HTTP/HTTPS requests",
                    checked = captureRequests,
                    onCheckedChange = { viewModel.setCaptureRequests(it) }
                )
                ActionRow(
                    title = "Clear All Data",
                    subtitle = "Remove all captured data",
                    isDestructive = true,
                    onClick = { showClearDialog = true }
                )
                Text(
                    text = "Usage: ${viewModel.eventCount} events (${viewModel.storageUsage})",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
            }

            // REDACTION (PRIVACY)
            SettingsSection(title = "REDACTION (PRIVACY)") {
                NavigationRow(
                    title = "Redaction Settings",
                    onClick = onNavigateToRedaction
                )
                ToggleRow(
                    title = "Show Redacted Placeholder",
                    subtitle = "Replace sensitive values with [REDACTED]",
                    checked = showRedactedPlaceholder,
                    onCheckedChange = { viewModel.setShowRedactedPlaceholder(it) }
                )
            }

            // DOMAIN FILTERING
            val domainFilter = com.hari.tracea.ui.TraceaServiceLocator.config?.domainFilterConfig
            val domainSummary = when {
                domainFilter != null && domainFilter.allowedDomains.isNotEmpty() ->
                    "${domainFilter.allowedDomains.size} allowed"
                domainFilter != null && domainFilter.ignoredDomains.isNotEmpty() ->
                    "${domainFilter.ignoredDomains.size} ignored"
                else -> "All traffic"
            }
            SettingsSection(title = "DOMAIN FILTERING") {
                NavigationRow(
                    title = "Allowed & Ignored Domains",
                    value = domainSummary,
                    onClick = onDomainFilterClick
                )
            }

            // ABOUT TRACEA
            SettingsSection(title = "ABOUT TRACEA") {
                NavigationRow(
                    title = "SDK Version",
                    value = "1.6.0",
                    showChevron = false
                )
                NavigationRow(
                    title = "Platform",
                    value = "Android 8.0+ (API 26+)",
                    showChevron = false
                )
                NavigationRow(title = "GitHub Repository", value = "tracea")
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear Data") },
                text = { Text("Are you sure you want to clear all recorded network events? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                    }) {
                        Text("Clear", color = colors.statusError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel", color = colors.onSurface)
                    }
                },
                containerColor = colors.surface,
                titleContentColor = colors.onSurface,
                textContentColor = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = LocalDebuggerColors.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalDebuggerColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(title, color = colors.onSurface, fontSize = 15.sp)
                Text(subtitle, color = colors.onSurfaceVariant, fontSize = 12.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.onSurface,
                    checkedTrackColor = colors.primary,
                    uncheckedThumbColor = colors.onSurfaceVariant,
                    uncheckedTrackColor = colors.surfaceVariant
                )
            )
        }
        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
    }
}

@Composable
private fun NavigationRow(
    title: String,
    value: String? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    val colors = LocalDebuggerColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = showChevron, onClick = onClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = colors.onSurface, fontSize = 15.sp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                value?.let { Text(it, color = colors.onSurfaceVariant, fontSize = 13.sp) }
                if (showChevron) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
                }
            }
        }
        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val colors = LocalDebuggerColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = if (isDestructive) colors.errorDot else colors.onSurface, fontSize = 15.sp)
                Text(subtitle, color = colors.onSurfaceVariant, fontSize = 12.sp)
            }
            if (isDestructive) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.errorDot)
            }
        }
        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
    }
}
