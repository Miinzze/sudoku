package com.sudokuai.app.data

import com.sudokuai.app.domain.GameMapper
import com.sudokuai.app.domain.GameState
import com.sudokuai.core.model.Candidates
import com.sudokuai.core.model.Difficulty
import com.sudokuai.core.model.Grid
import org.junit.Assert.assertEquals
import org.junit.Test

class GameMapperTest {

    private val puzzleString = "0".repeat(81)

    @Test
    fun `entity round trip preserves grids, difficulty and flags`() {
        val candidates = Candidates()
        candidates.add(3, 7)

        val state = GameState(
            id = 42,
            originalPuzzle = Grid.fromString(puzzleString),
            solution = Grid.fromString("1".repeat(81)),
            currentState = Grid.fromString(puzzleString),
            candidates = candidates,
            difficulty = Difficulty.SCHWER,
            elapsedSeconds = 123,
            createdAt = 1000L,
            lastModifiedAt = 2000L,
            isFavorite = true,
            isSolved = false,
            isSolutionRevealed = false,
            isCustom = true,
            solvedAt = null,
        )

        val entity = GameMapper.toEntity(state)
        val roundTripped = GameMapper.toGameState(entity)

        assertEquals(state, roundTripped)
        assertEquals("SCHWER", entity.difficulty)
    }
}
