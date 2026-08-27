package com.sudokuai.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sudokuai_settings")

/**
 * Wraps a single Preferences DataStore with one Flow-exposed key per user-facing setting. Every
 * setting is stored independently (no combined "settings blob") so a single toggle change only
 * triggers a Flow update for readers of that specific key.
 */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val MISTAKE_HIGHLIGHTING = booleanPreferencesKey("mistake_highlighting")
        val SAME_NUMBER_HIGHLIGHT = booleanPreferencesKey("same_number_highlight")
        val ROW_COL_HIGHLIGHT = booleanPreferencesKey("row_col_highlight")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val AUTO_REMOVE_CANDIDATES = booleanPreferencesKey("auto_remove_candidates")
    }

    val theme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME]?.let { stored ->
            runCatching { AppTheme.valueOf(stored) }.getOrDefault(AppTheme.SYSTEM)
        } ?: AppTheme.SYSTEM
    }

    val mistakeHighlighting: Flow<Boolean> = boolFlow(Keys.MISTAKE_HIGHLIGHTING, default = true)
    val sameNumberHighlight: Flow<Boolean> = boolFlow(Keys.SAME_NUMBER_HIGHLIGHT, default = true)
    val rowColHighlight: Flow<Boolean> = boolFlow(Keys.ROW_COL_HIGHLIGHT, default = true)
    val soundEnabled: Flow<Boolean> = boolFlow(Keys.SOUND_ENABLED, default = true)
    val vibrationEnabled: Flow<Boolean> = boolFlow(Keys.VIBRATION_ENABLED, default = true)
    val autoRemoveCandidates: Flow<Boolean> = boolFlow(Keys.AUTO_REMOVE_CANDIDATES, default = true)

    private fun boolFlow(key: Preferences.Key<Boolean>, default: Boolean): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[key] ?: default }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setMistakeHighlighting(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MISTAKE_HIGHLIGHTING] = enabled }
    }

    suspend fun setSameNumberHighlight(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SAME_NUMBER_HIGHLIGHT] = enabled }
    }

    suspend fun setRowColHighlight(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ROW_COL_HIGHLIGHT] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setAutoRemoveCandidates(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_REMOVE_CANDIDATES] = enabled }
    }
}
