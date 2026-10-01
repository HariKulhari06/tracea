package com.hari.tracea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.ui.theme.LocalDebuggerColors

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow

/**
 * Collapsible section header for grouping requests by debugging session.
 * Displays inline share (export HAR) and delete buttons.
 */
@Composable
fun SessionHeader(
    name: String,
    requestCount: Int,
    isCollapsed: Boolean,
    onToggle: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant.copy(alpha = 0.4f))
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = if (isCollapsed) "Expand" else "Collapse",
            tint = colors.onSurfaceVariant,
            modifier = Modifier
                .size(16.dp)
                .rotate(if (isCollapsed) 0f else 90f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            color = colors.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$requestCount",
            color = colors.onSurface,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .background(colors.surfaceVariant, CircleShape)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        
        IconButton(
            onClick = onShareClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Export HAR",
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Session",
                tint = colors.statusError,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
