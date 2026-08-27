package com.sudokuai.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One saved (or in-progress) Sudoku game. Board contents are stored as plain 81-character
 * strings matching [com.sudokuai.core.model.Grid.toStringRepr]/[com.sudokuai.core.model.Grid.fromString]
 * so the mapping to/from `:core` types is a one-line call (see `data.repository.GameMapper`).
 */
@Entity(tableName = "sudoku_games")
data class SudokuGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 81-char string, '0' for empty cells — the puzzle as originally generated/created. */
    val originalPuzzle: String,
    /** 81-char string, fully solved reference solution. */
    val solution: String,
    /** 81-char string, the player's current board state (givens + entered digits). */
    val currentState: String,
    /** Comma-joined 81 ints, each a 9-bit candidate mask (0-511) for the corresponding cell. */
    val candidates: String,
    /** Name of a [com.sudokuai.core.model.Difficulty] enum constant. */
    val difficulty: String,
    val elapsedSeconds: Long = 0,
    val createdAt: Long,
    val lastModifiedAt: Long,
    val isFavorite: Boolean = false,
    /** True only once the player has genuinely filled in the correct, complete solution. */
    val isSolved: Boolean = false,
    val isSolutionRevealed: Boolean = false,
    val isCustom: Boolean = false,
    /** Epoch millis of the moment [isSolved] first became true; null until then. Used for the
     *  daily-streak statistic, which must be based on genuine solves rather than last-modified. */
    val solvedAt: Long? = null,
)
