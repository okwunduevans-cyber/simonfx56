package com.simonfx.app.data

data class ProviderHealth(
    val provider: String,
    val latencyMs: Long,
    val lastUpdated: Long
)
