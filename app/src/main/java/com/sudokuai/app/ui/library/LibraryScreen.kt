package com.sudokuai.app.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudokuai.app.R
import com.sudokuai.app.data.repository.SortOrder
import com.sudokuai.app.di.ServiceLocator
import com.sudokuai.app.domain.GameState
import com.sudokuai.app.ui.components.DifficultyBadge
import com.sudokuai.app.ui.components.formatElapsed
import com.sudokuai.core.model.Difficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onOpenGame: (Long) -> Unit) {
    val context = LocalContext.current
    val viewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.Factory(ServiceLocator.provideSudokuRepository(context)),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.pendingDeleteGame?.let {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteCancelled,
            title = { Text(stringResource(R.string.library_delete_title)) },
            text = { Text(stringResource(R.string.library_delete_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onDeleteConfirmed) {
                    Text(stringResource(R.string.library_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteCancelled) {
                    Text(stringResource(R.string.game_cancel))
                }
            },
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.library_title)) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            FilterBar(
                filters = uiState.filters,
                onSortOrderSelected = viewModel::onSortOrderSelected,
                onFavoritesOnlyToggled = viewModel::onFavoritesOnlyToggled,
                onDifficultyFilterSelected = viewModel::onDifficultyFilterSelected,
            )

            if (uiState.games.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.library_empty))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.games, key = { it.id }) { game ->
                        GameListItem(
                            game = game,
                            onOpen = { onOpenGame(game.id) },
                            onToggleFavorite = { viewModel.onToggleFavorite(game) },
                            onDelete = { viewModel.onDeleteRequested(game) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterBar(
    filters: LibraryFilters,
    onSortOrderSelected: (SortOrder) -> Unit,
    onFavoritesOnlyToggled: () -> Unit,
    onDifficultyFilterSelected: (Difficulty?) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = filters.favoritesOnly,
                    onClick = onFavoritesOnlyToggled,
                    label = { Text(stringResource(R.string.library_favorites_only)) },
                )
            }
            item {
                FilterChip(
                    selected = filters.difficulty == null,
                    onClick = { onDifficultyFilterSelected(null) },
                    label = { Text(stringResource(R.string.library_filter_all)) },
                )
            }
            items(Difficulty.entries.toList()) { difficulty ->
                FilterChip(
                    selected = filters.difficulty == difficulty,
                    onClick = { onDifficultyFilterSelected(difficulty) },
                    label = { Text(difficulty.displayNameDe) },
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            val sortLabels = mapOf(
                SortOrder.NEWEST to R.string.library_sort_newest,
                SortOrder.OLDEST to R.string.library_sort_oldest,
                SortOrder.DIFFICULTY to R.string.library_sort_difficulty,
                SortOrder.PLAYTIME to R.string.library_sort_playtime,
            )
            items(sortLabels.entries.toList()) { (order, label) ->
                FilterChip(
                    selected = filters.sortOrder == order,
                    onClick = { onSortOrderSelected(order) },
                    label = { Text(stringResource(label)) },
                )
            }
        }
    }
}

@Composable
private fun GameListItem(
    game: GameState,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable(onClick = onOpen),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                DifficultyBadge(game.difficulty)
                Text(
                    text = statusLabel(game),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = formatElapsed(game.elapsedSeconds),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (game.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(R.string.common_favorite),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.common_delete))
                }
            }
        }
    }
}

@Composable
private fun statusLabel(game: GameState): String = when {
    game.isSolved -> stringResource(R.string.library_status_solved)
    game.isSolutionRevealed -> stringResource(R.string.library_status_revealed)
    game.isCustom -> stringResource(R.string.library_status_custom)
    else -> stringResource(R.string.library_status_in_progress)
}
