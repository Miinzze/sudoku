package com.sudokuai.app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuai.app.data.datastore.SettingsDataStore
import com.sudokuai.app.data.repository.SudokuRepository
import com.sudokuai.app.domain.GameMapper
import com.sudokuai.app.domain.GameState
import com.sudokuai.core.model.Grid
import com.sudokuai.core.model.GridValidator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val gameId: Long,
    private val repository: SudokuRepository,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadGame()
        observeSettings()
    }

    private fun loadGame() {
        viewModelScope.launch {
            val state = repository.getById(gameId)
            if (state == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, gameNotFound = true)
                return@launch
            }
            _uiState.value = GameUiState.fromGameState(state).copy(
                notesMode = _uiState.value.notesMode,
                mistakeHighlighting = _uiState.value.mistakeHighlighting,
                sameNumberHighlight = _uiState.value.sameNumberHighlight,
                rowColHighlight = _uiState.value.rowColHighlight,
                autoRemoveCandidates = _uiState.value.autoRemoveCandidates,
            )
            if (_uiState.value.isInteractive) startTimer()
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsDataStore.mistakeHighlighting,
                settingsDataStore.sameNumberHighlight,
                settingsDataStore.rowColHighlight,
                settingsDataStore.autoRemoveCandidates,
            ) { mistakes, sameNumber, rowCol, autoRemove ->
                Quad(mistakes, sameNumber, rowCol, autoRemove)
            }.collect { (mistakes, sameNumber, rowCol, autoRemove) ->
                _uiState.value = _uiState.value.copy(
                    mistakeHighlighting = mistakes,
                    sameNumberHighlight = sameNumber,
                    rowColHighlight = rowCol,
                    autoRemoveCandidates = autoRemove,
                )
            }
        }
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    // --- Timer -------------------------------------------------------------------------------

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(1000)
                _uiState.value = _uiState.value.copy(elapsedSeconds = _uiState.value.elapsedSeconds + 1)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun resumeTimer() {
        if (_uiState.value.isInteractive && timerJob == null && !_uiState.value.isLoading) {
            startTimer()
        }
    }

    // --- Cell selection / input ---------------------------------------------------------------

    fun onCellSelected(index: Int) {
        if (!_uiState.value.isInteractive) return
        _uiState.value = _uiState.value.copy(selectedIndex = index)
    }

    fun onToggleNotesMode() {
        _uiState.value = _uiState.value.copy(notesMode = !_uiState.value.notesMode)
    }

    fun onNumberInput(digit: Int) {
        val state = _uiState.value
        if (!state.isInteractive) return
        val index = state.selectedIndex ?: return
        if (state.originalPuzzle.get(index) != 0) return // given cells are locked

        if (state.notesMode) {
            val candidates = state.candidates.copy()
            if (candidates.has(index, digit)) candidates.remove(index, digit) else candidates.add(index, digit)
            _uiState.value = state.copy(candidates = candidates)
            persist()
            return
        }

        val current = state.currentState.withSet(index, digit)
        val candidates = state.candidates.copy()
        candidates.clear(index)
        if (state.autoRemoveCandidates) {
            val row = Grid.row(index)
            val col = Grid.col(index)
            candidates.removeFromPeers(row, col, digit)
        }

        val solvedNow = GridValidator.isSolved(current) && current == state.solution
        var newState = state.copy(
            currentState = current,
            candidates = candidates,
            isSolved = solvedNow,
            showCompletionDialog = solvedNow,
        )
        if (solvedNow) {
            pauseTimer()
        }
        _uiState.value = newState
        persist(markSolvedNow = solvedNow)
    }

    fun onEraseClick() {
        val state = _uiState.value
        if (!state.isInteractive) return
        val index = state.selectedIndex ?: return
        if (state.originalPuzzle.get(index) != 0) return

        val hasValue = state.currentState.get(index) != 0
        val current = if (hasValue) state.currentState.withSet(index, 0) else state.currentState
        val candidates = state.candidates.copy()
        if (!hasValue) candidates.clear(index)

        _uiState.value = state.copy(currentState = current, candidates = candidates)
        persist()
    }

    fun onToggleFavorite() {
        val state = _uiState.value
        _uiState.value = state.copy(isFavorite = !state.isFavorite)
        persist()
    }

    fun onCompletionDialogDismissed() {
        _uiState.value = _uiState.value.copy(showCompletionDialog = false)
    }

    // --- "Lösung" menu -------------------------------------------------------------------------

    fun onSolveMenuOpened() {
        _uiState.value = _uiState.value.copy(showSolveMenu = true)
    }

    fun onSolveMenuDismissed() {
        _uiState.value = _uiState.value.copy(showSolveMenu = false)
    }

    fun onSolveOneCellRequested() {
        val state = _uiState.value
        _uiState.value = state.copy(showSolveMenu = false)
        if (!state.isInteractive) return

        val targetIndex = state.selectedIndex?.takeIf { state.currentState.get(it) == 0 }
            ?: (0 until 81).firstOrNull { state.currentState.get(it) == 0 }
            ?: return

        val value = state.solution.get(targetIndex)
        val current = state.currentState.withSet(targetIndex, value)
        val candidates = state.candidates.copy()
        candidates.clear(targetIndex)
        if (state.autoRemoveCandidates) {
            candidates.removeFromPeers(Grid.row(targetIndex), Grid.col(targetIndex), value)
        }

        val solvedNow = GridValidator.isSolved(current) && current == state.solution
        _uiState.value = _uiState.value.copy(
            currentState = current,
            candidates = candidates,
            selectedIndex = targetIndex,
            isSolved = solvedNow,
            showCompletionDialog = solvedNow,
        )
        if (solvedNow) pauseTimer()
        persist(markSolvedNow = solvedNow)
    }

    fun onSolveAllRequested() {
        _uiState.value = _uiState.value.copy(showSolveMenu = false, showSolveAllConfirm = true)
    }

    fun onSolveAllCancelled() {
        _uiState.value = _uiState.value.copy(showSolveAllConfirm = false)
    }

    fun onSolveAllConfirmed() {
        val state = _uiState.value
        _uiState.value = state.copy(
            showSolveAllConfirm = false,
            currentState = state.solution.copy(),
            candidates = com.sudokuai.core.model.Candidates(),
            isSolutionRevealed = true,
            // Explicitly NOT setting isSolved here — a revealed solution must never count as a
            // genuine solve for statistics/achievements.
        )
        pauseTimer()
        persist()
    }

    // --- Persistence ---------------------------------------------------------------------------

    /** Fire-and-forget save on every mutating action; simplicity over batching (see spec notes). */
    private fun persist(markSolvedNow: Boolean = false) {
        viewModelScope.launch {
            saveNow(markSolvedNow)
        }
    }

    /** Called from the UI on ON_STOP to guarantee the latest state survives process death. */
    fun flushSave() {
        viewModelScope.launch { saveNow(false) }
    }

    private suspend fun saveNow(markSolvedNow: Boolean) {
        val state = _uiState.value
        if (state.isLoading || state.gameNotFound) return
        val now = System.currentTimeMillis()
        val existing = repository.getById(state.gameId) ?: return
        val updated: GameState = existing.copy(
            currentState = state.currentState,
            candidates = state.candidates,
            elapsedSeconds = state.elapsedSeconds,
            lastModifiedAt = now,
            isFavorite = state.isFavorite,
            isSolved = state.isSolved,
            isSolutionRevealed = state.isSolutionRevealed,
            solvedAt = if (markSolvedNow) now else existing.solvedAt,
        )
        repository.update(updated)
    }

    class Factory(
        private val gameId: Long,
        private val repository: SudokuRepository,
        private val settingsDataStore: SettingsDataStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GameViewModel(gameId, repository, settingsDataStore) as T
    }
}
