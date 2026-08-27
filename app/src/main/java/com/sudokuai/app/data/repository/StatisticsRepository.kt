package com.sudokuai.app.data.repository

import com.sudokuai.app.domain.Statistics
import com.sudokuai.app.domain.StatisticsCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId

/**
 * Statistics are computed on the fly from [SudokuRepository]'s saved games rather than kept in a
 * separate running-totals table: with a 100-game save cap the input list is always small, so
 * recomputing on every emission is cheap, and it guarantees the numbers can never drift out of
 * sync with the underlying game rows (e.g. after a delete).
 */
class StatisticsRepository(
    private val sudokuRepository: SudokuRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun observeStatistics(): Flow<Statistics> =
        sudokuRepository.observeAll().map { games -> StatisticsCalculator.compute(games, zoneId) }
}
