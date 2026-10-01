package com.hari.tracea.ui.screens.detail.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.model.BodyData
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.model.isMocked
import com.hari.tracea.core.util.SizeFormatter
import com.hari.tracea.ui.components.CodeBlock
import com.hari.tracea.ui.components.HeadersSection
import com.hari.tracea.ui.components.JsonSyntaxHighlighter
import com.hari.tracea.ui.components.KeyValueCard
import com.hari.tracea.ui.components.SectionHeader
import com.hari.tracea.ui.components.UrlCard
import com.hari.tracea.ui.screens.detail.BodyDisplayMode
import com.hari.tracea.ui.theme.LocalDebuggerColors

@Composable
fun OverviewTab(
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
        if (event.isMocked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceVariant, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎭",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = "Mocked Response",
                        color = colors.sectionHeader,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "This transaction was simulated locally via a Tracea Mock Rule.",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Request URL Card
        SectionHeader(title = "Request URL")
        UrlCard(method = event.method, url = event.url)

        // General Metadata Card
        val generalItems = mutableListOf(
            "Method" to event.method.name.uppercase(),
            "Host" to event.host,
            "Scheme" to event.scheme
        )
        event.port?.let { generalItems.add("Port" to it.toString()) }
        event.statusCode?.let { code ->
            val msg = event.statusMessage?.let { " $it" } ?: ""
            generalItems.add("Status" to "$code$msg")
        }
        KeyValueCard(title = "Overview Info", items = generalItems)

        // Request Headers
        HeadersSection(
            title = "Request Headers",
            headers = event.requestHeaders,
            onCopy = {
                val text = event.requestHeaders.entries.joinToString("\n") { "${it.key}: ${it.value.joinToString(", ")}" }
                clipboardManager.setText(AnnotatedString(text))
            }
        )

        // Response Headers
        HeadersSection(
            title = "Response Headers",
            headers = event.responseHeaders,
            onCopy = {
                val text = event.responseHeaders.entries.joinToString("\n") { "${it.key}: ${it.value.joinToString(", ")}" }
                clipboardManager.setText(AnnotatedString(text))
            }
        )

        // Response Body
        SectionHeader(title = "Response Body")
        val resBody = event.responseBody
        if (resBody != null) {
            when (resBody) {
                is BodyData.Text -> {
                    val isJson = event.responseContentType?.contains("json", ignoreCase = true) == true ||
                            resBody.content.trim().startsWith("{") ||
                            resBody.content.trim().startsWith("[")

                    if (isJson) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surfaceVariant)
                                .padding(2.dp)
                        ) {
                            BodyModeToggle(
                                label = "Pretty",
                                isSelected = responseBodyMode == BodyDisplayMode.PRETTY,
                                onClick = { onResponseBodyModeChange(BodyDisplayMode.PRETTY) }
                            )
                            BodyModeToggle(
                                label = "Raw",
                                isSelected = responseBodyMode == BodyDisplayMode.RAW,
                                onClick = { onResponseBodyModeChange(BodyDisplayMode.RAW) }
                            )
                        }

                        val bodyText = if (responseBodyMode == BodyDisplayMode.PRETTY) {
                            JsonSyntaxHighlighter.formatAndHighlight(resBody.content).text
                        } else {
                            resBody.content
                        }
                        CodeBlock(
                            content = bodyText,
                            onCopy = { clipboardManager.setText(AnnotatedString(resBody.content)) }
                        )
                    } else {
                        CodeBlock(
                            content = resBody.content,
                            onCopy = { clipboardManager.setText(AnnotatedString(resBody.content)) }
                        )
                    }
                }
                is BodyData.FileReference -> {
                    KeyValueCard(
                        title = "File Reference",
                        items = listOf(
                            "Path" to resBody.path,
                            "Size" to SizeFormatter.format(resBody.size)
                        )
                    )
                }
                is BodyData.Truncated -> {
                    KeyValueCard(
                        title = "Truncated Payload",
                        items = listOf(
                            "Captured Size" to SizeFormatter.format(resBody.capturedSize),
                            "Actual Size" to SizeFormatter.format(resBody.actualSize)
                        )
                    )
                }
                is BodyData.Binary -> {
                    KeyValueCard(
                        title = "Binary Payload",
                        items = listOf(
                            "Size" to SizeFormatter.format(resBody.size)
                        )
                    )
                }
            }
        } else {
            Text(
                text = "No response body recorded",
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun BodyModeToggle(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalDebuggerColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) colors.surface else colors.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

