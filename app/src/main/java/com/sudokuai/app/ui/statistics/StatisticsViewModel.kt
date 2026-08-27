package com.sudokuai.app.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuai.app.data.repository.AchievementRepository
import com.sudokuai.app.data.repository.StatisticsRepository
import com.sudokuai.app.domain.Achievement
import com.sudokuai.app.domain.Statistics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatisticsUiState(
    val statistics: Statistics? = null,
    val achievements: List<Achievement> = emptyList(),
)

class StatisticsViewModel(
    statisticsRepository: StatisticsRepository,
    achievementRepository: AchievementRepository,
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = combine(
        statisticsRepository.observeStatistics(),
        achievementRepository.observeAchievements(),
    ) { stats, achievements ->
        StatisticsUiState(statistics = stats, achievements = achievements)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState())

    class Factory(
        private val statisticsRepository: StatisticsRepository,
        private val achievementRepository: AchievementRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StatisticsViewModel(statisticsRepository, achievementRepository) as T
    }
}
