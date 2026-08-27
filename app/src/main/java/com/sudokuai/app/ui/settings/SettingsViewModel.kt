package com.sudokuai.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuai.app.data.datastore.AppTheme
import com.sudokuai.app.data.datastore.SettingsDataStore
import com.sudokuai.app.data.update.UpdateCheckResult
import com.sudokuai.app.data.update.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Distinguishes "no check run yet" from "check in progress" for [SettingsViewModel.updateState]. */
sealed class UpdateUiState {
    object Idle : UpdateUiState()
    object Checking : UpdateUiState()
    data class Result(val result: UpdateCheckResult) : UpdateUiState()
}

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

    // --- Manual, opt-in update check (see UpdateChecker's kdoc for why this is the app's one
    // deliberate exception to being offline). --------------------------------------------------

    private val _updateState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

    fun checkForUpdates(currentVersionName: String) {
        if (_updateState.value == UpdateUiState.Checking) return
        viewModelScope.launch {
            _updateState.value = UpdateUiState.Checking
            val result = withContext(Dispatchers.IO) {
                UpdateChecker.checkForUpdate(currentVersionName)
            }
            _updateState.value = UpdateUiState.Result(result)
        }
    }

    fun onUpdateResultDismissed() {
        _updateState.value = UpdateUiState.Idle
    }

    class Factory(private val settingsDataStore: SettingsDataStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(settingsDataStore) as T
    }
}
