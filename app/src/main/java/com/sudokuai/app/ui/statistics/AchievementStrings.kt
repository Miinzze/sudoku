package com.sudokuai.app.ui.statistics

import com.sudokuai.app.R
import com.sudokuai.app.domain.AchievementId

data class AchievementStrings(val titleRes: Int, val descriptionRes: Int)

fun achievementStrings(id: AchievementId): AchievementStrings = when (id) {
    AchievementId.FIRST_SUDOKU -> AchievementStrings(R.string.achievement_first_sudoku, R.string.achievement_first_sudoku_desc)
    AchievementId.SOLVED_10 -> AchievementStrings(R.string.achievement_10_solved, R.string.achievement_10_solved_desc)
    AchievementId.SOLVED_50 -> AchievementStrings(R.string.achievement_50_solved, R.string.achievement_50_solved_desc)
    AchievementId.SOLVED_100 -> AchievementStrings(R.string.achievement_100_solved, R.string.achievement_100_solved_desc)
    AchievementId.FIRST_MONSTER -> AchievementStrings(R.string.achievement_first_monster, R.string.achievement_first_monster_desc)
    AchievementId.MONSTER_10 -> AchievementStrings(R.string.achievement_10_monster, R.string.achievement_10_monster_desc)
    AchievementId.FAST_SOLVE -> AchievementStrings(R.string.achievement_fast_solve, R.string.achievement_fast_solve_desc)
    AchievementId.CUSTOM_CREATOR -> AchievementStrings(R.string.achievement_custom_creator, R.string.achievement_custom_creator_desc)
    AchievementId.COLLECTOR -> AchievementStrings(R.string.achievement_collector, R.string.achievement_collector_desc)
    AchievementId.STREAK_7 -> AchievementStrings(R.string.achievement_streak_7, R.string.achievement_streak_7_desc)
}
