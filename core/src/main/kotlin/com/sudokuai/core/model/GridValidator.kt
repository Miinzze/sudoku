package com.sudokuai.core.model

/**
 * Pure structural validation and neighborhood queries for a [Grid]. No solving logic here —
 * just row/column/box consistency checks and peer relationships shared by solver and generator.
 */
object GridValidator {

    /** True if no duplicate non-zero value exists in any row, column, or box. */
    fun isConsistent(grid: Grid): Boolean {
        for (unit in 0 until GRID_SIZE) {
            if (hasDuplicate(rowValues(grid, unit))) return false
            if (hasDuplicate(colValues(grid, unit))) return false
            if (hasDuplicate(boxValues(grid, unit))) return false
        }
        return true
    }

    fun isComplete(grid: Grid): Boolean = grid.emptyCellCount() == 0

    fun isSolved(grid: Grid): Boolean = isComplete(grid) && isConsistent(grid)

    private fun hasDuplicate(values: List<Int>): Boolean {
        val seen = BooleanArray(10)
        for (v in values) {
            if (v == 0) continue
            if (seen[v]) return true
            seen[v] = true
        }
        return false
    }

    fun rowValues(grid: Grid, row: Int): List<Int> = (0 until GRID_SIZE).map { grid.get(row, it) }
    fun colValues(grid: Grid, col: Int): List<Int> = (0 until GRID_SIZE).map { grid.get(it, col) }

    fun boxValues(grid: Grid, box: Int): List<Int> {
        val boxRow = (box / BOX_SIZE) * BOX_SIZE
        val boxCol = (box % BOX_SIZE) * BOX_SIZE
        val result = ArrayList<Int>(9)
        for (r in boxRow until boxRow + BOX_SIZE) {
            for (c in boxCol until boxCol + BOX_SIZE) {
                result.add(grid.get(r, c))
            }
        }
        return result
    }

    fun rowIndices(row: Int): List<Int> = (0 until GRID_SIZE).map { Grid.index(row, it) }
    fun colIndices(col: Int): List<Int> = (0 until GRID_SIZE).map { Grid.index(it, col) }
    fun boxIndices(box: Int): List<Int> {
        val boxRow = (box / BOX_SIZE) * BOX_SIZE
        val boxCol = (box % BOX_SIZE) * BOX_SIZE
        val result = ArrayList<Int>(9)
        for (r in boxRow until boxRow + BOX_SIZE) {
            for (c in boxCol until boxCol + BOX_SIZE) {
                result.add(Grid.index(r, c))
            }
        }
        return result
    }

    /** Cell indices sharing a row, column, or box with (row, col), excluding itself. */
    fun peers(row: Int, col: Int): Set<Int> {
        val result = LinkedHashSet<Int>()
        for (c in 0 until GRID_SIZE) if (c != col) result.add(Grid.index(row, c))
        for (r in 0 until GRID_SIZE) if (r != row) result.add(Grid.index(r, col))
        val boxRow = (row / BOX_SIZE) * BOX_SIZE
        val boxCol = (col / BOX_SIZE) * BOX_SIZE
        for (r in boxRow until boxRow + BOX_SIZE) {
            for (c in boxCol until boxCol + BOX_SIZE) {
                if (r != row || c != col) result.add(Grid.index(r, c))
            }
        }
        return result
    }

    fun canPlace(grid: Grid, row: Int, col: Int, value: Int): Boolean {
        if (value == 0) return true
        for (i in 0 until GRID_SIZE) {
            if (i != col && grid.get(row, i) == value) return false
            if (i != row && grid.get(i, col) == value) return false
        }
        val boxRow = (row / BOX_SIZE) * BOX_SIZE
        val boxCol = (col / BOX_SIZE) * BOX_SIZE
        for (r in boxRow until boxRow + BOX_SIZE) {
            for (c in boxCol until boxCol + BOX_SIZE) {
                if ((r != row || c != col) && grid.get(r, c) == value) return false
            }
        }
        return true
    }
}
