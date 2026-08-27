package com.sudokuai.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SudokuGameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: SudokuGameEntity): Long

    @Update
    suspend fun update(game: SudokuGameEntity)

    @Delete
    suspend fun delete(game: SudokuGameEntity)

    @Query("DELETE FROM sudoku_games WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM sudoku_games WHERE id = :id")
    suspend fun getById(id: Long): SudokuGameEntity?

    @Query("SELECT * FROM sudoku_games WHERE id = :id")
    fun observeById(id: Long): Flow<SudokuGameEntity?>

    @Query("SELECT * FROM sudoku_games ORDER BY lastModifiedAt DESC")
    fun observeAllNewestFirst(): Flow<List<SudokuGameEntity>>

    @Query("SELECT * FROM sudoku_games ORDER BY lastModifiedAt ASC")
    fun observeAllOldestFirst(): Flow<List<SudokuGameEntity>>

    // Note: there is deliberately no SQL-level "ORDER BY difficulty" query here — difficulty is
    // stored as the enum's name (e.g. "EXPERTE"), and alphabetical order on that string does not
    // match the real Leicht->Monster ordering. Difficulty-sorted listings are produced by the
    // repository, which sorts [observeAllNewestFirst] results by Difficulty.ordinal in Kotlin.

    @Query("SELECT * FROM sudoku_games ORDER BY elapsedSeconds ASC")
    fun observeAllByPlaytime(): Flow<List<SudokuGameEntity>>

    @Query("SELECT * FROM sudoku_games WHERE isFavorite = 1 ORDER BY lastModifiedAt DESC")
    fun observeFavorites(): Flow<List<SudokuGameEntity>>

    @Query("SELECT * FROM sudoku_games WHERE difficulty = :difficulty ORDER BY lastModifiedAt DESC")
    fun observeByDifficulty(difficulty: String): Flow<List<SudokuGameEntity>>

    @Query(
        "SELECT * FROM sudoku_games WHERE isSolved = 0 AND isSolutionRevealed = 0 " +
            "ORDER BY lastModifiedAt DESC LIMIT 1"
    )
    suspend fun getMostRecentUnfinished(): SudokuGameEntity?

    @Query("SELECT COUNT(*) FROM sudoku_games")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM sudoku_games")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM sudoku_games WHERE isSolved = 1")
    suspend fun getAllSolved(): List<SudokuGameEntity>

    @Query("SELECT * FROM sudoku_games")
    suspend fun getAllOnce(): List<SudokuGameEntity>
}
