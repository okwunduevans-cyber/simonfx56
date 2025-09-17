package com.simonfx.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simonfx.app.data.Signal

@Composable
fun SignalsScreen(
  signals: List<Signal>,
  onSignalClick: (Signal) -> Unit
) {
  if (signals.isEmpty()) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text("Listening for live signals…", style = MaterialTheme.typography.titleMedium)
    }
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(signals) { sig ->
      SignalCard(signal = sig, onClick = { onSignalClick(sig) })
    }
  }
}