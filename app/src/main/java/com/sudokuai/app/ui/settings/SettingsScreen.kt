package com.sudokuai.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudokuai.app.R
import com.sudokuai.app.data.datastore.AppTheme
import com.sudokuai.app.data.update.UpdateCheckResult
import com.sudokuai.app.di.ServiceLocator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(ServiceLocator.provideSettingsDataStore(context)),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (e: Exception) {
            "?"
        }
    }

    (updateState as? UpdateUiState.Result)?.let { state ->
        UpdateResultDialog(state.result, onDismiss = viewModel::onUpdateResultDismissed)
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionTitle(stringResource(R.string.settings_appearance))
            Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip(AppTheme.HELL, R.string.settings_theme_light, uiState.theme, viewModel::onThemeSelected)
                ThemeChip(AppTheme.DUNKEL, R.string.settings_theme_dark, uiState.theme, viewModel::onThemeSelected)
                ThemeChip(AppTheme.SYSTEM, R.string.settings_theme_system, uiState.theme, viewModel::onThemeSelected)
            }

            HorizontalDivider()
            SectionTitle(stringResource(R.string.settings_gameplay))
            SettingSwitchRow(
                label = stringResource(R.string.settings_mistake_highlighting),
                checked = uiState.mistakeHighlighting,
                onCheckedChange = viewModel::onMistakeHighlightingToggled,
            )
            SettingSwitchRow(
                label = stringResource(R.string.settings_same_number_highlight),
                checked = uiState.sameNumberHighlight,
                onCheckedChange = viewModel::onSameNumberHighlightToggled,
            )
            SettingSwitchRow(
                label = stringResource(R.string.settings_row_col_highlight),
                checked = uiState.rowColHighlight,
                onCheckedChange = viewModel::onRowColHighlightToggled,
            )
            SettingSwitchRow(
                label = stringResource(R.string.settings_auto_remove_candidates),
                checked = uiState.autoRemoveCandidates,
                onCheckedChange = viewModel::onAutoRemoveCandidatesToggled,
            )

            HorizontalDivider()
            SectionTitle(stringResource(R.string.settings_feedback))
            SettingSwitchRow(
                label = stringResource(R.string.settings_sound),
                checked = uiState.soundEnabled,
                onCheckedChange = viewModel::onSoundToggled,
            )
            SettingSwitchRow(
                label = stringResource(R.string.settings_vibration),
                checked = uiState.vibrationEnabled,
                onCheckedChange = viewModel::onVibrationToggled,
            )

            HorizontalDivider()
            SectionTitle(stringResource(R.string.settings_updates))
            Text(
                stringResource(R.string.settings_current_version, versionName),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { viewModel.checkForUpdates(versionName) },
                    enabled = updateState !is UpdateUiState.Checking,
                ) {
                    Text(stringResource(R.string.settings_check_updates))
                }
                if (updateState is UpdateUiState.Checking) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun UpdateResultDialog(result: UpdateCheckResult, onDismiss: () -> Unit) {
    val context = LocalContext.current
    when (result) {
        is UpdateCheckResult.UpdateAvailable -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.settings_update_available_title, result.version)) },
            text = {
                Text(result.releaseNotes.ifBlank { stringResource(R.string.settings_update_available_message) })
            },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.downloadUrl)))
                    onDismiss()
                }) { Text(stringResource(R.string.settings_update_download)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_update_later)) }
            },
        )
        UpdateCheckResult.UpToDate -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.settings_update_up_to_date_title)) },
            text = { Text(stringResource(R.string.settings_update_up_to_date_message)) },
            confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) } },
        )
        is UpdateCheckResult.Error -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.settings_update_error_title)) },
            text = { Text(result.message) },
            confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) } },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ThemeChip(
    value: AppTheme,
    labelRes: Int,
    selected: AppTheme,
    onSelected: (AppTheme) -> Unit,
) {
    FilterChip(
        selected = selected == value,
        onClick = { onSelected(value) },
        label = { Text(stringResource(labelRes)) },
    )
}

@Composable
private fun SettingSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
