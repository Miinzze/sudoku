package com.sudokuai.app.data

import com.sudokuai.app.data.local.Converters
import com.sudokuai.core.model.CELL_COUNT
import com.sudokuai.core.model.Candidates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    @Test
    fun `round trip preserves all masks`() {
        val candidates = Candidates()
        candidates.add(0, 1)
        candidates.add(0, 5)
        candidates.add(40, 9)
        candidates.setMask(80, Candidates.FULL_MASK)

        val encoded = Converters.encodeCandidates(candidates)
        val decoded = Converters.decodeCandidates(encoded)

        assertEquals(candidates, decoded)
    }

    @Test
    fun `empty candidates encode to all zeros`() {
        val encoded = Converters.emptyEncodedCandidates()
        val parts = encoded.split(",")
        assertEquals(CELL_COUNT, parts.size)
        assertTrue(parts.all { it == "0" })
    }

    @Test
    fun `decoding blank string yields empty candidates`() {
        val decoded = Converters.decodeCandidates("")
        assertEquals(Candidates(), decoded)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decoding wrong length throws`() {
        Converters.decodeCandidates("0,0,0")
    }
}
