package com.simonfx.app.ui.state

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Full set (original + your spec)
enum class Provider(val displayName: String, val assetName: String) {
    ALPHAVANTAGE("AlphaVantage", "file:///android_asset/icons/provider_node.svg"),
    FINAGE("Finage", "file:///android_asset/icons/provider_node.svg"),
    GEMINI("Gemini", "file:///android_asset/icons/provider_node.svg"),
    OPENAI("OpenAI", "file:///android_asset/icons/provider_node.svg"),
    FOREXRATEAPI("ForexRateAPI", "file:///android_asset/icons/provider_node.svg"),
    CURRENCYSTACK("CurrencyStack", "file:///android_asset/icons/provider_node.svg"),
    OPENEXCHANGERATES("OpenExchangeRates", "file:///android_asset/icons/provider_node.svg"),
    EXCHANGERATE_HOST("Exchangerate.host", "file:///android_asset/icons/provider_node.svg"),
    TWELVEDATA("TwelveData", "file:///android_asset/icons/provider_node.svg"),
    OANDA("OANDA", "file:///android_asset/icons/provider_node.svg"),
    POLYGON("Polygon.io", "file:///android_asset/icons/provider_node.svg"),
    YAHOO_FINANCE("Yahoo Finance", "file:///android_asset/icons/provider_node.svg"),
    WEBHOOK("Webhook", "file:///android_asset/icons/provider_node.svg");
}

@Serializable
data class ProviderHealth(
    val status: String = "unknown",      // healthy | slow | fail | unknown
    val latencyMs: Int? = null,
    val lastTs: Long? = null
)

data class ProviderUi(
    val provider: Provider,
    val keyText: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isTesting: Boolean = false,
    val health: ProviderHealth = ProviderHealth(),
) {
    val iconAsset: String get() = provider.assetName

    fun statusColor(): Color = when (health.status) {
        "healthy" -> Color(0xFF2E7D32)
        "slow"    -> Color(0xFFF9A825)
        "fail"    -> Color(0xFFC62828)
        else      -> Color(0xFF8E8E93)
    }

    fun statusText(): String = when (health.status) {
        "healthy" -> "Healthy"
        "slow"    -> "Slow"
        "fail"    -> "Fail"
        else      -> "Unknown"
    }

    fun latencyText(): String = health.latencyMs?.let { "$it ms" } ?: "— ms"

    fun lastCheckedText(): String = health.lastTs?.let {
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
        "Last checked: ${fmt.format(Instant.ofEpochMilli(it))}"
    } ?: "Last checked: —"

    fun fromHealth(h: ProviderHealth?): ProviderUi = if (h == null) this else copy(health = h)
}

@Serializable
data class BoostersState(
    val intradayFx: Boolean = false,
    val llmRationales: Boolean = false,
    val pushNotifications: Boolean = false,
)

data class SettingsUiState(
    val providers: List<ProviderUi> = Provider.entries.map { ProviderUi(it) },
    val boosters: BoostersState = BoostersState(),
    val bootersSaved: Boolean = true,
    val toast: String? = null,
) {
    fun mapProviders(f: (ProviderUi) -> ProviderUi) = copy(providers = providers.map(f))
    fun update(p: Provider, f: ProviderUi.() -> ProviderUi) = copy(providers = providers.map { if (it.provider == p) f(it) else it })
    fun find(p: Provider) = providers.first { it.provider == p }
    fun withKeys(map: Map<Provider, String>) = mapProviders { it.copy(keyText = map[it.provider] ?: it.keyText) }
    companion object { fun initial() = SettingsUiState() }
}
