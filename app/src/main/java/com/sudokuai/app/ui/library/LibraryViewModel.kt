package com.sudokuai.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuai.app.data.repository.SortOrder
import com.sudokuai.app.data.repository.SudokuRepository
import com.sudokuai.app.domain.GameState
import com.sudokuai.core.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryFilters(
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val favoritesOnly: Boolean = false,
    val difficulty: Difficulty? = null,
)

data class LibraryUiState(
    val games: List<GameState> = emptyList(),
    val filters: LibraryFilters = LibraryFilters(),
    val pendingDeleteGame: GameState? = null,
)

class LibraryViewModel(private val repository: SudokuRepository) : ViewModel() {

    private val filters = MutableStateFlow(LibraryFilters())
    private val pendingDelete = MutableStateFlow<GameState?>(null)

    private val gamesFlow = filters
        .flatMapLatest { repository.observeAll(it.sortOrder) }

    val uiState: StateFlow<LibraryUiState> = combine(gamesFlow, filters, pendingDelete) { games, f, pending ->
        val filtered = games
            .filter { !f.favoritesOnly || it.isFavorite }
            .filter { f.difficulty == null || it.difficulty == f.difficulty }
        LibraryUiState(games = filtered, filters = f, pendingDeleteGame = pending)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    fun onSortOrderSelected(sortOrder: SortOrder) {
        filters.value = filters.value.copy(sortOrder = sortOrder)
    }

    fun onFavoritesOnlyToggled() {
        filters.value = filters.value.copy(favoritesOnly = !filters.value.favoritesOnly)
    }

    fun onDifficultyFilterSelected(difficulty: Difficulty?) {
        filters.value = filters.value.copy(difficulty = difficulty)
    }

    fun onToggleFavorite(game: GameState) {
        viewModelScope.launch { repository.setFavorite(game, !game.isFavorite) }
    }

    fun onDeleteRequested(game: GameState) {
        pendingDelete.value = game
    }

    fun onDeleteCancelled() {
        pendingDelete.value = null
    }

    fun onDeleteConfirmed() {
        val game = pendingDelete.value ?: return
        viewModelScope.launch { repository.delete(game) }
        pendingDelete.value = null
    }

    class Factory(private val repository: SudokuRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LibraryViewModel(repository) as T
    }
}
