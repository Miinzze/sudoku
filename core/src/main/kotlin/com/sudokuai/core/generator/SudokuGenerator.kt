package com.sudokuai.core.generator

import com.sudokuai.core.model.CELL_COUNT
import com.sudokuai.core.model.Difficulty
import com.sudokuai.core.model.Grid
import com.sudokuai.core.model.GridValidator
import com.sudokuai.core.solver.BacktrackingSolver
import kotlin.random.Random

/** A freshly generated puzzle together with its unique solution and computed difficulty. */
data class GeneratedPuzzle(
    val puzzle: Grid,
    val solution: Grid,
    val difficulty: Difficulty,
    val score: Int,
)

/**
 * Generates genuinely unique-solution Sudoku puzzles targeted at a requested [Difficulty],
 * using technique-based scoring (via [DifficultyScorer]) rather than clue count.
 *
 * Algorithm:
 * 1. Build a complete, valid solved grid via randomized backtracking.
 * 2. Remove clues one at a time in random order. After each removal, verify the puzzle still
 *    has exactly one solution ([BacktrackingSolver.countSolutions] capped at 2) — if removing
 *    a cell breaks uniqueness, the value is restored and a different cell is tried.
 * 3. Once enough cells are removed to form a real puzzle, the running puzzle is re-scored with
 *    [DifficultyScorer] after every successful removal. Removal stops as soon as the score
 *    lands in the target difficulty's bucket. A removal that would overshoot past the target
 *    bucket (make the puzzle harder than requested) is rejected and a different cell is tried
 *    instead, so difficulty increases gradually rather than in one unlucky jump.
 * 4. If a full pass over all cells still leaves the puzzle easier than requested (can happen
 *    for MONSTER, the open-ended top bucket), the whole attempt is retried with a fresh solved
 *    grid, up to [maxAttempts] times; the closest attempt found is returned as a fallback so
 *    generation always terminates.
 */
object SudokuGenerator {

    private const val MIN_EMPTY_CELLS_BEFORE_SCORING = 24

    fun generateFullSolution(random: Random = Random.Default): Grid {
        val grid = Grid()
        val filled = fillRandomly(grid, random)
        check(filled) { "Failed to generate a full solved grid" }
        return grid
    }

    fun generatePuzzle(
        difficulty: Difficulty,
        random: Random = Random.Default,
        maxAttempts: Int = 60,
    ): GeneratedPuzzle {
        var best: GeneratedPuzzle? = null

        repeat(maxAttempts) {
            val solution = generateFullSolution(random)
            val attempt = reduceToDifficulty(solution, difficulty, random)
            if (attempt.difficulty == difficulty) return attempt
            // Keep the closest-scoring attempt in case every attempt overshoots/undershoots.
            if (best == null || scoreDistance(attempt.difficulty, difficulty) < scoreDistance(best!!.difficulty, difficulty)) {
                best = attempt
            }
        }
        return best ?: error("Failed to generate a puzzle for difficulty $difficulty")
    }

    private fun scoreDistance(a: Difficulty, b: Difficulty): Int = kotlin.math.abs(a.ordinal - b.ordinal)

    private fun reduceToDifficulty(solution: Grid, target: Difficulty, random: Random): GeneratedPuzzle {
        val puzzle = solution.copy()
        val order = (0 until CELL_COUNT).shuffled(random)

        for (index in order) {
            if (puzzle.get(index) == 0) continue
            val backup = puzzle.get(index)
            puzzle.set(index, 0)

            if (!BacktrackingSolver.hasUniqueSolution(puzzle)) {
                puzzle.set(index, backup)
                continue
            }

            if (puzzle.emptyCellCount() < MIN_EMPTY_CELLS_BEFORE_SCORING) {
                continue // keep removing without scoring yet; not a "real" puzzle at this size
            }

            val currentDifficulty = DifficultyScorer.classify(puzzle)
            if (currentDifficulty.ordinal > target.ordinal) {
                // This removal overshot past the requested difficulty; try a different cell.
                puzzle.set(index, backup)
                continue
            }
            if (currentDifficulty == target) {
                val score = DifficultyScorer.scorePuzzle(puzzle)
                return GeneratedPuzzle(puzzle.copy(), solution.copy(), currentDifficulty, score)
            }
            // Still easier than target: keep the removal and continue.
        }

        val finalDifficulty = DifficultyScorer.classify(puzzle)
        val finalScore = DifficultyScorer.scorePuzzle(puzzle)
        return GeneratedPuzzle(puzzle.copy(), solution.copy(), finalDifficulty, finalScore)
    }

    /** Randomized backtracking fill: tries digits 1-9 in shuffled order at each empty cell. */
    private fun fillRandomly(grid: Grid, random: Random): Boolean {
        var emptyIndex = -1
        for (i in 0 until CELL_COUNT) {
            if (grid.get(i) == 0) {
                emptyIndex = i
                break
            }
        }
        if (emptyIndex == -1) return true // fully filled

        val row = Grid.row(emptyIndex)
        val col = Grid.col(emptyIndex)
        val digits = (1..9).shuffled(random)
        for (v in digits) {
            if (GridValidator.canPlace(grid, row, col, v)) {
                grid.set(emptyIndex, v)
                if (fillRandomly(grid, random)) return true
                grid.set(emptyIndex, 0)
            }
        }
        return false
    }
}
