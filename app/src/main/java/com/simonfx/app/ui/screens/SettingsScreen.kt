package com.simonfx.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.simonfx.app.ui.state.ProviderUi
import com.simonfx.app.ui.state.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val ui by vm.ui.collectAsState(initial = SettingsUiState.initial())
    val ctx = LocalContext.current

    ui.toast?.let { msg ->
        SideEffect { Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show(); vm.clearToast() }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Text("Providers", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }

            items(ui.providers) { p ->
                ProviderCard(
                    state = p,
                    onEditToggle = { vm.toggleEdit(p.provider) },
                    onKeyChange  = { vm.updateKeyText(p.provider, it) },
                    onSave       = { vm.saveKey(p.provider) },
                    onTest       = { vm.testProvider(p.provider) }
                )
            }

            item { Spacer(Modifier.height(8.dp)); Text("Boosters", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
            item { BoosterToggle("Intraday FX", ui.boosters.intradayFx)      { vm.setBooster { copy(intradayFx = it) } } }
            item { BoosterToggle("LLM rationales", ui.boosters.llmRationales){ vm.setBooster { copy(llmRationales = it) } } }
            item { BoosterToggle("Push/PWA notifications", ui.boosters.pushNotifications) { vm.setBooster { copy(pushNotifications = it) } } }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ProviderCard(
    state: ProviderUi,
    onEditToggle: () -> Unit,
    onKeyChange: (String) -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: icon + name + status dot
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = state.iconAsset,
                    contentDescription = state.provider.displayName,
                    modifier = Modifier.size(20.dp), // small to keep card compact
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(Modifier.width(8.dp))
                Text(state.provider.displayName, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                StatusDot(state.statusColor())
            }

            Spacer(Modifier.height(8.dp))

            // Row: masked key or editable field + actions (EDIT/TEST/SAVE)
            if (!state.isEditing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Masked dots like original
                    Text("●●●●●●")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onEditToggle) { Text("EDIT") }
                        Spacer(Modifier.width(16.dp))
                        TextButton(onClick = onTest, enabled = !state.isTesting) { Text("TEST") }
                    }
                }
            } else {
                var show by remember { mutableStateOf(false) }
                Column {
                    OutlinedTextField(
                        value = state.keyText,
                        onValueChange = onKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("API key") },
                        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { show = !show }) {
                                Icon(
                                    imageVector = if (show) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (show) "Hide" else "Show"
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onEditToggle) { Text("CANCEL") }
                        Spacer(Modifier.width(12.dp))
                        Button(onClick = onSave, enabled = state.keyText.isNotBlank() && !state.isSaving) {
                            if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp) else Text("SAVE")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Footer: latency + last checked (left/right)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(state.statusColor())
                    Spacer(Modifier.width(4.dp))
                    Text(state.latencyText(), style = MaterialTheme.typography.bodySmall)
                }
                Text(state.lastCheckedText(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BoosterToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) { Text(label); Switch(checked = checked, onCheckedChange = onChange) }
}

@Composable private fun StatusDot(color: Color) { Text("●", color = color) }
