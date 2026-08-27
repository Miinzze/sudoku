package com.sudokuai.core.model

/**
 * The five difficulty tiers exposed to the user, ordered easiest to hardest.
 *
 * WHY these score buckets: [com.sudokuai.core.generator.DifficultyScorer] assigns every
 * puzzle a numeric score derived from which human solving techniques are required and how
 * often (see the scorer for the exact weight table). The bucket boundaries below were picked
 * empirically so that puzzles solvable by singles alone land in LEICHT, puzzles that need
 * intersections/pairs land in MITTEL/SCHWER, puzzles that need triples/X-Wing land in EXPERTE,
 * and puzzles where logical techniques get stuck and a guess is required land in MONSTER. The
 * exact numbers are not sacred; what matters is that they are monotonically increasing so the
 * generator's technique-driven scoring maps cleanly onto five ordered buckets.
 */
enum class Difficulty(val displayNameDe: String, val minScore: Int, val maxScore: Int) {
    LEICHT("Leicht", 0, 60),
    MITTEL("Mittel", 61, 150),
    SCHWER("Schwer", 151, 280),
    EXPERTE("Experte", 281, 450),
    MONSTER("Monster", 451, Int.MAX_VALUE);

    companion object {
        fun fromScore(score: Int): Difficulty = entries.first { score in it.minScore..it.maxScore }
    }
}
