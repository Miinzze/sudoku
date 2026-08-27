package com.sudokuai.app.domain

import com.sudokuai.core.model.Difficulty
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

data class DifficultyStats(
    val solvedCount: Int,
    val bestTimeSeconds: Long?,
    val averageTimeSeconds: Long?,
)

data class Statistics(
    val playedCount: Int,
    val solvedCount: Int,
    val monsterSolvedCount: Int,
    val bestTimeSeconds: Long?,
    val averageTimeSeconds: Long?,
    val longestStreakDays: Int,
    val perDifficulty: Map<Difficulty, DifficultyStats>,
)

/**
 * Pure statistics computation over a list of [GameState]s — no Room/Android dependency, so it is
 * directly unit-testable. "Solved" always means [GameState.isSolved] (never solution-revealed).
 */
object StatisticsCalculator {

    fun compute(games: List<GameState>, zoneId: ZoneId = ZoneOffset.UTC): Statistics {
        val solved = games.filter { it.isSolved }
        val perDifficulty = Difficulty.entries.associateWith { difficulty ->
            val solvedForDifficulty = solved.filter { it.difficulty == difficulty }
            val times = solvedForDifficulty.map { it.elapsedSeconds }
            DifficultyStats(
                solvedCount = solvedForDifficulty.size,
                bestTimeSeconds = times.minOrNull(),
                averageTimeSeconds = if (times.isEmpty()) null else times.sum() / times.size,
            )
        }
        val allSolvedTimes = solved.map { it.elapsedSeconds }
        return Statistics(
            playedCount = games.size,
            solvedCount = solved.size,
            monsterSolvedCount = solved.count { it.difficulty == Difficulty.MONSTER },
            bestTimeSeconds = allSolvedTimes.minOrNull(),
            averageTimeSeconds = if (allSolvedTimes.isEmpty()) null else allSolvedTimes.sum() / allSolvedTimes.size,
            longestStreakDays = longestStreak(solved, zoneId),
            perDifficulty = perDifficulty,
        )
    }

    /** Longest run of consecutive calendar days containing at least one genuine solve. */
    fun longestStreak(solvedGames: List<GameState>, zoneId: ZoneId = ZoneOffset.UTC): Int {
        val days = solvedGames
            .mapNotNull { it.solvedAt ?: it.lastModifiedAt.takeIf { _ -> it.isSolved } }
            .map { epochMillis -> Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate() }
            .distinct()
            .sorted()
        if (days.isEmpty()) return 0

        var longest = 1
        var current = 1
        for (i in 1 until days.size) {
            current = if (days[i - 1].plusDays(1) == days[i]) current + 1 else 1
            if (current > longest) longest = current
        }
        return longest
    }
}
