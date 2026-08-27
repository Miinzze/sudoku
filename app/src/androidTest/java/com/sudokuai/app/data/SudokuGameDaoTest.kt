package com.sudokuai.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sudokuai.app.data.local.AppDatabase
import com.sudokuai.app.data.local.Converters
import com.sudokuai.app.data.local.SudokuGameDao
import com.sudokuai.app.data.local.SudokuGameEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Standard Room in-memory-database instrumented tests. NOTE: this test class could not be run
 * in the sandbox this module was built in (no Android SDK/emulator available there) — it is
 * written to the usual Room testing pattern for completeness and must be verified on a real
 * device/emulator (e.g. from Android Studio) before being relied on.
 */
@RunWith(AndroidJUnit4::class)
class SudokuGameDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SudokuGameDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.sudokuGameDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun sampleGame(
        isSolved: Boolean = false,
        isSolutionRevealed: Boolean = false,
        isFavorite: Boolean = false,
        difficulty: String = "MITTEL",
        elapsedSeconds: Long = 0,
        lastModifiedAt: Long = System.currentTimeMillis(),
    ) = SudokuGameEntity(
        originalPuzzle = "0".repeat(81),
        solution = "1".repeat(81),
        currentState = "0".repeat(81),
        candidates = Converters.emptyEncodedCandidates(),
        difficulty = difficulty,
        elapsedSeconds = elapsedSeconds,
        createdAt = 0,
        lastModifiedAt = lastModifiedAt,
        isFavorite = isFavorite,
        isSolved = isSolved,
        isSolutionRevealed = isSolutionRevealed,
    )

    @Test
    fun insertAndGetById() = runBlocking {
        val id = dao.insert(sampleGame())
        val loaded = dao.getById(id)
        assertNotNull(loaded)
        assertEquals(id, loaded!!.id)
    }

    @Test
    fun mostRecentUnfinishedExcludesSolvedAndRevealed() = runBlocking {
        dao.insert(sampleGame(isSolved = true, lastModifiedAt = 300))
        dao.insert(sampleGame(isSolutionRevealed = true, lastModifiedAt = 200))
        val unfinishedId = dao.insert(sampleGame(lastModifiedAt = 100))

        val result = dao.getMostRecentUnfinished()
        assertNotNull(result)
        assertEquals(unfinishedId, result!!.id)
    }

    @Test
    fun favoritesFlowReflectsOnlyFavorites() = runBlocking {
        dao.insert(sampleGame(isFavorite = true))
        dao.insert(sampleGame(isFavorite = false))

        val favorites = dao.observeFavorites().first()
        assertEquals(1, favorites.size)
        assertTrue(favorites.all { it.isFavorite })
    }

    @Test
    fun deleteRemovesRow() = runBlocking {
        val id = dao.insert(sampleGame())
        dao.deleteById(id)
        assertNull(dao.getById(id))
    }

    @Test
    fun countReflectsInsertedRows() = runBlocking {
        assertEquals(0, dao.count())
        dao.insert(sampleGame())
        dao.insert(sampleGame())
        assertEquals(2, dao.count())
    }
}
