package com.simonfx.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simonfx.app.repo.MarketRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
  repo: MarketRepository = MarketRepository()
) : ViewModel() {
  val state: StateFlow<MarketRepository.UiState> =
    repo.state().stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      MarketRepository.UiState()
    )
}