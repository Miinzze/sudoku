package com.sudokuai.core.generator

import com.sudokuai.core.model.Difficulty
import com.sudokuai.core.model.Grid
import com.sudokuai.core.solver.LogicalSolveResult
import com.sudokuai.core.solver.LogicalSolver
import com.sudokuai.core.solver.Technique

/**
 * Turns a [LogicalSolveResult] into a single numeric difficulty score.
 *
 * WHY a weighted technique sum instead of clue count: clue count alone is a poor difficulty
 * proxy — two puzzles with the same number of givens can require wildly different solving
 * skill. Instead each technique application recorded by [LogicalSolver] is weighted by how
 * advanced it is (naive singles barely register; X-Wing and outright guessing dominate), the
 * weighted counts are summed, and a small per-empty-cell baseline is added because a puzzle
 * that needs many *repetitions* of an easy technique is still a bit more tedious than one that
 * needs few. [Difficulty.fromScore] then buckets the result into the five user-facing tiers.
 * The weights are tuned by feel, not derived from a formal metric — what matters for this app
 * is that they are monotonically increasing with technique sophistication, which keeps
 * generated puzzles ordered consistently across difficulty levels (verified by unit tests).
 */
object DifficultyScorer {

    private val TECHNIQUE_WEIGHTS = mapOf(
        Technique.NAKED_SINGLE to 1,
        Technique.HIDDEN_SINGLE to 2,
        Technique.POINTING_PAIR to 8,
        Technique.BOX_LINE_REDUCTION to 8,
        Technique.NAKED_PAIR to 10,
        Technique.HIDDEN_PAIR to 14,
        Technique.NAKED_TRIPLE to 20,
        Technique.X_WING to 35,
    )

    /** Flat penalty added once logical techniques get stuck and a guess is required. */
    private const val BACKTRACKING_BASE_PENALTY = 120

    /** Additional penalty per cell still empty at the point techniques got stuck. */
    private const val BACKTRACKING_PER_EMPTY_CELL_PENALTY = 12

    /** Small per-empty-cell baseline so puzzles with more blanks score a bit higher regardless of technique mix. */
    private const val EMPTY_CELL_BASELINE_WEIGHT = 0.4

    fun score(result: LogicalSolveResult, totalEmptyCells: Int): Int {
        var score = 0.0
        for ((technique, count) in result.techniqueUsage) {
            val weight = TECHNIQUE_WEIGHTS[technique] ?: 0
            score += weight * count
        }
        if (result.requiredBacktracking) {
            score += BACKTRACKING_BASE_PENALTY
            score += BACKTRACKING_PER_EMPTY_CELL_PENALTY * result.emptyCellsWhenStuck
        }
        score += EMPTY_CELL_BASELINE_WEIGHT * totalEmptyCells
        return score.toInt()
    }

    /** Convenience: runs [LogicalSolver] on [puzzle] and scores the result in one call. */
    fun scorePuzzle(puzzle: Grid): Int {
        val result = LogicalSolver.solve(puzzle)
        return score(result, puzzle.emptyCellCount())
    }

    fun classify(puzzle: Grid): Difficulty = Difficulty.fromScore(scorePuzzle(puzzle))
}
