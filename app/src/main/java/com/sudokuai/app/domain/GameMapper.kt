package com.sudokuai.app.domain

import com.sudokuai.app.data.local.Converters
import com.sudokuai.app.data.local.SudokuGameEntity
import com.sudokuai.core.generator.GeneratedPuzzle
import com.sudokuai.core.model.Candidates
import com.sudokuai.core.model.Difficulty
import com.sudokuai.core.model.Grid

/** In-memory, `:core`-typed view of a saved game — what the UI/ViewModels actually work with. */
data class GameState(
    val id: Long = 0,
    val originalPuzzle: Grid,
    val solution: Grid,
    val currentState: Grid,
    val candidates: Candidates,
    val difficulty: Difficulty,
    val elapsedSeconds: Long = 0,
    val createdAt: Long,
    val lastModifiedAt: Long,
    val isFavorite: Boolean = false,
    val isSolved: Boolean = false,
    val isSolutionRevealed: Boolean = false,
    val isCustom: Boolean = false,
    val solvedAt: Long? = null,
)

/** Pure mapping functions between [SudokuGameEntity] (Room) and [GameState] (`:core`-typed). */
object GameMapper {

    fun toGameState(entity: SudokuGameEntity): GameState = GameState(
        id = entity.id,
        originalPuzzle = Grid.fromString(entity.originalPuzzle),
        solution = Grid.fromString(entity.solution),
        currentState = Grid.fromString(entity.currentState),
        candidates = Converters.decodeCandidates(entity.candidates),
        difficulty = Difficulty.valueOf(entity.difficulty),
        elapsedSeconds = entity.elapsedSeconds,
        createdAt = entity.createdAt,
        lastModifiedAt = entity.lastModifiedAt,
        isFavorite = entity.isFavorite,
        isSolved = entity.isSolved,
        isSolutionRevealed = entity.isSolutionRevealed,
        isCustom = entity.isCustom,
        solvedAt = entity.solvedAt,
    )

    fun toEntity(state: GameState): SudokuGameEntity = SudokuGameEntity(
        id = state.id,
        originalPuzzle = state.originalPuzzle.toStringRepr(),
        solution = state.solution.toStringRepr(),
        currentState = state.currentState.toStringRepr(),
        candidates = Converters.encodeCandidates(state.candidates),
        difficulty = state.difficulty.name,
        elapsedSeconds = state.elapsedSeconds,
        createdAt = state.createdAt,
        lastModifiedAt = state.lastModifiedAt,
        isFavorite = state.isFavorite,
        isSolved = state.isSolved,
        isSolutionRevealed = state.isSolutionRevealed,
        isCustom = state.isCustom,
        solvedAt = state.solvedAt,
    )

    /** Builds a fresh, unplayed [GameState] from a freshly generated puzzle. */
    fun fromGeneratedPuzzle(generated: GeneratedPuzzle, now: Long): GameState = GameState(
        originalPuzzle = generated.puzzle,
        solution = generated.solution,
        currentState = generated.puzzle.copy(),
        candidates = Candidates(),
        difficulty = generated.difficulty,
        elapsedSeconds = 0,
        createdAt = now,
        lastModifiedAt = now,
        isCustom = false,
    )

    /** Builds a fresh [GameState] from a custom puzzle validated by the editor. */
    fun fromCustomPuzzle(puzzle: Grid, solution: Grid, difficulty: Difficulty, now: Long): GameState = GameState(
        originalPuzzle = puzzle,
        solution = solution,
        currentState = puzzle.copy(),
        candidates = Candidates(),
        difficulty = difficulty,
        elapsedSeconds = 0,
        createdAt = now,
        lastModifiedAt = now,
        isCustom = true,
    )
}
