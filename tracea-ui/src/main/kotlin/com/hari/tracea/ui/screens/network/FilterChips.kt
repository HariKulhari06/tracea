package com.hari.tracea.ui.screens.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.model.HttpMethod
import com.hari.tracea.ui.theme.LocalDebuggerColors

enum class StatusFilter(val label: String) {
    ALL("All"),
    SUCCESS("Success"),
    ERRORS("Errors"),
    SUCCESS_2XX("2xx"),
    REDIRECT_3XX("3xx"),
    CLIENT_ERROR_4XX("4xx"),
    SERVER_ERROR_5XX("5xx")
}

enum class MethodFilter(val label: String, val method: HttpMethod?) {
    ALL("All Methods", null),
    GET("GET", HttpMethod.GET),
    POST("POST", HttpMethod.POST),
    PUT("PUT", HttpMethod.PUT),
    PATCH("PATCH", HttpMethod.PATCH),
    DELETE("DELETE", HttpMethod.DELETE)
}

/**
 * Option 3 Unified Filter Bar:
 * Compact single row with a Segmented Control [ All | ● Success | ● Errors ]
 * and a Method Dropdown Menu [ Method ▾ ] with reset capability.
 */
@Composable
fun NetworkFilterBar(
    activeStatusFilter: StatusFilter,
    activeMethodFilter: MethodFilter,
    onStatusSelected: (StatusFilter) -> Unit,
    onMethodSelected: (MethodFilter) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    var methodMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Segmented Control: [ All | ● Success | ● Errors ]
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(colors.surface)
                .border(1.dp, colors.outline.copy(alpha = 0.6f), RoundedCornerShape(9.dp))
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val segments = listOf(StatusFilter.ALL, StatusFilter.SUCCESS, StatusFilter.ERRORS)
            segments.forEach { filter ->
                val isSelected = activeStatusFilter == filter ||
                    (filter == StatusFilter.SUCCESS && activeStatusFilter == StatusFilter.SUCCESS_2XX) ||
                    (filter == StatusFilter.ERRORS && (activeStatusFilter == StatusFilter.CLIENT_ERROR_4XX || activeStatusFilter == StatusFilter.SERVER_ERROR_5XX))

                val itemBg = if (isSelected) colors.surfaceVariant else Color.Transparent
                val itemTextColor = if (isSelected) colors.onSurface else colors.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(7.dp))
                        .background(itemBg)
                        .clickable { onStatusSelected(filter) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (filter == StatusFilter.SUCCESS) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(colors.status2xx)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        } else if (filter == StatusFilter.ERRORS) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(colors.statusError)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        }

                        Text(
                            text = filter.label,
                            color = itemTextColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Method Dropdown Menu: [ Method ▾ ] or [ POST ▾ ]
        Box {
            val isMethodActive = activeMethodFilter != MethodFilter.ALL
            val methodColor = when (activeMethodFilter) {
                MethodFilter.GET -> colors.methodGet
                MethodFilter.POST -> colors.methodPost
                MethodFilter.PUT -> colors.methodPut
                MethodFilter.PATCH -> colors.methodPatch
                MethodFilter.DELETE -> colors.methodDelete
                else -> colors.onSurface
            }

            val buttonBg = if (isMethodActive) methodColor.copy(alpha = 0.18f) else colors.surface
            val buttonBorder = if (isMethodActive) methodColor.copy(alpha = 0.5f) else colors.outline.copy(alpha = 0.6f)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(buttonBg)
                    .border(1.dp, buttonBorder, RoundedCornerShape(9.dp))
                    .clickable { methodMenuExpanded = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (isMethodActive) {
                    Text(
                        text = activeMethodFilter.label,
                        color = methodColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = "Method",
                        color = colors.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select Method",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }

            DropdownMenu(
                expanded = methodMenuExpanded,
                onDismissRequest = { methodMenuExpanded = false },
                modifier = Modifier.background(colors.surfaceVariant)
            ) {
                MethodFilter.values().forEach { method ->
                    val isCurrent = activeMethodFilter == method
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = method.label,
                                    color = if (isCurrent) colors.primary else colors.onSurface,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onMethodSelected(method)
                            methodMenuExpanded = false
                        }
                    )
                }
            }
        }

        // Quick Clear Filter Button
        val hasActiveFilters = activeStatusFilter != StatusFilter.ALL || activeMethodFilter != MethodFilter.ALL
        AnimatedVisibility(
            visible = hasActiveFilters,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
                    .clickable { onReset() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear Filters",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun StatusFilterChips(
    selectedFilter: StatusFilter,
    onFilterSelected: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(StatusFilter.values()) { filter ->
            val isSelected = filter == selectedFilter

            val backgroundColor = if (isSelected) colors.primary else colors.surfaceVariant
            val textColor = if (isSelected) colors.onSurface else colors.onSurfaceVariant

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (filter == StatusFilter.ERRORS) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.errorDot)
                        )
                    }
                    Text(
                        text = filter.label,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
