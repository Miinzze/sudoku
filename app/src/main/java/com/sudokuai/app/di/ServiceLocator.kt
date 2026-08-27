package com.sudokuai.app.di

import android.content.Context
import com.sudokuai.app.data.datastore.SettingsDataStore
import com.sudokuai.app.data.local.AppDatabase
import com.sudokuai.app.data.repository.AchievementRepository
import com.sudokuai.app.data.repository.StatisticsRepository
import com.sudokuai.app.data.repository.SudokuRepository

/**
 * Manual, constructor-injection-based dependency graph — a small `ServiceLocator` rather than
 * Hilt/Dagger. This is a deliberate trade-off given this module could not be compiled in the
 * sandbox it was built in: Hilt's KSP-generated code and annotation wiring cannot be verified
 * without a working AGP/Android SDK toolchain, and a wrong annotation there fails the whole
 * build in a way that is hard to spot by inspection. A hand-written, single-file object graph
 * with plain constructors is easy to read top-to-bottom, has nothing "magic" for a reviewer to
 * second-guess, and is trivial to migrate to Hilt later if desired.
 */
object ServiceLocator {

    @Volatile
    private var appDatabase: AppDatabase? = null

    @Volatile
    private var sudokuRepository: SudokuRepository? = null

    @Volatile
    private var statisticsRepository: StatisticsRepository? = null

    @Volatile
    private var achievementRepository: AchievementRepository? = null

    @Volatile
    private var settingsDataStore: SettingsDataStore? = null

    fun provideDatabase(context: Context): AppDatabase =
        appDatabase ?: synchronized(this) {
            appDatabase ?: AppDatabase.getInstance(context.applicationContext).also { appDatabase = it }
        }

    fun provideSudokuRepository(context: Context): SudokuRepository =
        sudokuRepository ?: synchronized(this) {
            sudokuRepository ?: SudokuRepository(provideDatabase(context).sudokuGameDao()).also {
                sudokuRepository = it
            }
        }

    fun provideStatisticsRepository(context: Context): StatisticsRepository =
        statisticsRepository ?: synchronized(this) {
            statisticsRepository ?: StatisticsRepository(provideSudokuRepository(context)).also {
                statisticsRepository = it
            }
        }

    fun provideAchievementRepository(context: Context): AchievementRepository =
        achievementRepository ?: synchronized(this) {
            achievementRepository ?: AchievementRepository(provideSudokuRepository(context)).also {
                achievementRepository = it
            }
        }

    fun provideSettingsDataStore(context: Context): SettingsDataStore =
        settingsDataStore ?: synchronized(this) {
            settingsDataStore ?: SettingsDataStore(context.applicationContext).also {
                settingsDataStore = it
            }
        }
}
