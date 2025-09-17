package com.simonfx.app.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simonfx.app.data.Signal

@Composable
fun SignalDetailScreen(signal: Signal) {
  Column(modifier = Modifier.padding(16.dp)) {
    Text("Signal detail", style = MaterialTheme.typography.titleLarge)
    Text("symbol = ${signal.symbol}")
    Text("side = ${signal.side}")
    Text("entry = ${signal.entry}")
    Text("takeProfit = ${signal.takeProfit}")
    Text("stopLoss = ${signal.stopLoss}")
    Text("category = ${signal.category}")
    Text("provider = ${signal.provider}")
    Text("issuedAt = ${signal.issuedAt}")
    Text("validUntil = ${signal.validUntil}")
    Text("confidence = ${signal.confidence}")
    Text("note = ${signal.note}")
    Text("rrRatio = ${signal.rrRatio}")
    Text("pips = ${signal.pips}")
    Text("status = ${signal.status}")
  }
}
