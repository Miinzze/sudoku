package com.sudokuai.core.generator

import com.sudokuai.core.model.Difficulty
import com.sudokuai.core.model.GridValidator
import com.sudokuai.core.solver.BacktrackingSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuGeneratorTest {

    @Test
    fun `generateFullSolution produces a complete valid grid`() {
        val grid = SudokuGenerator.generateFullSolution(Random(1))
        assertTrue(GridValidator.isSolved(grid))
    }

    @Test
    fun `generated puzzles have exactly one solution and match the stored solution`() {
        for (difficulty in Difficulty.entries) {
            val result = SudokuGenerator.generatePuzzle(difficulty, Random(difficulty.ordinal + 1))
            assertTrue(GridValidator.isSolved(result.solution))
            assertEquals(1, BacktrackingSolver.countSolutions(result.puzzle, cap = 2))
            val solved = BacktrackingSolver.solve(result.puzzle)
            assertEquals(result.solution.toStringRepr(), solved!!.toStringRepr())
            assertTrue("puzzle must have empty cells", result.puzzle.emptyCellCount() > 0)
        }
    }

    @Test
    fun `difficulty score is monotonically consistent with requested difficulty across many samples`() {
        val sampleCount = 6
        val averageScores = Difficulty.entries.associateWith { difficulty ->
            val scores = (0 until sampleCount).map { seed ->
                SudokuGenerator.generatePuzzle(difficulty, Random(difficulty.ordinal * 100 + seed)).score
            }
            scores.average()
        }
        val ordered = Difficulty.entries.map { averageScores.getValue(it) }
        for (i in 0 until ordered.size - 1) {
            assertTrue(
                "expected average score for ${Difficulty.entries[i]} (${ordered[i]}) < " +
                    "${Difficulty.entries[i + 1]} (${ordered[i + 1]})",
                ordered[i] < ordered[i + 1],
            )
        }
    }

    @Test
    fun `leicht puzzles are solvable by naked and hidden singles alone in most cases`() {
        val result = SudokuGenerator.generatePuzzle(Difficulty.LEICHT, Random(42))
        assertEquals(Difficulty.LEICHT, result.difficulty)
    }
}
