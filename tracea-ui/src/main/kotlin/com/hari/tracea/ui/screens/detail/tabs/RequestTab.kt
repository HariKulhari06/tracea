package com.hari.tracea.ui.screens.detail.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.model.BodyData
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.util.SizeFormatter
import com.hari.tracea.ui.components.CodeBlock
import com.hari.tracea.ui.components.HeadersSection
import com.hari.tracea.ui.components.JsonSyntaxHighlighter
import com.hari.tracea.ui.components.KeyValueCard
import com.hari.tracea.ui.components.QueryParamsSection
import com.hari.tracea.ui.components.SectionHeader
import com.hari.tracea.ui.components.UrlCard
import com.hari.tracea.ui.screens.detail.BodyDisplayMode
import com.hari.tracea.ui.theme.LocalDebuggerColors

@Composable
fun RequestTab(
    event: NetworkEvent,
    requestBodyMode: BodyDisplayMode,
    onRequestBodyModeChange: (BodyDisplayMode) -> Unit,
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
        // Request URL Card
        SectionHeader(title = "Request URL")
        UrlCard(method = event.method, url = event.url)

        // Request Information Card
        val infoItems = mutableListOf(
            "Method" to event.method.name.uppercase(),
            "Host" to event.host,
            "Scheme" to event.scheme
        )
        event.port?.let { infoItems.add("Port" to it.toString()) }
        event.requestContentType?.let { infoItems.add("Content-Type" to it) }
        infoItems.add("Request Size" to SizeFormatter.format(event.requestSize))

        KeyValueCard(title = "Request Info", items = infoItems)

        // Query Parameters
        if (event.queryParameters.isNotEmpty()) {
            SectionHeader(title = "Query Parameters")
            QueryParamsSection(queryParameters = event.queryParameters)
        }

        // Request Headers
        HeadersSection(
            title = "Request Headers",
            headers = event.requestHeaders,
            onCopy = {
                val text = event.requestHeaders.entries.joinToString("\n") { "${it.key}: ${it.value.joinToString(", ")}" }
                clipboardManager.setText(AnnotatedString(text))
            }
        )

        // Request Body
        event.requestBody?.let { body ->
            SectionHeader(title = "Request Body")
            when (body) {
                is BodyData.Text -> {
                    val isJson = JsonSyntaxHighlighter.isValidJson(body.content)

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
                                    .background(if (requestBodyMode == BodyDisplayMode.PRETTY) colors.surface else colors.surfaceVariant)
                                    .clickable { onRequestBodyModeChange(BodyDisplayMode.PRETTY) }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Pretty",
                                    color = if (requestBodyMode == BodyDisplayMode.PRETTY) colors.onSurface else colors.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = if (requestBodyMode == BodyDisplayMode.PRETTY) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (requestBodyMode == BodyDisplayMode.RAW) colors.surface else colors.surfaceVariant)
                                    .clickable { onRequestBodyModeChange(BodyDisplayMode.RAW) }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Raw",
                                    color = if (requestBodyMode == BodyDisplayMode.RAW) colors.onSurface else colors.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = if (requestBodyMode == BodyDisplayMode.RAW) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        val (formattedText, annotated) = if (requestBodyMode == BodyDisplayMode.PRETTY) {
                            val highlighted = JsonSyntaxHighlighter.formatAndHighlight(body.content)
                            highlighted.text to highlighted
                        } else {
                            body.content to null
                        }
                        CodeBlock(
                            content = formattedText,
                            annotatedContent = annotated,
                            title = if (requestBodyMode == BodyDisplayMode.PRETTY) "JSON • ${SizeFormatter.format(body.size)}" else "JSON (RAW) • ${SizeFormatter.format(body.size)}",
                            onCopy = { clipboardManager.setText(AnnotatedString(body.content)) }
                        )
                    } else {
                        val displayTitle = if (event.requestContentType?.contains("json", ignoreCase = true) == true) {
                            "TEXT • ${SizeFormatter.format(body.size)}"
                        } else {
                            "${body.contentType.name.uppercase()} • ${SizeFormatter.format(body.size)}"
                        }
                        CodeBlock(
                            content = body.content,
                            title = displayTitle,
                            onCopy = { clipboardManager.setText(AnnotatedString(body.content)) }
                        )
                    }
                }
                is BodyData.Binary -> {
                    KeyValueCard(
                        title = "Binary Body",
                        items = listOf(
                            "Type" to body.contentType.name,
                            "Size" to SizeFormatter.format(body.size)
                        )
                    )
                }
                is BodyData.Truncated -> {
                    KeyValueCard(
                        title = "Truncated Body",
                        items = listOf(
                            "Captured Size" to SizeFormatter.format(body.capturedSize),
                            "Actual Size" to SizeFormatter.format(body.actualSize),
                            "Content-Type" to body.contentType.name
                        )
                    )
                }
                is BodyData.FileReference -> {
                    KeyValueCard(
                        title = "File Reference",
                        items = listOf(
                            "Path" to body.path,
                            "Type" to body.contentType.name,
                            "Size" to SizeFormatter.format(body.size)
                        )
                    )
                }
            }
        }
    }
}
