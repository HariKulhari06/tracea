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
fun QueryParamsSection(
    queryParameters: Map<String, List<String>>,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val clipboardManager = LocalClipboardManager.current
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
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surface)
            .border(1.dp, colors.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
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
                text = "Query Parameters",
                color = colors.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${queryParameters.size}",
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

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(copyAllBg)
                    .clickable {
                        val allText = queryParameters.entries
                            .flatMap { (k, vals) -> vals.map { "$k=$it" } }
                            .joinToString("&")
                        clipboardManager.setText(AnnotatedString(allText))
                        isAllCopied = true
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
                    text = if (isAllCopied) "Copied All!" else "Copy All",
                    color = copyAllColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        HorizontalDivider(color = colors.outline.copy(alpha = 0.4f), thickness = 0.5.dp)

        // Query Param Rows
        val sortedKeys = queryParameters.keys.sorted()
        Column(modifier = Modifier.fillMaxWidth()) {
            sortedKeys.forEachIndexed { index, key ->
                val vals = queryParameters[key] ?: emptyList()
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
                            color = Color(0xFF4EC9B0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Icon(
                            imageVector = if (isRowCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy parameter",
                            tint = if (isRowCopied) Color(0xFF4CD964) else colors.onSurfaceVariant,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("$key=$valString"))
                                    copiedKey = key
                                }
                        )
                    }

                    SelectionContainer {
                        Text(
                            text = valString,
                            color = colors.onSurface,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }

                if (index < sortedKeys.size - 1) {
                    HorizontalDivider(
                        color = colors.outline.copy(alpha = 0.25f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }
        }
    }
}
