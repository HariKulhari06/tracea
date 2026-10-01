package com.hari.tracea.ui.overlay

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.ui.theme.LocalDebuggerColors

@Composable
fun FloatingDebugButton(
    requestCount: Int,
    onDrag: (Float, Float) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    var isDragging by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(62.dp)
            .graphicsLayer { 
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { 
                        isDragging = false
                        onDragEnd()
                    },
                    onDragCancel = { 
                        isDragging = false
                        onDragEnd()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Main Button Disk (52x52)
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = if (isDragging) 12.dp else 6.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF7E97FF).copy(alpha = 0.5f),
                    spotColor = Color(0xFF7E97FF).copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8EA5FF),
                            Color(0xFF6781F8),
                            Color(0xFF485DD8)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable {
                    if (!isDragging) {
                        onClick()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Concentric radar ring
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
            )

            // Specular light reflection on top half
            Box(
                modifier = Modifier
                    .size(width = 32.dp, height = 16.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 2.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.38f),
                                Color.White.copy(alpha = 0.0f)
                            )
                        )
                    )
            )

            // Center Network Icon
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Tracea Debugger",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // Request Count Badge
        if (requestCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF5252),
                                Color(0xFFD32F2F)
                            )
                        )
                    )
                    .border(1.5.dp, Color(0xFF0F111A), RoundedCornerShape(50))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (requestCount > 99) "99+" else "$requestCount",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
