package com.simonfx.app.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WSStatusBanner(status: String, onRetry: () -> Unit) {
  val bg = when {
    status.startsWith("Conn", true) -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    status.startsWith("Disc", true) -> MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
    else -> MaterialTheme.colorScheme.surfaceVariant
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(bg)
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text("WS: $status", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    Text("Retry", style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { onRetry() })
  }
}