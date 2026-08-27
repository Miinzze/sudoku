package com.sudokuai.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GridValidatorTest {

    private val validSolved =
        "483921657967345821251876493548132976729564138136798245372689514814253769695417382"

    @Test
    fun `solved grid is consistent and complete`() {
        val grid = Grid.fromString(validSolved)
        assertTrue(GridValidator.isConsistent(grid))
        assertTrue(GridValidator.isComplete(grid))
        assertTrue(GridValidator.isSolved(grid))
    }

    @Test
    fun `duplicate in row is detected`() {
        val grid = Grid.fromString(validSolved).withSet(0, 1, 4) // row 0 already has a 4
        assertFalse(GridValidator.isConsistent(grid))
    }

    @Test
    fun `duplicate in column is detected`() {
        val grid = Grid.fromString(validSolved).withSet(1, 0, 4) // col 0 already has a 4 at (0,0)
        assertFalse(GridValidator.isConsistent(grid))
    }

    @Test
    fun `duplicate in box is detected`() {
        val grid = Grid.fromString(validSolved).withSet(1, 1, 4) // box 0 already has a 4 at (0,0)
        assertFalse(GridValidator.isConsistent(grid))
    }

    @Test
    fun `empty cells never count as duplicates`() {
        var grid = Grid.fromString(validSolved)
        grid = grid.withSet(0, 0, 0).withSet(1, 1, 0).withSet(2, 2, 0)
        assertTrue(GridValidator.isConsistent(grid))
        assertFalse(GridValidator.isComplete(grid))
    }

    @Test
    fun `peers exclude self and contain exactly 20 cells`() {
        val peers = GridValidator.peers(4, 4)
        assertEquals(20, peers.size)
        assertFalse(Grid.index(4, 4) in peers)
    }

    @Test
    fun `canPlace respects row col and box`() {
        val grid = Grid.fromString(validSolved).withSet(0, 0, 0)
        assertFalse(GridValidator.canPlace(grid, 0, 0, 9)) // 9 present elsewhere in row 0
        assertTrue(GridValidator.canPlace(grid, 0, 0, 4)) // original value always re-placeable
    }
}
