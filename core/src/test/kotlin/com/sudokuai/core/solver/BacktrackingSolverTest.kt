package com.sudokuai.core.solver

import com.sudokuai.core.model.Grid
import com.sudokuai.core.model.GridValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class BacktrackingSolverTest {

    // Classic example puzzle (Norvig "grid1") with a known unique solution.
    private val easyPuzzle =
        "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
    private val easySolution =
        "483921657967345821251876493548132976729564138136798245372689514814253769695417382"

    // Arto Inkala's widely cited "world's hardest sudoku" — near-worst-case for backtracking.
    private val hardPuzzle =
        "800000000003600000070090200050007000000045700000100030001000068008500010090000400"

    @Test
    fun `solve finds the known unique solution`() {
        val grid = Grid.fromString(easyPuzzle)
        val solved = BacktrackingSolver.solve(grid)
        assertNotNull(solved)
        assertEquals(easySolution, solved!!.toStringRepr())
        assertTrue(GridValidator.isSolved(solved))
    }

    @Test
    fun `solve returns null for an unsolvable grid`() {
        // Two 5s in the same row makes the puzzle inconsistent and unsolvable.
        val grid = Grid.fromString(easyPuzzle).withSet(0, 0, 5).withSet(0, 1, 5)
        assertNull(BacktrackingSolver.solve(grid))
    }

    @Test
    fun `countSolutions caps at 2 and reports uniqueness correctly`() {
        val unique = Grid.fromString(easyPuzzle)
        assertEquals(1, BacktrackingSolver.countSolutions(unique, cap = 2))
        assertTrue(BacktrackingSolver.hasUniqueSolution(unique))

        // An empty grid has a huge number of solutions; counting must stop at the cap.
        val empty = Grid()
        assertEquals(2, BacktrackingSolver.countSolutions(empty, cap = 2))
    }

    @Test
    fun `removing a clue from a minimal puzzle can break uniqueness`() {
        // Blank out an extra cell from the easy puzzle's already-sparse givens; a grid with too
        // few clues generally admits multiple solutions.
        var grid = Grid.fromString(easyPuzzle)
        // Clear every clue from the last two rows entirely to guarantee multiple completions.
        for (col in 0 until 9) {
            grid = grid.withSet(7, col, 0).withSet(8, col, 0)
        }
        assertTrue(BacktrackingSolver.countSolutions(grid, cap = 2) >= 2)
    }

    @Test
    fun `hard puzzle solves within a reasonable time budget`() {
        val grid = Grid.fromString(hardPuzzle)
        var solved: Grid? = null
        val elapsed = measureTimeMillis {
            solved = BacktrackingSolver.solve(grid)
        }
        assertNotNull(solved)
        assertTrue(GridValidator.isSolved(solved!!))
        assertTrue("Hard puzzle took too long: ${elapsed}ms", elapsed < 5000)
    }
}
