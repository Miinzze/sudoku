package com.sudokuai.app.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudokuai.app.R
import com.sudokuai.app.di.ServiceLocator
import com.sudokuai.app.domain.Achievement
import com.sudokuai.app.domain.Statistics
import com.sudokuai.app.ui.components.DifficultyBadge
import com.sudokuai.app.ui.components.formatElapsed
import com.sudokuai.core.model.Difficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen() {
    val context = LocalContext.current
    val viewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModel.Factory(
            ServiceLocator.provideStatisticsRepository(context),
            ServiceLocator.provideAchievementRepository(context),
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats = uiState.statistics

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.statistics_title)) }) }) { padding ->
        if (stats == null || stats.playedCount == 0) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.statistics_no_data))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { OverviewCard(stats) }
            item { DifficultyBreakdownCard(stats) }
            item {
                Text(
                    stringResource(R.string.statistics_achievements),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(uiState.achievements, key = { it.id }) { achievement ->
                AchievementRow(achievement)
            }
        }
    }
}

@Composable
private fun OverviewCard(stats: Statistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StatRow(stringResource(R.string.statistics_played_count), stats.playedCount.toString())
            StatRow(stringResource(R.string.statistics_solved_count), stats.solvedCount.toString())
            StatRow(stringResource(R.string.statistics_monster_solved), stats.monsterSolvedCount.toString())
            StatRow(
                stringResource(R.string.statistics_best_time),
                stats.bestTimeSeconds?.let { formatElapsed(it) } ?: "–",
            )
            StatRow(
                stringResource(R.string.statistics_average_time),
                stats.averageTimeSeconds?.let { formatElapsed(it) } ?: "–",
            )
            StatRow(stringResource(R.string.statistics_streak), stats.longestStreakDays.toString())
        }
    }
}

@Composable
private fun DifficultyBreakdownCard(stats: Statistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.statistics_by_difficulty), style = MaterialTheme.typography.titleMedium)
            for (difficulty in Difficulty.entries) {
                val d = stats.perDifficulty[difficulty]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DifficultyBadge(difficulty)
                    Text("${d?.solvedCount ?: 0}× · ${d?.bestTimeSeconds?.let { formatElapsed(it) } ?: "–"}")
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun AchievementRow(achievement: Achievement) {
    val strings = achievementStrings(achievement.id)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                if (achievement.unlocked) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (achievement.unlocked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Column {
                Text(stringResource(strings.titleRes), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(strings.descriptionRes), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
