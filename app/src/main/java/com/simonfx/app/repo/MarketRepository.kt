package com.simonfx.app.repo

import com.simonfx.app.data.Signal
import com.simonfx.app.net.SignalSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.runningFold

class MarketRepository(
    private val socket: SignalSocket = SignalSocket()
) {
    data class UiState(
        val status: String = "Disconnected",
        val signals: List<Signal> = emptyList()
    )

    fun state(): Flow<UiState> =
        socket.connect().runningFold(UiState()) { st, payload ->
            when (payload) {
                is SignalSocket.Payload.Status -> st.copy(status = payload.text)
                is SignalSocket.Payload.Signals -> {
                    val merged = (payload.list + st.signals)
                        .distinctBy { it.issuedAt.toString() + "|" + it.symbol }
                    st.copy(signals = merged)
                }
                is SignalSocket.Payload.Error -> {
                    st.copy(status = "Error: ${payload.message}")
                }
            }
        }
}
