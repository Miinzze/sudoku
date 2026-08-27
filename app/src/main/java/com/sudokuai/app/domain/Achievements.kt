package com.sudokuai.app.domain

enum class AchievementId {
    FIRST_SUDOKU,
    SOLVED_10,
    SOLVED_50,
    SOLVED_100,
    FIRST_MONSTER,
    MONSTER_10,
    FAST_SOLVE,
    CUSTOM_CREATOR,
    COLLECTOR,
    STREAK_7,
}

data class Achievement(val id: AchievementId, val unlocked: Boolean)

/** Everything an achievement-unlock check needs to know, gathered once from the repositories. */
data class AchievementInput(
    val solvedCount: Int,
    val monsterSolvedCount: Int,
    val fastestSolveSeconds: Long?,
    val hasCreatedCustomPuzzle: Boolean,
    val savedGameCount: Int,
    val longestStreakDays: Int,
)

/** Threshold, in seconds, under which a solve counts for the "Schnelle Lösung" achievement. */
const val FAST_SOLVE_THRESHOLD_SECONDS = 180L

/** Minimum saved games for the "Sammler" achievement. */
const val COLLECTOR_THRESHOLD = 20

/**
 * Pure unlock-condition evaluation — every function here takes plain data in and returns a
 * boolean, with no Room/DataStore/Android dependency, so unlock logic is directly unit-testable.
 * Unlock state is derived on the fly from [StatisticsCalculator] output + repository counts
 * rather than persisted separately; see the class doc on `AchievementsUseCase` for why.
 */
object AchievementEvaluator {

    fun evaluate(input: AchievementInput): List<Achievement> = AchievementId.entries.map {
        Achievement(it, isUnlocked(it, input))
    }

    fun isUnlocked(id: AchievementId, input: AchievementInput): Boolean = when (id) {
        AchievementId.FIRST_SUDOKU -> input.solvedCount >= 1
        AchievementId.SOLVED_10 -> input.solvedCount >= 10
        AchievementId.SOLVED_50 -> input.solvedCount >= 50
        AchievementId.SOLVED_100 -> input.solvedCount >= 100
        AchievementId.FIRST_MONSTER -> input.monsterSolvedCount >= 1
        AchievementId.MONSTER_10 -> input.monsterSolvedCount >= 10
        AchievementId.FAST_SOLVE -> (input.fastestSolveSeconds ?: Long.MAX_VALUE) < FAST_SOLVE_THRESHOLD_SECONDS
        AchievementId.CUSTOM_CREATOR -> input.hasCreatedCustomPuzzle
        AchievementId.COLLECTOR -> input.savedGameCount >= COLLECTOR_THRESHOLD
        AchievementId.STREAK_7 -> input.longestStreakDays >= 7
    }
}
