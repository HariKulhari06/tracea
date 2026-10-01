package com.hari.tracea.ui.screens.settings

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.config.DomainFilterConfig
import com.hari.tracea.ui.TraceaServiceLocator
import com.hari.tracea.ui.theme.LocalDebuggerColors

enum class DomainFilterTab(val label: String) {
    ALLOWED("Allowed"),
    IGNORED("Ignored")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainFilterSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tracea_settings", Context.MODE_PRIVATE) }

    // Read stored lists
    val initialAllowed = remember {
        val stored = prefs.getStringSet("allowed_domains", null)
        stored?.toList() ?: TraceaServiceLocator.config?.domainFilterConfig?.allowedDomains ?: emptyList()
    }
    val initialIgnored = remember {
        val stored = prefs.getStringSet("ignored_domains", null)
        stored?.toList() ?: TraceaServiceLocator.config?.domainFilterConfig?.ignoredDomains ?: emptyList()
    }

    var allowedDomains by remember { mutableStateOf(initialAllowed) }
    var ignoredDomains by remember { mutableStateOf(initialIgnored) }
    var selectedTab by remember { mutableStateOf(DomainFilterTab.ALLOWED) }
    var newDomainText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun persistChanges(newAllowed: List<String>, newIgnored: List<String>) {
        prefs.edit()
            .putStringSet("allowed_domains", newAllowed.toSet())
            .putStringSet("ignored_domains", newIgnored.toSet())
            .apply()

        val updatedConfig = DomainFilterConfig(allowedDomains = newAllowed, ignoredDomains = newIgnored)
        DomainFilterConfig.activeConfig = updatedConfig
        TraceaServiceLocator.config = TraceaServiceLocator.config?.copy(domainFilterConfig = updatedConfig)
    }

    fun addDomain(domain: String) {
        val cleaned = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").trimEnd('/')
        if (cleaned.isBlank()) {
            errorMessage = "Please enter a valid domain"
            return
        }

        if (selectedTab == DomainFilterTab.ALLOWED) {
            if (allowedDomains.contains(cleaned)) {
                errorMessage = "Domain is already in allowed list"
                return
            }
            val updated = allowedDomains + cleaned
            allowedDomains = updated
            persistChanges(updated, ignoredDomains)
        } else {
            if (ignoredDomains.contains(cleaned)) {
                errorMessage = "Domain is already in ignored list"
                return
            }
            val updated = ignoredDomains + cleaned
            ignoredDomains = updated
            persistChanges(allowedDomains, updated)
        }
        newDomainText = ""
        errorMessage = null
    }

    fun removeDomain(domain: String) {
        if (selectedTab == DomainFilterTab.ALLOWED) {
            val updated = allowedDomains.filter { it != domain }
            allowedDomains = updated
            persistChanges(updated, ignoredDomains)
        } else {
            val updated = ignoredDomains.filter { it != domain }
            ignoredDomains = updated
            persistChanges(allowedDomains, updated)
        }
    }

    fun clearAllCurrent() {
        if (selectedTab == DomainFilterTab.ALLOWED) {
            allowedDomains = emptyList()
            persistChanges(emptyList(), ignoredDomains)
        } else {
            ignoredDomains = emptyList()
            persistChanges(allowedDomains, emptyList())
        }
    }

    val commonIgnoredPresets = listOf(
        "*.firebaseio.com",
        "*.crashlytics.com",
        "*.sentry.io",
        "*.mixpanel.com",
        "*.google-analytics.com"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Domain Filtering",
                        color = colors.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Segmented Picker [ Allowed (N) | Ignored (N) ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DomainFilterTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val count = if (tab == DomainFilterTab.ALLOWED) allowedDomains.size else ignoredDomains.size
                    val activeBg = if (tab == DomainFilterTab.ALLOWED) colors.primary else Color(0xFFCE9178)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) activeBg else Color.Transparent)
                            .clickable {
                                selectedTab = tab
                                newDomainText = ""
                                errorMessage = null
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (tab == DomainFilterTab.ALLOWED) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else colors.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${tab.label} ($count)",
                                color = if (isSelected) Color.White else colors.onSurface,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Info Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = if (selectedTab == DomainFilterTab.ALLOWED) colors.primary else Color(0xFFCE9178),
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (selectedTab == DomainFilterTab.ALLOWED) "Targeted Capture" else "Suppressed Domains",
                            color = colors.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedTab == DomainFilterTab.ALLOWED)
                                "When configured, Tracea will ONLY intercept traffic to these domains. All other requests pass through untouched without recording."
                            else
                                "Tracea will NEVER capture or inspect requests matching these domains (e.g. noisy telemetry, crash reporters, analytics).",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Add Domain Card
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (selectedTab == DomainFilterTab.ALLOWED) "Add Allowed Domain" else "Add Ignored Domain",
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newDomainText,
                            onValueChange = {
                                newDomainText = it
                                errorMessage = null
                            },
                            placeholder = {
                                Text(
                                    text = "e.g. api.test.com or *.myserver.com",
                                    color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.outline,
                                focusedTextColor = colors.onSurface,
                                unfocusedTextColor = colors.onSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { addDomain(newDomainText) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == DomainFilterTab.ALLOWED) colors.primary else Color(0xFFCE9178)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    errorMessage?.let { error ->
                        Text(text = error, color = colors.status4xx, fontSize = 11.sp)
                    }

                    Text(
                        text = "💡 Tip: Use *.example.com to capture all subdomains.",
                        color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Presets (Ignored Tab Only)
            if (selectedTab == DomainFilterTab.IGNORED) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "COMMON PRESETS",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(commonIgnoredPresets) { preset ->
                            val isAdded = ignoredDomains.contains(preset)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAdded) colors.surfaceVariant else colors.surfaceVariant)
                                    .border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isAdded) { addDomain(preset) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!isAdded) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFFCE9178),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = preset,
                                    color = if (isAdded) colors.onSurfaceVariant else colors.onSurface,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Domains List Section
            val currentList = if (selectedTab == DomainFilterTab.ALLOWED) allowedDomains else ignoredDomains
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${if (selectedTab == DomainFilterTab.ALLOWED) "ALLOWED" else "IGNORED"} DOMAINS (${currentList.size})",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (currentList.isNotEmpty()) {
                        Text(
                            text = "Clear All",
                            color = colors.status4xx,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { clearAllCurrent() }
                                .padding(4.dp)
                        )
                    }
                }

                if (currentList.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedTab == DomainFilterTab.ALLOWED)
                                    "No domain restrictions set.\nAll network traffic will be captured."
                                else
                                    "No domains are ignored.\nAll traffic is eligible for capture.",
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }
                } else {
                    currentList.forEach { domain ->
                        val isWildcard = domain.startsWith("*.") || domain.startsWith(".")
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = domain,
                                        color = colors.onSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.surfaceVariant)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isWildcard) "Wildcard" else "Exact",
                                            color = colors.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { removeDomain(domain) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = colors.status4xx,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Matching Rules Guide
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "📖 Matching Rules Guide",
                        color = colors.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• api.example.com — Matches exact hostname only\n• *.example.com — Matches any subdomain\n• example.com — Matches root and all subdomains",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
