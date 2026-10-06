package com.hari.tracea.ui.screens.detail.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
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

@Composable
fun RequestTab(
    event: NetworkEvent,
    requestBodyMode: BodyDisplayMode,
    @Suppress("UNUSED_PARAMETER") onRequestBodyModeChange: (BodyDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
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
                    val isJson = event.requestContentType?.contains("json", ignoreCase = true) == true ||
                            body.content.trim().startsWith("{") ||
                            body.content.trim().startsWith("[")

                    val formatted = if (isJson && requestBodyMode == BodyDisplayMode.PRETTY) {
                        JsonSyntaxHighlighter.formatAndHighlight(body.content).text
                    } else {
                        body.content
                    }
                    CodeBlock(
                        content = formatted,
                        title = "${body.contentType.name.uppercase()} • ${SizeFormatter.format(body.size)}",
                        onCopy = { clipboardManager.setText(AnnotatedString(body.content)) }
                    )
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
