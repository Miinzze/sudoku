package com.sudokuai.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuai.app.data.datastore.AppTheme
import com.sudokuai.app.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val mistakeHighlighting: Boolean = true,
    val sameNumberHighlight: Boolean = true,
    val rowColHighlight: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val autoRemoveCandidates: Boolean = true,
)

class SettingsViewModel(private val settingsDataStore: SettingsDataStore) : ViewModel() {

    private data class FirstFive(
        val theme: AppTheme,
        val mistakeHighlighting: Boolean,
        val sameNumberHighlight: Boolean,
        val rowColHighlight: Boolean,
        val soundEnabled: Boolean,
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsDataStore.theme,
        settingsDataStore.mistakeHighlighting,
        settingsDataStore.sameNumberHighlight,
        settingsDataStore.rowColHighlight,
        settingsDataStore.soundEnabled,
    ) { theme, mistakes, sameNumber, rowCol, sound ->
        FirstFive(theme, mistakes, sameNumber, rowCol, sound)
    }.combine(settingsDataStore.vibrationEnabled) { first, vibration ->
        SettingsUiState(
            theme = first.theme,
            mistakeHighlighting = first.mistakeHighlighting,
            sameNumberHighlight = first.sameNumberHighlight,
            rowColHighlight = first.rowColHighlight,
            soundEnabled = first.soundEnabled,
            vibrationEnabled = vibration,
        )
    }.combine(settingsDataStore.autoRemoveCandidates) { state, autoRemove ->
        state.copy(autoRemoveCandidates = autoRemove)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch { settingsDataStore.setTheme(theme) }
    }

    fun onMistakeHighlightingToggled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setMistakeHighlighting(enabled) }
    }

    fun onSameNumberHighlightToggled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setSameNumberHighlight(enabled) }
    }

    fun onRowColHighlightToggled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setRowColHighlight(enabled) }
    }

    fun onSoundToggled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setSoundEnabled(enabled) }
    }

    fun onVibrationToggled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setVibrationEnabled(enabled) }
    }

    fun onAutoRemoveCandidatesToggled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setAutoRemoveCandidates(enabled) }
    }

    class Factory(private val settingsDataStore: SettingsDataStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(settingsDataStore) as T
    }
}
