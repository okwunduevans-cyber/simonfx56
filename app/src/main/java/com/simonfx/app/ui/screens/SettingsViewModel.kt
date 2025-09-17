package com.simonfx.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simonfx.app.data.KeysApiFake
import com.simonfx.app.data.LocalKeysStore
import com.simonfx.app.ui.state.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val store = LocalKeysStore(app)
    // Swap to real API when backend is ready:
    private val api = KeysApiFake()

    private val _ui = MutableStateFlow(SettingsUiState.initial())
    val ui = _ui.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val loaded = store.readKeys()
            _ui.value = _ui.value.withKeys(loaded)
            refreshHealth()
        }
    }

    fun clearToast() { _ui.value = _ui.value.copy(toast = null) }

    fun toggleEdit(provider: Provider) {
        _ui.value = _ui.value.mapProviders {
            if (it.provider == provider) it.copy(isEditing = !it.isEditing) else it
        }
    }

    fun updateKeyText(provider: Provider, text: String) {
        _ui.value = _ui.value.mapProviders {
            if (it.provider == provider) it.copy(keyText = text) else it
        }
    }

    fun setBooster(block: BoostersState.() -> BoostersState) {
        val next = block(_ui.value.boosters)
        _ui.value = _ui.value.copy(bootersSaved = false, boosters = next)
        viewModelScope.launch(Dispatchers.IO) { store.writeBoosters(next) }
    }

    fun saveKey(provider: Provider) {
        val current = _ui.value.find(provider)
        if (current.keyText.isBlank()) {
            _ui.value = _ui.value.copy(toast = "Key cannot be empty")
            return
        }
        _ui.value = _ui.value.update(provider) { copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val okLocal = store.writeKey(provider, current.keyText)
            val okRemote = api.patchKey(provider, current.keyText) // always true in fake
            _ui.value = _ui.value.update(provider) { copy(isSaving = false, isEditing = false) }
            _ui.value = _ui.value.copy(
                toast = when {
                    okRemote -> "Saved"
                    okLocal  -> "Saved locally"
                    else     -> "Save failed"
                }
            )
            refreshHealth()
        }
    }

    fun testProvider(provider: Provider) {
        _ui.value = _ui.value.update(provider) { copy(isTesting = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val health = api.healthFor(provider)
            _ui.value = _ui.value.update(provider) { fromHealth(health).copy(isTesting = false) }
        }
    }

    private fun refreshHealth() {
        viewModelScope.launch(Dispatchers.IO) {
            val map = api.health()
            _ui.value = _ui.value.mapProviders { it.fromHealth(map[it.provider]) }
        }
    }
}
