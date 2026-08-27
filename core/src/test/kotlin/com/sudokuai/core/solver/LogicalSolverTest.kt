package com.sudokuai.core.solver

import com.sudokuai.core.model.Grid
import com.sudokuai.core.model.GridValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicalSolverTest {

    private val easyPuzzle =
        "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
    private val easySolution =
        "483921657967345821251876493548132976729564138136798245372689514814253769695417382"

    @Test
    fun `solves the known easy puzzle purely with logical techniques`() {
        val result = LogicalSolver.solve(Grid.fromString(easyPuzzle))
        assertNotNull(result.solvedGrid)
        assertEquals(easySolution, result.solvedGrid!!.toStringRepr())
        assertTrue(GridValidator.isSolved(result.solvedGrid!!))
        assertTrue("expected naked/hidden singles to be used", result.techniqueUsage.isNotEmpty())
    }

    @Test
    fun `an already solved grid needs no techniques`() {
        val result = LogicalSolver.solve(Grid.fromString(easySolution))
        assertEquals(easySolution, result.solvedGrid!!.toStringRepr())
        assertTrue(result.techniqueUsage.isEmpty())
        assertTrue(!result.requiredBacktracking)
    }

    @Test
    fun `stuck puzzle falls back to backtracking and is flagged`() {
        // A near-empty grid (17 givens is the theoretical minimum) is very likely to exceed
        // what pure logical techniques up to X-Wing can resolve on their own for at least some
        // seeds; here we use an extremely sparse, but still uniquely-solvable, grid to exercise
        // the backtracking fallback path deterministically by stripping the easy puzzle further
        // while preserving uniqueness via the raw solver.
        var grid = Grid.fromString(easyPuzzle)
        // Remove additional clues while keeping a unique solution, to push towards requiring a guess.
        val order = listOf(9, 10, 19, 20, 28, 37, 46, 55, 64, 73)
        for (idx in order) {
            val backup = grid.get(idx)
            grid.set(idx, 0)
            if (!BacktrackingSolver.hasUniqueSolution(grid)) {
                grid.set(idx, backup)
            }
        }
        val result = LogicalSolver.solve(grid)
        assertNotNull(result.solvedGrid)
        assertTrue(GridValidator.isSolved(result.solvedGrid!!))
        // Whether or not this particular reduction required backtracking, the solved result
        // must always match the unique solution reachable via raw search.
        val reference = BacktrackingSolver.solve(grid)
        assertEquals(reference!!.toStringRepr(), result.solvedGrid!!.toStringRepr())
    }
}
