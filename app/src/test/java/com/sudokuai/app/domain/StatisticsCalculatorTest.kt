package com.sudokuai.app.domain

import com.sudokuai.core.model.Candidates
import com.sudokuai.core.model.Difficulty
import com.sudokuai.core.model.Grid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class StatisticsCalculatorTest {

    private fun game(
        difficulty: Difficulty = Difficulty.LEICHT,
        solved: Boolean = true,
        elapsed: Long = 100,
        solvedAt: Long? = null,
    ) = GameState(
        originalPuzzle = Grid(),
        solution = Grid(),
        currentState = Grid(),
        candidates = Candidates(),
        difficulty = difficulty,
        elapsedSeconds = elapsed,
        createdAt = 0,
        lastModifiedAt = solvedAt ?: 0,
        isSolved = solved,
        solvedAt = solvedAt,
    )

    @Test
    fun `unsolved games count toward played but not solved`() {
        val stats = StatisticsCalculator.compute(listOf(game(solved = false), game(solved = false)))
        assertEquals(2, stats.playedCount)
        assertEquals(0, stats.solvedCount)
        assertNull(stats.bestTimeSeconds)
    }

    @Test
    fun `best and average time only consider solved games`() {
        val games = listOf(
            game(solved = true, elapsed = 300),
            game(solved = true, elapsed = 100),
            game(solved = false, elapsed = 1),
        )
        val stats = StatisticsCalculator.compute(games)
        assertEquals(100L, stats.bestTimeSeconds)
        assertEquals(200L, stats.averageTimeSeconds)
    }

    @Test
    fun `monster solved count only counts monster difficulty`() {
        val games = listOf(
            game(difficulty = Difficulty.MONSTER, solved = true),
            game(difficulty = Difficulty.LEICHT, solved = true),
        )
        assertEquals(1, StatisticsCalculator.compute(games).monsterSolvedCount)
    }

    @Test
    fun `streak counts consecutive calendar days only`() {
        val zone = ZoneOffset.UTC
        fun epochAt(day: Int) = ZonedDateTime.of(2026, 1, day, 12, 0, 0, 0, zone).toInstant().toEpochMilli()

        val games = listOf(
            game(solvedAt = epochAt(1)),
            game(solvedAt = epochAt(2)),
            game(solvedAt = epochAt(3)),
            // gap on day 4
            game(solvedAt = epochAt(5)),
        )
        assertEquals(3, StatisticsCalculator.longestStreak(games, zone))
    }

    @Test
    fun `same-day solves do not inflate the streak`() {
        val zone = ZoneOffset.UTC
        fun epochAt(hour: Int) = ZonedDateTime.of(2026, 1, 1, hour, 0, 0, 0, zone).toInstant().toEpochMilli()

        val games = listOf(game(solvedAt = epochAt(1)), game(solvedAt = epochAt(20)))
        assertEquals(1, StatisticsCalculator.longestStreak(games, zone))
    }
}
