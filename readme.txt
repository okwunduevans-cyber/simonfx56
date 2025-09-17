## App shell & navigation

* `MainActivity.kt` — sets up Compose content + bottom nav. Starts at `usd_btc`.
* `AppNavHost.kt` — routes: `usd_btc`, `usd_jpy`, `xau_usd`, `settings`. Each Home screen is passed `BuildConfig.WS_BASE`/`HTTP_BASE`.
* `BottomNav.kt` — tabs: USD/BTC, USD/JPY, XAU/USD, Settings.

## Home & detail UI

* `SimonHomeScreen.kt` — filters the global signal list by symbol aliases (e.g., “BTCUSDT”|“BTC”|“XBTUSD”). `onSignalClick` is still a TODO.
* `SignalsScreen.kt` — list of signals; shows “Listening for live signals…” when empty.
* `SignalCard.kt` — clickable card (navigations still unwired).
* `SignalDetailScreen.kt` — renders a `Signal` in detail (unreachable today).

## Settings & keys

* `SettingsScreen.kt` — providers list with masked keys, EDIT/TEST/SAVE actions; booster toggles (Intraday FX, LLM rationales, Push).
* `SettingsViewModel.kt` — persists keys/boosters via `LocalKeysStore`; health/patch calls go to `KeysApiFake` (dev stub).
* `LocalKeysStore.kt` — writes `.local/keys.json`.
* `Provider.kt` (+ `ProviderHealth`, `ProviderUi`, `SettingsUiState`) — canonical provider enum (AlphaVantage, Finage, Gemini, OpenAI, Yahoo Finance, etc.), health formatting, booster state.

## Real-time data path (the “CDS skeleton”)

* `SignalSocket.kt` — **1 OkHttp WebSocket** to `WS_BASE`, auto-reconnect; emits `Status | Signals | Error`. It expects **an array** of JSON `Signal` objects, not a single object.
* `MarketRepository.kt` — subscribes to the socket, then runs the engine chain before exposing `UiState(status, signals)` to the UI.
* Engines:

  * `ProviderNormalizer.kt` — currently a stub (no schema transforms yet).
  * `SignalAdaptiveEngine.kt` — if `llmRationales` booster is ON, appends a tiny “AI: setup …” note to each signal.
  * `SignalRecyclingEngine.kt` — dedupes by `(symbol, issuedAt)`, sorts **newest first**.
  * `MarketTimer.kt` — drops anything with expired `validUntil`.
* `CentralDispatcher.kt` — event bus you can fan into/fan out of (already used by repo).

## Models & utilities

* `Signal.kt` — your current canonical signal type used everywhere (not yet migrated to `TradingSignal.kt`).
* `Candle.kt` — simple OHLC model (exists once correctly; see “duplicates” below).
* `SymbolAlias.kt` — tiny alias map (BTCUSDT→USD/BTC, GBPUSD→USD/GBP). Needs to grow to support your multi-provider nomenclatures.

## Widgets & theme

* `WSStatusBanner.kt` (not used yet), `LatencyDot.kt`, `TagPill.kt`.
* Compose theme + standard launcher resources.

# Duplicates / misplacements I found (clean these)

1. **`KeysApiFake` appears twice**

   * Once in the correct file `KeysApiFake.kt`.
   * Once mistakenly embedded inside a file block labeled `Candle.kt` (it’s literally a duplicate class definition in the wrong file block).
     Remove the stray copy so there’s a single source of truth.

2. **`ic_launcher_background` is defined twice by design**

   * Drawable vector and a color resource with the same conceptual name. That’s normal for Android launchers—just pointing it out so you know it’s intentional, not a logic bug.

3. **Detail navigation gap**

   * `SignalCard` is clickable, but `SimonHomeScreen`’s `onSignalClick` is TODO, and `AppNavHost` has no `detail/{id}` route. Your `SignalDetailScreen` never renders.

4. **Provider normalizer is a stub**

   * Today it’s a pass-through. For multi-provider ingestion (Binance/Gemini/Finage/Yahoo/etc.) you’ll need this to unify symbol formats, sides, field names, and timestamps.

5. **Model drift risk**

   * Everything uses `Signal.kt`. Your plan mentions `TradingSignal.kt` as the final contract. You’ll want to migrate the whole stack (socket decode, repo, engines, UI) to that final model before you turn on multiple providers.

# What your Stage-B note guarantees (and what it warns about)

* Build unblocked; XML theme fixed.
* WebSocket pipeline is real (array of signals in; dedupe/sort/expiry out).
* If you only see “Listening…”, one of these is happening: `WS_BASE` unreachable, server sending a **single object** instead of an **array**, schema mismatch, or all items are already expired.
* You can flip the LLM booster to see the tiny “AI: setup …” augmentation to know the engine chain is alive.

# How this maps to your trader-first vision

Your design says: **experienced human trader systems + many data feeds → one canonical river → fast emission of multiple signals to UI in real time.** The code you have is already shaped for this:

* **Fan-in point**: keep `SignalSocket` for your house feed, then add provider adapters (`BinanceWsClient`, `GeminiWsClient`, etc.) that **push into `CentralDispatcher`** after `ProviderNormalizer`.
* **House schema**: finish `TradingSignal.kt` and teach `ProviderNormalizer` to translate every incoming provider event (FX/crypto) into that schema.
* **Continuous flow**: `SignalRecyclingEngine` + `MarketTimer` already prevent spam and stale leaks.
* **Human trader emitters**: as soon as a trader posts a signal (to your server or directly via a WS channel), it’s just another item in the river → normalized → deduped → displayed within the same second.
* **AI assist (never in the driver’s seat)**: keep `SignalAdaptiveEngine` strictly additive—commentary, risk notes, cross-venue confirmation—no mutation of entries/SL/TP.

# Exact gaps between “what’s there” vs “what your idea promises”

* **WebSockets**: 1 stream today → **N streams** (Binance, Gemini, Finage, your Human-Trader WS). Merge them.
* **History backfill**: no scheduled candles today → add REST fetchers (e.g., Yahoo/Finnhub/TwelveData) into a “CDS Booster” that periodically enriches context.
* **Model**: `Signal.kt` now → migrate to **`TradingSignal.kt`** before widening the ingress.
* **Booster wiring**: only LLM note is live → **OpenAI/Gemini commentary** with real keys; **Intraday FX** toggle should activate a real provider; **Push** should actually schedule/show local notifications.
* **UI wiring**: add `detail/{id}` route + pass `signal.id` from `SignalCard`; add an optional top-bar socket status using your `WSStatusBanner`.

# Concrete next moves (no guesses, just the work)

1. **Lock the schema**

   * Promote `TradingSignal.kt` to the app model and remove `Signal.kt`.
   * Update `SignalSocket` to decode `List<TradingSignal>`.
   * Update engines and UI to `TradingSignal`.

2. **Finish the Settings “realness”**

   * Replace `KeysApiFake` with a tiny `KeysApi` that:

     * Saves keys (already done locally).
     * **TEST**: Yahoo (chart ping), Gemini (ticker), OpenAI (chat-completions “ping” prompt). Reflect real `ProviderHealth` (status/latency/lastTs).

3. **Provider adapters (fan-in)**

   * Add `BinanceWsClient`, `GeminiWsClient`, optional `FinageWsClient`.
   * Each pushes its native payloads → `ProviderNormalizer` → `CentralDispatcher`.
   * Expand `ProviderNormalizer` to handle symbol mapping (`BTCUSDT`/`BTC-USD`/`XBTUSD`, `USDJPY`/`JPYUSD`, `XAUUSD`/`GOLD`/`XAU`) and unify times (ISO-8601 Z).

4. **Human-trader ingress**

   * Define a trivial WS message (`TradingSignal[]`) your trader desks publish to. That’s your authoritative stream. Treat exchange ticks as **context** not mandatory source.

5. **AI commentary booster (production path)**

   * In `SignalAdaptiveEngine`, when booster is ON and a fresh signal arrives:

     * Build a compact prompt with pair, side, entry/SL/TP, recent ATR|RSI|EMA (if available), and provenance (who/where).
     * Call OpenAI **or** Gemini (selected by Settings).
     * Append the returned 1–2 sentence rationale to `note`.
   * Time-box to <500 ms and fail-open (no blockage of the signal path).

6. **Detail nav & status**

   * Add `detail/{id}` in `AppNavHost`; navigate from `SignalCard`.
   * Optionally surface `WSStatusBanner` on the Home screens.

# Why this meets your “multiple signals dropping in real time” requirement

* Any trader posting to your WS → normalized → deduped/prioritized → UI within a tick.
* Multiple providers streaming ticks simultaneously are merged and used for **confirmation** or **context**, not to alter human signal payloads.
* Expired signals don’t linger; duplicates don’t spam; commentary is additive and optional.

If you want, I’ll produce the **exact file replacements** (full scripts) for:

* `TradingSignal.kt` (final schema),
* `SignalSocket.kt` (decode `List<TradingSignal>`),
* `ProviderNormalizer.kt` (real mappings for BTC/JPY/XAU across Binance/Gemini/Yahoo),
* `KeysApi.kt` (OpenAI/Gemini/Yahoo tests),
* `SignalAdaptiveEngine.kt` (real LLM commentary wiring),
* `AppNavHost.kt` + `SimonHomeScreen.kt` (detail route + click).

Then we can light up the fan-in (Binance/Gemini + your Trader WS) and you’ll see **multiple, concurrent human signals** landing in the app stream exactly as your experienced desks emit them—fast, clean, and auditable.




## Sources That Support Historical + Real-Time FX / Crypto for Your Pairs

Here are a few APIs that are known to give both historical (candles / OHLC) and real-time trades / quote streaming (or frequent updates) that could cover your requested pairs:

| Provider                                 | Pairs / Markets likely covered                                                                                                            | Historical API / Candles                                                        | Real-time / WebSocket                                                                            |
| ---------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| **Finnhub**                              | Forex (USD/JPY etc), Crypto (USD/BTC etc)                                                                                                 | `stock_candles` endpoint gives OHLC for stocks / FX / crypto. ([finnhub.io][1]) | WebSocket stream of trades / price updates for FX / crypto / stocks. ([finnhub.io][2])           |
| **Binance**                              | Crypto (USD/BTC etc) very well; doesn’t usually provide FX (fiat) pairs like USD/JPY unless via crypto markets pegged or via derivatives. | Historical REST endpoints (candlestick / klines) for crypto.                    | WebSocket streams (trade, aggregate trade, order book etc) for crypto. ([Binance Developers][3]) |
| **Twelve Data** (you mentioned earlier)  | Stocks, Crypto, Forex likely includes USD/JPY etc                                                                                         | Yes, candles / quote history via REST.                                          | Yes, WebSocket for quotes / price streaming.                                                     |
| **Alpha Vantage / MarketData.app / etc** | Might have FX + crypto + USD pairs via REST mainly; real-time streaming less certain or limited.                                          |                                                                                 |                                                                                                  |

So a good combination is: use **Finnhub** + **Binance** + maybe **Twelve Data** + possibly a “FX-specialized” provider if needed for USD/JPY etc.

---

## Known Constraints & What to Check

* API keys will likely be needed for historical data or higher request rates.
* Free tier limits: how many candles per request, how far back you can go, rate limits.
* For WebSockets: some free or limited streaming (e.g. only price updates, not full order book), message / symbol subscription limits.
* For real-time data, ensure low enough latency for your trade-opportunity spotting.

---

## Kotlin Modules / Snippets

Here are Kotlin code “pluggable” modules / classes for:

* Fetching historical candles (REST) for your pair (USD/BTC, USD/JPY, USD/AUX).
* Opening WebSocket streams for real-time price/trade updates for those pairs.

You’ll need to adapt (especially the URL, JSON fields) depending on the API you pick; I’ll sketch using Finnhub + Binance + a generic interface so you can add more.

---

### Historical OHLC Data Fetcher (Kotlin, using Retrofit + Coroutines)

*File: `data/RemoteHistoryFetcher.kt`*

```kotlin
package com.yourapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data classes for Finnhub candles
data class CandleResponse(
    val c: List<Double>,  // close
    val h: List<Double>,  // high
    val l: List<Double>,  // low
    val o: List<Double>,  // open
    val s: String,        // status ("ok" etc)
    val t: List<Long>,    // timestamps (unix)
    val v: List<Double>   // volume
)

// Retrofit interface
interface FinnhubApi {
    @GET("stock/candle")
    suspend fun getCandles(
        @Query("symbol") symbol: String,   // e.g. "BTCUSD", "USDJPY"
        @Query("resolution") resolution: String,  // e.g. "1", "5", "15", "60", "D"
        @Query("from") from: Long,  // unix seconds
        @Query("to") to: Long,      // unix seconds
        @Query("token") token: String
    ): CandleResponse
}

// Usage class
class RemoteHistoryFetcher(
    baseUrl: String,
    private val apiKey: String
) {
    private val api: FinnhubApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(FinnhubApi::class.java)
    }

    suspend fun fetchHistory(
        symbol: String, 
        resolution: String, 
        fromUnixSec: Long, 
        toUnixSec: Long
    ): List<Candle> {
        val resp = api.getCandles(
            symbol = symbol,
            resolution = resolution,
            from = fromUnixSec,
            to = toUnixSec,
            token = apiKey
        )
        if (resp.s != "ok") {
            throw RuntimeException("Finnhub history fetch failed: status ${resp.s}")
        }
        // convert to your internal Candle model
        return resp.t.mapIndexed { idx, ts ->
            Candle(
                open = resp.o[idx],
                high = resp.h[idx],
                low = resp.l[idx],
                close = resp.c[idx],
                volume = resp.v.getOrNull(idx) ?: 0.0,
                timestamp = ts
            )
        }
    }
}

// Your internal model
data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val timestamp: Long
)
```

**What to change if using Binance**: The REST endpoint format for Binance is different (symbol naming, intervals, etc). You’ll need a separate retrofit interface for Binance history (klines). But structure is similar.

---

### Real-Time WebSocket Client (Kotlin, using OkHttp)

*File: `realtime/WebSocketClient.kt`*

```kotlin
package com.yourapp.realtime

import okhttp3.*
import okio.ByteString
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

// Define message types
sealed class RealTimeMessage {
    data class PriceUpdate(
        val symbol: String,
        val price: Double,
        val timestamp: Long
    ) : RealTimeMessage()

    data class TradeUpdate(
        val symbol: String,
        val price: Double,
        val volume: Double,
        val timestamp: Long
    ) : RealTimeMessage()

    // add more types as needed
}

class RealTimeWebSocketClient(
    private val url: String,
    private val subscribeMessage: String,
    private val client: OkHttpClient = OkHttpClient()
) {
    private var webSocket: WebSocket? = null
    private val _messages = MutableSharedFlow<RealTimeMessage>(replay = 0)
    val messages: SharedFlow<RealTimeMessage> = _messages

    fun connect() {
        val request = Request.Builder()
            .url(url)
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                ws.send(subscribeMessage)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                // Parse message JSON, extract symbol, price etc.
                // Example pseudo:
                // val json = JsonParser.parse(text)
                // if price update:
                // _messages.tryEmit( RealTimeMessage.PriceUpdate(...) )
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                // optionally handle binary
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(1000, null)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                // handle reconnect maybe
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client shutting down")
    }
}
```

---

### Example: Setting Up for Your Symbols & Sources

Let’s say you decide on **Finnhub** + **Binance**. You could write something like:

```kotlin
// Example usage

val historyFetcher = RemoteHistoryFetcher(
    baseUrl = "https://finnhub.io/api/v1/",
    apiKey = "YOUR_FINNHUB_KEY"
)

// To get historical data for USD/BTC
// Finnhub uses symbol naming like "BINANCE:BTCUSD" or similar; you’ll need to check their docs.
val historyUsdbtc = historyFetcher.fetchHistory(
    symbol = "BTCUSD",
    resolution = "1",  // 1 minute
    fromUnixSec = someStartTime,
    toUnixSec = currentTimeSec()
)

// Real-time via WebSocket

// For Finnhub WebSocket (trades / quotes)
val finnhubWsUrl = "wss://ws.finnhub.io?token=YOUR_FINNHUB_KEY"
val subscribeMsg = """{"type":"subscribe","symbol":"USD/JPY"}"""  // this syntax depends on provider

val rtClient = RealTimeWebSocketClient(finnhubWsUrl, subscribeMsg)
rtClient.connect()

// Then somewhere collect from rtClient.messages flow, filter for symbol, process price/trade updates
```

---

## Putting It Together: Architecture Ideas

To make this robust, you’ll want:

1. **Abstraction**: Define interfaces like `HistoryProvider` and `RealtimeProvider` so you can plug in Finnhub, Binance, or any other service cleanly.

2. **Symbol mapping & normalization**: Each provider might name symbols differently (e.g. “BTCUSD”, “USD/BTC”, “BTC-USD”, or “X\:BTCUSD”). Build a mapping layer so the downstream logic sees unified symbols.

3. **Buffering & caching**:




   * Historical data can be cached locally so you don’t keep refetching.
   * For real-time, use flows or queues. Possibly buffer or aggregate to reduce load.

4. **Filtering**: Only accept messages for your target symbols (USD/BTC, USD/JPY, USD/AUX). For real-time, drop fields you don’t need.

5. **Fallbacks / redundancy**: If one provider’s real-time feed lags or drops, have another provider you can switch to.

6. **Backfill functionality**: If real-time misses some data (down periods), use historical API to fill gaps.




