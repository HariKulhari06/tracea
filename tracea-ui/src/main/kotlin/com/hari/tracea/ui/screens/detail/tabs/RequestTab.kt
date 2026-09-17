package com.hari.tracea.ui.screens.detail.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.model.BodyData
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.ui.components.CodeBlock
import com.hari.tracea.ui.components.CookieParser
import com.hari.tracea.ui.components.CookiesSection
import com.hari.tracea.ui.components.HeadersSection
import com.hari.tracea.ui.components.JsonSyntaxHighlighter
import com.hari.tracea.ui.components.SectionHeader
import com.hari.tracea.ui.screens.detail.BodyDisplayMode
import com.hari.tracea.ui.theme.LocalDebuggerColors

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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // URL Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "URL")
            CodeBlock(
                content = event.url, 
                onCopy = { clipboardManager.setText(AnnotatedString(event.url)) }
            )
        }

        // Summary Info
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(title = "Information")
            
            InfoRow(label = "Method", value = event.method.name)
            InfoRow(label = "Scheme", value = event.scheme.uppercase())
            event.protocol?.let { InfoRow(label = "Protocol", value = it) }
            InfoRow(label = "Host", value = event.host)
            event.port?.let { InfoRow(label = "Port", value = it.toString()) }
        }

        // Query Parameters
        if (event.queryParameters.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Query Parameters")
                val formattedParams = event.queryParameters.entries.joinToString("\n") { (key, values) ->
                    "$key: ${values.joinToString(", ")}"
                }
                CodeBlock(
                    content = formattedParams,
                    onCopy = { clipboardManager.setText(AnnotatedString(formattedParams)) }
                )
            }
        }

        // Headers
        HeadersSection(
            title = "Request Headers",
            headers = event.requestHeaders,
            onCopy = {
                val text = event.requestHeaders.entries.joinToString("\n") { "${it.key}: ${it.value.joinToString(", ")}" }
                clipboardManager.setText(AnnotatedString(text))
            }
        )

        // Request Cookies
        val cookieValues = event.requestHeaders
            .filter { it.key.equals("Cookie", ignoreCase = true) }
            .values.flatten()
        if (cookieValues.isNotEmpty()) {
            val parsedCookies = CookieParser.parseRequestCookies(cookieValues)
            CookiesSection(
                title = "Request Cookies",
                cookies = parsedCookies,
                onCopy = {
                    val text = cookieValues.joinToString("; ")
                    clipboardManager.setText(AnnotatedString(text))
                }
            )
        }

        // Request Body
        event.requestBody?.let { body ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "Request Body")

                when (body) {
                    is BodyData.Text -> {
                        val formatted = if (requestBodyMode == BodyDisplayMode.PRETTY) {
                            JsonSyntaxHighlighter.formatAndHighlight(body.content).text
                        } else {
                            body.content
                        }
                        CodeBlock(
                            content = formatted, 
                            onCopy = { clipboardManager.setText(AnnotatedString(body.content)) }
                        )
                    }
                    is BodyData.Binary -> CodeBlock(content = "Binary body (${body.size} bytes)")
                    is BodyData.Truncated -> CodeBlock(content = "Truncated body (${body.capturedSize}/${body.actualSize} bytes)")
                    is BodyData.FileReference -> CodeBlock(content = "Stored in file: ${body.path}")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = LocalDebuggerColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            color = colors.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            color = colors.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}
