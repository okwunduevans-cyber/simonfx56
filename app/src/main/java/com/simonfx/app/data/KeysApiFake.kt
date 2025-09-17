package com.simonfx.app.data

import com.simonfx.app.ui.state.Provider
import com.simonfx.app.ui.state.ProviderHealth
import kotlin.random.Random

/**
 * Dev-only fake: accepts any key and returns randomized health.
 */
class KeysApiFake {
    fun patchKey(@Suppress("UNUSED_PARAMETER") provider: Provider, @Suppress("UNUSED_PARAMETER") apiKey: String): Boolean = true

    fun health(): Map<Provider, ProviderHealth> =
        Provider.entries.associateWith { randomHealth() }

    fun healthFor(@Suppress("UNUSED_PARAMETER") provider: Provider): ProviderHealth = randomHealth()

    private fun randomHealth(): ProviderHealth {
        val now = System.currentTimeMillis()
        val latency = Random.nextInt(40, 700)
        val status = when {
            latency <= 180 -> "healthy"
            latency <= 450 -> "slow"
            else -> "fail"
        }
        return ProviderHealth(status = status, latencyMs = latency, lastTs = now)
    }
}
