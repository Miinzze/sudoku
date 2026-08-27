package com.sudokuai.app.ui.game

import com.sudokuai.app.domain.GameState
import com.sudokuai.core.model.Candidates
import com.sudokuai.core.model.Grid

data class GameUiState(
    val isLoading: Boolean = true,
    val gameId: Long = 0,
    val originalPuzzle: Grid = Grid(),
    val currentState: Grid = Grid(),
    val candidates: Candidates = Candidates(),
    val solution: Grid = Grid(),
    val difficulty: com.sudokuai.core.model.Difficulty = com.sudokuai.core.model.Difficulty.LEICHT,
    val elapsedSeconds: Long = 0,
    val isFavorite: Boolean = false,
    val isSolved: Boolean = false,
    val isSolutionRevealed: Boolean = false,
    val selectedIndex: Int? = null,
    val notesMode: Boolean = false,
    val mistakeHighlighting: Boolean = true,
    val sameNumberHighlight: Boolean = true,
    val rowColHighlight: Boolean = true,
    val autoRemoveCandidates: Boolean = true,
    val showSolveMenu: Boolean = false,
    val showSolveAllConfirm: Boolean = false,
    val showCompletionDialog: Boolean = false,
    val gameNotFound: Boolean = false,
) {
    val isInteractive: Boolean get() = !isLoading && !gameNotFound && !isSolved && !isSolutionRevealed

    companion object {
        fun fromGameState(state: GameState): GameUiState = GameUiState(
            isLoading = false,
            gameId = state.id,
            originalPuzzle = state.originalPuzzle,
            currentState = state.currentState,
            candidates = state.candidates,
            solution = state.solution,
            difficulty = state.difficulty,
            elapsedSeconds = state.elapsedSeconds,
            isFavorite = state.isFavorite,
            isSolved = state.isSolved,
            isSolutionRevealed = state.isSolutionRevealed,
        )
    }
}
