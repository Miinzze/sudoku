package com.sudokuai.app.data.repository

import com.sudokuai.app.data.local.SudokuGameDao
import com.sudokuai.app.domain.GameMapper
import com.sudokuai.app.domain.GameState
import com.sudokuai.core.model.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Maximum number of games a user may keep saved at once (requirement: library size cap). */
const val MAX_SAVED_GAMES = 100

sealed class SaveResult {
    data class Success(val id: Long) : SaveResult()
    object LimitReached : SaveResult()
}

enum class SortOrder { NEWEST, OLDEST, DIFFICULTY, PLAYTIME }

/** Wraps [SudokuGameDao], translating to/from `:core` types via [GameMapper]. */
class SudokuRepository(private val dao: SudokuGameDao) {

    fun observeAll(sortOrder: SortOrder = SortOrder.NEWEST): Flow<List<GameState>> {
        val flow = when (sortOrder) {
            SortOrder.NEWEST -> dao.observeAllNewestFirst()
            SortOrder.OLDEST -> dao.observeAllOldestFirst()
            SortOrder.DIFFICULTY -> dao.observeAllNewestFirst()
            SortOrder.PLAYTIME -> dao.observeAllByPlaytime()
        }
        return flow.map { entities ->
            val states = entities.map(GameMapper::toGameState)
            if (sortOrder == SortOrder.DIFFICULTY) {
                states.sortedBy { it.difficulty.ordinal }
            } else {
                states
            }
        }
    }

    fun observeFavorites(): Flow<List<GameState>> =
        dao.observeFavorites().map { it.map(GameMapper::toGameState) }

    fun observeByDifficulty(difficulty: Difficulty): Flow<List<GameState>> =
        dao.observeByDifficulty(difficulty.name).map { it.map(GameMapper::toGameState) }

    fun observeById(id: Long): Flow<GameState?> =
        dao.observeById(id).map { it?.let(GameMapper::toGameState) }

    suspend fun getById(id: Long): GameState? = dao.getById(id)?.let(GameMapper::toGameState)

    suspend fun getMostRecentUnfinished(): GameState? =
        dao.getMostRecentUnfinished()?.let(GameMapper::toGameState)

    suspend fun count(): Int = dao.count()

    fun observeCount(): Flow<Int> = dao.observeCount()

    /**
     * Inserts a brand-new game, enforcing [MAX_SAVED_GAMES]. Updating an already-saved game
     * (state.id != 0) always succeeds regardless of the current count.
     */
    suspend fun save(state: GameState): SaveResult {
        if (state.id == 0L && dao.count() >= MAX_SAVED_GAMES) {
            return SaveResult.LimitReached
        }
        val id = dao.insert(GameMapper.toEntity(state))
        return SaveResult.Success(id)
    }

    suspend fun update(state: GameState) {
        dao.update(GameMapper.toEntity(state))
    }

    suspend fun delete(state: GameState) {
        dao.delete(GameMapper.toEntity(state))
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun setFavorite(state: GameState, favorite: Boolean) {
        dao.update(GameMapper.toEntity(state.copy(isFavorite = favorite)))
    }
}
