package com.sudokuai.app.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudokuai.app.R
import com.sudokuai.app.di.ServiceLocator
import com.sudokuai.app.ui.components.NumberPad
import com.sudokuai.app.ui.components.SudokuBoard
import com.sudokuai.core.model.Candidates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    onNavigateToGame: (Long) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: EditorViewModel = viewModel(
        factory = EditorViewModel.Factory(ServiceLocator.provideSudokuRepository(context)),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val limitMessage = stringResource(R.string.game_save_limit_reached)

    LaunchedEffect(uiState.navigateToGameId) {
        uiState.navigateToGameId?.let {
            onNavigateToGame(it)
            viewModel.onNavigationHandled()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(limitMessage)
            viewModel.onErrorShown()
        }
    }

    when (uiState.validationResult) {
        ValidationResult.VALID -> AlertDialog(
            onDismissRequest = viewModel::onValidationDialogDismissed,
            title = { Text(stringResource(R.string.editor_valid_title)) },
            text = { Text(stringResource(R.string.editor_valid_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onValidationDialogDismissed()
                    viewModel.onPlayRequested()
                }) { Text(stringResource(R.string.editor_play)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onValidationDialogDismissed) {
                    Text(stringResource(R.string.common_close))
                }
            },
        )
        ValidationResult.NO_SOLUTION -> AlertDialog(
            onDismissRequest = viewModel::onValidationDialogDismissed,
            title = { Text(stringResource(R.string.editor_no_solution_title)) },
            text = { Text(stringResource(R.string.editor_no_solution_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onValidationDialogDismissed) { Text(stringResource(R.string.common_close)) }
            },
        )
        ValidationResult.MULTIPLE_SOLUTIONS -> AlertDialog(
            onDismissRequest = viewModel::onValidationDialogDismissed,
            title = { Text(stringResource(R.string.editor_multiple_solutions_title)) },
            text = { Text(stringResource(R.string.editor_multiple_solutions_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onValidationDialogDismissed) { Text(stringResource(R.string.common_close)) }
            },
        )
        ValidationResult.NONE -> Unit
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.editor_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SudokuBoard(
                originalPuzzle = uiState.puzzle,
                currentState = uiState.puzzle,
                candidates = Candidates(),
                solution = null,
                selectedIndex = uiState.selectedIndex,
                highlightMistakes = false,
                highlightSameNumber = false,
                highlightRowCol = true,
                onCellClick = viewModel::onCellSelected,
            )

            NumberPad(
                onNumberClick = viewModel::onDigitEntered,
                onEraseClick = viewModel::onEraseSelected,
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = viewModel::onClearAll, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.editor_clear))
                }
                Button(onClick = viewModel::onValidateRequested, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.editor_validate))
                }
            }
        }
    }
}
