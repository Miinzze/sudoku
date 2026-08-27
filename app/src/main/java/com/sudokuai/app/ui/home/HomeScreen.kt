package com.sudokuai.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudokuai.app.R
import com.sudokuai.app.di.ServiceLocator
import com.sudokuai.core.model.Difficulty
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGame: (Long) -> Unit,
    onNavigateToEditor: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(ServiceLocator.provideSudokuRepository(context)),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val limitReachedMessage = stringResource(R.string.game_save_limit_reached)

    LaunchedEffect(uiState.navigateToGameId) {
        uiState.navigateToGameId?.let {
            onNavigateToGame(it)
            viewModel.onNavigationHandled()
        }
    }
    LaunchedEffect(uiState.navigateToEditor) {
        if (uiState.navigateToEditor) {
            onNavigateToEditor()
            viewModel.onNavigationHandled()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(limitReachedMessage)
            viewModel.onErrorShown()
        }
    }

    if (uiState.showResumeDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onResumeDeclined,
            title = { Text(stringResource(R.string.home_resume_title)) },
            text = { Text(stringResource(R.string.home_resume_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onResumeConfirmed) {
                    Text(stringResource(R.string.home_resume_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onResumeDeclined) {
                    Text(stringResource(R.string.home_resume_new))
                }
            },
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        HomeContent(
            padding = padding,
            uiState = uiState,
            onSelectDifficulty = viewModel::onSelectDifficulty,
            onNewSudoku = viewModel::onNewSudokuRequested,
            onEditor = viewModel::onEditorRequested,
        )
    }
}

@Composable
private fun HomeContent(
    padding: PaddingValues,
    uiState: HomeUiState,
    onSelectDifficulty: (Difficulty) -> Unit,
    onNewSudoku: () -> Unit,
    onEditor: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.home_choose_difficulty), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(Difficulty.entries) { difficulty ->
                FilterChip(
                    selected = uiState.selectedDifficulty == difficulty,
                    onClick = { onSelectDifficulty(difficulty) },
                    label = { Text(difficulty.displayNameDe) },
                )
            }
        }

        if (uiState.isGenerating) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.home_generating), modifier = Modifier.padding(top = 8.dp))
                }
            }
        } else {
            Button(onClick = onNewSudoku, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.home_new_sudoku))
            }
        }

        OutlinedButton(onClick = onEditor, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_custom_puzzle))
        }
    }
}
