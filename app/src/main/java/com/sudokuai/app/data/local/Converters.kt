package com.sudokuai.app.data.local

import com.sudokuai.core.model.CELL_COUNT
import com.sudokuai.core.model.Candidates

/**
 * Pure (no Android/Room dependency) encode/decode helpers between [Candidates] and the
 * comma-joined-int String representation stored in [SudokuGameEntity.candidates]. Kept as plain
 * functions rather than a `@androidx.room.TypeConverter`-annotated class because the entity
 * column is already declared as `String` — Room needs no converter for it — but the encode/decode
 * logic itself is exactly the kind of pure logic this module's unit tests should cover directly,
 * so it lives here, independent of Room, and is exercised by ConvertersTest without any Android
 * dependency.
 */
object Converters {

    /** Encodes 81 candidate bitmasks (0-511 each) as a comma-joined string, e.g. "0,0,7,...". */
    fun encodeCandidates(candidates: Candidates): String =
        (0 until CELL_COUNT).joinToString(",") { candidates.get(it).toString() }

    /** Decodes a string produced by [encodeCandidates] back into a [Candidates] instance. */
    fun decodeCandidates(encoded: String): Candidates {
        if (encoded.isBlank()) return Candidates()
        val masks = encoded.split(",").map { it.trim().toIntOrNull() ?: 0 }
        require(masks.size == CELL_COUNT) {
            "Encoded candidates must have $CELL_COUNT entries, got ${masks.size}"
        }
        return Candidates.fromMaskArray(masks.toIntArray())
    }

    /** Convenience: an encoded string representing 81 empty candidate masks. */
    fun emptyEncodedCandidates(): String = encodeCandidates(Candidates())
}
