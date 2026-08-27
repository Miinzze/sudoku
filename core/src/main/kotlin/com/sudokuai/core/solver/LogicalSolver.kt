package com.sudokuai.core.solver

import com.sudokuai.core.model.CELL_COUNT
import com.sudokuai.core.model.Candidates
import com.sudokuai.core.model.GRID_SIZE
import com.sudokuai.core.model.Grid
import com.sudokuai.core.model.GridValidator

/**
 * Result of a full logical-solve attempt, used by the generator to score difficulty.
 *
 * @param solvedGrid the fully solved grid if logical techniques (plus, if needed, the
 *   backtracking fallback) reached a solution; null only if the puzzle turned out to have no
 *   solution at all.
 * @param techniqueUsage how many times each [Technique] was applied.
 * @param requiredBacktracking true if logical techniques alone got stuck before the grid was
 *   complete, meaning a guess (trial and error) was needed to finish. Legitimate for Monster
 *   puzzles; the raw [BacktrackingSolver] is used only to complete the grid in that case,
 *   never to fabricate a fake "technique".
 */
data class LogicalSolveResult(
    val solvedGrid: Grid?,
    val techniqueUsage: Map<Technique, Int>,
    val requiredBacktracking: Boolean,
    val emptyCellsWhenStuck: Int,
)

/**
 * Simulates how a human would solve a puzzle: repeatedly applies the simplest technique that
 * currently makes progress, starting over from the simplest technique after every successful
 * step (a human always looks for the easiest move first). This is deliberately separate from
 * [BacktrackingSolver], which is a raw computer search used only for uniqueness checks — the
 * technique trace produced here is what [com.sudokuai.core.generator.DifficultyScorer] uses to
 * assign a real, technique-based difficulty rather than a naive clue-count heuristic.
 */
object LogicalSolver {

    fun solve(input: Grid): LogicalSolveResult {
        val grid = input.copy()
        val candidates = Candidates.computeAll(grid)
        val usage = LinkedHashMap<Technique, Int>()

        while (!GridValidator.isComplete(grid)) {
            val step = findNextStep(grid, candidates)
            if (step == null) {
                // Logical techniques are stuck; fall back to raw search to complete the grid,
                // but record that this puzzle required a guess.
                val completed = BacktrackingSolver.solve(grid)
                return LogicalSolveResult(
                    solvedGrid = completed,
                    techniqueUsage = usage,
                    requiredBacktracking = true,
                    emptyCellsWhenStuck = grid.emptyCellCount(),
                )
            }
            usage[step.technique] = (usage[step.technique] ?: 0) + 1
            applyStep(grid, candidates, step)
        }
        return LogicalSolveResult(
            solvedGrid = grid,
            techniqueUsage = usage,
            requiredBacktracking = false,
            emptyCellsWhenStuck = 0,
        )
    }

    private fun applyStep(grid: Grid, candidates: Candidates, step: TechniqueStep) {
        when (step) {
            is TechniqueStep.Placement -> {
                grid.set(step.index, step.value)
                candidates.clear(step.index)
                candidates.removeFromPeers(Grid.row(step.index), Grid.col(step.index), step.value)
            }
            is TechniqueStep.Elimination -> {
                for ((idx, value) in step.eliminations) candidates.remove(idx, value)
            }
        }
    }

    private fun findNextStep(grid: Grid, candidates: Candidates): TechniqueStep? {
        return findNakedSingle(grid, candidates)
            ?: findHiddenSingle(grid, candidates)
            ?: findPointingPair(grid, candidates)
            ?: findBoxLineReduction(grid, candidates)
            ?: findNakedPair(grid, candidates)
            ?: findHiddenPair(grid, candidates)
            ?: findNakedTriple(grid, candidates)
            ?: findXWing(grid, candidates)
    }

    // --- Naked Single: a cell with exactly one candidate must be that value. ---
    private fun findNakedSingle(grid: Grid, candidates: Candidates): TechniqueStep? {
        for (i in 0 until CELL_COUNT) {
            if (grid.get(i) != 0) continue
            if (candidates.count(i) == 1) {
                val value = candidates.values(i)[0]
                return TechniqueStep.Placement(Technique.NAKED_SINGLE, i, value)
            }
        }
        return null
    }

    // --- Hidden Single: a value that fits in only one cell of a unit must go there. ---
    private fun findHiddenSingle(grid: Grid, candidates: Candidates): TechniqueStep? {
        for (unitIndices in allUnits()) {
            for (value in 1..9) {
                var onlyCell = -1
                var count = 0
                for (idx in unitIndices) {
                    if (grid.get(idx) == 0 && candidates.has(idx, value)) {
                        count++
                        onlyCell = idx
                        if (count > 1) break
                    }
                }
                if (count == 1) {
                    return TechniqueStep.Placement(Technique.HIDDEN_SINGLE, onlyCell, value)
                }
            }
        }
        return null
    }

    // --- Pointing Pair/Triple: if a value in a box is confined to one row/col, remove it
    // from that row/col outside the box. ---
    private fun findPointingPair(grid: Grid, candidates: Candidates): TechniqueStep? {
        for (box in 0 until GRID_SIZE) {
            val boxCells = GridValidator.boxIndices(box).filter { grid.get(it) == 0 }
            for (value in 1..9) {
                val cellsWithValue = boxCells.filter { candidates.has(it, value) }
                if (cellsWithValue.size < 2) continue
                val rows = cellsWithValue.map { Grid.row(it) }.distinct()
                val cols = cellsWithValue.map { Grid.col(it) }.distinct()
                if (rows.size == 1) {
                    val elims = GridValidator.rowIndices(rows[0])
                        .filter { it !in cellsWithValue && grid.get(it) == 0 && candidates.has(it, value) }
                        .map { it to value }
                    if (elims.isNotEmpty()) return TechniqueStep.Elimination(Technique.POINTING_PAIR, elims)
                }
                if (cols.size == 1) {
                    val elims = GridValidator.colIndices(cols[0])
                        .filter { it !in cellsWithValue && grid.get(it) == 0 && candidates.has(it, value) }
                        .map { it to value }
                    if (elims.isNotEmpty()) return TechniqueStep.Elimination(Technique.POINTING_PAIR, elims)
                }
            }
        }
        return null
    }

    // --- Box-Line Reduction: if a value in a row/col is confined to one box, remove it
    // from the rest of that box. ---
    private fun findBoxLineReduction(grid: Grid, candidates: Candidates): TechniqueStep? {
        val lines = (0 until GRID_SIZE).map { GridValidator.rowIndices(it) } +
            (0 until GRID_SIZE).map { GridValidator.colIndices(it) }
        for (line in lines) {
            val lineCells = line.filter { grid.get(it) == 0 }
            for (value in 1..9) {
                val cellsWithValue = lineCells.filter { candidates.has(it, value) }
                if (cellsWithValue.size < 2) continue
                val boxes = cellsWithValue.map { Grid.box(it) }.distinct()
                if (boxes.size == 1) {
                    val elims = GridValidator.boxIndices(boxes[0])
                        .filter { it !in cellsWithValue && grid.get(it) == 0 && candidates.has(it, value) }
                        .map { it to value }
                    if (elims.isNotEmpty()) return TechniqueStep.Elimination(Technique.BOX_LINE_REDUCTION, elims)
                }
            }
        }
        return null
    }

    // --- Naked Pair: two cells in a unit share the same 2-candidate set; remove those two
    // values from every other cell in the unit. ---
    private fun findNakedPair(grid: Grid, candidates: Candidates): TechniqueStep? {
        for (unitIndices in allUnits()) {
            val emptyCells = unitIndices.filter { grid.get(it) == 0 }
            val pairCells = emptyCells.filter { candidates.count(it) == 2 }
            for (i in pairCells.indices) {
                for (j in i + 1 until pairCells.size) {
                    val a = pairCells[i]
                    val b = pairCells[j]
                    if (candidates.get(a) != candidates.get(b)) continue
                    val mask = candidates.get(a)
                    val elims = mutableListOf<Pair<Int, Int>>()
                    for (idx in emptyCells) {
                        if (idx == a || idx == b) continue
                        for (value in 1..9) {
                            if ((mask and Candidates.bit(value)) != 0 && candidates.has(idx, value)) {
                                elims.add(idx to value)
                            }
                        }
                    }
                    if (elims.isNotEmpty()) return TechniqueStep.Elimination(Technique.NAKED_PAIR, elims)
                }
            }
        }
        return null
    }

    // --- Hidden Pair: two values are confined to the same two cells in a unit; strip all
    // other candidates from those two cells. ---
    private fun findHiddenPair(grid: Grid, candidates: Candidates): TechniqueStep? {
        for (unitIndices in allUnits()) {
            val emptyCells = unitIndices.filter { grid.get(it) == 0 }
            val cellsForValue = HashMap<Int, MutableList<Int>>()
            for (value in 1..9) {
                cellsForValue[value] = emptyCells.filter { candidates.has(it, value) }.toMutableList()
            }
            for (v1 in 1..9) {
                val cells1 = cellsForValue[v1]!!
                if (cells1.size != 2) continue
                for (v2 in v1 + 1..9) {
                    val cells2 = cellsForValue[v2]!!
                    if (cells2.size != 2) continue
                    if (cells1[0] != cells2[0] || cells1[1] != cells2[1]) continue
                    // v1 and v2 both live only in the same two cells: hidden pair found.
                    val elims = mutableListOf<Pair<Int, Int>>()
                    for (idx in cells1) {
                        for (value in 1..9) {
                            if (value != v1 && value != v2 && candidates.has(idx, value)) {
                                elims.add(idx to value)
                            }
                        }
                    }
                    if (elims.isNotEmpty()) return TechniqueStep.Elimination(Technique.HIDDEN_PAIR, elims)
                }
            }
        }
        return null
    }

    // --- Naked Triple: three cells in a unit whose combined candidates total exactly 3
    // values; remove those values from every other cell in the unit. ---
    private fun findNakedTriple(grid: Grid, candidates: Candidates): TechniqueStep? {
        for (unitIndices in allUnits()) {
            val emptyCells = unitIndices.filter { grid.get(it) == 0 }
            val tripleCandidates = emptyCells.filter { candidates.count(it) in 2..3 }
            for (i in tripleCandidates.indices) {
                for (j in i + 1 until tripleCandidates.size) {
                    for (k in j + 1 until tripleCandidates.size) {
                        val a = tripleCandidates[i]
                        val b = tripleCandidates[j]
                        val c = tripleCandidates[k]
                        val union = candidates.get(a) or candidates.get(b) or candidates.get(c)
                        if (Integer.bitCount(union) != 3) continue
                        val elims = mutableListOf<Pair<Int, Int>>()
                        for (idx in emptyCells) {
                            if (idx == a || idx == b || idx == c) continue
                            for (value in 1..9) {
                                if ((union and Candidates.bit(value)) != 0 && candidates.has(idx, value)) {
                                    elims.add(idx to value)
                                }
                            }
                        }
                        if (elims.isNotEmpty()) return TechniqueStep.Elimination(Technique.NAKED_TRIPLE, elims)
                    }
                }
            }
        }
        return null
    }

    // --- X-Wing: a value confined to the same two columns across two rows (or vice versa)
    // forms a rectangle; the value can be eliminated from those columns/rows elsewhere. ---
    private fun findXWing(grid: Grid, candidates: Candidates): TechniqueStep? {
        for (value in 1..9) {
            // Row-based X-Wing: two rows where the value's candidate columns are the same pair.
            val rowToCols = HashMap<Int, List<Int>>()
            for (r in 0 until GRID_SIZE) {
                val cols = (0 until GRID_SIZE).filter {
                    val idx = Grid.index(r, it)
                    grid.get(idx) == 0 && candidates.has(idx, value)
                }
                if (cols.size == 2) rowToCols[r] = cols
            }
            val rows = rowToCols.keys.toList()
            for (i in rows.indices) {
                for (j in i + 1 until rows.size) {
                    val r1 = rows[i]
                    val r2 = rows[j]
                    if (rowToCols[r1] != rowToCols[r2]) continue
                    val cols = rowToCols[r1]!!
                    val elims = mutableListOf<Pair<Int, Int>>()
                    for (r in 0 until GRID_SIZE) {
                        if (r == r1 || r == r2) continue
                        for (c in cols) {
                            val idx = Grid.index(r, c)
                            if (grid.get(idx) == 0 && candidates.has(idx, value)) elims.add(idx to value)
                        }
                    }
                    if (elims.isNotEmpty()) return TechniqueStep.Elimination(Technique.X_WING, elims)
                }
            }

            // Column-based X-Wing: symmetric case.
            val colToRows = HashMap<Int, List<Int>>()
            for (c in 0 until GRID_SIZE) {
                val rowsForCol = (0 until GRID_SIZE).filter {
                    val idx = Grid.index(it, c)
                    grid.get(idx) == 0 && candidates.has(idx, value)
                }
                if (rowsForCol.size == 2) colToRows[c] = rowsForCol
            }
            val cols = colToRows.keys.toList()
            for (i in cols.indices) {
                for (j in i + 1 until cols.size) {
                    val c1 = cols[i]
                    val c2 = cols[j]
                    if (colToRows[c1] != colToRows[c2]) continue
                    val rowsPair = colToRows[c1]!!
                    val elims = mutableListOf<Pair<Int, Int>>()
                    for (c in 0 until GRID_SIZE) {
                        if (c == c1 || c == c2) continue
                        for (r in rowsPair) {
                            val idx = Grid.index(r, c)
                            if (grid.get(idx) == 0 && candidates.has(idx, value)) elims.add(idx to value)
                        }
                    }
                    if (elims.isNotEmpty()) return TechniqueStep.Elimination(Technique.X_WING, elims)
                }
            }
        }
        return null
    }

    private fun allUnits(): List<List<Int>> {
        val units = ArrayList<List<Int>>(27)
        for (i in 0 until GRID_SIZE) units.add(GridValidator.rowIndices(i))
        for (i in 0 until GRID_SIZE) units.add(GridValidator.colIndices(i))
        for (i in 0 until GRID_SIZE) units.add(GridValidator.boxIndices(i))
        return units
    }
}
