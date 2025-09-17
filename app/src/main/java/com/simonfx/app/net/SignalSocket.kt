package com.simonfx.app.net

import com.simonfx.app.data.Signal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

class SignalSocket {

    sealed class Payload {
        data class Status(val text: String) : Payload()
        data class Signals(val list: List<Signal>) : Payload()
        data class Error(val message: String) : Payload()
    }

    private val channel = Channel<Payload>(Channel.BUFFERED)

    fun connect(): Flow<Payload> = flow {
        try {
            emit(Payload.Status("Connecting..."))
            emit(Payload.Signals(emptyList()))
            channel.receiveAsFlow().collect { payload ->
                emit(payload)
            }
        } catch (e: Exception) {
            emit(Payload.Error(e.localizedMessage ?: "Unknown error"))
        }
    }

    suspend fun sendStatus(status: String) {
        channel.send(Payload.Status(status))
    }

    suspend fun sendSignals(signals: List<Signal>) {
        channel.send(Payload.Signals(signals))
    }

    suspend fun sendError(error: String) {
        channel.send(Payload.Error(error))
    }
}
