package com.hari.tracea.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hari.tracea.core.model.BodyData
import com.hari.tracea.ui.components.SummaryCardsRow
import com.hari.tracea.ui.screens.detail.tabs.OverviewTab
import com.hari.tracea.ui.screens.detail.tabs.RequestTab
import com.hari.tracea.ui.screens.detail.tabs.ResponseTab
import com.hari.tracea.ui.screens.detail.tabs.TimingTab
import com.hari.tracea.ui.theme.LocalDebuggerColors
import com.hari.tracea.ui.util.ShareUtility
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RequestDetailViewModel = viewModel()
) {
    val colors = LocalDebuggerColors.current
    val event by viewModel.event.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val responseBodyMode by viewModel.responseBodyMode.collectAsState()
    val requestBodyMode by viewModel.requestBodyMode.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showShareMenu by remember { mutableStateOf(false) }
    var showCopiedToast by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    LaunchedEffect(showCopiedToast) {
        if (showCopiedToast) {
            delay(1500)
            showCopiedToast = false
        }
    }

    val currentEvent = event

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Details",
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
                    actions = {
                        if (currentEvent != null) {
                            Box {
                                IconButton(onClick = { showShareMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = colors.onSurface
                                    )
                                }

                                DropdownMenu(
                                    expanded = showShareMenu,
                                    onDismissRequest = { showShareMenu = false },
                                    modifier = Modifier.background(colors.surfaceVariant)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Copy cURL", color = colors.onSurface) },
                                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = colors.primary) },
                                        onClick = {
                                            showShareMenu = false
                                            val curl = viewModel.getCurlCommand()
                                            clipboardManager.setText(AnnotatedString(curl))
                                            showCopiedToast = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share Text", color = colors.onSurface) },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = colors.primary) },
                                        onClick = {
                                            showShareMenu = false
                                            val report = ShareUtility.generateFullReport(currentEvent)
                                            ShareUtility.shareText(context, report)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share HAR", color = colors.onSurface) },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = colors.primary) },
                                        onClick = {
                                            showShareMenu = false
                                            val har = ShareUtility.generateHar(currentEvent)
                                            ShareUtility.shareFile(
                                                context = context,
                                                content = har,
                                                fileName = "transaction_${currentEvent.id}.har",
                                                title = "Share HAR"
                                            )
                                        }
                                    )
                                    if (currentEvent.responseBody != null) {
                                        DropdownMenuItem(
                                            text = { Text("Share Response Body", color = colors.onSurface) },
                                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = colors.primary) },
                                            onClick = {
                                                showShareMenu = false
                                                val body = currentEvent.responseBody
                                                if (body is BodyData.Text) {
                                                    val extension = if (currentEvent.responseContentType?.contains("json", true) == true) "json" else "txt"
                                                    ShareUtility.shareFile(
                                                        context = context,
                                                        content = body.content,
                                                        fileName = "response_body_${currentEvent.id}.$extension",
                                                        title = "Share Response Body"
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
                )
            },
            containerColor = colors.surface,
            modifier = modifier
        ) { paddingValues ->
            if (currentEvent == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading details...", color = colors.onSurfaceVariant)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Summary Cards Row (2x2 grid matching iOS)
                    SummaryCardsRow(
                        event = currentEvent,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 10.dp)
                    )

                    // iOS-style Segmented Tab Picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        DetailTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) colors.surface else Color.Transparent)
                                    .clickable { viewModel.selectTab(tab) }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.label,
                                    color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = colors.outline.copy(alpha = 0.4f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Tab Content
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            DetailTab.OVERVIEW -> OverviewTab(
                                event = currentEvent,
                                responseBodyMode = responseBodyMode,
                                onResponseBodyModeChange = { viewModel.setResponseBodyMode(it) }
                            )
                            DetailTab.REQUEST -> RequestTab(
                                event = currentEvent,
                                requestBodyMode = requestBodyMode,
                                onRequestBodyModeChange = { viewModel.setRequestBodyMode(it) }
                            )
                            DetailTab.RESPONSE -> ResponseTab(
                                event = currentEvent,
                                responseBodyMode = responseBodyMode,
                                onResponseBodyModeChange = { viewModel.setResponseBodyMode(it) }
                            )
                            DetailTab.TIMING -> TimingTab(
                                event = currentEvent
                            )
                        }
                    }
                }
            }
        }

        // Copied toast overlay matching iOS
        AnimatedVisibility(
            visible = showCopiedToast,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF34C759).copy(alpha = 0.95f))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Copied to clipboard!",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

