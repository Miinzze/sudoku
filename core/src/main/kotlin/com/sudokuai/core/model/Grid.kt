package com.sudokuai.core.model

const val GRID_SIZE = 9
const val BOX_SIZE = 3
const val CELL_COUNT = 81

/**
 * A 9x9 Sudoku grid backed by a flat 81-length IntArray.
 * Cell value 0 means empty; 1-9 are digits. Index = row * 9 + col, row/col in 0..8.
 * Mutable in place (set) for solver performance, but [copy] and [withSet] support
 * immutable-style usage where needed.
 */
class Grid private constructor(private val cells: IntArray) {

    init {
        require(cells.size == CELL_COUNT) { "Grid must have exactly $CELL_COUNT cells" }
    }

    constructor() : this(IntArray(CELL_COUNT))

    fun get(row: Int, col: Int): Int = cells[index(row, col)]
    fun get(index: Int): Int = cells[index]

    fun copy(): Grid = Grid(cells.copyOf())

    fun withSet(row: Int, col: Int, value: Int): Grid {
        val copy = cells.copyOf()
        copy[index(row, col)] = value
        return Grid(copy)
    }

    fun set(row: Int, col: Int, value: Int) {
        cells[index(row, col)] = value
    }

    fun set(index: Int, value: Int) {
        cells[index] = value
    }

    fun isEmpty(row: Int, col: Int): Boolean = get(row, col) == 0
    fun isEmpty(index: Int): Boolean = get(index) == 0

    fun toIntArray(): IntArray = cells.copyOf()

    fun toStringRepr(): String = cells.joinToString("") { it.toString() }

    fun emptyCellCount(): Int = cells.count { it == 0 }

    fun filledCellCount(): Int = CELL_COUNT - emptyCellCount()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Grid) return false
        return cells.contentEquals(other.cells)
    }

    override fun hashCode(): Int = cells.contentHashCode()

    override fun toString(): String = toStringRepr()

    companion object {
        fun index(row: Int, col: Int): Int = row * GRID_SIZE + col
        fun row(index: Int): Int = index / GRID_SIZE
        fun col(index: Int): Int = index % GRID_SIZE
        fun box(row: Int, col: Int): Int = (row / BOX_SIZE) * BOX_SIZE + (col / BOX_SIZE)
        fun box(index: Int): Int = box(row(index), col(index))

        fun fromString(s: String): Grid {
            require(s.length == CELL_COUNT) { "Puzzle string must have length $CELL_COUNT, got ${s.length}" }
            val arr = IntArray(CELL_COUNT)
            for (i in s.indices) {
                val c = s[i]
                arr[i] = when {
                    c in '1'..'9' -> c - '0'
                    c == '0' || c == '.' -> 0
                    else -> throw IllegalArgumentException("Invalid character '$c' at position $i")
                }
            }
            return Grid(arr)
        }

        fun fromIntArray(values: IntArray): Grid = Grid(values.copyOf())
    }
}
