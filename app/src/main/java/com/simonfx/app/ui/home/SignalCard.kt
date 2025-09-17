package com.simonfx.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simonfx.app.data.Signal

@Composable
fun SignalCard(
  signal: Signal,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      // Header
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
          signal.symbol,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(signal.side.uppercase(), style = MaterialTheme.typography.labelLarge)
      }

      // Provider / Category
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(signal.provider, style = MaterialTheme.typography.bodySmall)
        Text(signal.category, style = MaterialTheme.typography.bodySmall)
      }

      Divider()

      // Metrics
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Entry: ${signal.entry}")
        Text("TP: ${signal.takeProfit}   SL: ${signal.stopLoss}")
        signal.rrRatio?.let { Text("R:R: $it") }
        signal.pips?.let { Text("Pips/Δ: $it") }
        signal.confidence?.let { Text("Confidence: ${it}%") }
      }

      // Footer
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Issued: ${signal.issuedAt}", style = MaterialTheme.typography.bodySmall)
        Text(signal.status ?: "ACTIVE", style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}