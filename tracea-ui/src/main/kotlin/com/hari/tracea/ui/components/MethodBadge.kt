package com.hari.tracea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.core.model.HttpMethod
import com.hari.tracea.ui.theme.LocalDebuggerColors

import androidx.compose.ui.graphics.Color

@Composable
fun MethodBadge(
    method: HttpMethod,
    modifier: Modifier = Modifier
) {
    val colors = LocalDebuggerColors.current
    val color = colors.methodColor(method)
    
    Box(
        modifier = modifier
            .widthIn(min = 52.dp)
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = method.name,
            color = Color(0xFF0F111A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
