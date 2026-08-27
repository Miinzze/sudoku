package com.sudokuai.core.solver

/**
 * Human solving techniques recognized by [LogicalSolver], ordered from simplest to most
 * advanced. The order also defines the priority in which [LogicalSolver] searches for an
 * applicable move at each step, mirroring how a human solver would always look for the
 * easiest available move first.
 */
enum class Technique {
    NAKED_SINGLE,
    HIDDEN_SINGLE,
    POINTING_PAIR,
    BOX_LINE_REDUCTION,
    NAKED_PAIR,
    HIDDEN_PAIR,
    NAKED_TRIPLE,
    X_WING,

    /** Not a real technique — marks that logical deduction got stuck and a guess was required. */
    BACKTRACKING,
}

/** Result of one applied solving step: either a digit placement or a set of candidate eliminations. */
sealed class TechniqueStep {
    abstract val technique: Technique

    data class Placement(
        override val technique: Technique,
        val index: Int,
        val value: Int,
    ) : TechniqueStep()

    data class Elimination(
        override val technique: Technique,
        val eliminations: List<Pair<Int, Int>>, // (cellIndex, value)
    ) : TechniqueStep()
}
