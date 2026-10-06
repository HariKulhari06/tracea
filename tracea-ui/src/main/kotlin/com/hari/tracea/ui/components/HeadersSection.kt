package com.hari.tracea.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
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

@Composable
fun HeadersSection(
    title: String,
    headers: Map<String, List<String>>,
    modifier: Modifier = Modifier,
    onCopy: (() -> Unit)? = null
) {
    if (headers.isEmpty()) return

    val colors = LocalDebuggerColors.current
    val clipboardManager = LocalClipboardManager.current
    
    var expanded by remember { mutableStateOf(false) }
    var isAllCopied by remember { mutableStateOf(false) }
    var copiedKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isAllCopied) {
        if (isAllCopied) {
            delay(1500)
            isAllCopied = false
        }
    }

    LaunchedEffect(copiedKey) {
        if (copiedKey != null) {
            delay(1200)
            copiedKey = null
        }
    }

    val copyAllBg by animateColorAsState(
        targetValue = if (isAllCopied) Color(0xFF4CD964).copy(alpha = 0.15f) else colors.surfaceVariant,
        label = "copyAllBg"
    )
    val copyAllColor by animateColorAsState(
        targetValue = if (isAllCopied) Color(0xFF4CD964) else colors.primary,
        label = "copyAllColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surface)
            .border(1.dp, colors.divider, RoundedCornerShape(10.dp))
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = colors.onBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${headers.size}",
                color = colors.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (headers.size > 5) {
                Text(
                    text = if (expanded) "Show Less" else "View All",
                    color = colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(copyAllBg)
                    .clickable {
                        val allText = headers.entries.joinToString("\n") { (k, vals) -> 
                            "$k: ${vals.joinToString(", ")}"
                        }
                        clipboardManager.setText(AnnotatedString(allText))
                        isAllCopied = true
                        onCopy?.invoke()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isAllCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = copyAllColor,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isAllCopied) "Copied!" else "Copy All",
                    color = copyAllColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        HorizontalDivider(color = colors.divider, thickness = 1.dp)

        val displayHeaders = if (expanded || headers.size <= 5) {
            headers
        } else {
            headers.entries.take(5).associate { it.key to it.value }
        }

        // Header Rows
        val entriesList = displayHeaders.entries.toList()
        Column(modifier = Modifier.fillMaxWidth()) {
            entriesList.forEachIndexed { index, entry ->
                val key = entry.key
                val vals = entry.value
                val valString = vals.joinToString(", ")
                val isRowCopied = copiedKey == key

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key,
                            color = colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )

                        Icon(
                            imageVector = if (isRowCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy header",
                            tint = if (isRowCopied) Color(0xFF4CD964) else colors.onSurfaceVariant,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("$key: $valString"))
                                    copiedKey = key
                                }
                        )
                    }

                    SelectionContainer {
                        Text(
                            text = valString,
                            color = colors.onBackground,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }

                if (index < entriesList.size - 1) {
                    HorizontalDivider(
                        color = colors.divider.copy(alpha = 0.6f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }
        }
    }
}
