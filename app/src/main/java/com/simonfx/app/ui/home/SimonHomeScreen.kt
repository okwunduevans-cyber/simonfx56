package com.simonfx.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simonfx.app.ui.vm.MainViewModel
import com.simonfx.app.data.Signal

@Composable
fun SimonHomeScreen(
    wsBase: String,
    httpBase: String,
    targetSymbols: String
) {
    val viewModel: MainViewModel = viewModel()
    val uiState = viewModel.state.collectAsState()

    // Normalize symbol matching
    val normalizedSymbols = when (targetSymbols.uppercase()) {
        "BTCUSDT", "BTC" -> listOf("BTCUSDT", "BTC", "XBTUSD")
        "USDJPY", "JPY" -> listOf("USDJPY", "JPYUSD", "JPY")
        "XAUUSD", "GOLD" -> listOf("XAUUSD", "GOLD", "XAU")
        else -> listOf(targetSymbols)
    }

    val filteredSignals = uiState.value.signals.filter { sig ->
        normalizedSymbols.any { alias -> sig.symbol.equals(alias, ignoreCase = true) }
    }

    SignalsScreen(
        signals = filteredSignals,
        onSignalClick = {
            // TODO: Navigate to detail screen, or show dialog
        }
    )
}
