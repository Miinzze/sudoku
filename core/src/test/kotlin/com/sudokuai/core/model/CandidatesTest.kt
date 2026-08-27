package com.sudokuai.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CandidatesTest {

    @Test
    fun `add has and remove roundtrip`() {
        val c = Candidates()
        assertFalse(c.has(0, 5))
        c.add(0, 5)
        assertTrue(c.has(0, 5))
        c.remove(0, 5)
        assertFalse(c.has(0, 5))
    }

    @Test
    fun `values and count reflect mask contents`() {
        val c = Candidates()
        c.add(10, 1)
        c.add(10, 3)
        c.add(10, 9)
        assertEquals(listOf(1, 3, 9), c.values(10))
        assertEquals(3, c.count(10))
    }

    @Test
    fun `computeAll only fills empty cells with legal values`() {
        val grid = Grid()
        grid.set(0, 0, 5) // filled cell
        val candidates = Candidates.computeAll(grid)
        assertEquals(0, candidates.count(0)) // filled cell has no candidates recorded
        // Every other cell in row 0 must not offer 5 as a candidate.
        for (col in 1 until GRID_SIZE) {
            assertFalse(candidates.has(0, col, 5))
        }
        // An unrelated cell should have all 9 candidates on an otherwise-empty grid,
        // except where it shares a unit with the filled cell.
        assertEquals(9, candidates.count(Grid.index(5, 5)))
    }

    @Test
    fun `removeFromPeers strips value from row column and box peers only`() {
        val grid = Grid()
        val candidates = Candidates.computeAll(grid)
        val changed = candidates.removeFromPeers(4, 4, 7)
        assertEquals(20, changed.size)
        for (peer in GridValidator.peers(4, 4)) {
            assertFalse(candidates.has(peer, 7))
        }
        // A cell outside row/col/box 4,4 keeps the candidate.
        assertTrue(candidates.has(0, 0, 7))
    }

    @Test
    fun `placing a final number removes it from candidates in same row column and box`() {
        val grid = Grid()
        val candidates = Candidates.computeAll(grid)
        // Simulate placing the digit 3 at (2, 2).
        grid.set(2, 2, 3)
        candidates.clear(Grid.index(2, 2))
        candidates.removeFromPeers(2, 2, 3)

        assertFalse(candidates.has(2, 5, 3)) // same row
        assertFalse(candidates.has(5, 2, 3)) // same column
        assertFalse(candidates.has(0, 0, 3)) // same box
        assertTrue(candidates.has(5, 5, 3)) // unrelated cell keeps the candidate
    }
}
