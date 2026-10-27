package com.hari.tracea.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.ui.theme.LocalDebuggerColors

/**
 * Collapsible sub-header for grouping network events by domain within a session
 * (matches iOS v1.5.0 DomainHeader).
 */
@Composable
fun DomainHeader(
    domain: String,
    requestCount: Int,
    errorCount: Int = 0,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 90f else 0f, label = "domainChevron")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant.copy(alpha = 0.25f))
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = if (isExpanded) "Collapse Domain" else "Expand Domain",
            tint = colors.onSurfaceVariant,
            modifier = Modifier
                .size(12.dp)
                .rotate(rotation)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.Public,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(13.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = domain,
            color = colors.onBackground,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        if (errorCount > 0) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.statusError.copy(alpha = 0.18f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = colors.statusError,
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = "$errorCount",
                    color = colors.statusError,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceVariant)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$requestCount",
                color = colors.onSurface,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
