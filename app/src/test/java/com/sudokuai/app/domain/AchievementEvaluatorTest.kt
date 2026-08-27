package com.sudokuai.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEvaluatorTest {

    private val zero = AchievementInput(
        solvedCount = 0,
        monsterSolvedCount = 0,
        fastestSolveSeconds = null,
        hasCreatedCustomPuzzle = false,
        savedGameCount = 0,
        longestStreakDays = 0,
    )

    @Test
    fun `first sudoku unlocks at exactly one solve`() {
        assertFalse(AchievementEvaluator.isUnlocked(AchievementId.FIRST_SUDOKU, zero))
        assertTrue(AchievementEvaluator.isUnlocked(AchievementId.FIRST_SUDOKU, zero.copy(solvedCount = 1)))
    }

    @Test
    fun `solved count thresholds are inclusive boundaries`() {
        assertFalse(AchievementEvaluator.isUnlocked(AchievementId.SOLVED_10, zero.copy(solvedCount = 9)))
        assertTrue(AchievementEvaluator.isUnlocked(AchievementId.SOLVED_10, zero.copy(solvedCount = 10)))
        assertFalse(AchievementEvaluator.isUnlocked(AchievementId.SOLVED_50, zero.copy(solvedCount = 49)))
        assertTrue(AchievementEvaluator.isUnlocked(AchievementId.SOLVED_100, zero.copy(solvedCount = 100)))
    }

    @Test
    fun `monster achievements track monster-specific count, not total`() {
        val input = zero.copy(solvedCount = 100, monsterSolvedCount = 0)
        assertFalse(AchievementEvaluator.isUnlocked(AchievementId.FIRST_MONSTER, input))
        assertTrue(AchievementEvaluator.isUnlocked(AchievementId.FIRST_MONSTER, input.copy(monsterSolvedCount = 1)))
        assertTrue(AchievementEvaluator.isUnlocked(AchievementId.MONSTER_10, input.copy(monsterSolvedCount = 10)))
    }

    @Test
    fun `fast solve requires strictly under the threshold`() {
        assertFalse(AchievementEvaluator.isUnlocked(AchievementId.FAST_SOLVE, zero.copy(fastestSolveSeconds = 180)))
        assertTrue(AchievementEvaluator.isUnlocked(AchievementId.FAST_SOLVE, zero.copy(fastestSolveSeconds = 179)))
        assertFalse(AchievementEvaluator.isUnlocked(AchievementId.FAST_SOLVE, zero.copy(fastestSolveSeconds = null)))
    }

    @Test
    fun `collector and streak and custom-creator thresholds`() {
        assertTrue(AchievementEvaluator.isUnlocked(AchievementId.COLLECTOR, zero.copy(savedGameCount = 20)))
        assertFalse(AchievementEvaluator.isUnlocked(AchievementId.COLLECTOR, zero.copy(savedGameCount = 19)))
        assertTrue(AchievementEvaluator.isUnlocked(AchievementId.STREAK_7, zero.copy(longestStreakDays = 7)))
        assertTrue(AchievementEvaluator.isUnlocked(AchievementId.CUSTOM_CREATOR, zero.copy(hasCreatedCustomPuzzle = true)))
    }
}
