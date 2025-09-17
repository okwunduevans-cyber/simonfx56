package com.simonfx.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Signal(
  val symbol: String,
  val side: String,            // "buy" | "sell"
  val entry: Double,
  val takeProfit: Double,
  val stopLoss: Double,
  val category: String,        // "FOREX" | "CRYPTO-FX"
  val provider: String,        // e.g., "Polygon.io", "Binance", "Kraken"
  val issuedAt: String,        // ISO-8601 UTC
  val validUntil: String? = null,
  val confidence: Int? = null, // 0..100
  val note: String? = null,
  val rrRatio: Double? = null,
  val pips: Double? = null,
  val status: String? = null   // "ACTIVE" | "HIT_TP" | "HIT_SL" | "EXPIRED" | "LOSS" | "WIN"
)