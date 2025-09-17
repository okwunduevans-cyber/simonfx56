package com.simonfx.app.data

import android.content.Context
import com.simonfx.app.ui.state.BoostersState
import com.simonfx.app.ui.state.Provider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class KeysFile(
    val keys: Map<String, String> = emptyMap(),
    @SerialName("boosters") val boosters: BoostersState = BoostersState()
)

class LocalKeysStore(private val ctx: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val dir: File get() = File(ctx.filesDir, ".local").apply { if (!exists()) mkdirs() }
    private val file: File get() = File(dir, "keys.json")

    fun readKeys(): Map<Provider, String> {
        if (!file.exists()) return emptyMap()
        val content = file.readText()
        val parsed = runCatching { json.decodeFromString<KeysFile>(content) }.getOrNull() ?: return emptyMap()
        return parsed.keys.mapNotNull { (k, v) ->
            runCatching { Provider.valueOf(k) }.getOrNull()?.let { it to v }
        }.toMap()
    }

    fun writeKey(provider: Provider, key: String): Boolean {
        val current = readAll()
        val next = current.copy(keys = current.keys + (provider.name to key))
        return runCatching { file.writeText(json.encodeToString(next)); true }.getOrDefault(false)
    }

    fun writeBoosters(b: BoostersState): Boolean {
        val current = readAll()
        val next = current.copy(boosters = b)
        return runCatching { file.writeText(json.encodeToString(next)); true }.getOrDefault(false)
    }

    private fun readAll(): KeysFile {
        if (!file.exists()) return KeysFile()
        val content = runCatching { file.readText() }.getOrNull() ?: return KeysFile()
        return runCatching { json.decodeFromString<KeysFile>(content) }.getOrElse { KeysFile() }
    }
}
