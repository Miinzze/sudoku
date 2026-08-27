package com.sudokuai.app.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuai.app.data.repository.SaveResult
import com.sudokuai.app.data.repository.SudokuRepository
import com.sudokuai.app.domain.GameMapper
import com.sudokuai.core.model.Difficulty
import com.sudokuai.core.model.Grid
import com.sudokuai.core.model.GridValidator
import com.sudokuai.core.solver.BacktrackingSolver
import com.sudokuai.core.generator.DifficultyScorer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorViewModel(private val repository: SudokuRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun onCellSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedIndex = index)
    }

    fun onDigitEntered(digit: Int) {
        val index = _uiState.value.selectedIndex ?: return
        val puzzle = _uiState.value.puzzle.withSet(index, digit)
        _uiState.value = _uiState.value.copy(puzzle = puzzle, validationResult = ValidationResult.NONE)
    }

    fun onEraseSelected() {
        val index = _uiState.value.selectedIndex ?: return
        val puzzle = _uiState.value.puzzle.withSet(index, 0)
        _uiState.value = _uiState.value.copy(puzzle = puzzle, validationResult = ValidationResult.NONE)
    }

    fun onClearAll() {
        _uiState.value = _uiState.value.copy(puzzle = Grid(), validationResult = ValidationResult.NONE)
    }

    fun onValidateRequested() {
        viewModelScope.launch {
            val puzzle = _uiState.value.puzzle
            val result = withContext(Dispatchers.Default) {
                if (!GridValidator.isConsistent(puzzle)) {
                    ValidationResult.NO_SOLUTION
                } else {
                    when (BacktrackingSolver.countSolutions(puzzle, cap = 2)) {
                        0 -> ValidationResult.NO_SOLUTION
                        1 -> ValidationResult.VALID
                        else -> ValidationResult.MULTIPLE_SOLUTIONS
                    }
                }
            }
            _uiState.value = _uiState.value.copy(validationResult = result)
        }
    }

    fun onValidationDialogDismissed() {
        _uiState.value = _uiState.value.copy(validationResult = ValidationResult.NONE)
    }

    /** Saves the puzzle and requests navigation to the game screen; only valid after a VALID result. */
    fun onPlayRequested() {
        if (_uiState.value.validationResult != ValidationResult.VALID) return
        viewModelScope.launch {
            val puzzle = _uiState.value.puzzle
            val solution = withContext(Dispatchers.Default) {
                BacktrackingSolver.solve(puzzle)
            } ?: return@launch
            val difficulty = withContext(Dispatchers.Default) { DifficultyScorer.classify(puzzle) }
            val gameState = GameMapper.fromCustomPuzzle(puzzle, solution, difficulty, System.currentTimeMillis())
            when (val result = repository.save(gameState)) {
                is SaveResult.Success -> _uiState.value = _uiState.value.copy(navigateToGameId = result.id)
                SaveResult.LimitReached -> _uiState.value = _uiState.value.copy(errorMessage = "LIMIT_REACHED")
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateToGameId = null)
    }

    fun onErrorShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(private val repository: SudokuRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = EditorViewModel(repository) as T
    }
}
