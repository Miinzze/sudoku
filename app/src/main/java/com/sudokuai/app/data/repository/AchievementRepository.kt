package com.sudokuai.app.data.repository

import com.sudokuai.app.domain.Achievement
import com.sudokuai.app.domain.AchievementEvaluator
import com.sudokuai.app.domain.AchievementInput
import com.sudokuai.app.domain.StatisticsCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId

/**
 * Like [StatisticsRepository], achievement unlock state is derived on the fly from saved games
 * rather than persisted in its own Room table or DataStore entry — there is no notion of an
 * achievement being "revoked" once its underlying condition (solved count, streak, ...) is
 * recomputed from the same source of truth, so a dedicated unlocked-timestamp table would only
 * add write paths to keep in sync for no behavioral benefit at this app's scale. If a later
 * version wants "achievement unlocked on <date>" notifications, that is the natural point to add
 * a small `AchievementUnlockEntity` table instead of changing this evaluation logic.
 */
class AchievementRepository(
    private val sudokuRepository: SudokuRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun observeAchievements(): Flow<List<Achievement>> =
        sudokuRepository.observeAll().map { games ->
            val solved = games.filter { it.isSolved }
            val stats = StatisticsCalculator.compute(games, zoneId)
            val input = AchievementInput(
                solvedCount = solved.size,
                monsterSolvedCount = stats.monsterSolvedCount,
                fastestSolveSeconds = stats.bestTimeSeconds,
                hasCreatedCustomPuzzle = games.any { it.isCustom },
                savedGameCount = games.size,
                longestStreakDays = stats.longestStreakDays,
            )
            AchievementEvaluator.evaluate(input)
        }
}
