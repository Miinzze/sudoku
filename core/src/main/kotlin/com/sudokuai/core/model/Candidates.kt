package com.sudokuai.core.model

/**
 * Bitmask-based candidate (notes) set for all 81 cells. Bit (value-1) set means `value`
 * is currently marked as a candidate/note in that cell. Candidates are meaningful only for
 * empty cells by convention; callers should [clear] a cell's mask once it is filled.
 */
class Candidates private constructor(private val masks: IntArray) {

    constructor() : this(IntArray(CELL_COUNT))

    fun copy(): Candidates = Candidates(masks.copyOf())

    fun get(index: Int): Int = masks[index]
    fun get(row: Int, col: Int): Int = masks[Grid.index(row, col)]

    fun has(index: Int, value: Int): Boolean = (masks[index] and bit(value)) != 0
    fun has(row: Int, col: Int, value: Int): Boolean = has(Grid.index(row, col), value)

    fun add(index: Int, value: Int) {
        masks[index] = masks[index] or bit(value)
    }

    fun add(row: Int, col: Int, value: Int) = add(Grid.index(row, col), value)

    fun remove(index: Int, value: Int) {
        masks[index] = masks[index] and bit(value).inv()
    }

    fun remove(row: Int, col: Int, value: Int) = remove(Grid.index(row, col), value)

    fun clear(index: Int) {
        masks[index] = 0
    }

    fun clear(row: Int, col: Int) = clear(Grid.index(row, col))

    fun setMask(index: Int, mask: Int) {
        masks[index] = mask and FULL_MASK
    }

    fun values(index: Int): List<Int> = (1..9).filter { has(index, it) }

    fun count(index: Int): Int = Integer.bitCount(masks[index])

    fun isEmpty(index: Int): Boolean = masks[index] == 0

    /**
     * Removes `value` from the candidate sets of every peer (same row, column, box) of the
     * cell at (row, col). Called after a final digit is placed. Whether this is invoked at all
     * is gated by the caller (e.g. the "automatisches Entfernen von Kandidaten" setting) —
     * this function itself always performs the removal when called.
     * Returns the peer indices whose candidate set actually changed.
     */
    fun removeFromPeers(row: Int, col: Int, value: Int): Set<Int> {
        val changed = LinkedHashSet<Int>()
        for (peer in GridValidator.peers(row, col)) {
            if (has(peer, value)) {
                remove(peer, value)
                changed.add(peer)
            }
        }
        return changed
    }

    fun toMaskArray(): IntArray = masks.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Candidates) return false
        return masks.contentEquals(other.masks)
    }

    override fun hashCode(): Int = masks.contentHashCode()

    companion object {
        const val FULL_MASK = 0b111111111

        fun bit(value: Int): Int {
            require(value in 1..9) { "value must be in 1..9, was $value" }
            return 1 shl (value - 1)
        }

        fun fromMaskArray(maskArray: IntArray): Candidates {
            require(maskArray.size == CELL_COUNT)
            return Candidates(maskArray.copyOf())
        }

        /** Computes the naive full candidate set (all legally-placeable values) for every empty cell. */
        fun computeAll(grid: Grid): Candidates {
            val c = Candidates()
            for (i in 0 until CELL_COUNT) {
                if (grid.get(i) != 0) continue
                val row = Grid.row(i)
                val col = Grid.col(i)
                var mask = 0
                for (v in 1..9) {
                    if (GridValidator.canPlace(grid, row, col, v)) mask = mask or bit(v)
                }
                c.setMask(i, mask)
            }
            return c
        }
    }
}
