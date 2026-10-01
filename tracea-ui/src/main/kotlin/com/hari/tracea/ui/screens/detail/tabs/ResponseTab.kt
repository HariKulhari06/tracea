package com.hari.tracea.ui.screens.detail.tabs

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.model.BodyData
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.util.DurationFormatter
import com.hari.tracea.core.util.SizeFormatter
import com.hari.tracea.ui.components.CodeBlock
import com.hari.tracea.ui.components.EmptyState
import com.hari.tracea.ui.components.HeadersSection
import com.hari.tracea.ui.components.JsonSyntaxHighlighter
import com.hari.tracea.ui.components.KeyValueCard
import com.hari.tracea.ui.components.SectionHeader
import com.hari.tracea.ui.components.StatusBadge
import com.hari.tracea.ui.screens.detail.BodyDisplayMode
import com.hari.tracea.ui.theme.LocalDebuggerColors

@Composable
fun ResponseTab(
    event: NetworkEvent,
    responseBodyMode: BodyDisplayMode,
    onResponseBodyModeChange: (BodyDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val statusCode = event.statusCode
        val networkError = event.error
        if (statusCode != null) {
            // Status Header Card
            SectionHeader(title = "Response Status")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(statusCode = statusCode, statusMessage = event.statusMessage, showMessage = true)

                Spacer(modifier = Modifier.weight(1f))

                event.timing.totalMs?.let { totalMs ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = DurationFormatter.format(totalMs),
                            color = colors.onSurface,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = SizeFormatter.format(event.responseSize),
                        color = colors.onSurface,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Information Card
            val infoItems = mutableListOf(
                "Status Code" to "$statusCode",
                "Response Size" to SizeFormatter.format(event.responseSize)
            )
            event.statusMessage?.let { msg ->
                if (msg.isNotBlank()) infoItems.add("Status Message" to msg)
            }
            event.responseContentType?.let { cType ->
                infoItems.add("Content-Type" to cType)
            }
            KeyValueCard(title = "Response Info", items = infoItems)

            // Response Headers
            HeadersSection(
                title = "Response Headers",
                headers = event.responseHeaders,
                onCopy = {
                    val text = event.responseHeaders.entries.joinToString("\n") { "${it.key}: ${it.value.joinToString(", ")}" }
                    clipboardManager.setText(AnnotatedString(text))
                }
            )

            // Cookies
            val setCookieValues = event.responseHeaders
                .filter { it.key.equals("Set-Cookie", ignoreCase = true) }
                .values.flatten()
            if (setCookieValues.isNotEmpty()) {
                SectionHeader(title = "Set-Cookie")
                val cookieItems = setCookieValues.mapIndexed { idx, cookie ->
                    "Cookie ${idx + 1}" to cookie
                }
                KeyValueCard(
                    title = "Cookies (${setCookieValues.size})",
                    items = cookieItems
                )
            }

            // Response Body
            event.responseBody?.let { body ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(title = "Response Body")

                    when (body) {
                        is BodyData.Text -> {
                            val isJson = event.responseContentType?.contains("json", ignoreCase = true) == true ||
                                    body.content.trim().startsWith("{") ||
                                    body.content.trim().startsWith("[")

                            if (isJson) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.surfaceVariant)
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (responseBodyMode == BodyDisplayMode.PRETTY) colors.surface else colors.surfaceVariant)
                                            .clickable { onResponseBodyModeChange(BodyDisplayMode.PRETTY) }
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Pretty",
                                            color = if (responseBodyMode == BodyDisplayMode.PRETTY) colors.onSurface else colors.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = if (responseBodyMode == BodyDisplayMode.PRETTY) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (responseBodyMode == BodyDisplayMode.RAW) colors.surface else colors.surfaceVariant)
                                            .clickable { onResponseBodyModeChange(BodyDisplayMode.RAW) }
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Raw",
                                            color = if (responseBodyMode == BodyDisplayMode.RAW) colors.onSurface else colors.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = if (responseBodyMode == BodyDisplayMode.RAW) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }

                                val formatted = if (responseBodyMode == BodyDisplayMode.PRETTY) {
                                    JsonSyntaxHighlighter.formatAndHighlight(body.content).text
                                } else {
                                    body.content
                                }
                                CodeBlock(content = formatted, onCopy = { clipboardManager.setText(AnnotatedString(body.content)) })
                            } else {
                                CodeBlock(content = body.content, onCopy = { clipboardManager.setText(AnnotatedString(body.content)) })
                            }
                        }
                        is BodyData.Binary -> {
                            KeyValueCard(
                                title = "Binary Body",
                                items = listOf("Size" to SizeFormatter.format(body.size))
                            )
                        }
                        is BodyData.Truncated -> {
                            KeyValueCard(
                                title = "Truncated Body",
                                items = listOf(
                                    "Captured Size" to SizeFormatter.format(body.capturedSize),
                                    "Actual Size" to SizeFormatter.format(body.actualSize)
                                )
                            )
                        }
                        is BodyData.FileReference -> {
                            KeyValueCard(
                                title = "File Reference",
                                items = listOf(
                                    "Path" to body.path,
                                    "Size" to SizeFormatter.format(body.size)
                                )
                            )
                        }
                    }
                }
            }
        } else if (networkError != null) {
            SectionHeader(title = "Error Details")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.statusError.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = colors.statusError,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = networkError.type.name,
                        color = colors.statusError,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                networkError.message?.let { msg ->
                    SelectionContainer {
                        Text(
                            text = msg,
                            color = colors.onSurface,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            EmptyState(
                title = "No Response",
                subtitle = "Response is pending or not available."
            )
        }
    }
}

