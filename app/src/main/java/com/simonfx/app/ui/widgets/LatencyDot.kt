package com.simonfx.app.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp

@Composable
fun LatencyDot(latencyMs: Long? = null) {
    val color = when {
        latencyMs == null -> Color.Gray
        latencyMs < 200 -> Color.Green
        latencyMs < 600 -> Color.Yellow
        else -> Color.Red
    }

    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}
