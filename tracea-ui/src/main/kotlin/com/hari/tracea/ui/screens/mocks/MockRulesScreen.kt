package com.hari.tracea.ui.screens.mocks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hari.tracea.core.model.HttpMethod
import com.hari.tracea.core.model.MockRule
import com.hari.tracea.core.util.SizeFormatter
import com.hari.tracea.ui.components.EmptyState
import com.hari.tracea.ui.components.MethodBadge
import com.hari.tracea.ui.components.SectionHeader
import com.hari.tracea.ui.components.StatusBadge
import com.hari.tracea.ui.theme.LocalDebuggerColors
import java.util.UUID
import kotlinx.coroutines.launch

/**
 * Screen displaying the Mock Rules list and rule creation/editing tools, matching iOS TraceaUI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockRulesScreen(
    modifier: Modifier = Modifier,
    viewModel: MockRulesViewModel = viewModel()
) {
    val colors = LocalDebuggerColors.current
    val rules by viewModel.rules.collectAsState()
    val capturedPaths by viewModel.capturedPaths.collectAsState()
    val globalMocksEnabled by viewModel.mockingEnabled.collectAsState()

    var showEditorRule by remember { mutableStateOf<MockRule?>(null) }
    var isNewRule by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(colors.background)) {
                // Top Navigation Header: Mocks + (+) Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .height(52.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mocks",
                        color = colors.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            isNewRule = true
                            showEditorRule = MockRule(
                                id = UUID.randomUUID().toString(),
                                pathPattern = "",
                                method = HttpMethod.GET,
                                statusCode = 200,
                                responseBody = "{\n  \"status\": \"success\"\n}",
                                contentType = "application/json",
                                delayMs = 0L,
                                enabled = true
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Mock Rule",
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Global Mocking Toggle Bar — matches iOS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (globalMocksEnabled) Color(0xFF4EC9B0) else colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Network Mocking",
                            color = colors.onBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (globalMocksEnabled) "Active — matching requests will be intercepted" else "Disabled — all requests bypass mocks",
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = globalMocksEnabled,
                        onCheckedChange = { viewModel.setMockingEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primary,
                            uncheckedThumbColor = colors.onSurfaceVariant,
                            uncheckedTrackColor = colors.surfaceVariant
                        ),
                        modifier = Modifier.scale(0.85f)
                    )
                }

                HorizontalDivider(color = colors.divider, thickness = 1.dp)

                // Warning Banner when globally disabled — matches iOS terracotta banner
                if (!globalMocksEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFCE9178).copy(alpha = 0.12f))
                            .border(
                                BorderStroke(1.dp, Color(0xFFCE9178).copy(alpha = 0.25f))
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFCE9178),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Mocking is globally disabled. Toggle on above to activate rules.",
                            color = colors.onBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    isNewRule = true
                    showEditorRule = MockRule(
                        id = UUID.randomUUID().toString(),
                        pathPattern = "",
                        method = HttpMethod.GET,
                        statusCode = 200,
                        responseBody = "{\n  \"status\": \"success\"\n}",
                        contentType = "application/json",
                        delayMs = 0L,
                        enabled = true
                    )
                },
                containerColor = colors.primary,
                contentColor = colors.background,
                shape = CircleShape,
                modifier = Modifier.size(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Rule",
                    modifier = Modifier.size(24.dp)
                )
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
            if (rules.isEmpty()) {
                EmptyState(
                    title = "No Mock Rules",
                    subtitle = "Add a rule to intercept and mock matching network requests with custom responses and delays."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(rules, key = { it.id }) { rule ->
                        MockRuleCard(
                            rule = rule,
                            isEnabled = globalMocksEnabled,
                            onEdit = {
                                isNewRule = false
                                showEditorRule = rule
                            },
                            onDelete = { viewModel.removeRule(rule.id) },
                            onToggle = {
                                viewModel.updateRule(rule.copy(enabled = !rule.enabled))
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Mock Rule Editor
    showEditorRule?.let { rule ->
        MockRuleEditorBottomSheet(
            rule = rule,
            isNew = isNewRule,
            capturedPaths = capturedPaths,
            onImportPayload = { path -> viewModel.getResponseBodyForPath(path) },
            onDismiss = { showEditorRule = null },
            onSave = { updatedRule ->
                if (isNewRule) {
                    viewModel.addRule(updatedRule)
                } else {
                    viewModel.updateRule(updatedRule)
                }
                showEditorRule = null
            }
        )
    }
}

/**
 * Single card for a mock rule, matching iOS MockRuleCard.
 */
@Composable
private fun MockRuleCard(
    rule: MockRule,
    isEnabled: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val colors = LocalDebuggerColors.current
    val isCardActive = isEnabled && rule.enabled

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isCardActive) 1f else 0.55f)
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(
            1.dp,
            if (isCardActive) colors.divider else colors.divider.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Method Badge + Status Badge + Delay Pill + Spacer + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MethodBadge(method = rule.method)
                StatusBadge(statusCode = rule.statusCode)

                if (rule.delayMs > 0L) {
                    Row(
                        modifier = Modifier
                            .background(colors.surfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "${rule.delayMs}ms",
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = rule.enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.primary,
                        uncheckedThumbColor = colors.onSurfaceVariant,
                        uncheckedTrackColor = colors.surfaceVariant
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }

            // Path Pattern (Monospace, 2 lines max)
            Text(
                text = rule.pathPattern,
                color = colors.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Footer Row: Content-Type • Payload Size | Edit Button, Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (rule.contentType.isNotEmpty()) {
                    Text(
                        text = rule.contentType,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (rule.responseBody.isNotEmpty()) {
                    Text(
                        text = "•",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${rule.responseBody.toByteArray().size} B payload",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Edit Button
                Row(
                    modifier = Modifier
                        .clickable { onEdit() }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = colors.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Edit",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.statusError.copy(alpha = 0.85f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full-featured Add / Edit Mock Rule Editor matching iOS MockRuleEditor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MockRuleEditorBottomSheet(
    rule: MockRule,
    isNew: Boolean,
    capturedPaths: List<String>,
    onImportPayload: suspend (String) -> String?,
    onDismiss: () -> Unit,
    onSave: (MockRule) -> Unit
) {
    val colors = LocalDebuggerColors.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pathPattern by remember { mutableStateOf(rule.pathPattern) }
    var selectedMethod by remember { mutableStateOf(rule.method) }
    var statusCode by remember { mutableIntStateOf(rule.statusCode) }
    var contentType by remember { mutableStateOf(rule.contentType.ifEmpty { "application/json" }) }
    var delayMs by remember { mutableIntStateOf(rule.delayMs.toInt()) }
    var responseBody by remember { mutableStateOf(rule.responseBody) }
    var enabled by remember { mutableStateOf(rule.enabled) }

    var isImporting by remember { mutableStateOf(false) }
    var formatFeedback by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val availableMethods = listOf(
        HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT,
        HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.OPTIONS
    )

    val statusPresets = listOf(
        200 to "200 OK",
        201 to "201 Created",
        204 to "204 No Content",
        400 to "400 Bad Request",
        401 to "401 Unauthorized",
        403 to "403 Forbidden",
        404 to "404 Not Found",
        500 to "500 Error"
    )

    val contentTypePresets = listOf(
        "application/json",
        "text/plain",
        "text/html",
        "application/xml"
    )

    val delayPresets = listOf(
        0 to "0ms (Instant)",
        200 to "200ms",
        500 to "500ms",
        1500 to "1.5s (3G)",
        3000 to "3s (Slow)"
    )

    val isSaveDisabled = pathPattern.trim().isEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Bar: Cancel | Title | Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = colors.onSurface, fontSize = 15.sp)
                }

                Text(
                    text = if (isNew) "Add Mock Rule" else "Edit Mock Rule",
                    color = colors.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        if (!isSaveDisabled) {
                            onSave(
                                rule.copy(
                                    pathPattern = pathPattern.trim(),
                                    method = selectedMethod,
                                    statusCode = statusCode,
                                    contentType = contentType.trim(),
                                    delayMs = delayMs.toLong(),
                                    responseBody = responseBody,
                                    enabled = enabled
                                )
                            )
                        }
                    },
                    enabled = !isSaveDisabled
                ) {
                    Text(
                        text = "Save",
                        color = if (isSaveDisabled) colors.onSurfaceVariant else colors.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Section 1: Request Match
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Request Match")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.divider)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Method Selector
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "HTTP Method",
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableMethods.forEach { m ->
                                    val isSelected = selectedMethod == m
                                    val mColor = colors.methodColor(m)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) mColor else colors.surfaceVariant
                                            )
                                            .border(
                                                BorderStroke(
                                                    1.dp,
                                                    if (isSelected) Color.Transparent else colors.divider
                                                ),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedMethod = m }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = m.name,
                                            color = if (isSelected) colors.background else mColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = colors.divider, thickness = 1.dp)

                        // Path Pattern Input
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Path Pattern",
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surfaceVariant, RoundedCornerShape(8.dp))
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (pathPattern.isEmpty()) colors.divider else colors.primary.copy(alpha = 0.4f)
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BasicTextField(
                                    value = pathPattern,
                                    onValueChange = { pathPattern = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = colors.onBackground,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    cursorBrush = SolidColor(colors.primary),
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { innerTextField ->
                                        if (pathPattern.isEmpty()) {
                                            Text(
                                                text = "/api/v1/resource or pattern",
                                                color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                                                fontSize = 13.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        innerTextField()
                                    }
                                )

                                if (pathPattern.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = colors.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { pathPattern = "" }
                                    )
                                }

                                if (capturedPaths.isNotEmpty()) {
                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .clickable { dropdownExpanded = true }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Recent",
                                                color = colors.primary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = dropdownExpanded,
                                            onDismissRequest = { dropdownExpanded = false },
                                            modifier = Modifier.background(colors.surface)
                                        ) {
                                            capturedPaths.take(15).forEach { path ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = path,
                                                            color = colors.onBackground,
                                                            fontSize = 12.sp,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    },
                                                    onClick = {
                                                        pathPattern = path
                                                        dropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Intercepts any network call where the URL path contains or matches this pattern.",
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Section 2: Response Status & Format
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Response Status & Format")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.divider)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Status Code Header & Live Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status Code",
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            StatusBadge(statusCode = statusCode)
                        }

                        // Status Presets (Horizontal Scrollable)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statusPresets.forEach { (code, label) ->
                                val isSelected = statusCode == code
                                val sColor = colors.statusColor(code)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) sColor else colors.surfaceVariant)
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (isSelected) Color.Transparent else colors.divider
                                            ),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { statusCode = code }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) colors.background else sColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Fine-tune Code Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fine-tune Code:",
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Row(
                                modifier = Modifier
                                    .background(colors.surfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "−",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { if (statusCode > 100) statusCode -= 1 }
                                        .padding(horizontal = 4.dp)
                                )

                                Text(
                                    text = "$statusCode",
                                    color = colors.statusColor(statusCode),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                Text(
                                    text = "+",
                                    color = colors.primary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { if (statusCode < 599) statusCode += 1 }
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = colors.divider, thickness = 1.dp)

                        // Content-Type Section
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Content-Type",
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                contentTypePresets.forEach { cType ->
                                    val isSelected = contentType == cType
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) colors.primary.copy(alpha = 0.18f) else colors.surfaceVariant
                                            )
                                            .border(
                                                BorderStroke(
                                                    1.dp,
                                                    if (isSelected) colors.primary.copy(alpha = 0.5f) else colors.divider
                                                ),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { contentType = cType }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cType,
                                            color = if (isSelected) colors.primary else colors.onSurface,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surfaceVariant, RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, colors.divider), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = contentType,
                                    onValueChange = { contentType = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = colors.onBackground,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    cursorBrush = SolidColor(colors.primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Response Payload Editor
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Response Payload")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.divider)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Toolbar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (contentType.contains("json")) "JSON" else "PAYLOAD",
                                color = colors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )

                            Text(
                                text = SizeFormatter.format(responseBody.toByteArray().size.toLong()),
                                color = colors.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Format JSON Button
                            Row(
                                modifier = Modifier
                                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        try {
                                            val trimmed = responseBody.trim()
                                            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                                                val parsed = kotlinx.serialization.json.Json.parseToJsonElement(trimmed)
                                                responseBody = kotlinx.serialization.json.Json { prettyPrint = true }.encodeToString(
                                                    kotlinx.serialization.json.JsonElement.serializer(),
                                                    parsed
                                                )
                                                formatFeedback = "Formatted JSON successfully"
                                            } else {
                                                formatFeedback = "Invalid JSON syntax. Unable to format."
                                            }
                                        } catch (e: Exception) {
                                            formatFeedback = "Invalid JSON syntax. Unable to format."
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Format",
                                    tint = colors.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Format",
                                    color = colors.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Import Latest Button
                            Row(
                                modifier = Modifier
                                    .background(
                                        Color(0xFF4EC9B0).copy(alpha = if (pathPattern.isBlank() || isImporting) 0.05f else 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable(enabled = pathPattern.isNotBlank() && !isImporting) {
                                        isImporting = true
                                        formatFeedback = null
                                        coroutineScope.launch {
                                            val latest = onImportPayload(pathPattern.trim())
                                            if (latest != null) {
                                                responseBody = latest
                                                formatFeedback = "Imported latest payload (${SizeFormatter.format(latest.toByteArray().size.toLong())})"
                                            } else {
                                                formatFeedback = "No recorded network events match this path pattern."
                                            }
                                            isImporting = false
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Import Latest",
                                    tint = if (pathPattern.isBlank() || isImporting) colors.onSurfaceVariant else Color(0xFF4EC9B0),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Import Latest",
                                    color = if (pathPattern.isBlank() || isImporting) colors.onSurfaceVariant else Color(0xFF4EC9B0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (responseBody.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            responseBody = ""
                                            formatFeedback = null
                                        }
                                )
                            }
                        }

                        HorizontalDivider(color = colors.divider, thickness = 1.dp)

                        // Format Feedback Banner
                        formatFeedback?.let { feedback ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surfaceVariant.copy(alpha = 0.8f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (feedback.contains("success") || feedback.contains("Imported")) Icons.Default.AutoAwesome else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (feedback.contains("success") || feedback.contains("Imported")) Color(0xFF4EC9B0) else Color(0xFFCE9178),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = feedback,
                                    color = colors.onBackground,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Payload Text Editor
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(12.dp)
                        ) {
                            if (responseBody.isEmpty()) {
                                Text(
                                    text = "{\n  \"status\": \"success\",\n  \"message\": \"Mock response payload\"\n}",
                                    color = colors.onSurfaceVariant.copy(alpha = 0.35f),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            BasicTextField(
                                value = responseBody,
                                onValueChange = { responseBody = it },
                                textStyle = TextStyle(
                                    color = colors.onBackground,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                cursorBrush = SolidColor(colors.primary),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Section 4: Network Simulation
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Network Simulation")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.divider)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (delayMs == 0) Color(0xFF4EC9B0) else Color(0xFFCE9178),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Response Latency",
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "$delayMs ms",
                                color = colors.onBackground,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Delay Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            delayPresets.forEach { (ms, label) ->
                                val isSelected = delayMs == ms
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) colors.primary.copy(alpha = 0.18f) else colors.surfaceVariant
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (isSelected) colors.primary.copy(alpha = 0.5f) else colors.divider
                                            ),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { delayMs = ms }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) colors.primary else colors.onSurface,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Delay Fine-tune Stepper
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surfaceVariant, RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, colors.divider), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fine-tune Delay:",
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "−100ms",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { if (delayMs >= 100) delayMs -= 100 }
                                        .padding(horizontal = 4.dp)
                                )

                                Text(
                                    text = "$delayMs ms",
                                    color = colors.primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                Text(
                                    text = "+100ms",
                                    color = colors.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { delayMs += 100 }
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 5: Rule Active Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.divider)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Rule Active",
                            color = colors.onBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (enabled) "This mock rule will be applied when mocking is enabled" else "Rule is temporarily paused",
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primary,
                            uncheckedThumbColor = colors.onSurfaceVariant,
                            uncheckedTrackColor = colors.surfaceVariant
                        ),
                        modifier = Modifier.scale(0.85f)
                    )
                }
            }
        }
    }
}
