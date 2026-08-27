package com.sudokuai.core.solver

import com.sudokuai.core.model.CELL_COUNT
import com.sudokuai.core.model.Grid
import com.sudokuai.core.model.GridValidator

/**
 * Raw backtracking solver used for solvability checks and solution counting (not for
 * human-style difficulty scoring — see [com.sudokuai.core.solver.LogicalSolver] for that).
 *
 * Uses minimum-remaining-values (MRV) cell selection: it always branches on the empty cell
 * with the fewest legal candidates. This prunes the search tree dramatically compared to
 * naive row-major scanning and keeps even hard 17-clue puzzles solvable in well under a
 * second on typical hardware.
 */
object BacktrackingSolver {

    /** Finds a single solution, or null if the puzzle has no solution. Does not mutate [grid]. */
    fun solve(grid: Grid): Grid? {
        val working = grid.copy()
        return if (solveInternal(working)) working else null
    }

    /**
     * Counts distinct solutions, stopping early once [cap] is reached. Does not mutate [grid].
     * A puzzle has a unique solution iff `countSolutions(grid, 2) == 1`.
     */
    fun countSolutions(grid: Grid, cap: Int = 2): Int {
        val working = grid.copy()
        val counter = intArrayOf(0)
        countInternal(working, cap, counter)
        return counter[0]
    }

    fun hasUniqueSolution(grid: Grid): Boolean = countSolutions(grid, 2) == 1

    private fun solveInternal(grid: Grid): Boolean {
        val next = findMrvCell(grid) ?: return true // no empty cells left => solved
        val (index, candidates) = next
        if (candidates.isEmpty()) return false
        for (v in candidates) {
            grid.set(index, v)
            if (solveInternal(grid)) return true
        }
        grid.set(index, 0)
        return false
    }

    private fun countInternal(grid: Grid, cap: Int, counter: IntArray) {
        if (counter[0] >= cap) return
        val next = findMrvCell(grid)
        if (next == null) {
            counter[0]++
            return
        }
        val (index, candidates) = next
        if (candidates.isEmpty()) return
        for (v in candidates) {
            grid.set(index, v)
            countInternal(grid, cap, counter)
            if (counter[0] >= cap) {
                grid.set(index, 0)
                return
            }
        }
        grid.set(index, 0)
    }

    /** Returns the empty cell index with fewest legal candidates, plus that candidate list. */
    private fun findMrvCell(grid: Grid): Pair<Int, List<Int>>? {
        var bestIndex = -1
        var bestCandidates: List<Int>? = null
        var bestCount = 10
        for (i in 0 until CELL_COUNT) {
            if (grid.get(i) != 0) continue
            val row = Grid.row(i)
            val col = Grid.col(i)
            val candidates = ArrayList<Int>(9)
            for (v in 1..9) if (GridValidator.canPlace(grid, row, col, v)) candidates.add(v)
            if (candidates.size < bestCount) {
                bestCount = candidates.size
                bestIndex = i
                bestCandidates = candidates
                if (bestCount == 0) return i to emptyList()
                if (bestCount == 1) break
            }
        }
        return if (bestIndex == -1) null else bestIndex to bestCandidates!!
    }
}
