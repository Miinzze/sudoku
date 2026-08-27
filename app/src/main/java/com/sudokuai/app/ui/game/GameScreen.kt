package com.sudokuai.app.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudokuai.app.R
import com.sudokuai.app.di.ServiceLocator
import com.sudokuai.app.ui.components.DifficultyBadge
import com.sudokuai.app.ui.components.SudokuBoard
import com.sudokuai.app.ui.components.NumberPad
import com.sudokuai.app.ui.components.TimerText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: GameViewModel = viewModel(
        key = "game_$gameId",
        factory = GameViewModel.Factory(
            gameId,
            ServiceLocator.provideSudokuRepository(context),
            ServiceLocator.provideSettingsDataStore(context),
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.pauseTimer()
                Lifecycle.Event.ON_RESUME -> viewModel.resumeTimer()
                Lifecycle.Event.ON_STOP -> viewModel.flushSave()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (uiState.gameNotFound) {
        onBack()
        return
    }

    if (uiState.showCompletionDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onCompletionDialogDismissed,
            title = { Text(stringResource(R.string.game_solved_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.game_solved_message,
                        com.sudokuai.app.ui.components.formatElapsed(uiState.elapsedSeconds),
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onCompletionDialogDismissed()
                    onBack()
                }) { Text(stringResource(R.string.game_back_to_home)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCompletionDialogDismissed) {
                    Text(stringResource(R.string.game_ok))
                }
            },
        )
    }

    if (uiState.showSolveAllConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onSolveAllCancelled,
            title = { Text(stringResource(R.string.game_solve_all_confirm_title)) },
            text = { Text(stringResource(R.string.game_solve_all_confirm_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onSolveAllConfirmed) {
                    Text(stringResource(R.string.game_solve_all))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onSolveAllCancelled) {
                    Text(stringResource(R.string.game_cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { DifficultyBadge(uiState.difficulty) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TimerText(uiState.elapsedSeconds, modifier = Modifier.padding(end = 12.dp))
                    IconButton(onClick = viewModel::onToggleFavorite) {
                        Icon(
                            if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(R.string.common_favorite),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SudokuBoard(
                originalPuzzle = uiState.originalPuzzle,
                currentState = uiState.currentState,
                candidates = uiState.candidates,
                solution = uiState.solution,
                selectedIndex = uiState.selectedIndex,
                highlightMistakes = uiState.mistakeHighlighting,
                highlightSameNumber = uiState.sameNumberHighlight,
                highlightRowCol = uiState.rowColHighlight,
                onCellClick = viewModel::onCellSelected,
            )

            NumberPad(
                onNumberClick = viewModel::onNumberInput,
                onEraseClick = viewModel::onEraseClick,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FilterChip(
                    selected = uiState.notesMode,
                    onClick = viewModel::onToggleNotesMode,
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    label = { Text(stringResource(R.string.game_notes)) },
                )

                androidx.compose.foundation.layout.Box {
                    Button(onClick = viewModel::onSolveMenuOpened) {
                        Text(stringResource(R.string.game_solve))
                    }
                    DropdownMenu(
                        expanded = uiState.showSolveMenu,
                        onDismissRequest = viewModel::onSolveMenuDismissed,
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.game_solve_one_cell)) },
                            onClick = viewModel::onSolveOneCellRequested,
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.game_solve_all)) },
                            onClick = viewModel::onSolveAllRequested,
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.game_cancel)) },
                            onClick = viewModel::onSolveMenuDismissed,
                        )
                    }
                }
            }
        }
    }
}
