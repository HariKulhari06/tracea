package com.hari.tracea.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.hari.tracea.ui.theme.LocalDebuggerColors
import kotlinx.coroutines.delay

private const val MAX_DISPLAY_LENGTH = 20_000

@Composable
fun CodeBlock(
    content: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    showCopyButton: Boolean = true,
    onCopy: (() -> Unit)? = null
) {
    val colors = LocalDebuggerColors.current
    val clipboardManager = LocalClipboardManager.current
    var isWrapped by remember { mutableStateOf(false) }
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(1500)
            isCopied = false
        }
    }

    val isTruncated = content.length > MAX_DISPLAY_LENGTH
    val displayContent = remember(content, isTruncated) {
        if (isTruncated) {
            val prefix = content.take(MAX_DISPLAY_LENGTH)
            "$prefix\n\n... [Truncated for display: showing first $MAX_DISPLAY_LENGTH of ${content.length} characters. Full payload copied on copy] ..."
        } else {
            content
        }
    }

    val copyBgColor by animateColorAsState(
        targetValue = if (isCopied) Color(0xFF4CD964).copy(alpha = 0.15f) else colors.surfaceVariant,
        label = "codeCopyBg"
    )
    val copyTextColor by animateColorAsState(
        targetValue = if (isCopied) Color(0xFF4CD964) else colors.primary,
        label = "codeCopyText"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surface)
            .border(1.dp, colors.divider, RoundedCornerShape(10.dp))
    ) {
        // Control Header Bar — completely separated from the content below
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceVariant.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!title.isNullOrEmpty()) {
                Text(
                    text = title,
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Wrap / Horizontal toggle button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isWrapped) colors.primary.copy(alpha = 0.15f)
                        else colors.surfaceVariant.copy(alpha = 0.6f)
                    )
                    .clickable { isWrapped = !isWrapped }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isWrapped) Icons.Default.WrapText else Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = if (isWrapped) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isWrapped) "Wrap: ON" else "Wrap: OFF",
                    color = if (isWrapped) colors.primary else colors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Copy button
            if (showCopyButton) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(copyBgColor)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(content))
                            isCopied = true
                            onCopy?.invoke()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = copyTextColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (isCopied) "Copied!" else "Copy",
                        color = copyTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        HorizontalDivider(color = colors.divider, thickness = 1.dp)

        // Monospace content area
        val textToDisplay = if (displayContent.isEmpty()) "(empty)" else displayContent
        val textColor = if (displayContent.isEmpty()) colors.onSurfaceVariant else colors.onBackground

        SelectionContainer {
            if (isWrapped) {
                Text(
                    text = textToDisplay,
                    color = textColor,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = textToDisplay,
                        color = textColor,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp,
                        softWrap = false
                    )
                }
            }
        }
    }
}
